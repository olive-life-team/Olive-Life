package com.ecommerce.chatdemo.domain.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberRole {

    CUSTOMER("ROLE_CUSTOMER"),
    SUPER_ADMIN("ROLE_SUPER_ADMIN"),
    CS_ADMIN("ROLE_CS_ADMIN");

    private final String key;
}
