package com.ecommerce.chatdemo.racecondition;

import com.ecommerce.chatdemo.domain.coupon.entity.Coupon;
import com.ecommerce.chatdemo.domain.coupon.entity.CouponStatus;
import com.ecommerce.chatdemo.domain.coupon.repository.CouponRepository;
import com.ecommerce.chatdemo.domain.coupon.service.CouponCoreService;
import com.ecommerce.chatdemo.domain.coupon.service.CouponService;
import com.ecommerce.chatdemo.domain.coupon.service.LockService;
import com.ecommerce.chatdemo.domain.coupon.service.NamedLockService;
import com.ecommerce.chatdemo.domain.member.entity.Member;
import com.ecommerce.chatdemo.domain.member.entity.MemberRole;
import com.ecommerce.chatdemo.domain.member.repository.MemberRepository;
import com.ecommerce.chatdemo.domain.membercoupon.repository.MemberCouponRepository;
import com.ecommerce.chatdemo.domain.membership.entity.Membership;
import com.ecommerce.chatdemo.domain.membership.repository.MembershipRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
public class CouponRaceTest {

    @Autowired
    private CouponService couponService;
    @Autowired
    private CouponCoreService couponCoreService;
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberCouponRepository memberCouponRepository;
    @Autowired
    private MembershipRepository membershipRepository;
    @Autowired
    private LockService lockService;
    @Autowired
    private NamedLockService namedLockService;

    private Long membershipId;
    private Long couponId;
    private List<Long> testMemberIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // 1. Membership 생성
        Membership membership = Membership.create("일반", 0L, 1L);
        membershipRepository.save(membership);
        membershipId = membership.getId();

