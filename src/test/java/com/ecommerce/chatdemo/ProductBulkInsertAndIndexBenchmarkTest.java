package com.ecommerce.chatdemo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Disabled("쿠폰 동시성 테스트")
@ActiveProfiles("test")
@SpringBootTest
class ProductBulkInsertAndIndexBenchmarkTest {

    // SQL 실행과 배치 insert에 사용
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 더미데이터 개수
    private static final int TOTAL_COUNT = 50_000;

    // 배치 insert 묶음 크기
    private static final int BATCH_SIZE = 1_000;

    // 같은 조회 쿼리 반복 횟수
    private static final int REPEAT = 10;

    // 조회 최대 건수
    private static final int LIMIT = 20;

    // 상품명 앞부분 후보
    private static final String[] PRODUCT_PREFIXES = {
            "립스틱", "에센스", "토너", "세럼", "쿠션",
            "클렌징폼", "선크림", "마스크팩", "앰플", "크림"
    };

    // 상품명 뒷부분 후보
    private static final String[] PRODUCT_SUFFIXES = {
            "글로우", "모이스처", "벨벳", "시카", "퓨어",
            "브라이트", "리페어", "커버", "내추럴", "수딩"
    };

    @Test
    void 상품_벌크인서트_및_인덱스_성능_검증() {

        // 카테고리 없으면 생성
        insertCategoriesIfNotExists();

        // 이전 상품 데이터 초기화
        resetProducts();

        // 이전 인덱스가 남아 있으면 제거
        dropIndexIfExists();

        // 상품 5만 건 적재
        bulkInsertProducts();

        // "립" 검색에 가장 적합한 카테고리 선택
        long categoryId = findBenchmarkCategoryId("립");

        // 통계 갱신 후 인덱스 없는 상태 측정
        jdbcTemplate.execute("analyze table product");
        BenchmarkResult before = benchmark(categoryId, "ON_SALE", "립", REPEAT, LIMIT);

        System.out.println("======================================");
        System.out.println("인덱스 없이 조회 성능 측정");
        printResult(before);
        System.out.println("======================================");

        // 복합 인덱스 생성
        jdbcTemplate.execute("""
                create index idx_product_category_status_name
                on product (category_id, status, name)
                """);

        // 통계 갱신 후 인덱스 있는 상태 측정
        jdbcTemplate.execute("analyze table product");
        BenchmarkResult after = benchmark(categoryId, "ON_SALE", "립", REPEAT, LIMIT);

        System.out.println("======================================");
        System.out.println("인덱스 생성 후 조회 성능 측정");
        printResult(after);
        System.out.println("======================================");
    }

