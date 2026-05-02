package com.sungho.letterpick.newsletter.adapter.persistence;

import com.sungho.letterpick.newsletter.application.provided.NewsletterListItem;
import com.sungho.letterpick.newsletter.application.provided.NewsletterSearchCondition;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface CustomNewslettersRepository {
    Slice<NewsletterListItem> findAllBySearchCondition(NewsletterSearchCondition searchCondition, Pageable pageable);
}
