package com.sungho.letterpick.newsletter.adapter.mail;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

@Component
public class MimeMailParser {

    private final MimeTextContentReader textContentReader;

    public MimeMailParser() {
        this(new MimeTextContentReader());
    }

    MimeMailParser(MimeTextContentReader textContentReader) {
        this.textContentReader = requireNonNull(textContentReader, "textContentReader must not be null");
    }

    public ParsedMimeMail parse(InputStream rawMime) {
        requireNonNull(rawMime, "rawMime must not be null");
        try {
            MimeMessage message = new MimeMessage(
                    Session.getInstance(new Properties()),
                    rawMime
            );

            return new ParsedMimeMail(
                    extractSenderEmail(message),
                    requiredText(message.getSubject(), "subject"),
                    requiredText(extractContent(message), "content")
            );
        } catch (MessagingException | IOException e) {
            throw new IllegalArgumentException("Invalid MIME mail", e);
        }
    }

    private String extractSenderEmail(MimeMessage message) throws MessagingException {
        Address[] from = message.getFrom();
        if (from == null || from.length != 1) {
            throw new IllegalArgumentException("MIME mail must contain exactly one From address");
        }
        if (!(from[0] instanceof InternetAddress internetAddress)) {
            throw new IllegalArgumentException("MIME From address must be an InternetAddress");
        }
        return requiredText(internetAddress.getAddress(), "from");
    }

    private String extractContent(Part part) throws MessagingException, IOException {
        if (part.isMimeType("text/html") || part.isMimeType("text/plain")) {
            return contentAsString(part);
        }
        if (part.isMimeType("multipart/alternative")) {
            return extractAlternativeMultipartContent((Multipart) part.getContent());
        }
        if (part.isMimeType("multipart/*")) {
            return extractMultipartContent((Multipart) part.getContent());
        }
        throw new IllegalArgumentException("Unsupported MIME content type");
    }

    private String extractAlternativeMultipartContent(Multipart multipart) throws MessagingException, IOException {
        for (int i = multipart.getCount() - 1; i >= 0; i--) {
            Part bodyPart = multipart.getBodyPart(i);
            if (isAttachment(bodyPart) || !isBodyCandidate(bodyPart)) {
                continue;
            }
            String content = extractContent(bodyPart);
            if (!content.isBlank()) {
                return content;
            }
        }
        throw new IllegalArgumentException("Unsupported alternative MIME content");
    }

    private String extractMultipartContent(Multipart multipart) throws MessagingException, IOException {
        List<String> bodyContents = new ArrayList<>();

        for (int i = 0; i < multipart.getCount(); i++) {
            Part bodyPart = multipart.getBodyPart(i);
            if (isAttachment(bodyPart) || !isBodyCandidate(bodyPart)) {
                continue;
            }
            String content = extractContent(bodyPart);
            if (!content.isBlank()) {
                bodyContents.add(content);
            }
        }

        if (!bodyContents.isEmpty()) {
            return String.join("\n\n", bodyContents);
        }
        throw new IllegalArgumentException("Unsupported multipart MIME content");
    }

    private boolean isBodyCandidate(Part part) throws MessagingException {
        return part.isMimeType("text/html")
                || part.isMimeType("text/plain")
                || part.isMimeType("multipart/*");
    }

    private boolean isAttachment(Part part) throws MessagingException {
        String disposition = part.getDisposition();
        return disposition != null && disposition.equalsIgnoreCase(Part.ATTACHMENT);
    }

    private String contentAsString(Part part) throws MessagingException, IOException {
        return textContentReader.read(part);
    }

    private String requiredText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing MIME mail field: " + fieldName);
        }
        return value;
    }
}
