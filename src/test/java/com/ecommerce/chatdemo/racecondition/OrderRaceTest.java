package com.ecommerce.chatdemo.racecondition;

import com.ecommerce.chatdemo.domain.cart.entity.Cart;
import com.ecommerce.chatdemo.domain.cart.repository.CartRepository;
import com.ecommerce.chatdemo.domain.cartitem.entity.CartItem;
import com.ecommerce.chatdemo.domain.cartitem.repository.CartItemRepository;
import com.ecommerce.chatdemo.domain.category.entity.Category;
import com.ecommerce.chatdemo.domain.category.repository.CategoryRepository;
import com.ecommerce.chatdemo.domain.member.entity.Member;
import com.ecommerce.chatdemo.domain.member.entity.MemberRole;
import com.ecommerce.chatdemo.domain.member.repository.MemberRepository;
import com.ecommerce.chatdemo.domain.membership.entity.Membership;
import com.ecommerce.chatdemo.domain.membership.repository.MembershipRepository;
import com.ecommerce.chatdemo.domain.order.dto.request.CreateOrderRequest;
import com.ecommerce.chatdemo.domain.order.dto.request.DirectOrderRequest;
import com.ecommerce.chatdemo.domain.order.repository.OrderRepository;
import com.ecommerce.chatdemo.domain.order.service.OrderService;
import com.ecommerce.chatdemo.domain.orderitem.entity.repository.OrderItemRepository;
import com.ecommerce.chatdemo.domain.product.entity.Product;
import com.ecommerce.chatdemo.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("쿠폰,사용자 더미 데이터")
@ActiveProfiles("test")
@SpringBootTest
public class OrderRaceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    private Long memberId;
    private Long productId;
    private Long categoryId;
    private Long membershipId;
    private Long cartId;

    @BeforeEach
    void setUp() {
        // 1. Membership 생성
        Membership membership = Membership.create("일반", 0L, 1L);
        membershipRepository.save(membership);
        membershipId = membership.getId();

        // 2. Member 생성
        Member member = Member.create(
                membership,
                "test_" + UUID.randomUUID() + "@test.com",
                "abc1234!",
                "테스트유저",
                MemberRole.CUSTOMER
        );
        member.increasePointBalance(99999999L);
        memberRepository.save(member);
        memberId = member.getId();

        // 3. Category 생성
        Category category = Category.of("테스트카테고리", null);
        categoryRepository.save(category);
        categoryId = category.getId();

        // 4. Product 생성 (재고 5개)
        Product product = Product.create(
                category,
                "테스트상품",
                10000L,
                5L,
                "테스트상품설명"
        );
        productRepository.save(product);
        productId = product.getId();

        // Cart 생성
        Cart cart = Cart.create(member);
        cartRepository.save(cart);
        cartId = cart.getId();

        // CartItem 생성
        CartItem cartItem = CartItem.create(cart, product, 1);
        cartItemRepository.save(cartItem);
    }

    @AfterEach
    void tearDown() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        memberRepository.deleteAll();
        productRepository.deleteById(productId);

        categoryRepository.deleteById(categoryId);
        membershipRepository.deleteById(membershipId);
    }

    @Test
    void 즉시결제_동시성_테스트() throws InterruptedException {
        int poolSize = 100; // 스레드
        int threadCount = 1000; // 트래픽
        ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
        CyclicBarrier barrier = new CyclicBarrier(poolSize);
        CountDownLatch latch = new CountDownLatch(threadCount);

        DirectOrderRequest request = new DirectOrderRequest(productId, 1, null, 0L);
        AtomicInteger successCount = new AtomicInteger(0);

        Membership membership = membershipRepository.findById(membershipId).orElseThrow();
        List<Long> memberIds = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Member newMember = Member.create(
                    membership,
                    UUID.randomUUID().toString().substring(0, 8) + "@test.com",
                    "abc1234!",
                    "테스트유저" + i,
                    MemberRole.CUSTOMER
            );
            newMember.increasePointBalance(99999999L);
            memberRepository.save(newMember);
            memberIds.add(newMember.getId());
        }

        for (int i = 0; i < threadCount; i++) {
            Long currentMemberId = memberIds.get(i);
            executorService.submit(() -> {
                try {
                    try {
                        barrier.await(5, TimeUnit.SECONDS);
                    } catch (BrokenBarrierException | TimeoutException ignored) { }
                    orderService.createDirectOrder(currentMemberId, request);
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

        // 결과 검증
        Product product = productRepository.findById(productId).orElseThrow();

        System.out.println("===== 결과 =====");
        System.out.println("최종 재고: " + product.getStock());
        System.out.println("결제 성공 건수: " + successCount.get());
        System.out.println("초과 결제: " + (successCount.get() - 5) + "건");

        assertThat(product.getStock()).isEqualTo(0);
        assertThat(successCount.get()).isEqualTo(5);
    }

    @Test
    void 장바구니_주문_동시성_테스트() throws InterruptedException {
        Membership membership = membershipRepository.findById(membershipId).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();
        int poolSize = 32;
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
        CyclicBarrier barrier = new CyclicBarrier(poolSize);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);

        CreateOrderRequest request = new CreateOrderRequest(null, 0L);

        // 100명의 유저와 각각의 장바구니를 미리 생성
        List<Long> concurrentMemberIds = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            String randomEmail = UUID.randomUUID().toString().substring(0, 8) + "@test.com";
            // 유저 생성
            Member newMember = memberRepository.save(Member.create(
                    membership, randomEmail, "pw", "유저" + i, MemberRole.CUSTOMER));
            newMember.increasePointBalance(999999L);

            Cart cart = cartRepository.save(Cart.create(newMember));
            cartItemRepository.save(CartItem.create(cart, product, 1)); // 같은 상품 담기

            concurrentMemberIds.add(newMember.getId());
        }

        // for문 안에서 각 스레드가 서로 '다른' 유저 ID를 들고 뛰게 만듬
        for (int i = 0; i < threadCount; i++) {
            Long currentMemberId = concurrentMemberIds.get(i); // 각기 다른 100명의 ID

            executorService.submit(() -> {
                try {
                    try {
                        barrier.await(5, TimeUnit.SECONDS); // ← 안쪽 try
                    } catch (BrokenBarrierException | TimeoutException ignored) { }
                    // 100명이 각자의 장바구니로 동시에 결제 시도
                    orderService.createOrder(currentMemberId, request);
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
        // 결과 검증
        Product updatedProduct = productRepository.findById(productId).orElseThrow();
        System.out.println("===== 결과 =====");
        System.out.println("최종 재고: " + updatedProduct.getStock());
        System.out.println("결제 성공 건수: " + successCount.get());

        assertThat(updatedProduct.getStock()).isEqualTo(0);
        assertThat(successCount.get()).isEqualTo(5);
    }
}
