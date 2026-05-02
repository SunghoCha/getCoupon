package com.sungho.letterpick.newsletter.application;

import com.sungho.letterpick.newsletter.application.provided.NewsletterCategoryItem;
import com.sungho.letterpick.newsletter.domain.NewsletterCategory;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NewsletterCategoryQueryServiceTest {

    private final NewsletterCategoryQueryService categoryQueryService = new NewsletterCategoryQueryService();

    @Test
    @DisplayName("뉴스레터 카테고리 enum 전체를 코드와 라벨로 반환한다")
    void find_returns_all_newsletter_categories_with_code_and_label() {
        List<NewsletterCategoryItem> categories = categoryQueryService.find();

        assertThat(categories).hasSize(NewsletterCategory.values().length);
        NewsletterCategory[] expectedCategories = NewsletterCategory.values();
        for (int i = 0; i < expectedCategories.length; i++) {
            NewsletterCategory category = expectedCategories[i];
            NewsletterCategoryItem item = categories.get(i);
            assertThat(item.code()).isEqualTo(category.name());
            assertThat(item.label()).isEqualTo(category.label());
        }
    }
}
