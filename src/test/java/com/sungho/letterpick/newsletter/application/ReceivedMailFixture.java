package com.sungho.letterpick.newsletter.application;

import java.time.Instant;

public class ReceivedMailFixture {

    public static ReceivedMail create() {
        return create(
                "message-1",
                "abcd1234efgh@inbound.letterpick.test",
                "newsletter@example.com"
        );
    }

    public static ReceivedMail create(String recipientAddress, String senderEmail) {
        return create("message-1", recipientAddress, senderEmail);
    }

    public static ReceivedMail create(String messageKey, String recipientAddress, String senderEmail) {
        return new ReceivedMail(
                messageKey,
                recipientAddress,
                senderEmail,
                "이번 주 뉴스레터",
                Instant.parse("2026-05-11T00:00:00Z"),
                "뉴스레터 본문",
                "s3://letterpick-raw-mail/" + messageKey
        );
    }
}
