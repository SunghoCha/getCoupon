package com.sungho.letterpick.newsletter.adapter.persistence;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sungho.letterpick.newsletter.application.provided.NewsletterListItem;
import com.sungho.letterpick.newsletter.application.provided.NewsletterSearchCondition;
import com.sungho.letterpick.newsletter.domain.NewsletterCategory;
import com.sungho.letterpick.newsletter.domain.QNewsletter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

@RequiredArgsConstructor
public class NewslettersRepositoryImpl implements CustomNewslettersRepository {

    private final QNewsletter newsletter = QNewsletter.newsletter;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Slice<NewsletterListItem> findAllBySearchCondition(NewsletterSearchCondition condition, Pageable pageable) {
        // TODO : 추후에 구독수같은걸로 정렬 추가 예정
        List<NewsletterListItem> results = jpaQueryFactory
                .select(Projections.constructor(
                        NewsletterListItem.class,
                        newsletter.id,
                        newsletter.name,
                        newsletter.description,
                        newsletter.category
                ))
                .from(newsletter)
                .where(categoryEq(condition.category()))
                .orderBy(newsletter.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1L)
                .fetch();

        boolean hasNext = results.size() > pageable.getPageSize();
        List<NewsletterListItem> content = hasNext ? results.subList(0, pageable.getPageSize())
                : results;

        return new SliceImpl<>(content, pageable, hasNext);
    }

    private BooleanExpression categoryEq(NewsletterCategory category) {
        return category == null ? null : newsletter.category.eq(category);
    }
}
