package com.sungho.letterpick.newsletter.adapter.mail;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import org.springframework.stereotype.Component;

import java.io.InputStream;

import static java.util.Objects.requireNonNull;

@Component
public class S3RawMailReader {

    private final S3Client s3Client;

    public S3RawMailReader(S3Client s3Client) {
        this.s3Client = requireNonNull(s3Client, "s3Client must not be null");
    }

    public InputStream open(String bucketName, String objectKey) {
        requireNonNull(bucketName, "bucketName must not be null");
        requireNonNull(objectKey, "objectKey must not be null");

        return s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build());
    }
}
