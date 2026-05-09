package com.sungho.letterpick.common.auth;

import static com.sungho.letterpick.common.auth.SecurityAuthorities.ROLE_USER;

import com.sungho.letterpick.member.adapter.security.CustomOAuth2Principal;
import com.sungho.letterpick.member.domain.Member;
import com.sungho.letterpick.member.domain.MemberFixture;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithLoginUserSecurityContextFactory implements WithSecurityContextFactory<WithLoginUser> {

    @Override
    public SecurityContext createSecurityContext(WithLoginUser annotation) {
        Member member = MemberFixture.createMemberWithId(annotation.memberId());

        OAuth2User delegate = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(ROLE_USER)),
                Map.of("sub", "test-sub-" + annotation.memberId()),
                "sub"
        );
        SocialPrincipal principal = CustomOAuth2Principal.existing(member, delegate);

        List<SimpleGrantedAuthority> authorities = annotation.authorities().length == 0
                ? List.of(new SimpleGrantedAuthority(ROLE_USER))
                : Arrays.stream(annotation.authorities())
                .map(SimpleGrantedAuthority::new)
                .toList();
        OAuth2AuthenticationToken token = new OAuth2AuthenticationToken(
                principal, authorities, "test-registration"
        );
        token.setAuthenticated(true);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(token);
        return context;
    }
}
