package com.ecommerce.chatdemo;

import com.ecommerce.chatdemo.domain.coupon.entity.CouponStatus;
import com.ecommerce.chatdemo.domain.member.entity.MemberRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SpringBootTest
class CouponAndMemberDummyDataInsertTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int TARGET_MEMBER_COUNT = 300;
    private static final int BATCH_SIZE = 100;

    private static final String DEFAULT_MEMBERSHIP_NAME = "BASIC";
    private static final String DEFAULT_PASSWORD = "Dummy!1234";

    private static final String SCENARIO_COUPON_NAME = "선착순_100명_쿠폰_이벤트";
    private static final long SCENARIO_DISCOUNT_AMOUNT = 3000L;
    private static final int SCENARIO_COUPON_QUANTITY = 100;

    private static final LocalDateTime ISSUE_START_AT = LocalDateTime.of(2026, 4, 16, 0, 0, 0);
    private static final LocalDateTime ISSUE_END_AT = LocalDateTime.of(2026, 4, 18, 23, 59, 59);
    private static final LocalDateTime USE_START_AT = LocalDateTime.of(2026, 4, 16, 0, 0, 0);
    private static final LocalDateTime USE_END_AT = LocalDateTime.of(2026, 4, 30, 23, 59, 59);

    @Test
    void 사용자_200명_더미데이터_적재() {
        Long membershipId = getOrCreateBasicMembershipId();
        insertDummyCustomers(membershipId);
        printMemberSummary();
    }

    @Test
    void 선착순_쿠폰_더미데이터_적재() {
        upsertScenarioCoupon();
        printCouponSummary();
    }

    private Long getOrCreateBasicMembershipId() {
        List<Long> ids = jdbcTemplate.query(
                "select id from membership where name = ? limit 1",
                (rs, rowNum) -> rs.getLong("id"),
                DEFAULT_MEMBERSHIP_NAME
        );

        if (!ids.isEmpty()) {
            return ids.get(0);
        }

        jdbcTemplate.update(
                """
                insert into membership (
                    name,
                    min_spent_amount,
                    point_rate,
                    created_at,
                    modified_at
                ) values (?, ?, ?, now(), now())
                """,
                DEFAULT_MEMBERSHIP_NAME,
                0L,
                1L
        );

        return jdbcTemplate.queryForObject(
                "select id from membership where name = ? limit 1",
                Long.class,
                DEFAULT_MEMBERSHIP_NAME
        );
    }

    private void insertDummyCustomers(Long membershipId) {
        Set<String> existingEmails = new HashSet<>(jdbcTemplate.queryForList(
                "select email from member where email like 'coupon-user-%@example.com'",
                String.class
        ));

        String sql = """
                insert into member (
                    membership_id,
                    email,
                    password,
                    name,
                    role,
                    point_balance,
                    created_at,
                    modified_at
                ) values (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);
        List<Object[]> batchArgs = new ArrayList<>(BATCH_SIZE);

        for (int i = 1; i <= TARGET_MEMBER_COUNT; i++) {
            String email = String.format("coupon-user-%03d@example.com", i);

            if (existingEmails.contains(email)) {
                continue;
            }

            LocalDateTime createdAt = LocalDateTime.of(2026, 4, 15, 12, 0, 0).minusMinutes(i);
            LocalDateTime modifiedAt = createdAt;

            batchArgs.add(new Object[]{
                    membershipId,
                    email,
                    encodedPassword,
                    String.format("쿠폰테스트유저%03d", i),
                    MemberRole.CUSTOMER.name(),
                    0L,
                    Timestamp.valueOf(createdAt),
                    Timestamp.valueOf(modifiedAt)
            });

            if (batchArgs.size() == BATCH_SIZE) {
                jdbcTemplate.batchUpdate(sql, batchArgs);
                batchArgs.clear();
            }
        }

        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }
    }

    private void upsertScenarioCoupon() {
        List<Long> couponIds = jdbcTemplate.query(
                "select id from coupon where name = ?",
                (rs, rowNum) -> rs.getLong("id"),
                SCENARIO_COUPON_NAME
        );

        if (couponIds.isEmpty()) {
            jdbcTemplate.update(
                    """
                    insert into coupon (
                        name,
                        discount_amount,
                        quantity,
                        status,
                        issue_start_at,
                        issue_end_at,
                        use_start_at,
                        use_end_at,
                        created_at,
                        modified_at
                    ) values (?, ?, ?, ?, ?, ?, ?, ?, now(), now())
                    """,
                    SCENARIO_COUPON_NAME,
                    SCENARIO_DISCOUNT_AMOUNT,
                    SCENARIO_COUPON_QUANTITY,
                    CouponStatus.AVAILABLE.name(),
                    Timestamp.valueOf(ISSUE_START_AT),
                    Timestamp.valueOf(ISSUE_END_AT),
                    Timestamp.valueOf(USE_START_AT),
                    Timestamp.valueOf(USE_END_AT)
            );
            return;
        }

        jdbcTemplate.update(
                """
                update coupon
                set discount_amount = ?,
                    quantity = ?,
                    status = ?,
                    issue_start_at = ?,
                    issue_end_at = ?,
                    use_start_at = ?,
                    use_end_at = ?,
                    modified_at = now()
                where name = ?
                """,
                SCENARIO_DISCOUNT_AMOUNT,
                SCENARIO_COUPON_QUANTITY,
                CouponStatus.AVAILABLE.name(),
                Timestamp.valueOf(ISSUE_START_AT),
                Timestamp.valueOf(ISSUE_END_AT),
                Timestamp.valueOf(USE_START_AT),
                Timestamp.valueOf(USE_END_AT),
                SCENARIO_COUPON_NAME
        );
    }

    private void printMemberSummary() {
        Integer memberCount = jdbcTemplate.queryForObject(
                "select count(*) from member where email like 'coupon-user-%@example.com'",
                Integer.class
        );

        System.out.println("======================================");
        System.out.println("사용자 더미데이터 적재 완료");
        System.out.println("사용자 수 = " + memberCount);
        System.out.println("테스트 계정 비밀번호 = " + DEFAULT_PASSWORD);
        System.out.println("======================================");
    }

    private void printCouponSummary() {
        Integer couponCount = jdbcTemplate.queryForObject(
                "select count(*) from coupon where name = ?",
                Integer.class,
                SCENARIO_COUPON_NAME
        );

        Integer couponQuantity = jdbcTemplate.queryForObject(
                "select quantity from coupon where name = ?",
                Integer.class,
                SCENARIO_COUPON_NAME
        );

        System.out.println("======================================");
        System.out.println("쿠폰 더미데이터 적재 완료");
        System.out.println("이벤트 쿠폰 row 수 = " + couponCount);
        System.out.println("이벤트 쿠폰 수량 = " + couponQuantity);
        System.out.println("======================================");
    }
}