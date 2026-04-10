package com.ecommerce.chatdemo.global.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * 인증 실패 시 JSON 형태로 401 응답을 내려준다.
     * 프론트가 일관된 형식으로 에러를 받을 수 있게 해준다.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.getWriter().write("""
                {
                  "success": false,
                  "data": null,
                  "error": {
                    "code": "AUTH_001",
                    "message": "인증이 필요합니다."
                  }
                }
                """);
    }
}