package com.sungho.letterpick.newsletter.application;

import java.time.Instant;

import static java.util.Objects.requireNonNull;

public record ReceivedMail(
        String messageKey,
        String recipientAddress,
        String senderEmail,
        String subject,
        Instant receivedAt,
        String content,
        String rawReference
) {

    public ReceivedMail {
        requireNonNull(messageKey, "messageKey must not be null");
        requireNonNull(recipientAddress, "recipientAddress must not be null");
        requireNonNull(senderEmail, "senderEmail must not be null");
        requireNonNull(subject, "subject must not be null");
        requireNonNull(receivedAt, "receivedAt must not be null");
        requireNonNull(content, "content must not be null");
        requireNonNull(rawReference, "rawReference must not be null");
    }
}
