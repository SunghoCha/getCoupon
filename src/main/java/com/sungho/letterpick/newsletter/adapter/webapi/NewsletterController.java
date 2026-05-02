package com.sungho.letterpick.newsletter.adapter.webapi;

import com.sungho.letterpick.newsletter.adapter.webapi.dto.NewsletterCategoriesResponse;
import com.sungho.letterpick.newsletter.adapter.webapi.dto.NewslettersResponse;
import com.sungho.letterpick.newsletter.application.provided.NewsletterCategoryFinder;
import com.sungho.letterpick.newsletter.application.provided.NewsletterFinder;
import com.sungho.letterpick.newsletter.application.provided.NewsletterListItem;
import com.sungho.letterpick.newsletter.application.provided.NewsletterSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/newsletters")
@RequiredArgsConstructor
public class NewsletterController implements NewsletterControllerApi {

    private final NewsletterCategoryFinder categoryFinder;
    private final NewsletterFinder newsletterFinder;

    @Override
    @GetMapping("/categories")
    public NewsletterCategoriesResponse getCategories() {
        return NewsletterCategoriesResponse.from(categoryFinder.find());
    }

    @Override
    @GetMapping
    public NewslettersResponse getNewslettersWithCategory(@ModelAttribute NewsletterSearchCondition searchCondition,
                                                           @PageableDefault(size = 20) Pageable pageable) {
        Slice<NewsletterListItem> newsletters = newsletterFinder.getNewsletters(searchCondition, pageable);
        return NewslettersResponse.from(newsletters);
    }
}
