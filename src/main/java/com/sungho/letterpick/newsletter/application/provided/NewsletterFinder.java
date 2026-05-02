package com.sungho.letterpick.newsletter.application.provided;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface NewsletterFinder {
    Slice<NewsletterListItem> getNewsletters(NewsletterSearchCondition newsletterSearchCondition, Pageable pageable);
}
