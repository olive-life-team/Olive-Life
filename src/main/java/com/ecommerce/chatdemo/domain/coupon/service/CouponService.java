package com.ecommerce.chatdemo.domain.coupon.service;

import com.ecommerce.chatdemo.domain.coupon.dto.request.CreateCouponRequest;
import com.ecommerce.chatdemo.domain.coupon.dto.response.CreateCouponResponse;
import com.ecommerce.chatdemo.domain.coupon.dto.response.GetCouponResponse;
import com.ecommerce.chatdemo.domain.coupon.entity.Coupon;
import com.ecommerce.chatdemo.domain.coupon.entity.CouponStatus;
import com.ecommerce.chatdemo.domain.coupon.repository.CouponRepository;
import com.ecommerce.chatdemo.domain.member.entity.Member;
import com.ecommerce.chatdemo.domain.member.entity.MemberRole;
import com.ecommerce.chatdemo.domain.member.repository.MemberRepository;
import com.ecommerce.chatdemo.global.exception.AuthErrorCode;
import com.ecommerce.chatdemo.global.exception.BusinessException;
import com.ecommerce.chatdemo.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;
    private final MemberRepository memberRepository;

    // 관리자가 쿠폰 생성
    @Transactional
    public CreateCouponResponse createCoupon(Long memberId, CreateCouponRequest request) {

        // TODO: RBAC 도입시 리팩토링 필요
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new BusinessException(AuthErrorCode.USER_NOT_FOUND)
        );
        // 멤버의 역할이 고객이면 권한 에러
        if (member.getRole().equals(MemberRole.CUSTOMER)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }

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
    public Page<GetCouponResponse> getCoupon(Long memberId, int page, int size) {

        // TODO: RBAC 도입시 리팩토링 필요
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new BusinessException(AuthErrorCode.USER_NOT_FOUND)
        );
        // 멤버의 역할이 고객이면 권한 에러
        if (member.getRole().equals(MemberRole.CUSTOMER)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }

        Pageable pageable = PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Coupon> coupons = couponRepository.findAll(pageable);

        return coupons.map(GetCouponResponse::new);
    }
}
