import http from 'k6/http';
import { check, sleep } from 'k6';

// 테스트 부하 설정 (Ramp-up으로 점진적 부하 증가)
export const options = {
    stages: [
        { duration: '30s', target: 20 }, // 30초 동안 사용자 0명에서 20명까지 점진적 증가
        { duration: '1m', target: 20 },  // 1분 동안 사용자 20명 유지 (평균 응답속도 측정)
        { duration: '30s', target: 50 }, // 30초 동안 사용자 50명까지 추가 증가 (부하 한계 측정)
        { duration: '1m', target: 50 },  // 1분 동안 사용자 50명 유지
        { duration: '30s', target: 0 },  // 30초 동안 종료
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'], // 에러율 1% 미만 유지
        http_req_duration: ['p(95)<300'], // 95%의 요청은 300ms 이내에 완료되어야 함
    },
};

// 테스트 시작 전 로그인을 수행하고 토큰을 가져오는 함수
export function setup() {
    const loginUrl = 'http://host.docker.internal:8080/api/auth/login';
    const payload = JSON.stringify({
        email: 'test3@test.com',
        password: 'Test1234!',
    });

    const params = {
        headers: { 'Content-Type': 'application/json' },
    };

    const res = http.post(loginUrl, payload, params);
    const body = res.json();

    // ApiResponse<LoginResponse> 구조에 맞춰 data 필드 접근
    const authToken = body.data && body.data.accessToken;

    const success = check(res, {
        'setup login success': (r) => r.status === 200,
        'has token': () => authToken !== undefined && authToken !== null,
    });

    if (!success) {
        throw new Error(`Login failed! Status: ${res.status}, Message: ${body.message}`);
    }

    return { token: authToken };
}

// 테스트할 검색 키워드 목록
const keywords = ['상품', '토너', '에센스', '앰플', '클렌징폼'];

export default function (data) {
    // 랜덤 키워드 선택
    const keyword = keywords[Math.floor(Math.random() * keywords.length)];
    const encodedKeyword = encodeURIComponent(keyword);

    // 환경변수 API_VER (v1, v2, v3)에 따른 엔드포인트 분기
    const API_VERSION = 'v1';
    const page = 0;
    const size = 10;
    const url = `http://host.docker.internal:8080/api/${API_VERSION}/products/search?keyword=${encodedKeyword}&page=${page}&size=${size}`;

    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${data.token}`,
        },
    };

    const res = http.get(url, params);

    // 디버깅: 여전히 400이 난다면 서버가 받는 URL을 확인
    if (res.status === 400) {
        console.log(`[400 Error] Requested URL: ${url}`);
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