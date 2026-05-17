package com.sungho.letterpick.newsletter.application;

import com.sungho.letterpick.newsletter.adapter.persistence.InboundEmailRepository;
import com.sungho.letterpick.newsletter.adapter.persistence.MemberNewsletterRepository;
import com.sungho.letterpick.newsletter.adapter.persistence.NewsletterIssueRepository;
import com.sungho.letterpick.newsletter.adapter.persistence.NewslettersRepository;
import com.sungho.letterpick.newsletter.domain.InboundEmail;
import com.sungho.letterpick.newsletter.domain.MemberNewsletter;
import com.sungho.letterpick.newsletter.domain.Newsletter;
import com.sungho.letterpick.newsletter.domain.NewsletterIssue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class NewsletterMailReceiveService {

    private final InboundEmailRepository inboundEmailRepository;
    private final RecipientAddressResolver recipientAddressResolver;
    private final NewslettersRepository newslettersRepository;
    private final MemberNewsletterRepository memberNewsletterRepository;
    private final NewsletterIssueRepository newsletterIssueRepository;
    private final NewsletterIssuePreviewGenerator newsletterIssuePreviewGenerator;

    public void receive(ReceivedMail receivedMail) {
        if (inboundEmailRepository.existsByMessageKey(receivedMail.messageKey())) return;

        InboundEmail inboundEmail = saveInboundEmail(receivedMail);

        RecipientAddressResolution recipientAddressResolution = recipientAddressResolver.resolve(receivedMail.recipientAddress());
        if (recipientAddressResolution.type() == RecipientAddressResolution.Type.INVALID_ADDRESS) {
            inboundEmail.markInvalidRecipientAddress();
            return;
        }
        if (recipientAddressResolution.type() == RecipientAddressResolution.Type.NOT_FOUND) {
            inboundEmail.markRecipientNotFound();
            return;
        }

        Long memberId = recipientAddressResolution.memberId();
        Optional<Newsletter> newsletterOpt = newslettersRepository.findByEmailAddress(receivedMail.senderEmail());
        if (newsletterOpt.isEmpty()) {
            inboundEmail.markNewsletterNotFound(memberId);
            return;
        }

        Long newsletterId = newsletterOpt.get().getId();
        processSubscriptionReceive(memberId, newsletterId, receivedMail.content(), inboundEmail);

    }

    private InboundEmail saveInboundEmail(ReceivedMail receivedMail) {
        InboundEmail inboundEmail = InboundEmail.create(
                receivedMail.messageKey(),
                receivedMail.rawReference(),
                receivedMail.recipientAddress(),
                receivedMail.senderEmail(),
                receivedMail.subject(),
                receivedMail.receivedAt()
        );
        return inboundEmailRepository.save(inboundEmail);
    }

    private void processSubscriptionReceive(
            Long memberId,
            Long newsletterId,
            String content,
            InboundEmail inboundEmail
    ) {
        Optional<MemberNewsletter> memberNewsletterOpt = memberNewsletterRepository
                .findByMemberIdAndNewsletterId(memberId, newsletterId);

        if (memberNewsletterOpt.isEmpty()) {
            memberNewsletterRepository.save(MemberNewsletter.create(memberId, newsletterId));
            completeIssueCreation(memberId, newsletterId, content, inboundEmail);
            return;
        }

        MemberNewsletter memberNewsletter = memberNewsletterOpt.get();
        if (memberNewsletter.isUnsubscribed()) {
            inboundEmail.markSkippedUnsubscribed(memberId, newsletterId);
            return;
        }

        if (memberNewsletter.isActive()) {
            completeIssueCreation(memberId, newsletterId, content, inboundEmail);
            return;
        }

        throw new IllegalStateException("지원하지 않는 구독 상태입니다.");
    }

    private void completeIssueCreation(Long memberId, Long newsletterId, String content, InboundEmail inboundEmail) {
        String previewText = newsletterIssuePreviewGenerator.generate(content);
        NewsletterIssue newsletterIssue = NewsletterIssue.create(
                memberId,
                newsletterId,
                inboundEmail.getId(),
                inboundEmail.getSubject(),
                content,
                previewText,
                inboundEmail.getReceivedAt()
        );
        newsletterIssueRepository.save(newsletterIssue);
        inboundEmail.markIssueCreated(memberId, newsletterId);
    }
}
