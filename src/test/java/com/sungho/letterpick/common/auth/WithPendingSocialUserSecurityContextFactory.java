package com.sungho.letterpick.common.auth;

import com.sungho.letterpick.member.adapter.security.CustomOAuth2Principal;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithPendingSocialUserSecurityContextFactory
        implements WithSecurityContextFactory<WithPendingSocialUser> {

    @Override
    public SecurityContext createSecurityContext(WithPendingSocialUser annotation) {
        SocialUserInfo info = new SocialUserInfo(
                SocialProvider.GOOGLE, "test-sub-pending", "newuser@example.com", "tempName", null
        );

        OAuth2User delegate = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_PENDING_SIGNUP")),
                Map.of("sub", "test-sub-pending"),
                "sub"
        );

        SocialPrincipal principal = CustomOAuth2Principal.pending(info, delegate);

        OAuth2AuthenticationToken token = new OAuth2AuthenticationToken(
                principal, principal.getAuthorities(), "test-registration"
        );
        token.setAuthenticated(true);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(token);
        return context;
    }
}
