package com.sungho.letterpick.newsletter.adapter.mail;

import com.sungho.letterpick.newsletter.application.ReceivedMail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ReceivedMailAssemblerTest {

    @Test
    @DisplayName("SES 메타데이터와 MIME 파싱 결과가 주어졌을 때 ReceivedMail이 올바르게 만들어진다")
    void test1() {
        // given
        SesMailMetadata metadata = new SesMailMetadata(
                "ses-message-id",
                "abcd1234efgh@inbound.letterpick.test",
                Instant.parse("2026-05-15T10:00:00Z"),
                "letterpick-raw-mail",
                "ses-message-id"
        );
        ParsedMimeMail parsedMimeMail = new ParsedMimeMail(
                "newsletter@example.com",
                "이번 주 뉴스레터",
                "<html><body>뉴스레터 본문</body></html>"
        );
        ReceivedMailAssembler assembler = new ReceivedMailAssembler();

        // when
        ReceivedMail receivedMail = assembler.assemble(metadata, parsedMimeMail);

        // then
        assertThat(receivedMail.messageKey()).isEqualTo(metadata.messageKey());
        assertThat(receivedMail.recipientAddress()).isEqualTo(metadata.recipientAddress());
        assertThat(receivedMail.senderEmail()).isEqualTo(parsedMimeMail.senderEmail());
        assertThat(receivedMail.subject()).isEqualTo(parsedMimeMail.subject());
        assertThat(receivedMail.receivedAt()).isEqualTo(metadata.receivedAt());
        assertThat(receivedMail.content()).isEqualTo(parsedMimeMail.content());
        assertThat(receivedMail.rawReference()).isEqualTo(metadata.rawReference());
    }
}
