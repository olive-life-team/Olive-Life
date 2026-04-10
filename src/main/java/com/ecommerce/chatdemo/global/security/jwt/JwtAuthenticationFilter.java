package com.ecommerce.chatdemo.global.security.jwt;

import com.ecommerce.chatdemo.global.constant.AuthConstants;
import com.ecommerce.chatdemo.global.security.dto.LoginUserInfo;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 모든 HTTP 요청 앞에서 JWT를 검사한다.
     * 컨트롤러에 들어가기 전에 인증 정보를 SecurityContext에 넣어두기 위한 필터다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        try {
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                Long userId = jwtTokenProvider.getUserId(token);
                String role = jwtTokenProvider.getRole(token);

                LoginUserInfo loginUserInfo = new LoginUserInfo(userId, role);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                loginUserInfo,
                                null,
                                List.of(new SimpleGrantedAuthority(role))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | SignatureException e) {
            request.setAttribute("exception", e);
        } catch (Exception e) {
            request.setAttribute("exception", e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더에서 Bearer 토큰만 추출한다.
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AuthConstants.AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(AuthConstants.BEARER_PREFIX)) {
            return bearerToken.substring(AuthConstants.BEARER_PREFIX.length());
        }

        return null;
    }
}