package com.sungho.letterpick.newsletter.adapter.mail;

import io.awspring.cloud.sqs.annotation.SqsListener;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class SesMailSqsListener {

    private final SesMailReceiveProcessor processor;

    public SesMailSqsListener(SesMailReceiveProcessor processor) {
        this.processor = requireNonNull(processor, "processor must not be null");
    }

    @SqsListener("${letterpick.mail.receive-queue}")
    public void receive(String sqsMessageBody) {
        processor.process(sqsMessageBody);
    }
}
