package com.sungho.letterpick.newsletter.adapter.webapi;

import com.sungho.letterpick.newsletter.application.provided.NewsletterCategoryFinder;
import com.sungho.letterpick.newsletter.application.provided.NewsletterCategoryItem;
import com.sungho.letterpick.newsletter.application.provided.NewsletterFinder;
import com.sungho.letterpick.newsletter.application.provided.NewsletterListItem;
import com.sungho.letterpick.newsletter.application.provided.NewsletterSearchCondition;
import com.sungho.letterpick.newsletter.domain.NewsletterCategory;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NewsletterController.class)
@AutoConfigureMockMvc(addFilters = false)
class NewsletterControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    NewsletterCategoryFinder categoryFinder;

    @MockitoBean
    NewsletterFinder newsletterFinder;

    @Test
    @DisplayName("GET /api/v1/newsletters/categories 요청 시 카테고리 코드와 라벨 목록이 반환된다")
    void getCategories_returns_category_code_and_label_envelope() throws Exception {
        List<NewsletterCategoryItem> categories = List.of(
                new NewsletterCategoryItem("BIZ", "비즈·재테크"),
                new NewsletterCategoryItem("TECH", "IT·테크")
        );
        given(categoryFinder.find()).willReturn(categories);

        mockMvc.perform(get("/api/v1/newsletters/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[0].code").value("BIZ"))
                .andExpect(jsonPath("$.categories[0].label").value("비즈·재테크"))
                .andExpect(jsonPath("$.categories[1].code").value("TECH"))
                .andExpect(jsonPath("$.categories[1].label").value("IT·테크"));

        verify(categoryFinder).find();
    }

    @Test
    @DisplayName("GET /api/v1/newsletters 요청 시 카테고리와 페이지 조건을 바인딩하고 목록 페이지 응답을 반환한다")
    void getNewsletters_binds_category_and_pageable_then_returns_items_page_envelope() throws Exception {
        PageRequest pageable = PageRequest.of(0, 2);
        List<NewsletterListItem> newsletters = List.of(
                new NewsletterListItem(
                        1L,
                        "테크 레터",
                        "기술 뉴스레터 설명",
                        "https://example.com/tech.png",
                        new NewsletterCategoryItem("TECH", "IT·테크"),
                        "NONE"
                ),
                new NewsletterListItem(
                        2L,
                        "AI 레터",
                        "AI 뉴스레터 설명",
                        "https://example.com/ai.png",
                        new NewsletterCategoryItem("TECH", "IT·테크"),
                        "NONE"
                )
        );
        given(newsletterFinder.getNewsletters(any(), any()))
                .willReturn(new SliceImpl<>(newsletters, pageable, true));

        mockMvc.perform(get("/api/v1/newsletters")
                        .param("category", "TECH")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].newsletterId").value(1L))
                .andExpect(jsonPath("$.items[0].name").value("테크 레터"))
                .andExpect(jsonPath("$.items[0].description").value("기술 뉴스레터 설명"))
                .andExpect(jsonPath("$.items[0].imageUrl").value("https://example.com/tech.png"))
                .andExpect(jsonPath("$.items[0].category.code").value("TECH"))
                .andExpect(jsonPath("$.items[0].category.label").value("IT·테크"))
                .andExpect(jsonPath("$.items[0].memberNewsletterStatus").value("NONE"))
                .andExpect(jsonPath("$.items[1].newsletterId").value(2L))
                .andExpect(jsonPath("$.items[1].name").value("AI 레터"))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.hasNext").value(true));

        ArgumentCaptor<NewsletterSearchCondition> conditionCaptor =
                ArgumentCaptor.forClass(NewsletterSearchCondition.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(newsletterFinder).getNewsletters(conditionCaptor.capture(), pageableCaptor.capture());

        assertEquals(NewsletterCategory.TECH, conditionCaptor.getValue().category());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(2, pageableCaptor.getValue().getPageSize());
    }

    @Test
    @DisplayName("GET /api/v1/newsletters 요청 시 존재하지 않는 카테고리 값이면 400이 반환된다")
    void getNewsletters_returns_400_when_category_is_unknown() throws Exception {
        mockMvc.perform(get("/api/v1/newsletters")
                        .param("category", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value(containsString("category")))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(newsletterFinder);
    }
}
