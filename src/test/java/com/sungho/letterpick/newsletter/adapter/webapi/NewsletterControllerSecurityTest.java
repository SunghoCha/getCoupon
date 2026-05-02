package com.sungho.letterpick.newsletter.adapter.webapi;

import com.sungho.letterpick.common.config.SecurityConfig;
import com.sungho.letterpick.member.adapter.security.CustomOAuth2UserService;
import com.sungho.letterpick.member.adapter.security.CustomOidcUserService;
import com.sungho.letterpick.member.adapter.security.OAuth2LoginFailureHandler;
import com.sungho.letterpick.member.adapter.security.OAuth2LoginSuccessHandler;
import com.sungho.letterpick.newsletter.application.provided.NewsletterCategoryFinder;
import com.sungho.letterpick.newsletter.application.provided.NewsletterFinder;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NewsletterController.class)
@Import(SecurityConfig.class)
class NewsletterControllerSecurityTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    NewsletterCategoryFinder categoryFinder;

    @MockitoBean
    NewsletterFinder newsletterFinder;

    @MockitoBean
    CustomOidcUserService customOidcUserService;

    @MockitoBean
    CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    @MockitoBean
    OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Test
    @DisplayName("익명 사용자가 뉴스레터 카테고리 목록을 조회할 수 있다")
    void getCategories_passes_for_anonymous() throws Exception {
        given(categoryFinder.find()).willReturn(List.of());

        mockMvc.perform(get("/api/v1/newsletters/categories"))
                .andExpect(status().isOk());

        verify(categoryFinder).find();
    }

    @Test
    @DisplayName("익명 사용자가 뉴스레터 목록을 조회할 수 있다")
    void getNewsletters_passes_for_anonymous() throws Exception {
        given(newsletterFinder.getNewsletters(any(), any()))
                .willReturn(new SliceImpl<>(List.of(), PageRequest.of(0, 20), false));

        mockMvc.perform(get("/api/v1/newsletters"))
                .andExpect(status().isOk());

        verify(newsletterFinder).getNewsletters(any(), any());
    }
}
