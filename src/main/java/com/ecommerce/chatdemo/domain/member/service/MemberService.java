package com.ecommerce.chatdemo.domain.member.service;

import com.ecommerce.chatdemo.domain.member.dto.*;
import com.ecommerce.chatdemo.domain.member.entity.Member;
import com.ecommerce.chatdemo.domain.member.repository.MemberRepository;
import com.ecommerce.chatdemo.domain.membership.entity.Membership;
import com.ecommerce.chatdemo.domain.membership.repository.MembershipRepository;
import com.ecommerce.chatdemo.global.exception.AuthErrorCode;
import com.ecommerce.chatdemo.global.exception.AuthException;
import com.ecommerce.chatdemo.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private static final String DEFAULT_MEMBERSHIP_NAME = "BASIC";
    private static final BigDecimal DEFAULT_MIN_SPENT_AMOUNT = BigDecimal.ZERO;
    private static final BigDecimal DEFAULT_POINT_RATE = new BigDecimal("1.00");

    private final MemberRepository memberRepository;
    private final MembershipRepository membershipRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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

        return new MyInfoResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getRole().name(),
                member.getPointBalance(),
                member.getMembership().getName()
        );
    }

    private void validateDuplicate(SignUpRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL);
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
}