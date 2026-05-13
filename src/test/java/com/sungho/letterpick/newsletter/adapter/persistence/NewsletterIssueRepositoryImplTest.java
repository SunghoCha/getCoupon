package com.sungho.letterpick.newsletter.adapter.persistence;

import com.sungho.letterpick.LetterPickTestConfiguration;
import com.sungho.letterpick.newsletter.application.provided.NewsletterIssueDetail;
import com.sungho.letterpick.newsletter.application.provided.NewsletterIssueItem;
import com.sungho.letterpick.newsletter.application.provided.NewsletterIssueSearchCondition;
import com.sungho.letterpick.newsletter.domain.MemberNewsletter;
import com.sungho.letterpick.newsletter.domain.Newsletter;
import com.sungho.letterpick.newsletter.domain.NewsletterCategory;
import com.sungho.letterpick.newsletter.domain.NewsletterFixture;
import com.sungho.letterpick.newsletter.domain.NewsletterIssue;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({LetterPickTestConfiguration.class})
class NewsletterIssueRepositoryImplTest {

    @Autowired
    NewsletterIssueRepository newsletterIssueRepository;

    @Autowired
    NewslettersRepository newslettersRepository;

    @Autowired
    MemberNewsletterRepository memberNewsletterRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    @DisplayName("회원의 삭제되지 않은 활성 구독 이슈를 수신시각 최신순으로 조회한다")
    void findAllByMemberId_returns_active_subscription_issues_ordered_by_receivedAt_desc() {
        // given
        Long memberId = 1L;
        Long otherMemberId = 2L;
        Instant receivedFrom = Instant.parse("2050-05-11T15:00:00Z");
        Instant receivedTo = Instant.parse("2050-05-12T15:00:00Z");

        Newsletter firstNewsletter = newslettersRepository.save(
                NewsletterFixture.createNewsletter("첫 번째 뉴스레터", NewsletterCategory.TECH)
        );
        Newsletter secondNewsletter = newslettersRepository.save(
                NewsletterFixture.createNewsletter("두 번째 뉴스레터", NewsletterCategory.BIZ)
        );
        Newsletter unsubscribedNewsletter = newslettersRepository.save(
                NewsletterFixture.createNewsletter("구독 해지 뉴스레터", NewsletterCategory.TECH)
        );

        memberNewsletterRepository.save(MemberNewsletter.create(memberId, firstNewsletter.getId()));
        memberNewsletterRepository.save(MemberNewsletter.create(memberId, secondNewsletter.getId()));
        memberNewsletterRepository.save(MemberNewsletter.create(otherMemberId, firstNewsletter.getId()));

        MemberNewsletter unsubscribedMemberNewsletter = MemberNewsletter.create(memberId, unsubscribedNewsletter.getId());
        unsubscribedMemberNewsletter.unsubscribe();
        memberNewsletterRepository.save(unsubscribedMemberNewsletter);

        NewsletterIssue oldIssue = newsletterIssueRepository.save(
                createIssue(memberId, firstNewsletter.getId(), 1L, "오래된 이슈",
                        "오래된 본문", "오래된 미리보기", Instant.parse("2050-05-12T00:00:00Z"))
        );
        NewsletterIssue latestIssue = newsletterIssueRepository.save(
                createIssue(memberId, secondNewsletter.getId(), 2L, "최신 이슈",
                        "최신 본문", "최신 미리보기", Instant.parse("2050-05-12T01:00:00Z"))
        );
        newsletterIssueRepository.save(
                createIssue(otherMemberId, firstNewsletter.getId(), 3L, "다른 회원 이슈",
                        "다른 회원 본문", "다른 회원 미리보기", Instant.parse("2050-05-12T02:00:00Z"))
        );
        newsletterIssueRepository.save(
                createIssue(memberId, firstNewsletter.getId(), 4L, "범위 밖 이슈",
                        "범위 밖 본문", "범위 밖 미리보기", Instant.parse("2050-05-12T15:00:00Z"))
        );
        newsletterIssueRepository.save(
                createIssue(memberId, unsubscribedNewsletter.getId(), 5L, "구독 해지 이슈",
                        "구독 해지 본문", "구독 해지 미리보기", Instant.parse("2050-05-12T03:00:00Z"))
        );

        NewsletterIssue deletedIssue = createIssue(memberId, firstNewsletter.getId(), 6L, "삭제된 이슈",
                "삭제된 본문", "삭제된 미리보기", Instant.parse("2050-05-12T04:00:00Z"));
        deletedIssue.deleteFromList();
        newsletterIssueRepository.save(deletedIssue);

        entityManager.flush();
        entityManager.clear();

        // when
        Slice<NewsletterIssueItem> result = newsletterIssueRepository.findAllByMemberId(
                memberId,
                new NewsletterIssueSearchCondition(receivedFrom, receivedTo),
                PageRequest.of(0, 10)
        );
        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.getContent())
                .extracting(NewsletterIssueItem::issueId)
                .containsExactly(latestIssue.getId(), oldIssue.getId());

