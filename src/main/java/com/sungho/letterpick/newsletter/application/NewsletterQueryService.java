package com.sungho.letterpick.newsletter.application;

import com.sungho.letterpick.newsletter.application.provided.NewsletterFinder;
import com.sungho.letterpick.newsletter.application.provided.NewsletterListItem;
import com.sungho.letterpick.newsletter.application.provided.NewsletterSearchCondition;
import com.sungho.letterpick.newsletter.adapter.persistence.NewslettersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NewsletterQueryService implements NewsletterFinder {

    private final NewslettersRepository newsletterRepository;

    @Override
    public Slice<NewsletterListItem> getNewsletters(NewsletterSearchCondition searchCondition, Pageable pageable) {
        return newsletterRepository.findAllBySearchCondition(searchCondition, pageable);
    }
}
