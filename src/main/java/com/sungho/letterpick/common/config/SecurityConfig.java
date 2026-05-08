package com.sungho.letterpick.common.config;

import com.sungho.letterpick.common.exception.ErrorResponse;
import com.sungho.letterpick.member.adapter.security.CustomOAuth2UserService;
import com.sungho.letterpick.member.adapter.security.CustomOidcUserService;
import com.sungho.letterpick.member.adapter.security.OAuth2LoginFailureHandler;
import com.sungho.letterpick.member.adapter.security.OAuth2LoginSuccessHandler;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_GET_ENDPOINTS = {
            "/api/v1/newsletters",
            "/api/v1/newsletters/categories"
    };

    private final String frontendBaseUrl;

    public SecurityConfig(@Value("${frontend.base-url}") String frontendBaseUrl) {
        this.frontendBaseUrl = frontendBaseUrl;
    }

    /**
     * /api/** 전용 체인.
     * - JSON 401/403 응답
     * - oauth2Login 없음 (브라우저 redirect 흐름 분리)
     * - 세션 쿠키로 OAuth 체인이 저장한 인증 정보 공유
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http,
            AccessDeniedHandler accessDeniedHandler,
            AuthenticationEntryPoint apiAuthenticationEntryPoint,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        CsrfTokenRequestAttributeHandler csrfTokenRequestHandler = new CsrfTokenRequestAttributeHandler();

        http
                .securityMatcher("/api/**")
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // TODO: 로그인 직후 프론트가 CSRF 토큰을 다시 확보할 수 있도록 /api/csrf 같은 엔드포인트 필요 여부 결정.
                // TODO: 배포 환경 기준으로 세션 쿠키 SameSite/Secure 설정 확정.
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(csrfTokenRequestHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll()
                        .requestMatchers("/api/v1/auth/signup").hasAuthority("ROLE_PENDING_SIGNUP")
                        .requestMatchers("/api/v1/members/**").hasAuthority("ROLE_USER")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e
                        .accessDeniedHandler(accessDeniedHandler)
                        .authenticationEntryPoint(apiAuthenticationEntryPoint)
                );
        return http.build();
    }

    /**
     * OAuth 로그인 진입 전용 체인.
     * - 브라우저 redirect 흐름
     * - 로그인 성공 시 세션에 Authentication 저장 -> 이후 /api/** 호출이 세션 읽음
     */
    @Bean
    @Order(2)
    public SecurityFilterChain oauthSecurityFilterChain(
            HttpSecurity http,
            CustomOidcUserService customOidcUserService,
            CustomOAuth2UserService customOAuth2UserService,
            OAuth2LoginFailureHandler oAuth2LoginFailureHandler,
            OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        http
                .securityMatcher("/oauth2/**", "/login/oauth2/**")
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcUserService)
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oAuth2LoginFailureHandler)
                );
        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint apiAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, ex) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            ErrorResponse body = new ErrorResponse("UNAUTHORIZED", "인증이 필요합니다", Instant.now());
            objectMapper.writeValue(response.getWriter(), body);
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(frontendBaseUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
        return (request, response, ex) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            ErrorResponse body = new ErrorResponse("FORBIDDEN", "권한이 없습니다", Instant.now());
            objectMapper.writeValue(response.getWriter(), body);
        };
    }
}