        NewsletterIssueItem latestIssueItem = result.getContent().get(0);
        assertThat(latestIssueItem.newsletterId()).isEqualTo(secondNewsletter.getId());
        assertThat(latestIssueItem.newsletterName()).isEqualTo(secondNewsletter.getName());
        assertThat(latestIssueItem.newsletterImageUrl()).isEqualTo(secondNewsletter.getImageUrl());
        assertThat(latestIssueItem.subject()).isEqualTo("최신 이슈");
        assertThat(latestIssueItem.previewText()).isEqualTo("최신 미리보기");
        assertThat(latestIssueItem.receivedAt()).isEqualTo(Instant.parse("2050-05-12T01:00:00Z"));
        assertThat(latestIssueItem.read()).isFalse();
    }

    @Test
    @DisplayName("조회 결과가 페이지 크기보다 많으면 다음 페이지가 있다고 표시한다")
    void findAllByMemberId_calculates_hasNext_when_result_exceeds_page_size() {
        Long memberId = 1L;
        Instant receivedFrom = Instant.parse("2050-05-11T15:00:00Z");
        Instant receivedTo = Instant.parse("2050-05-12T15:00:00Z");

        Newsletter newsletter = newslettersRepository.save(
                NewsletterFixture.createNewsletter("테크 뉴스레터", NewsletterCategory.TECH)
        );
        memberNewsletterRepository.save(MemberNewsletter.create(memberId, newsletter.getId()));
        newsletterIssueRepository.save(
                createIssue(memberId, newsletter.getId(), 1L, "첫 번째 이슈",
                        "첫 번째 본문", "첫 번째 미리보기", Instant.parse("2050-05-12T00:00:00Z"))
        );
        newsletterIssueRepository.save(
                createIssue(memberId, newsletter.getId(), 2L, "두 번째 이슈",
                        "두 번째 본문", "두 번째 미리보기", Instant.parse("2050-05-12T01:00:00Z"))
        );

        entityManager.flush();
        entityManager.clear();

        Slice<NewsletterIssueItem> result = newsletterIssueRepository.findAllByMemberId(
                memberId,
                new NewsletterIssueSearchCondition(receivedFrom, receivedTo),
                PageRequest.of(0, 1)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("회원의 삭제되지 않은 이슈 상세를 뉴스레터 정보와 함께 조회한다")
    void findDetailByMemberIdAndIssueId_returns_issue_detail_with_newsletter_info() {
        // given
        Long memberId = 1L;
        Long otherMemberId = 2L;

        Newsletter newsletter = newslettersRepository.save(
                NewsletterFixture.createNewsletter("상세 뉴스레터", NewsletterCategory.TECH)
        );

        NewsletterIssue targetIssue = createIssue(
                memberId,
                newsletter.getId(),
                10L,
                "상세 이슈",
                "상세 본문",
                "상세 미리보기",
                Instant.parse("2050-05-12T01:00:00Z")
        );
        targetIssue.markRead();
        newsletterIssueRepository.save(targetIssue);

        newsletterIssueRepository.save(
                createIssue(memberId, newsletter.getId(), 11L, "다른 이슈",
                        "다른 본문", "다른 미리보기", Instant.parse("2050-05-12T02:00:00Z"))
        );
        newsletterIssueRepository.save(
                createIssue(otherMemberId, newsletter.getId(), 12L, "다른 회원 이슈",
                        "다른 회원 본문", "다른 회원 미리보기", Instant.parse("2050-05-12T03:00:00Z"))
        );

        NewsletterIssue deletedIssue = createIssue(memberId, newsletter.getId(), 13L, "삭제된 이슈",
                "삭제된 본문", "삭제된 미리보기", Instant.parse("2050-05-12T04:00:00Z"));
        deletedIssue.deleteFromList();
        newsletterIssueRepository.save(deletedIssue);

        entityManager.flush();
        entityManager.clear();

        // when
        NewsletterIssueDetail detail = newsletterIssueRepository
                .findDetailByMemberIdAndIssueId(memberId, targetIssue.getId())
                .orElseThrow();

        // then
        assertThat(detail.issueId()).isEqualTo(targetIssue.getId());
        assertThat(detail.newsletterId()).isEqualTo(newsletter.getId());
        assertThat(detail.newsletterName()).isEqualTo(newsletter.getName());
        assertThat(detail.newsletterImageUrl()).isEqualTo(newsletter.getImageUrl());
        assertThat(detail.subject()).isEqualTo("상세 이슈");
        assertThat(detail.content()).isEqualTo("상세 본문");
        assertThat(detail.receivedAt()).isEqualTo(Instant.parse("2050-05-12T01:00:00Z"));
        assertThat(detail.read()).isTrue();
        assertThat(newsletterIssueRepository.findDetailByMemberIdAndIssueId(otherMemberId, targetIssue.getId())).isEmpty();
        assertThat(newsletterIssueRepository.findDetailByMemberIdAndIssueId(memberId, deletedIssue.getId())).isEmpty();
    }

    private NewsletterIssue createIssue(Long memberId, Long newsletterId, Long inboundEmailId,
                                        String subject, String content, String previewText, Instant receivedAt) {
        return NewsletterIssue.create(memberId, newsletterId, inboundEmailId,
                                      subject, content, previewText, receivedAt);
    }
}
