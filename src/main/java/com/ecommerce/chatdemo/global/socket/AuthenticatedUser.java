package com.ecommerce.chatdemo.global.socket;

import com.ecommerce.chatdemo.domain.member.entity.Member;
import lombok.Getter;

import java.security.Principal;

@Getter
public class AuthenticatedUser implements Principal {

    private final Member member;
    private final String name;

    public AuthenticatedUser(Member member) {
        this.member = member;
        this.name = member.getName();
    }

    public static Member fromPrincipal(Principal principal) {
        return ((AuthenticatedUser) principal).getMember();
    }
}
