package com.ecommerce.chatdemo.racecondition;

import com.ecommerce.chatdemo.domain.coupon.entity.Coupon;
import com.ecommerce.chatdemo.domain.coupon.entity.CouponStatus;
import com.ecommerce.chatdemo.domain.coupon.repository.CouponRepository;
import com.ecommerce.chatdemo.domain.coupon.service.*;
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
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("카테고리,상품 벌크 인서트")
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
    @Autowired
    private RedissonService redissonService;

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

    // 순수 비지니스 로직 처리시간 측정용
    @Test
    void 쿠폰_발급_처리시간_측정() {
        List<Long> memberIds = createMembers(1);
        Long memberId = memberIds.get(0);

        long start = System.currentTimeMillis();
        couponService.issueCoupon(memberId, couponId);
        long end = System.currentTimeMillis();

        System.out.println("쿠폰 발급 처리 시간: " + (end - start) + "ms");
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
    private int runConcurrentTest(List<Long> memberIds, Consumer<Long> task, String lockType)
            throws InterruptedException {
        int poolSize = 32; //스레트 풀
        int threadCount = memberIds.size();
        ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
        CyclicBarrier barrier = new CyclicBarrier(poolSize);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        long start = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            Long currentMemberId = memberIds.get(i);
            executorService.submit(() -> {
                try {
                    try {
                        barrier.await(5, TimeUnit.SECONDS);
                    } catch (BrokenBarrierException | TimeoutException ignored) { }
                    task.accept(currentMemberId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println(Thread.currentThread().getName()
                            + " 실패: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        long end = System.currentTimeMillis();
        executorService.shutdown();

        Coupon coupon = couponRepository.findById(couponId).orElseThrow();
        System.out.println("\n===== [" + lockType + "] 결과 =====");
        System.out.println("총 요청 수    : " + threadCount);
        System.out.println("성공 건수     : " + successCount.get());
        System.out.println("실패 건수     : " + failCount.get());
        System.out.println("남은 수량     : " + coupon.getQuantity());
        System.out.println("초과 발급     : " + Math.max(0, successCount.get() - 100) + "건");
        System.out.println("소요 시간     : " + (end - start) + "ms");
        System.out.println("==============================\n");

        return successCount.get();
    }

    // 동시성 재현용 헬퍼 메서드 (락없는 버전)
    private int runConcurrentTestForRaceCondition(List<Long> memberIds, Consumer<Long> task)
            throws InterruptedException {
        int threadCount = memberIds.size();
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CyclicBarrier barrier = new CyclicBarrier(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        long start = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            Long currentMemberId = memberIds.get(i);
            executorService.submit(() -> {
                try {
                    try {
                        barrier.await(5, TimeUnit.SECONDS);
                    } catch (BrokenBarrierException | TimeoutException ignored) { }
                    task.accept(currentMemberId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long end = System.currentTimeMillis();
        executorService.shutdown();

        Coupon coupon = couponRepository.findById(couponId).orElseThrow();
        System.out.println("\n===== [락 없음 - Race Condition 재현 결과] =====");
        System.out.println("총 요청 수    : " + threadCount);
        System.out.println("성공 건수     : " + successCount.get());
        System.out.println("실패 건수     : " + failCount.get());
        System.out.println("남은 수량     : " + coupon.getQuantity());
        System.out.println("초과 발급     : " + Math.max(0, successCount.get() - 100) + "건");
        System.out.println("소요 시간     : " + (end - start) + "ms");
        System.out.println("============================================\n");

        return successCount.get();
    }

    @Test
    void 쿠폰_락없음_동시성_테스트() throws InterruptedException {
        List<Long> memberIds = createMembers(300); // 트래픽
        int successCount = runConcurrentTestForRaceCondition(memberIds,
                memberId -> couponService.issueCoupon(memberId, couponId));

        Coupon coupon = couponRepository.findById(couponId).orElseThrow();

        assertThat(coupon.getQuantity()).isEqualTo(0);
        assertThat(successCount).isEqualTo(100);
    }
    @Test // 테스트 전에 쿠폰 엔터티에 @Version 주석 해제하기
    void 쿠폰_낙관락_동시성_테스트() throws InterruptedException{
        List<Long> memberIds = createMembers(300);
        int successCount = runConcurrentTest(memberIds,
                // 재시도 없는 버전으로 테스트 시 주석 해제
//                memberId -> couponService.issueCouponWithOptimisticLock(memberId, couponId), "낙관적 락");
                 memberId -> couponCoreService.issueCouponRetry(memberId, couponId), "낙관적 락");

        Coupon coupon = couponRepository.findById(couponId).orElseThrow();

        assertThat(coupon.getQuantity()).isEqualTo(0);
        assertThat(successCount).isEqualTo(100);
    }

    @Test
    void 쿠폰_비관락_동시성_테스트() throws InterruptedException{
        List<Long> memberIds = createMembers(300);
        int successCount = runConcurrentTest(memberIds,
                memberId -> couponService.issueCouponWithPessimisticLock(memberId, couponId), "비관적 락");

        Coupon coupon = couponRepository.findById(couponId).orElseThrow();

        assertThat(coupon.getQuantity()).isEqualTo(0);
        assertThat(successCount).isEqualTo(100);
    }

    // 네임드 락은 MySQL로 테스트해야함
    // @ActiveProfiles("test") 주석처리하고 테스트
    @Test
    void 쿠폰_네임드락_동시성_테스트() throws InterruptedException {
        List<Long> memberIds = createMembers(300);
        int successCount = runConcurrentTest(memberIds,
                memberId -> namedLockService.issueCouponWithNamedLock(memberId, couponId), "MySQL 네임드 락");

        Coupon coupon = couponRepository.findById(couponId).orElseThrow();

        assertThat(coupon.getQuantity()).isEqualTo(0);
        assertThat(successCount).isEqualTo(100);
    }

    @Test
    void 쿠폰_분산락_동시성_테스트_FailFast() throws InterruptedException{
        List<Long> memberIds = createMembers(300);
        int successCount = runConcurrentTest(memberIds,
                memberId -> lockService.issueCouponWithRedisLock(memberId, couponId), "분산 락(Fail Fast)");

        Coupon coupon = couponRepository.findById(couponId).orElseThrow();

        assertThat(coupon.getQuantity()).isEqualTo(0);
        assertThat(successCount).isEqualTo(100);
    }

    @Test
    void 쿠폰_분산락_동시성_테스트_Retry() throws InterruptedException {
        List<Long> memberIds = createMembers(300);
        int successCount = runConcurrentTest(memberIds,
                memberId -> lockService.issueCouponWithRedisLockWithRetry(memberId, couponId), "분산 락(Retry with Backoff)");

        Coupon coupon = couponRepository.findById(couponId).orElseThrow();

        assertThat(coupon.getQuantity()).isEqualTo(0);
        assertThat(successCount).isEqualTo(100);
    }

    @Test
    void 쿠폰_분산락_동시성_테스트_Blocking() throws InterruptedException {
        List<Long> memberIds = createMembers(300);
        int successCount = runConcurrentTest(memberIds,
                memberId -> lockService.issueCouponWithRedisLockWithBlocking(memberId, couponId), "분산 락(Blocking)");

        Coupon coupon = couponRepository.findById(couponId).orElseThrow();

        assertThat(coupon.getQuantity()).isEqualTo(0);
        assertThat(successCount).isEqualTo(100);
    }

    @Test
    void 쿠폰_Redisson_락_동시성_테스트() throws InterruptedException {
        List<Long> memberIds = createMembers(300);
        int successCount = runConcurrentTest(memberIds,
                memberId -> redissonService.issueCouponWithRedisson(memberId, couponId), "Redisson 락");

        Coupon coupon = couponRepository.findById(couponId).orElseThrow();

        assertThat(coupon.getQuantity()).isEqualTo(0);
        assertThat(successCount).isEqualTo(100);
    }




}
