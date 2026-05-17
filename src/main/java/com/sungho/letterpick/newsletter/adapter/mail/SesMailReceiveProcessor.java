package com.sungho.letterpick.newsletter.adapter.mail;

import com.sungho.letterpick.newsletter.application.NewsletterMailReceiveService;
import com.sungho.letterpick.newsletter.application.ReceivedMail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import static java.util.Objects.requireNonNull;

@Slf4j
@Component
public class SesMailReceiveProcessor {

    private final SesNotificationParser sesNotificationParser;
    private final S3RawMailReader s3RawMailReader;
    private final MimeMailParser mimeMailParser;
    private final ReceivedMailAssembler receivedMailAssembler;
    private final NewsletterMailReceiveService newsletterMailReceiveService;

    public SesMailReceiveProcessor(
            SesNotificationParser sesNotificationParser,
            S3RawMailReader s3RawMailReader,
            MimeMailParser mimeMailParser,
            ReceivedMailAssembler receivedMailAssembler,
            NewsletterMailReceiveService newsletterMailReceiveService
    ) {
        this.sesNotificationParser = requireNonNull(sesNotificationParser, "sesNotificationParser must not be null");
        this.s3RawMailReader = requireNonNull(s3RawMailReader, "s3RawMailReader must not be null");
        this.mimeMailParser = requireNonNull(mimeMailParser, "mimeMailParser must not be null");
        this.receivedMailAssembler = requireNonNull(receivedMailAssembler, "receivedMailAssembler must not be null");
        this.newsletterMailReceiveService = requireNonNull(
                newsletterMailReceiveService,
                "newsletterMailReceiveService must not be null"
        );
    }

    public void process(String sqsMessageBody) {
        requireNonNull(sqsMessageBody, "sqsMessageBody must not be null");

        SesMailMetadata metadata = null;
        try {
            metadata = sesNotificationParser.parse(sqsMessageBody);
            log.info("메일 수신 처리를 시작합니다. messageKey={}, rawReference={}",
                    metadata.messageKey(), metadata.rawReference());

            ReceivedMail receivedMail;
            try (InputStream rawMail = s3RawMailReader.open(metadata.bucketName(), metadata.objectKey())) {
                ParsedMimeMail parsedMimeMail = mimeMailParser.parse(rawMail);
                receivedMail = receivedMailAssembler.assemble(metadata, parsedMimeMail);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to close raw mail input stream", e);
            }

            newsletterMailReceiveService.receive(receivedMail);
            log.info("메일 수신 처리를 완료했습니다. messageKey={}, rawReference={}",
                    metadata.messageKey(), metadata.rawReference());
        } catch (RuntimeException e) {
            logFailure(metadata, e);
            throw e;
        }
    }

    private void logFailure(SesMailMetadata metadata, RuntimeException e) {
        if (metadata == null) {
            log.warn("메일 수신 처리에 실패했습니다. SES notification에서 메타데이터를 얻지 못했습니다. failureType={}, failureMessage={}",
                    e.getClass().getSimpleName(), e.getMessage());
            return;
        }

        log.warn("메일 수신 처리에 실패했습니다. messageKey={}, rawReference={}, failureType={}, failureMessage={}",
                metadata.messageKey(), metadata.rawReference(), e.getClass().getSimpleName(), e.getMessage());
    }
}
