package com.sungho.letterpick.common.auth;

import java.util.Arrays;
import java.util.List;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithLoginUserSecurityContextFactory implements WithSecurityContextFactory<WithLoginUser> {

    @Override
    public SecurityContext createSecurityContext(WithLoginUser annotation) {
        List<SimpleGrantedAuthority> authorities = Arrays.stream(annotation.authorities())
                .map(SimpleGrantedAuthority::new)
                .toList();
        TestingAuthenticationToken token =
                new TestingAuthenticationToken(new LoginUser(annotation.memberId()), null, authorities);
        token.setAuthenticated(true);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(token);
        return context;
    }
}
