package com.sungho.letterpick.newsletter.adapter.webapi;

import com.sungho.letterpick.common.auth.WithLoginUser;
import com.sungho.letterpick.common.config.WebMvcConfig;
import com.sungho.letterpick.newsletter.application.SubscriptionInfo;
import com.sungho.letterpick.newsletter.application.provided.MemberNewsletterFinder;
import com.sungho.letterpick.newsletter.application.provided.MemberNewsletterModifier;
import com.sungho.letterpick.newsletter.domain.exception.MemberNewsletterNotFoundException;
import com.sungho.letterpick.newsletter.domain.exception.NewsletterNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberNewsletterController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcConfig.class)
class MemberNewsletterControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MemberNewsletterFinder memberNewsletterFinder;

    @MockitoBean
    MemberNewsletterModifier memberNewsletterModifier;

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("구독 정보 조회 요청 시 200과 구독 정보가 반환된다")
    void getSubscriptionInfo_returns_200_and_subscription_information() throws Exception {
        given(memberNewsletterFinder.findSubscriptionInfo(42L, 7L))
                .willReturn(SubscriptionInfo.none("https://example.com/subscribe"));

        mockMvc.perform(get("/api/v1/me/newsletter-subscriptions/{newsletterId}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NONE"))
                .andExpect(jsonPath("$.externalSubscribeUrl").value("https://example.com/subscribe"));

        verify(memberNewsletterFinder).findSubscriptionInfo(42L, 7L);
    }

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("구독 정보 조회 시 이미 구독 중이면 외부 구독 URL은 null이다")
    void getSubscriptionInfo_returns_active_with_null_external_subscribe_url() throws Exception {
        given(memberNewsletterFinder.findSubscriptionInfo(42L, 7L))
                .willReturn(SubscriptionInfo.active());

        mockMvc.perform(get("/api/v1/me/newsletter-subscriptions/{newsletterId}", 7L))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "status": "ACTIVE",
                          "externalSubscribeUrl": null
                        }
                        """));

        verify(memberNewsletterFinder).findSubscriptionInfo(42L, 7L);
    }

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("구독 정보 조회 시 구독 해지 상태이면 외부 구독 URL은 null이다")
    void getSubscriptionInfo_returns_unsubscribed_with_null_external_subscribe_url() throws Exception {
        given(memberNewsletterFinder.findSubscriptionInfo(42L, 7L))
                .willReturn(SubscriptionInfo.unsubscribed());

        mockMvc.perform(get("/api/v1/me/newsletter-subscriptions/{newsletterId}", 7L))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "status": "UNSUBSCRIBED",
                          "externalSubscribeUrl": null
                        }
                        """));

        verify(memberNewsletterFinder).findSubscriptionInfo(42L, 7L);
    }

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("구독 정보 조회 시 뉴스레터를 찾지 못하면 404가 반환된다")
    void getSubscriptionInfo_returns_404_when_newsletter_not_found() throws Exception {
        given(memberNewsletterFinder.findSubscriptionInfo(42L, 999L))
                .willThrow(new NewsletterNotFoundException());

        mockMvc.perform(get("/api/v1/me/newsletter-subscriptions/{newsletterId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NWL-001"));
    }

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("재구독 요청 시 204가 반환되고 서비스에 위임된다")
    void resubscribe_returns_204_and_delegates_to_service() throws Exception {
        mockMvc.perform(patch("/api/v1/me/newsletter-subscriptions/{newsletterId}", 7L))
                .andExpect(status().isNoContent());

        verify(memberNewsletterModifier).resubscribe(42L, 7L);
    }

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("재구독 시 뉴스레터를 찾지 못하면 404가 반환된다")
    void resubscribe_returns_404_when_newsletter_not_found() throws Exception {
        doThrow(new NewsletterNotFoundException())
                .when(memberNewsletterModifier).resubscribe(42L, 999L);

        mockMvc.perform(patch("/api/v1/me/newsletter-subscriptions/{newsletterId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NWL-001"));
    }

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("재구독 시 구독 이력이 없으면 404가 반환된다")
    void resubscribe_returns_404_when_member_newsletter_not_found() throws Exception {
        doThrow(new MemberNewsletterNotFoundException())
                .when(memberNewsletterModifier).resubscribe(42L, 7L);

        mockMvc.perform(patch("/api/v1/me/newsletter-subscriptions/{newsletterId}", 7L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NWL-002"));
    }

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("구독 해지 요청 시 204가 반환되고 서비스에 위임된다")
    void unsubscribe_returns_204_and_delegates_to_service() throws Exception {
        mockMvc.perform(delete("/api/v1/me/newsletter-subscriptions/{newsletterId}", 7L))
                .andExpect(status().isNoContent());

        verify(memberNewsletterModifier).unsubscribe(42L, 7L);
    }

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("구독 해지 시 뉴스레터를 찾지 못하면 404가 반환된다")
    void unsubscribe_returns_404_when_newsletter_not_found() throws Exception {
        doThrow(new NewsletterNotFoundException())
                .when(memberNewsletterModifier).unsubscribe(42L, 999L);

        mockMvc.perform(delete("/api/v1/me/newsletter-subscriptions/{newsletterId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NWL-001"));
    }

    @Test
    @WithLoginUser(memberId = 42L)
    @DisplayName("구독 해지 시 구독 이력이 없으면 404가 반환된다")
    void unsubscribe_returns_404_when_member_newsletter_not_found() throws Exception {
        doThrow(new MemberNewsletterNotFoundException())
                .when(memberNewsletterModifier).unsubscribe(42L, 7L);

        mockMvc.perform(delete("/api/v1/me/newsletter-subscriptions/{newsletterId}", 7L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NWL-002"));
    }
}