    private void bulkInsertProducts() {
        // 사용할 소분류 카테고리 목록 조회
        List<Long> categoryIds = findCategoryIds();

        // product 배치 insert SQL
        String sql = """
                insert into product (
                    category_id,
                    name,
                    price,
                    stock,
                    description,
                    status,
                    views,
                    created_at,
                    modified_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        // 적재 시작 시간
        long start = System.currentTimeMillis();

        // 배치 insert용 데이터 저장소
        List<Object[]> batchArgs = new ArrayList<>(BATCH_SIZE);

        for (int i = 1; i <= TOTAL_COUNT; i++) {
            // 더미 상품 1건 생성
            batchArgs.add(createDummyRow(categoryIds, i));

            // 1000건마다 배치 insert 실행
            if (i % BATCH_SIZE == 0) {
                jdbcTemplate.batchUpdate(sql, batchArgs);
                batchArgs.clear();
            }
        }

        // 남은 데이터가 있으면 마지막 배치 insert 실행
        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }

        // 적재 종료 시간
        long end = System.currentTimeMillis();

        // 실제 저장된 상품 수 확인
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from product",
                Integer.class
        );

        System.out.println("======================================");
        System.out.println("상품 더미 적재 완료");
        System.out.println("요청 건수 = " + TOTAL_COUNT);
        System.out.println("전체 상품 수 = " + count);
        System.out.println("소요 시간(ms) = " + (end - start));
        System.out.println("======================================");
    }

    private void insertCategoriesIfNotExists() {
        // 대분류 생성 또는 조회
        Long skincare = getOrCreateParentCategoryId("스킨케어");
        Long makeup = getOrCreateParentCategoryId("메이크업");
        Long hair = getOrCreateParentCategoryId("헤어");
        Long body = getOrCreateParentCategoryId("바디");
        Long perfume = getOrCreateParentCategoryId("향수");

        // 소분류 생성 또는 조회
        insertSubCategoryIfNotExists("선크림", skincare);
        insertSubCategoryIfNotExists("토너", skincare);
        insertSubCategoryIfNotExists("세럼", skincare);
        insertSubCategoryIfNotExists("에센스", skincare);
        insertSubCategoryIfNotExists("크림", skincare);

        insertSubCategoryIfNotExists("립스틱", makeup);
        insertSubCategoryIfNotExists("쿠션", makeup);
        insertSubCategoryIfNotExists("마스크팩", makeup);
        insertSubCategoryIfNotExists("앰플", makeup);

        insertSubCategoryIfNotExists("헤어에센스", hair);
        insertSubCategoryIfNotExists("클렌징폼", hair);

        insertSubCategoryIfNotExists("바디크림", body);
        insertSubCategoryIfNotExists("바디워시", body);

        insertSubCategoryIfNotExists("퍼퓸", perfume);
    }

    private Long getOrCreateParentCategoryId(String name) {
        // 같은 이름의 대분류 조회
        List<Long> ids = jdbcTemplate.query(
                "select id from category where name = ? and parent_id is null",
                (rs, rowNum) -> rs.getLong("id"),
                name
        );

        // 이미 있으면 그대로 사용
        if (!ids.isEmpty()) {
            return ids.get(0);
        }

        // 없으면 새로 생성
        jdbcTemplate.update(
                "insert into category (name, parent_id, created_at, modified_at) values (?, null, now(), now())",
                name
        );

        // 생성 후 다시 조회해서 id 반환
        return jdbcTemplate.queryForObject(
                "select id from category where name = ? and parent_id is null",
                Long.class,
                name
        );
    }

    private void insertSubCategoryIfNotExists(String name, Long parentId) {
        // 같은 이름 + 같은 부모 카테고리 존재 여부 확인
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from category where name = ? and parent_id = ?",
                Integer.class,
                name,
                parentId
        );

        // 이미 있으면 생성하지 않음
        if (count != null && count > 0) {
            return;
        }

        // 없으면 새로 생성
        jdbcTemplate.update(
                "insert into category (name, parent_id, created_at, modified_at) values (?, ?, now(), now())",
                name,
                parentId
        );
    }

    private void resetProducts() {
        // 기존 product 데이터 전체 삭제
        jdbcTemplate.update("delete from product");
    }


    /** dropIndexIfExists()
     * BulkInsert를 하기 전 index를 지우는 로직이 있음
     * P) information_schema.statistics (index를 관리하는 메타데이터)에 접근하려다보니 권한 문제가 있어서 조회를 실패!
     * S) product 테이블의 인덱스 목록을 찾아서 지우는 방향으로 수정!
     */
//    private void dropIndexIfExists() {
//        Integer count = jdbcTemplate.queryForObject(
//                "select count(*) from information_schema.statistics " +
//                        "where table_schema = database() " +
//                        "and table_name = 'product' " +
//                        "and index_name = 'idx_product_category_status_name'",
//                Integer.class
//        );
//
//        if (count != null && count > 0) {
//            jdbcTemplate.execute("drop index idx_product_category_status_name on product");
//        }
//    }

    private void dropIndexIfExists() {
        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
                "SHOW INDEX FROM product WHERE Key_name = 'idx_product_category_status_name'"
        );
        if (!indexes.isEmpty()) {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
            jdbcTemplate.execute("DROP INDEX idx_product_category_status_name ON product");
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }


    private List<Long> findCategoryIds() {
        // 소분류 카테고리 id 목록 조회
        List<Long> categoryIds = jdbcTemplate.queryForList(
                "select id from category where parent_id is not null",
                Long.class
        );

        // 없으면 상품 적재 불가
        if (categoryIds.isEmpty()) {
            throw new IllegalStateException("소분류 카테고리 데이터가 없습니다.");
        }

        return categoryIds;
    }

    private Object[] createDummyRow(List<Long> categoryIds, int sequence) {
        // 랜덤 객체 생성
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // 랜덤 카테고리 선택
        Long categoryId = categoryIds.get(random.nextInt(categoryIds.size()));

        // 랜덤 상품명 조합
        String prefix = PRODUCT_PREFIXES[random.nextInt(PRODUCT_PREFIXES.length)];
        String suffix = PRODUCT_SUFFIXES[random.nextInt(PRODUCT_SUFFIXES.length)];

        // 최종 상품명
        String name = prefix + " " + suffix + " " + sequence;

        // 가격, 재고, 조회수 랜덤 생성
        long price = random.nextLong(5_000, 80_001);
        long stock = random.nextLong(0, 301);
        long views = random.nextLong(0, 50_000);

        // 재고 0이면 품절, 아니면 판매중
        String status = stock == 0 ? "OUT_OF_STOCK" : "ON_SALE";

        // 생성일, 수정일 랜덤 생성
        LocalDateTime createdAt = LocalDateTime.now().minusDays(random.nextInt(0, 365));
        LocalDateTime modifiedAt = createdAt.plusMinutes(random.nextInt(0, 1440));

        // SQL 바인딩 순서에 맞춰 Object 배열 반환
        return new Object[]{
                categoryId,
                name,
                price,
                stock,
                name + " 설명입니다. 인덱스 테스트용 더미 데이터입니다.",
                status,
                views,
                Timestamp.valueOf(createdAt),
                Timestamp.valueOf(modifiedAt)
        };
    }

    private long findBenchmarkCategoryId(String keyword) {
        // 검색어로 시작하는 판매중 상품이 가장 많은 category_id 선택
        List<Long> ids = jdbcTemplate.queryForList(
                """
                select category_id
                from product
                where status = 'ON_SALE'
                  and name like concat(?, '%')
                group by category_id
                order by count(*) desc
                limit 1
                """,
                Long.class,
                keyword
        );

        // 없으면 벤치마크 불가
        if (ids.isEmpty()) {
            throw new IllegalStateException("벤치마크에 사용할 상품 데이터가 없습니다.");
        }

        return ids.get(0);
    }

    private BenchmarkResult benchmark(Long categoryId, String status, String keyword, int repeat, int limit) {
        // 워밍업 조회
        for (int i = 0; i < 3; i++) {
            search(categoryId, status, keyword, limit);
        }

        // 실행 시간 저장용 리스트
        List<Long> executionTimes = new ArrayList<>();

        // 마지막 조회 결과 건수 저장
        int resultCount = 0;

        for (int i = 0; i < repeat; i++) {
            // 시작 시간 측정
            long start = System.nanoTime();

            // 실제 조회 실행
            List<Long> result = search(categoryId, status, keyword, limit);

            // 종료 시간 측정
            long end = System.nanoTime();

            // 조회 결과 개수 저장
            resultCount = result.size();

            // 밀리초 단위로 변환해서 저장
            executionTimes.add((end - start) / 1_000_000);
        }

        // 최소값 계산
        long minMs = executionTimes.stream().mapToLong(Long::longValue).min().orElse(0L);

        // 최대값 계산
        long maxMs = executionTimes.stream().mapToLong(Long::longValue).max().orElse(0L);

        // 평균값 계산
        double avgMs = executionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);

        // 측정 결과를 record로 묶어서 반환
        return new BenchmarkResult(categoryId, status, keyword, repeat, limit, resultCount, minMs, maxMs, avgMs);
    }

    private List<Long> search(Long categoryId, String status, String keyword, int limit) {
        // 인덱스 전후 비교 대상 조회 쿼리
        return jdbcTemplate.query(
                """
                select id
                from product
                where category_id = ?
                  and status = ?
                  and name like concat(?, '%')
                order by name asc
                limit ?
                """,
                (rs, rowNum) -> rs.getLong("id"),
                categoryId, status, keyword, limit
        );
    }

    private void printResult(BenchmarkResult result) {
        // 벤치마크 결과 출력
        System.out.println("categoryId = " + result.categoryId());
        System.out.println("status = " + result.status());
        System.out.println("keyword = " + result.keyword());
        System.out.println("repeat = " + result.repeat());
        System.out.println("limit = " + result.limit());
        System.out.println("resultCount = " + result.resultCount());
        System.out.println("minMs = " + result.minMs());
        System.out.println("maxMs = " + result.maxMs());
        System.out.println("avgMs = " + result.avgMs());
    }

    private record BenchmarkResult(
            Long categoryId,
            String status,
            String keyword,
            int repeat,
            int limit,
            int resultCount,
            long minMs,
            long maxMs,
            double avgMs
    ) {
    }
}