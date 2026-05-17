package com.sungho.letterpick.newsletter.adapter.mail;

import static java.util.Objects.requireNonNull;

public record ParsedMimeMail(
        String senderEmail,
        String subject,
        String content
) {

    public ParsedMimeMail {
        requireNonNull(senderEmail, "senderEmail must not be null");
        requireNonNull(subject, "subject must not be null");
        requireNonNull(content, "content must not be null");
    }
}
