package com.ecommerce.chatdemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootTest
@ActiveProfiles("test")
class ProductBulkInsertAndIndexBenchmarkTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final int TOTAL_COUNT = 50_000;
    private static final int BATCH_SIZE = 1_000;
    private static final int REPEAT = 10;
    private static final int LIMIT = 20;

    private static final String[] PRODUCT_PREFIXES = {
            "립스틱", "에센스", "토너", "세럼", "쿠션",
            "클렌징폼", "선크림", "마스크팩", "앰플", "크림"
    };

    private static final String[] PRODUCT_SUFFIXES = {
            "글로우", "모이스처", "벨벳", "시카", "퓨어",
            "브라이트", "리페어", "커버", "내추럴", "수딩"
    };

    @Test
    void 상품_벌크인서트_및_인덱스_성능_검증() {
        insertCategoriesIfNotExists();
        resetProducts();
        dropIndexIfExists();
        bulkInsertProducts();

        long categoryId = findBenchmarkCategoryId("립");

        jdbcTemplate.execute("analyze table product");
        BenchmarkResult before = benchmark(categoryId, "ON_SALE", "립", REPEAT, LIMIT);

        System.out.println("======================================");
        System.out.println("인덱스 없이 조회 성능 측정");
        printResult(before);
        System.out.println("======================================");

        jdbcTemplate.execute("""
                create index idx_product_category_status_name
                on product (category_id, status, name)
                """);

        jdbcTemplate.execute("analyze table product");
        BenchmarkResult after = benchmark(categoryId, "ON_SALE", "립", REPEAT, LIMIT);

        System.out.println("======================================");
        System.out.println("인덱스 생성 후 조회 성능 측정");
        printResult(after);
        System.out.println("======================================");
    }

    private void bulkInsertProducts() {
        List<Long> categoryIds = findCategoryIds();

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

        long start = System.currentTimeMillis();
        List<Object[]> batchArgs = new ArrayList<>(BATCH_SIZE);

        for (int i = 1; i <= TOTAL_COUNT; i++) {
            batchArgs.add(createDummyRow(categoryIds, i));

            if (i % BATCH_SIZE == 0) {
                jdbcTemplate.batchUpdate(sql, batchArgs);
                batchArgs.clear();
            }
        }

        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }

        long end = System.currentTimeMillis();

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
        Long skincare = getOrCreateParentCategoryId("스킨케어");
        Long makeup = getOrCreateParentCategoryId("메이크업");
        Long hair = getOrCreateParentCategoryId("헤어");
        Long body = getOrCreateParentCategoryId("바디");
        Long perfume = getOrCreateParentCategoryId("향수");

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
        List<Long> ids = jdbcTemplate.query(
                "select id from category where name = ? and parent_id is null",
                (rs, rowNum) -> rs.getLong("id"),
                name
        );

        if (!ids.isEmpty()) {
            return ids.get(0);
        }

        jdbcTemplate.update(
                "insert into category (name, parent_id, created_at, modified_at) values (?, null, now(), now())",
                name
        );

        return jdbcTemplate.queryForObject(
                "select id from category where name = ? and parent_id is null",
                Long.class,
                name
        );
    }

    private void insertSubCategoryIfNotExists(String name, Long parentId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from category where name = ? and parent_id = ?",
                Integer.class,
                name,
                parentId
        );

        if (count != null && count > 0) {
            return;
        }

        jdbcTemplate.update(
                "insert into category (name, parent_id, created_at, modified_at) values (?, ?, now(), now())",
                name,
                parentId
        );
    }

    private void resetProducts() {
        jdbcTemplate.update("delete from product");
    }

    private void dropIndexIfExists() {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.statistics " +
                        "where table_schema = database() " +
                        "and table_name = 'product' " +
                        "and index_name = 'idx_product_category_status_name'",
                Integer.class
        );

        if (count != null && count > 0) {
            jdbcTemplate.execute("drop index idx_product_category_status_name on product");
        }
    }

    private List<Long> findCategoryIds() {
        List<Long> categoryIds = jdbcTemplate.queryForList(
                "select id from category where parent_id is not null",
                Long.class
        );

        if (categoryIds.isEmpty()) {
            throw new IllegalStateException("소분류 카테고리 데이터가 없습니다.");
        }

        return categoryIds;
    }

    private Object[] createDummyRow(List<Long> categoryIds, int sequence) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        Long categoryId = categoryIds.get(random.nextInt(categoryIds.size()));
        String prefix = PRODUCT_PREFIXES[random.nextInt(PRODUCT_PREFIXES.length)];
        String suffix = PRODUCT_SUFFIXES[random.nextInt(PRODUCT_SUFFIXES.length)];

        String name = prefix + " " + suffix + " " + sequence;
        long price = random.nextLong(5_000, 80_001);
        long stock = random.nextLong(0, 301);
        String status = stock == 0 ? "OUT_OF_STOCK" : "ON_SALE";
        long views = random.nextLong(0, 50_000);

        LocalDateTime createdAt = LocalDateTime.now().minusDays(random.nextInt(0, 365));
        LocalDateTime modifiedAt = createdAt.plusMinutes(random.nextInt(0, 1440));

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

        if (ids.isEmpty()) {
            throw new IllegalStateException("벤치마크에 사용할 상품 데이터가 없습니다.");
        }

        return ids.get(0);
    }

    private BenchmarkResult benchmark(Long categoryId, String status, String keyword, int repeat, int limit) {
        for (int i = 0; i < 3; i++) {
            search(categoryId, status, keyword, limit);
        }

        List<Long> executionTimes = new ArrayList<>();
        int resultCount = 0;

        for (int i = 0; i < repeat; i++) {
            long start = System.nanoTime();
            List<Long> result = search(categoryId, status, keyword, limit);
            long end = System.nanoTime();

            resultCount = result.size();
            executionTimes.add((end - start) / 1_000_000);
        }

        long minMs = executionTimes.stream().mapToLong(Long::longValue).min().orElse(0L);
        long maxMs = executionTimes.stream().mapToLong(Long::longValue).max().orElse(0L);
        double avgMs = executionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);

        return new BenchmarkResult(categoryId, status, keyword, repeat, limit, resultCount, minMs, maxMs, avgMs);
    }

    private List<Long> search(Long categoryId, String status, String keyword, int limit) {
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