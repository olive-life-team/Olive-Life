package com.ecommerce.chatdemo.global.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컨트롤러에서 로그인 사용자 정보를 직접 꺼내지 않고 파라미터로 주입받기 위한 어노테이션이다.
 * 채팅방 생성, 메시지 전송, 검색 기록 저장 API에서 반복 코드가 줄어든다.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginUser {
}