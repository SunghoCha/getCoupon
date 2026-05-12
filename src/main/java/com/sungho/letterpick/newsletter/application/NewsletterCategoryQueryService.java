package com.sungho.letterpick.newsletter.application;

import com.sungho.letterpick.newsletter.application.provided.NewsletterCategoryFinder;
import com.sungho.letterpick.newsletter.application.provided.NewsletterCategoryItem;
import com.sungho.letterpick.newsletter.domain.NewsletterCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NewsletterCategoryQueryService implements NewsletterCategoryFinder {

    @Override // TODO : 메서드명 점검
    public List<NewsletterCategoryItem> find() {
        return Arrays.stream(NewsletterCategory.values())
                .map(NewsletterCategoryItem::from)
                .toList();
    }
}
