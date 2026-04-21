package com.ecommerce.chatdemo.domain.member.service;

import com.ecommerce.chatdemo.domain.member.dto.*;
import com.ecommerce.chatdemo.domain.member.entity.Member;
import com.ecommerce.chatdemo.domain.member.repository.MemberRepository;
import com.ecommerce.chatdemo.domain.membercoupon.repository.MemberCouponRepository;
import com.ecommerce.chatdemo.domain.membership.entity.Membership;
import com.ecommerce.chatdemo.domain.membership.repository.MembershipRepository;
import com.ecommerce.chatdemo.global.exception.AuthErrorCode;
import com.ecommerce.chatdemo.global.exception.AuthException;
import com.ecommerce.chatdemo.global.exception.CommonErrorCode;
import com.ecommerce.chatdemo.global.security.jwt.JwtTokenProvider;
import com.ecommerce.chatdemo.global.security.jwt.TokenBlacklistService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import io.jsonwebtoken.security.SignatureException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private static final String DEFAULT_MEMBERSHIP_NAME = "BASIC";
    private static final Long DEFAULT_MIN_SPENT_AMOUNT = 0L;
    private static final Long DEFAULT_POINT_RATE = 1L;

    private final MemberRepository memberRepository;
    private final MembershipRepository membershipRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final MemberCouponRepository memberCouponRepository;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        validateDuplicate(request);

        Membership defaultMembership = getOrCreateDefaultMembership();

        Member member = Member.create(
                defaultMembership,
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name(),
                request.role()
        );

        Member savedMember = memberRepository.save(member);

        return new SignUpResponse(
                savedMember.getId(),
                savedMember.getEmail(),
                savedMember.getName(),
                "회원가입이 완료되었습니다."
        );
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getRole().getKey());

        return new LoginResponse(
                accessToken,
                new LoginResponse.MemberInfo(
                        member.getId(),
                        member.getEmail(),
                        member.getName(),
                        member.getRole().name()
                )
        );
    }

    public MyInfoResponse getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        List<MyInfoResponse.CouponInfo> coupons = memberCouponRepository.findByMemberId(memberId)
                .stream()
                .map(mc -> new MyInfoResponse.CouponInfo(
                        mc.getId(),
                        mc.getCoupon().getName(),
                        mc.getCoupon().getDiscountAmount(),
                        mc.getStatus(),
                        mc.getCoupon().getUseStartAt(),
                        mc.getCoupon().getUseEndAt()
                ))
                .toList();

        return new MyInfoResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getRole().name(),
                member.getPointBalance(),
                member.getMembership().getName(),
                coupons
        );
    }

    private void validateDuplicate(SignUpRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new AuthException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }
    }

    private Membership getOrCreateDefaultMembership() {
        return membershipRepository.findFirstByOrderByMinSpentAmountAsc()
                .orElseGet(this::createDefaultMembership);
    }

    private Membership createDefaultMembership() {
        return membershipRepository.save(
                Membership.create(
                        DEFAULT_MEMBERSHIP_NAME,
                        DEFAULT_MIN_SPENT_AMOUNT,
                        DEFAULT_POINT_RATE
                )
        );
    }

    @Transactional
    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new AuthException(CommonErrorCode.UNAUTHORIZED);
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        String token = authorizationHeader.substring(7).trim();

        if (token.isBlank()) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        try {
            jwtTokenProvider.validateToken(token);

            Long userId = jwtTokenProvider.getUserId(token);

            if (!memberRepository.existsById(userId)) {
                throw new AuthException(AuthErrorCode.USER_NOT_FOUND);
            }

            long remainingExpiration = jwtTokenProvider.getRemainingExpiration(token);
            tokenBlacklistService.blacklist(token, remainingExpiration);

        } catch (ExpiredJwtException e) {
            throw new AuthException(AuthErrorCode.EXPIRED_TOKEN);
        } catch (MalformedJwtException | UnsupportedJwtException | SignatureException | IllegalArgumentException e) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
    }
}