package com.sungho.letterpick.newsletter.adapter.mail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class S3RawMailReaderTest {

    @Test
    @DisplayName("bucketName과 objectKey로 S3 raw MIME InputStream을 연다")
    void open_opens_raw_mime_input_stream_from_s3() throws Exception {
        // given
        S3Client s3Client = mock(S3Client.class);
        ResponseInputStream<GetObjectResponse> responseInputStream = new ResponseInputStream<>(
                GetObjectResponse.builder().build(),
                AbortableInputStream.create(new ByteArrayInputStream("raw MIME".getBytes(StandardCharsets.UTF_8)))
        );
        given(s3Client.getObject(any(GetObjectRequest.class)))
                .willReturn(responseInputStream);

        S3RawMailReader reader = new S3RawMailReader(s3Client);

        // when
        InputStream rawMail = reader.open("letterpick-raw-mail", "ses-message-id");

        // then
        assertThat(new String(rawMail.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("raw MIME");
        verify(s3Client).getObject(GetObjectRequest.builder()
                .bucket("letterpick-raw-mail")
                .key("ses-message-id")
                .build());
        rawMail.close();
    }
}
