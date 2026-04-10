package com.ecommerce.chatdemo.domain.member.dto;

import java.math.BigDecimal;

public record MyInfoResponse(
        Long id,
        String email,
        String name,
        String role,
        BigDecimal pointBalance,
        String membershipName
) {
}