        // 2. coupon 100개 생성
        Coupon coupon = couponRepository.save(
                Coupon.create(
                        "선착순 쿠폰",
                        3000L,
                        100,  // 수량 100개
                        CouponStatus.AVAILABLE,
                        LocalDateTime.now().minusDays(1),  // 발급 시작
                        LocalDateTime.now().plusDays(1),   // 발급 종료
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(7)
                )
        );
        couponId = coupon.getId();
    }
    @AfterEach
    void tearDown() {
        memberCouponRepository.deleteAll();
        memberRepository.deleteAllById(testMemberIds);
        couponRepository.deleteById(couponId);
        membershipRepository.deleteById(membershipId);
    }

    // 헬퍼 메서드
    private List<Long> createMembers(int count) {
        Membership membership = membershipRepository.findById(membershipId).orElseThrow();
        for (int i = 0; i < count; i++) {
            Member member = memberRepository.save(
                    Member.create(membership,
                            UUID.randomUUID().toString().substring(0, 8) + "@test.com",
                            "pw", "유저" + i, MemberRole.CUSTOMER)
            );
            testMemberIds.add(member.getId());
        }
        return testMemberIds;
    }
    private int runConcurrentTest(List<Long> memberIds, Function<Long, ?> task)
            throws InterruptedException {
        int poolSize = 32; //스레트 풀
        int threadCount = memberIds.size();
        ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
        CyclicBarrier barrier = new CyclicBarrier(poolSize);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            Long currentMemberId = memberIds.get(i);
            executorService.submit(() -> {
                try {
                    try {
                        barrier.await(5, TimeUnit.SECONDS);
                    } catch (BrokenBarrierException | TimeoutException ignored) { }
                    task.apply(currentMemberId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println(Thread.currentThread().getName() + " 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
        return successCount.get();
    }

    @Test
    void 쿠폰_락없음_동시성_테스트() throws InterruptedException {
        List<Long> memberIds = createMembers(1000); // 트래픽
        int successCount = runConcurrentTest(memberIds,
                memberId -> couponService.issueCoupon(memberId, couponId));

        Coupon coupon = couponRepository.findById(couponId).orElseThrow();
        System.out.println("===== 결과 =====");
        System.out.println("남은 수량: " + coupon.getQuantity());
        System.out.println("발급 성공 건수: " + successCount);
        System.out.println("초과 발급 건수: " + (successCount - 100) + "건");

        assertThat(coupon.getQuantity()).isEqualTo(0);
        assertThat(successCount).isEqualTo(100);
    }
    @Test
    void 쿠폰_낙관락_동시성_테스트() throws InterruptedException{
        List<Long> memberIds = createMembers(1000);
        int successCount = runConcurrentTest(memberIds,
                memberId -> couponService.issueCouponWithOptimisticLock(memberId, couponId));

        Coupon coupon = couponRepository.findById(couponId).orElseThrow();
        System.out.println("===== 결과 =====");
        System.out.println("남은 수량: " + coupon.getQuantity());
        System.out.println("발급 성공 건수: " + successCount);
        System.out.println("초과 발급 건수: " + (successCount - 100) + "건");

        assertThat(coupon.getQuantity()).isEqualTo(0);
        assertThat(successCount).isEqualTo(100);
    }

    @Test
    void 쿠폰_비관락_동시성_테스트() throws InterruptedException{
        List<Long> memberIds = createMembers(1000);
        int successCount = runConcurrentTest(memberIds,
                memberId -> couponService.issueCouponWithPessimisticLock(memberId, couponId));

        Coupon coupon = couponRepository.findById(couponId).orElseThrow();
        System.out.println("===== 결과 =====");
        System.out.println("남은 수량: " + coupon.getQuantity());
        System.out.println("발급 성공 건수: " + successCount);
        System.out.println("초과 발급 건수: " + (successCount - 100) + "건");

        assertThat(coupon.getQuantity()).isEqualTo(0);
        assertThat(successCount).isEqualTo(100);
    }

    // 네임드 락은 MySQL로 테스트해야함
    // @ActiveProfiles("test") 주석처리하고 테스트
    @Test
    void 쿠폰_네임드락_동시성_테스트() throws InterruptedException {
        List<Long> memberIds = createMembers(1000);
        int successCount = runConcurrentTest(memberIds,
                memberId -> namedLockService.issueCouponWithNamedLock(memberId, couponId));

        Coupon coupon = couponRepository.findById(couponId).orElseThrow();
        System.out.println("===== 결과 =====");
        System.out.println("남은 수량: " + coupon.getQuantity());
        System.out.println("발급 성공 건수: " + successCount);
        System.out.println("초과 발급 건수: " + (successCount - 100) + "건");

        assertThat(coupon.getQuantity()).isEqualTo(0);
        assertThat(successCount).isEqualTo(100);
    }

    @Test
    void 쿠폰_분산락_동시성_테스트_FailFast() throws InterruptedException{
        List<Long> memberIds = createMembers(1000);
        int successCount = runConcurrentTest(memberIds,
                memberId -> lockService.issueCouponWithRedisLock(memberId, couponId));

        Coupon coupon = couponRepository.findById(couponId).orElseThrow();
        System.out.println("===== 결과 =====");
        System.out.println("남은 수량: " + coupon.getQuantity());
        System.out.println("발급 성공 건수: " + successCount);

        assertThat(coupon.getQuantity()).isEqualTo(0);
        assertThat(successCount).isEqualTo(100);
    }

    @Test
    void 쿠폰_분산락_동시성_테스트_Retry() throws InterruptedException {
        List<Long> memberIds = createMembers(1000);
        int successCount = runConcurrentTest(memberIds,
                memberId -> lockService.issueCouponWithRedisLockWithRetry(memberId, couponId));

        Coupon coupon = couponRepository.findById(couponId).orElseThrow();
        System.out.println("===== 결과 =====");
        System.out.println("남은 수량: " + coupon.getQuantity());
        System.out.println("발급 성공 건수: " + successCount);

        assertThat(coupon.getQuantity()).isEqualTo(0);
        assertThat(successCount).isEqualTo(100);
    }

    @Test
    void 쿠폰_분산락_동시성_테스트_Blocking() throws InterruptedException {
        List<Long> memberIds = createMembers(1000);
        int successCount = runConcurrentTest(memberIds,
                memberId -> lockService.issueCouponWithRedisLockWithBlocking(memberId, couponId));

        Coupon coupon = couponRepository.findById(couponId).orElseThrow();
        System.out.println("===== 결과 =====");
        System.out.println("남은 수량: " + coupon.getQuantity());
        System.out.println("발급 성공 건수: " + successCount);

        assertThat(coupon.getQuantity()).isEqualTo(0);
        assertThat(successCount).isEqualTo(100);
    }



}
