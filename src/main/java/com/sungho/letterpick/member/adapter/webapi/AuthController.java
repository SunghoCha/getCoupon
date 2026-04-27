package com.sungho.letterpick.member.adapter.webapi;

import com.sungho.letterpick.common.auth.SocialPrincipal;
import com.sungho.letterpick.common.auth.SocialUserInfo;
import com.sungho.letterpick.member.application.provided.MemberModifier;
import com.sungho.letterpick.member.application.provided.MemberRegisterRequest;
import com.sungho.letterpick.member.application.provided.MemberSignupRequest;
import com.sungho.letterpick.member.domain.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerApi {

    private final MemberModifier memberModifier;

    private final SecurityContextHolderStrategy securityContextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    @Override
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public void signup(
            @AuthenticationPrincipal SocialPrincipal principal,
            @Valid @RequestBody MemberSignupRequest request
    ) {
        SocialUserInfo info = principal.getSocialUserInfo();
        Member savedMember = memberModifier.register(new MemberRegisterRequest(
                info.email(),
                request.nickname(),
                info.provider(),
                info.providerId()
        ));

        refreshSecurityContext(principal, savedMember);
    }

    private void refreshSecurityContext(SocialPrincipal pendingPrincipal, Member savedMember) {
        SocialPrincipal nextPrincipal = pendingPrincipal.withMember(savedMember);

        Authentication authentication = securityContextHolderStrategy.getContext().getAuthentication();
        if (!(authentication instanceof OAuth2AuthenticationToken oldToken)) {
            throw new IllegalStateException(
                    "가입 완료 후 인증 갱신은 OAuth2AuthenticationToken에서만 지원합니다: "
                            + (authentication == null ? "null" : authentication.getClass().getName())
            );
        }

        OAuth2AuthenticationToken newToken = new OAuth2AuthenticationToken(
                nextPrincipal,
                nextPrincipal.getAuthorities(),
                oldToken.getAuthorizedClientRegistrationId()
        );
        newToken.setDetails(oldToken.getDetails());

        SecurityContext newContext = securityContextHolderStrategy.createEmptyContext();
        newContext.setAuthentication(newToken);
        securityContextHolderStrategy.setContext(newContext);

        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpRequest = attrs.getRequest();
        HttpServletResponse httpResponse = attrs.getResponse();
        securityContextRepository.saveContext(newContext, httpRequest, httpResponse);
    }
}
