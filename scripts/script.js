import http from 'k6/http';
import { check, sleep } from 'k6';

// 테스트 부하 설정 (Ramp-up으로 점진적 부하 증가)
export const options = {
    setupTimeout: '3m', // setup() 대기 시간을 3분으로 연장
    stages: [
        { duration: '30s', target: 50 }, // 우선 50명까지 천천히 증가
        { duration: '1m', target: 50 },  // 50명 유지
        { duration: '30s', target: 100 }, // 100명으로 증가
        { duration: '1m', target: 100 },  // 100명 유지
        { duration: '30s', target: 150 }, // 150명으로 증가
        { duration: '1m', target: 150 },  // 150명 유지
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.05'],   // 에러율 5% 미만 목표
        http_req_duration: ['p(95)<500'], // 95% 응답속도 500ms 이내 목표
    },
};

// 테스트 시작 전 로그인을 수행하고 토큰을 가져오는 함수
export function setup() {
    const loginUrl = 'http://host.docker.internal:8080/api/auth/login';
    const params = {
        headers: { 'Content-Type': 'application/json' },
    };

    const tokens = [];

    // 200명의 더미 데이터 계정 순회
    for (let i = 1; i <= 200; i++) {
        const userNumber = String(i).padStart(3, '0');
        const payload = JSON.stringify({
            email: `user${userNumber}@example.com`,
            password: 'Dummy!1234',
        });

        const res = http.post(loginUrl, payload, params);

        if (res.status === 200) {
            const resBody = res.json();
            const token = resBody.data && resBody.data.accessToken;
            if (token) {
                tokens.push(token);
            } else {
                console.warn(`User ${userNumber} login success but no token in body`);
            }
        }
    }

    if (tokens.length === 0) {
        throw new Error("No tokens acquired! Check DB or Login API.");
    }

    console.log(`Setup complete: ${tokens.length} user tokens loaded.`);

    return { tokens: tokens };
}

// 테스트할 검색 키워드 목록
const keywords = ["립스틱", "에센스", "토너", "세럼", "쿠션", "클렌징폼", "선크림", "마스크팩", "앰플", "크림"];
const API_VERSION = __ENV.API_VER || 'v1';

export default function (data) {
    const tokens = data.tokens;

    // 토큰이 없는 경우 요청을 보내지 않음
    if (!tokens || tokens.length === 0) {
        console.error("No tokens available in default function!");
        return;
    }

    const tokenIndex = (__VU - 1) % tokens.length;
    const currentToken = tokens[tokenIndex];

    if (!currentToken) {
        console.error(`Token at index ${tokenIndex} is undefined! VU: ${__VU}`);
        return;
    }

    // 랜덤 키워드 선택
    const keyword = keywords[Math.floor(Math.random() * keywords.length)];
    const encodedKeyword = encodeURIComponent(keyword);

    console.log(`[Test Start] API Version: ${API_VERSION}`);
    const url = `http://host.docker.internal:8080/api/products/${API_VERSION}/search?keyword=${encodedKeyword}&page=0&size=10`;

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${currentToken}`,
        },
    };

    const res = http.get(url, params);
    // 실패한 요청(status 200이 아닌 경우)만 상세 로그 출력
    if (res.status !== 200) {
        let errorDetail = "Unknown Error";

        try {
            // 1. 서버 응답 바디가 JSON인 경우 에러 메시지 추출
            const body = JSON.parse(res.body);
            errorDetail = body.error && body.error.message ? body.error.message : "No message in body";
        } catch (e) {
            // 2. JSON이 아니거나 에러인 경우 k6 에러 정보나 바디 일부 사용
            errorDetail = res.error ? res.error : (res.body ? res.body.substring(0, 100) : "No response body");
        }

        // console.warn으로 터미널 확인
        console.warn(`[Fail] VU: ${__VU}, Status: ${res.status}, Error: ${errorDetail}`);

        // 서버의 Logback으로 상세 에러 전송
        pushLog(`Error: ${errorDetail}`, res.status);
    }

    check(res, {
        'status is 200': (r) => r.status === 200,
        // Page 객체 응답이므로 body.data.content가 존재하는지 확인
        'content exists': (r) => {
            const body = r.json();
            return body.data && body.data.content !== undefined;
        },
    });

    sleep(1);
}

const LOG_ENDPOINT = 'http://host.docker.internal:8080/api/performance/logs';

function pushLog(message, status) {
    const payload = JSON.stringify({
        apiVersion: API_VERSION,
        vu: __VU,
        iter: __ITER,
        msg: message,
        status: status
    });

    // 로그 전송은 테스트 결과에 영향을 주지 않도록 가볍게 처리
    http.post(LOG_ENDPOINT, payload, { headers: { 'Content-Type': 'application/json' } });
}