package com.sungho.letterpick.common.auth;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        // 인증 보장은 SecurityFilterChain의 책임. 여기서는 SocialPrincipal에서 memberId만 추출한다.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof SocialPrincipal social && !social.isPending()) {
            return new LoginUser(social.getMember().getId());
        }
        throw new IllegalStateException(
                "Principal이 가입 완료된 SocialPrincipal이 아님: "
                        + (principal == null ? "null" : principal.getClass().getSimpleName()));
    }
}
