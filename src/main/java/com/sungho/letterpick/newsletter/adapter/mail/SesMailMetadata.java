package com.sungho.letterpick.newsletter.adapter.mail;

import java.time.Instant;

import static java.util.Objects.requireNonNull;

public record SesMailMetadata(
        String messageKey,
        String recipientAddress,
        Instant receivedAt,
        String bucketName,
        String objectKey
) {

    public SesMailMetadata {
        requireNonNull(messageKey, "messageKey must not be null");
        requireNonNull(recipientAddress, "recipientAddress must not be null");
        requireNonNull(receivedAt, "receivedAt must not be null");
        requireNonNull(bucketName, "bucketName must not be null");
        requireNonNull(objectKey, "objectKey must not be null");
    }

    public String rawReference() {
        return "s3://" + bucketName + "/" + objectKey;
    }
}
