package com.sungho.letterpick.newsletter.adapter.mail;

import com.sungho.letterpick.newsletter.application.ReceivedMail;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class ReceivedMailAssembler {

    public ReceivedMail assemble(SesMailMetadata metadata, ParsedMimeMail parsedMimeMail) {
        requireNonNull(metadata, "metadata must not be null");
        requireNonNull(parsedMimeMail, "parsedMimeMail must not be null");

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
