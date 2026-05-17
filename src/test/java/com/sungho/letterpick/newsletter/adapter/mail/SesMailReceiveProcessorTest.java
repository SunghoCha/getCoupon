package com.sungho.letterpick.newsletter.adapter.mail;

import com.sungho.letterpick.newsletter.application.NewsletterMailReceiveService;
import com.sungho.letterpick.newsletter.application.ReceivedMail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SesMailReceiveProcessorTest {

    private static final String SQS_MESSAGE_BODY = "ses notification json";
    private static final String INVALID_SQS_MESSAGE_BODY = "invalid ses notification json";

    @InjectMocks
    private SesMailReceiveProcessor processor;

    @Mock
    private SesNotificationParser sesNotificationParser;

    @Mock
    private S3RawMailReader s3RawMailReader;

    @Mock
    private MimeMailParser mimeMailParser;

    @Mock
    private ReceivedMailAssembler receivedMailAssembler;

    @Mock
    private NewsletterMailReceiveService newsletterMailReceiveService;

    @Test
    @DisplayName("SES notification과 S3 raw MIME으로 ReceivedMail을 만들어 수신 유스케이스에 전달한다")
    void process_receives_mail_from_ses_notification_and_s3_raw_mime() throws Exception {
        // given
        SesMailMetadata metadata = metadata();
        ParsedMimeMail parsedMimeMail = parsedMimeMail();
        ReceivedMail receivedMail = receivedMail(metadata, parsedMimeMail);
        InputStream rawMail = mock(InputStream.class);

        given(sesNotificationParser.parse(SQS_MESSAGE_BODY)).willReturn(metadata);
        given(s3RawMailReader.open(metadata.bucketName(), metadata.objectKey())).willReturn(rawMail);
        given(mimeMailParser.parse(rawMail)).willReturn(parsedMimeMail);
        given(receivedMailAssembler.assemble(metadata, parsedMimeMail)).willReturn(receivedMail);

        // when
        processor.process(SQS_MESSAGE_BODY);

        // then
        verify(sesNotificationParser).parse(SQS_MESSAGE_BODY);
        verify(s3RawMailReader).open(metadata.bucketName(), metadata.objectKey());
        verify(mimeMailParser).parse(rawMail);
        verify(receivedMailAssembler).assemble(metadata, parsedMimeMail);
        InOrder inOrder = inOrder(rawMail, newsletterMailReceiveService);
        inOrder.verify(rawMail).close();
        inOrder.verify(newsletterMailReceiveService).receive(receivedMail);
    }

    @Test
    @DisplayName("MIME 파싱에 실패하면 S3 raw MIME stream을 닫고 수신 유스케이스를 호출하지 않는다")
    void process_closes_raw_mime_stream_when_mime_parsing_fails() throws Exception {
        // given
        SesMailMetadata metadata = metadata();
        InputStream rawMail = mock(InputStream.class);
        IllegalArgumentException parseFailure = new IllegalArgumentException("Invalid MIME mail");

        given(sesNotificationParser.parse(SQS_MESSAGE_BODY)).willReturn(metadata);
        given(s3RawMailReader.open(metadata.bucketName(), metadata.objectKey())).willReturn(rawMail);
        given(mimeMailParser.parse(rawMail)).willThrow(parseFailure);

        // when & then
        assertThatThrownBy(() -> processor.process(SQS_MESSAGE_BODY))
                .isSameAs(parseFailure);

        InOrder inOrder = inOrder(mimeMailParser, rawMail);
        inOrder.verify(mimeMailParser).parse(rawMail);
        inOrder.verify(rawMail).close();
        verify(receivedMailAssembler, never()).assemble(any(), any());
        verify(newsletterMailReceiveService, never()).receive(any());
    }

    @Test
    @DisplayName("S3 raw MIME을 열지 못하면 이후 처리를 진행하지 않는다")
    void process_stops_when_s3_raw_mime_open_fails() {
        // given
        SesMailMetadata metadata = metadata();
        IllegalStateException s3Failure = new IllegalStateException("S3 raw MIME open failed");

        given(sesNotificationParser.parse(SQS_MESSAGE_BODY)).willReturn(metadata);
        given(s3RawMailReader.open(metadata.bucketName(), metadata.objectKey())).willThrow(s3Failure);

        // when & then
        assertThatThrownBy(() -> processor.process(SQS_MESSAGE_BODY))
                .isSameAs(s3Failure);

        verify(s3RawMailReader).open(metadata.bucketName(), metadata.objectKey());
        verify(mimeMailParser, never()).parse(any(InputStream.class));
        verify(receivedMailAssembler, never()).assemble(any(), any());
        verify(newsletterMailReceiveService, never()).receive(any());
    }

    @Test
    @DisplayName("SES notification 파싱에 실패하면 S3 raw MIME을 열지 않는다")
    void process_stops_when_ses_notification_parsing_fails() {
        // given
        IllegalArgumentException parseFailure = new IllegalArgumentException("Invalid SES notification json");
        given(sesNotificationParser.parse(INVALID_SQS_MESSAGE_BODY)).willThrow(parseFailure);

        // when & then
        assertThatThrownBy(() -> processor.process(INVALID_SQS_MESSAGE_BODY))
                .isSameAs(parseFailure);

        verify(sesNotificationParser).parse(INVALID_SQS_MESSAGE_BODY);
        verifyNoInteractions(
                s3RawMailReader,
                mimeMailParser,
                receivedMailAssembler,
                newsletterMailReceiveService
        );
    }

    @Test
    @DisplayName("수신 유스케이스가 실패하면 예외를 밖으로 전파한다")
    void process_propagates_receive_failure() throws Exception {
        // given
        SesMailMetadata metadata = metadata();
        ParsedMimeMail parsedMimeMail = parsedMimeMail();
        ReceivedMail receivedMail = receivedMail(metadata, parsedMimeMail);
        InputStream rawMail = mock(InputStream.class);
        IllegalStateException receiveFailure = new IllegalStateException("Receive failed");

        given(sesNotificationParser.parse(SQS_MESSAGE_BODY)).willReturn(metadata);
        given(s3RawMailReader.open(metadata.bucketName(), metadata.objectKey())).willReturn(rawMail);
        given(mimeMailParser.parse(rawMail)).willReturn(parsedMimeMail);
        given(receivedMailAssembler.assemble(metadata, parsedMimeMail)).willReturn(receivedMail);
        doThrow(receiveFailure).when(newsletterMailReceiveService).receive(receivedMail);

        // when & then
        assertThatThrownBy(() -> processor.process(SQS_MESSAGE_BODY))
                .isSameAs(receiveFailure);

        InOrder inOrder = inOrder(rawMail, newsletterMailReceiveService);
        inOrder.verify(rawMail).close();
        inOrder.verify(newsletterMailReceiveService).receive(receivedMail);
    }

    private SesMailMetadata metadata() {
        return new SesMailMetadata(
                "ses-message-id",
                "abcd1234efgh@inbound.letterpick.test",
                Instant.parse("2026-05-15T10:15:30Z"),
                "letterpick-raw-mail",
                "ses-message-id"
        );
    }

    private ParsedMimeMail parsedMimeMail() {
        return new ParsedMimeMail(
                "newsletter@example.com",
                "오늘의 뉴스레터",
                "뉴스레터 본문"
        );
    }

    private ReceivedMail receivedMail(SesMailMetadata metadata, ParsedMimeMail parsedMimeMail) {
        return new ReceivedMail(
                metadata.messageKey(),
                metadata.recipientAddress(),
                parsedMimeMail.senderEmail(),
                parsedMimeMail.subject(),
                metadata.receivedAt(),
                parsedMimeMail.content(),
                metadata.rawReference()
        );
    }
}
