package com.ecommerce.chatdemo.domain.member.dto;

public record SignUpResponse(
        Long id,
        String email,
        String name,
        String message
) {
}
