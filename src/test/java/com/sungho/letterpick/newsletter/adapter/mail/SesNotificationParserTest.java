package com.sungho.letterpick.newsletter.adapter.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class SesNotificationParserTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String MESSAGE_ID = "ses-message-id";
    private static final String TIMESTAMP = "2026-05-15T10:00:00.000Z";
    private static final String RECIPIENT_ADDRESS = "abcd1234efgh@inbound.letterpick.test";
    private static final String ANOTHER_RECIPIENT_ADDRESS = "mnopqrstuvwx@inbound.letterpick.test";
    private static final String BUCKET_NAME = "letterpick-raw-mail";
    private static final String OBJECT_KEY = "ses-message-id";

    private final SesNotificationParser parser = new SesNotificationParser();

    @Test
    @DisplayName("SES notification에서 수신 메타데이터를 추출한다")
    void parse_extracts_mail_metadata_from_ses_notification() {
        // given
        ObjectNode notification = validNotification();

        // when
        SesMailMetadata metadata = parser.parse(json(notification));

        // then
        assertThat(metadata.messageKey()).isEqualTo(MESSAGE_ID);
        assertThat(metadata.recipientAddress()).isEqualTo(RECIPIENT_ADDRESS);
        assertThat(metadata.receivedAt()).isEqualTo(Instant.parse("2026-05-15T10:00:00Z"));
        assertThat(metadata.bucketName()).isEqualTo(BUCKET_NAME);
        assertThat(metadata.objectKey()).isEqualTo(OBJECT_KEY);
        assertThat(metadata.rawReference()).isEqualTo("s3://letterpick-raw-mail/ses-message-id");
    }

    @Test
    @DisplayName("SES notification의 recipients가 여러 개이면 파싱에 실패한다")
    void parse_fails_when_recipients_has_multiple_values() {
        // given
        ObjectNode notification = validNotification();
        recipients(notification).add(ANOTHER_RECIPIENT_ADDRESS);

        // when & then
        assertThatThrownBy(() -> parser.parse(json(notification)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("SES notification의 messageId가 없으면 파싱에 실패한다")
    void parse_fails_when_message_id_is_missing() {
        // given
        ObjectNode notification = validNotification();
        mail(notification).remove("messageId");

        // when & then
        assertThatThrownBy(() -> parser.parse(json(notification)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("SES notification의 timestamp가 없으면 파싱에 실패한다")
    void parse_fails_when_timestamp_is_missing() {
        // given
        ObjectNode notification = validNotification();
        mail(notification).remove("timestamp");

        // when & then
        assertThatThrownBy(() -> parser.parse(json(notification)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("SES notification의 timestamp 형식이 잘못되면 파싱에 실패한다")
    void parse_fails_when_timestamp_is_invalid() {
        // given
        ObjectNode notification = validNotification();
        mail(notification).put("timestamp", "not-a-timestamp");

        // when & then
        assertThatThrownBy(() -> parser.parse(json(notification)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("SES notification의 recipients가 없으면 파싱에 실패한다")
    void parse_fails_when_recipients_is_missing() {
        // given
        ObjectNode notification = validNotification();
        receipt(notification).remove("recipients");

        // when & then
        assertThatThrownBy(() -> parser.parse(json(notification)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("SES notification의 bucketName이 없으면 파싱에 실패한다")
    void parse_fails_when_bucket_name_is_missing() {
        // given
        ObjectNode notification = validNotification();
        action(notification).remove("bucketName");

        // when & then
        assertThatThrownBy(() -> parser.parse(json(notification)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("SES notification의 objectKey가 없으면 파싱에 실패한다")
    void parse_fails_when_object_key_is_missing() {
        // given
        ObjectNode notification = validNotification();
        action(notification).remove("objectKey");

        // when & then
        assertThatThrownBy(() -> parser.parse(json(notification)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("SES notification의 action type이 S3가 아니면 파싱에 실패한다")
    void parse_fails_when_action_type_is_not_s3() {
        // given
        ObjectNode notification = validNotification();
        action(notification).put("type", "SNS");

        // when & then
        assertThatThrownBy(() -> parser.parse(json(notification)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private ObjectNode validNotification() {
        ObjectNode root = OBJECT_MAPPER.createObjectNode();
        root.put("notificationType", "Received");

        ObjectNode mail = root.putObject("mail");
        mail.put("messageId", MESSAGE_ID);
        mail.put("timestamp", TIMESTAMP);
        mail.put("source", "bounce-or-envelope@example.com");
        mail.putArray("destination").add(RECIPIENT_ADDRESS);

        ObjectNode receipt = root.putObject("receipt");
        receipt.putArray("recipients").add(RECIPIENT_ADDRESS);
        receipt.putObject("spamVerdict").put("status", "PASS");
        receipt.putObject("virusVerdict").put("status", "PASS");

        ObjectNode action = receipt.putObject("action");
        action.put("type", "S3");
        action.put("bucketName", BUCKET_NAME);
        action.put("objectKey", OBJECT_KEY);

        return root;
    }

    private ObjectNode mail(ObjectNode notification) {
        return (ObjectNode) notification.path("mail");
    }

    private ObjectNode receipt(ObjectNode notification) {
        return (ObjectNode) notification.path("receipt");
    }

    private ArrayNode recipients(ObjectNode notification) {
        return (ArrayNode) receipt(notification).path("recipients");
    }

    private ObjectNode action(ObjectNode notification) {
        return (ObjectNode) receipt(notification).path("action");
    }

    private String json(ObjectNode notification) {
        return notification.toString();
    }
}
