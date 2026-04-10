package com.ecommerce.chatdemo.global.security.resolver;

import com.ecommerce.chatdemo.global.exception.BusinessException;
import com.ecommerce.chatdemo.global.exception.CommonErrorCode;
import com.ecommerce.chatdemo.global.security.annotation.LoginUser;
import com.ecommerce.chatdemo.global.security.dto.LoginUserInfo;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
/**
 * SecurityContext에 저장된 로그인 사용자 정보를 컨트롤러 파라미터에 자동으로 넣어준다.
 * 이후 컨트롤러에서 SecurityContextHolder를 직접 만지지 않아도 되는 장점이 있다.
 */
@Component
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(LoginUser.class);
        boolean hasType = LoginUserInfo.class.isAssignableFrom(parameter.getParameterType());
        return hasAnnotation && hasType;
    }

    @Override
    public Object resolveArgument(
            @NonNull MethodParameter parameter,
            @Nullable ModelAndViewContainer mavContainer,
            @NonNull NativeWebRequest webRequest,
            @Nullable WebDataBinderFactory binderFactory
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUserInfo loginUserInfo)) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }

        return loginUserInfo;
    }
}
