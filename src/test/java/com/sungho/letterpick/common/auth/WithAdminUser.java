package com.sungho.letterpick.common.auth;

import static com.sungho.letterpick.common.auth.SecurityAuthorities.ROLE_ADMIN;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.test.context.support.WithMockUser;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@WithMockUser(authorities = ROLE_ADMIN)
public @interface WithAdminUser {
}
