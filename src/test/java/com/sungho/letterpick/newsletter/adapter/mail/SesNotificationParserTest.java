package com.sungho.letterpick.newsletter.adapter.mail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class SesNotificationParserTest {


    @Test
    @DisplayName("SES notification에서 수신 메타데이터를 추출한다")
    void test1() {
        // given
        String json = """
            {
              "notificationType": "Received",
              "mail": {
                "messageId": "ses-message-id",
                "timestamp": "2026-05-15T10:00:00.000Z",
                "source": "bounce-or-envelope@example.com",
                "destination": ["abcd1234efgh@inbound.letterpick.test"]
              },
              "receipt": {
                "recipients": ["abcd1234efgh@inbound.letterpick.test"],
                "spamVerdict": { "status": "PASS" },
                "virusVerdict": { "status": "PASS" },
                "action": {
                  "type": "S3",
                  "bucketName": "letterpick-raw-mail",
                  "objectKey": "ses-message-id"
                }
              }
            }
            """;
        SesNotificationParser parser = new SesNotificationParser();
        // when
        SesMailMetadata metadata = parser.parse(json);
        // then
        assertThat(metadata.messageKey()).isEqualTo("ses-message-id");
        assertThat(metadata.recipientAddress()).isEqualTo("abcd1234efgh@inbound.letterpick.test");
        assertThat(metadata.receivedAt()).isEqualTo(Instant.parse("2026-05-15T10:00:00Z"));
        assertThat(metadata.bucketName()).isEqualTo("letterpick-raw-mail");
        assertThat(metadata.objectKey()).isEqualTo("ses-message-id");
        assertThat(metadata.rawReference()).isEqualTo("s3://letterpick-raw-mail/ses-message-id");
    }

    @Test
    @DisplayName("SES notification의 recipients가 여러 개이면 파싱에 실패한다")
    void test2() {
        // given
        String json = """
            {
              "notificationType": "Received",
              "mail": {
                "messageId": "ses-message-id",
                "timestamp": "2026-05-15T10:00:00.000Z",
                "source": "bounce-or-envelope@example.com",
                "destination": [
                  "abcd1234efgh@inbound.letterpick.test",
                  "mnopqrstuvwx@inbound.letterpick.test"
                ]
              },
              "receipt": {
                "recipients": [
                  "abcd1234efgh@inbound.letterpick.test",
                  "mnopqrstuvwx@inbound.letterpick.test"
                ],
                "spamVerdict": { "status": "PASS" },
                "virusVerdict": { "status": "PASS" },
                "action": {
                  "type": "S3",
                  "bucketName": "letterpick-raw-mail",
                  "objectKey": "ses-message-id"
                }
              }
            }
            """;
        SesNotificationParser parser = new SesNotificationParser();

        // when & then
        assertThatThrownBy(() -> parser.parse(json))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("SES notification의 messageId가 없으면 파싱에 실패한다")
    void parse_fails_when_message_id_is_missing() {
        // given
        String json = """
            {
              "notificationType": "Received",
              "mail": {
                "timestamp": "2026-05-15T10:00:00.000Z",
                "source": "bounce-or-envelope@example.com",
                "destination": ["abcd1234efgh@inbound.letterpick.test"]
              },
              "receipt": {
                "recipients": ["abcd1234efgh@inbound.letterpick.test"],
                "spamVerdict": { "status": "PASS" },
                "virusVerdict": { "status": "PASS" },
                "action": {
                  "type": "S3",
                  "bucketName": "letterpick-raw-mail",
                  "objectKey": "ses-message-id"
                }
              }
            }
            """;
        SesNotificationParser parser = new SesNotificationParser();

        // when & then
        assertThatThrownBy(() -> parser.parse(json))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("SES notification의 timestamp가 없으면 파싱에 실패한다")
    void parse_fails_when_timestamp_is_missing() {
        // given
        String json = """
            {
              "notificationType": "Received",
              "mail": {
                "messageId": "ses-message-id",
                "source": "bounce-or-envelope@example.com",
                "destination": ["abcd1234efgh@inbound.letterpick.test"]
              },
              "receipt": {
                "recipients": ["abcd1234efgh@inbound.letterpick.test"],
                "spamVerdict": { "status": "PASS" },
                "virusVerdict": { "status": "PASS" },
                "action": {
                  "type": "S3",
                  "bucketName": "letterpick-raw-mail",
                  "objectKey": "ses-message-id"
                }
              }
            }
            """;
        SesNotificationParser parser = new SesNotificationParser();

        // when & then
        assertThatThrownBy(() -> parser.parse(json))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("SES notification의 recipients가 없으면 파싱에 실패한다")
    void parse_fails_when_recipients_is_missing() {
        // given
        String json = """
            {
              "notificationType": "Received",
              "mail": {
                "messageId": "ses-message-id",
                "timestamp": "2026-05-15T10:00:00.000Z",
                "source": "bounce-or-envelope@example.com",
                "destination": ["abcd1234efgh@inbound.letterpick.test"]
              },
              "receipt": {
                "spamVerdict": { "status": "PASS" },
                "virusVerdict": { "status": "PASS" },
                "action": {
                  "type": "S3",
                  "bucketName": "letterpick-raw-mail",
                  "objectKey": "ses-message-id"
                }
              }
            }
            """;
        SesNotificationParser parser = new SesNotificationParser();

        // when & then
        assertThatThrownBy(() -> parser.parse(json))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("SES notification의 bucketName이 없으면 파싱에 실패한다")
    void parse_fails_when_bucket_name_is_missing() {
        // given
        String json = """
            {
              "notificationType": "Received",
              "mail": {
                "messageId": "ses-message-id",
                "timestamp": "2026-05-15T10:00:00.000Z",
                "source": "bounce-or-envelope@example.com",
                "destination": ["abcd1234efgh@inbound.letterpick.test"]
              },
              "receipt": {
                "recipients": ["abcd1234efgh@inbound.letterpick.test"],
                "spamVerdict": { "status": "PASS" },
                "virusVerdict": { "status": "PASS" },
                "action": {
                  "type": "S3",
                  "objectKey": "ses-message-id"
                }
              }
            }
            """;
        SesNotificationParser parser = new SesNotificationParser();

        // when & then
        assertThatThrownBy(() -> parser.parse(json))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("SES notification의 objectKey가 없으면 파싱에 실패한다")
    void parse_fails_when_object_key_is_missing() {
        // given
        String json = """
            {
              "notificationType": "Received",
              "mail": {
                "messageId": "ses-message-id",
                "timestamp": "2026-05-15T10:00:00.000Z",
                "source": "bounce-or-envelope@example.com",
                "destination": ["abcd1234efgh@inbound.letterpick.test"]
              },
              "receipt": {
                "recipients": ["abcd1234efgh@inbound.letterpick.test"],
                "spamVerdict": { "status": "PASS" },
                "virusVerdict": { "status": "PASS" },
                "action": {
                  "type": "S3",
                  "bucketName": "letterpick-raw-mail"
                }
              }
            }
            """;
        SesNotificationParser parser = new SesNotificationParser();

        // when & then
        assertThatThrownBy(() -> parser.parse(json))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("SES notification의 action type이 S3가 아니면 파싱에 실패한다")
    void parse_fails_when_action_type_is_not_s3() {
        // given
        String json = """
            {
              "notificationType": "Received",
              "mail": {
                "messageId": "ses-message-id",
                "timestamp": "2026-05-15T10:00:00.000Z",
                "source": "bounce-or-envelope@example.com",
                "destination": ["abcd1234efgh@inbound.letterpick.test"]
              },
              "receipt": {
                "recipients": ["abcd1234efgh@inbound.letterpick.test"],
                "spamVerdict": { "status": "PASS" },
                "virusVerdict": { "status": "PASS" },
                "action": {
                  "type": "SNS",
                  "bucketName": "letterpick-raw-mail",
                  "objectKey": "ses-message-id"
                }
              }
            }
            """;
        SesNotificationParser parser = new SesNotificationParser();

        // when & then
        assertThatThrownBy(() -> parser.parse(json))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
