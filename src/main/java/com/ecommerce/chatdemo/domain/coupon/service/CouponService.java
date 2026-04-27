package com.ecommerce.chatdemo.domain.coupon.service;

import com.ecommerce.chatdemo.domain.coupon.dto.request.CreateCouponRequest;
import com.ecommerce.chatdemo.domain.coupon.dto.response.CreateCouponResponse;
import com.ecommerce.chatdemo.domain.coupon.dto.response.GetCouponResponse;
import com.ecommerce.chatdemo.domain.coupon.dto.response.GetMemberCouponResponse;
import com.ecommerce.chatdemo.domain.coupon.dto.response.IssueCouponResponse;
import com.ecommerce.chatdemo.domain.coupon.entity.Coupon;
import com.ecommerce.chatdemo.domain.coupon.entity.CouponStatus;
import com.ecommerce.chatdemo.domain.coupon.exception.CouponErrorCode;
import com.ecommerce.chatdemo.domain.coupon.exception.CouponException;
import com.ecommerce.chatdemo.domain.coupon.repository.CouponRepository;
import com.ecommerce.chatdemo.domain.member.entity.Member;
import com.ecommerce.chatdemo.domain.member.repository.MemberRepository;
import com.ecommerce.chatdemo.domain.membercoupon.entity.MemberCoupon;
import com.ecommerce.chatdemo.domain.membercoupon.repository.MemberCouponRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;
    private final MemberRepository memberRepository;
    private final MemberCouponRepository memberCouponRepository;

    // 관리자가 쿠폰 생성
    @Transactional
    public CreateCouponResponse createCoupon(CreateCouponRequest request) {
        Coupon coupon = Coupon.create(
                request.getCouponName(),
                request.getDiscountAmount(),
                request.getQuantity(),
                CouponStatus.AVAILABLE,
                request.getIssueStartAt(),
                request.getIssueEndAt(),
                request.getUseStartAt(),
                request.getUseEndAt()
        );
        Coupon saveCoupon = couponRepository.save(coupon);

        return new CreateCouponResponse(
                saveCoupon.getId(),
                saveCoupon.getStatus(),
                "쿠폰이 정상적으로 생성되었습니다."
        );
    }

    // 관리자가 쿠폰 목록 조회
    public Page<GetCouponResponse> getCoupon(int page, int size) {
        Pageable pageable = PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Coupon> coupons = couponRepository.findAll(pageable);

        return coupons.map(GetCouponResponse::new);
    }

    // 사용자가 쿠폰 목록 조회
    public Page<GetMemberCouponResponse> getMemberCoupon(Long memberId, int page, int size) {
        Pageable pageable = PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return memberCouponRepository.findByMemberIdWithPage(memberId, pageable) // N+1 방지
                .map(GetMemberCouponResponse::new);
    }

    // 사용자가 쿠폰 등록 (redis 분산 락, LockService에서 해당 메서드 사용)
    @Transactional
    public IssueCouponResponse issueCoupon(Long memberId, Long couponId) {
        Member member = memberRepository.getReferenceById(memberId);

        Coupon coupon = couponRepository.findById(couponId).orElseThrow(
                () -> new CouponException(CouponErrorCode.COUPON_NOT_FOUND)
        );
        // 쿠폰 발급 기간이 맞는 지
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getIssueStartAt()) || now.isAfter(coupon.getIssueEndAt())) {
            throw new CouponException(CouponErrorCode.INVALID_COUPON_ISSUE_DATE);
        }

        // 이미 발급받은 쿠폰인지
        if (memberCouponRepository.existsByMemberIdAndCouponId(memberId, couponId)) {
            throw new CouponException(CouponErrorCode.ALREADY_ISSUED_COUPON);
        }
        // 쿠폰 수량 차감
        coupon.decreaseCouponQuantity();

        // 사용자 쿠폰 생성
        MemberCoupon memberCoupon = memberCouponRepository.save(
                MemberCoupon.create(member, coupon)
        );
        return new IssueCouponResponse(memberCoupon);
    }

    // 낙관락 버전
    @Transactional
    public IssueCouponResponse issueCouponWithOptimisticLock(Long memberId, Long couponId) {
        Member member = memberRepository.getReferenceById(memberId);

        Coupon coupon = couponRepository.findById(couponId).orElseThrow(
                () -> new CouponException(CouponErrorCode.COUPON_NOT_FOUND)
        );
        // 쿠폰 발급 기간이 맞는 지
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getIssueStartAt()) || now.isAfter(coupon.getIssueEndAt())) {
            throw new CouponException(CouponErrorCode.INVALID_COUPON_ISSUE_DATE);
        }

        // 이미 발급받은 쿠폰인지
        if (memberCouponRepository.existsByMemberIdAndCouponId(memberId, couponId)) {
            throw new CouponException(CouponErrorCode.ALREADY_ISSUED_COUPON);
        }
        // 쿠폰 수량 차감
        coupon.decreaseCouponQuantity();

        // 사용자 쿠폰 생성
        MemberCoupon memberCoupon = memberCouponRepository.save(
                MemberCoupon.create(member, coupon)
        );
        return new IssueCouponResponse(memberCoupon);
    }
    // 비관적 락 버전
    @Transactional
    public IssueCouponResponse issueCouponWithPessimisticLock(Long memberId, Long couponId) {
        Member member = memberRepository.getReferenceById(memberId);

        Coupon coupon = couponRepository.findByIdWithLock(couponId).orElseThrow(
                () -> new CouponException(CouponErrorCode.COUPON_NOT_FOUND)
        );
        // 쿠폰 발급 기간이 맞는 지
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getIssueStartAt()) || now.isAfter(coupon.getIssueEndAt())) {
            throw new CouponException(CouponErrorCode.INVALID_COUPON_ISSUE_DATE);
        }

        // 이미 발급받은 쿠폰인지
        if (memberCouponRepository.existsByMemberIdAndCouponId(memberId, couponId)) {
            throw new CouponException(CouponErrorCode.ALREADY_ISSUED_COUPON);
        }
        // 쿠폰 수량 차감
        coupon.decreaseCouponQuantity();

        // 사용자 쿠폰 생성
        MemberCoupon memberCoupon = memberCouponRepository.save(
                MemberCoupon.create(member, coupon)
        );
        return new IssueCouponResponse(memberCoupon);
    }
}
