package com.sungho.letterpick.newsletter.adapter.mail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import static java.util.Objects.requireNonNull;

@Component
public class SesNotificationParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SesMailMetadata parse(String json) {
        requireNonNull(json, "json must not be null");

        try {
            JsonNode root = objectMapper.readTree(json);
            String messageKey = requiredText(root.path("mail").path("messageId"), "mail.messageId");
            Instant receivedAt = requiredInstant(root.path("mail").path("timestamp"), "mail.timestamp");
            String recipientAddress = requiredSingleText(root.path("receipt").path("recipients"), "receipt.recipients");
            JsonNode action = root.path("receipt").path("action");
            String actionType = requiredText(action.path("type"), "receipt.action.type");
            if (!"S3".equals(actionType)) {
                throw new IllegalArgumentException("Unsupported SES notification action type: " + actionType);
            }
            String bucketName = requiredText(action.path("bucketName"), "receipt.action.bucketName");
            String objectKey = requiredText(action.path("objectKey"), "receipt.action.objectKey");

            return new SesMailMetadata(
                    messageKey,
                    recipientAddress,
                    receivedAt,
                    bucketName,
                    objectKey
            );
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid SES notification json", e);
        }
    }

    private Instant requiredInstant(JsonNode node, String fieldPath) {
        String value = requiredText(node, fieldPath);
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid SES notification timestamp: " + fieldPath, e);
        }
    }

    private String requiredText(JsonNode node, String fieldPath) {
        if (!node.isTextual() || node.asText().isBlank()) {
            throw new IllegalArgumentException("Missing SES notification field: " + fieldPath);
        }
        return node.asText();
    }

    private String requiredSingleText(JsonNode node, String fieldPath) {
        if (!node.isArray() || node.isEmpty()) {
            throw new IllegalArgumentException("Missing SES notification field: " + fieldPath);
        }
        if (node.size() != 1) {
            throw new IllegalArgumentException("SES notification field must contain exactly one value: " + fieldPath);
        }
        return requiredText(node.get(0), fieldPath + "[0]");
    }
}
