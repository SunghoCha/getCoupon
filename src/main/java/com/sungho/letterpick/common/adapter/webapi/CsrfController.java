package com.sungho.letterpick.common.adapter.webapi;

import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/csrf")
public class CsrfController {

    /**
     * 응답 본문 없이 CSRF 쿠키만 준비한다.
     * {@link CsrfToken#getToken()} 호출은 csrf 메서드에서 토큰 생성을 확정하고
     * XSRF-TOKEN 쿠키가 내려가게 하기 위한 용도다.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void csrf(CsrfToken csrfToken) {
        csrfToken.getToken();
    }
}
