package com.ecommerce.chatdemo.domain.member.dto;

public record LoginResponse(
        String accessToken,
        MemberInfo member
) {

    public record MemberInfo(
            Long id,
            String email,
            String name,
            String role
    ) {
    }
}
