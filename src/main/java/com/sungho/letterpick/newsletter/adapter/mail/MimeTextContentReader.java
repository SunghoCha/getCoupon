package com.sungho.letterpick.newsletter.adapter.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

class MimeTextContentReader {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    String read(Part part) throws MessagingException, IOException {
        Object content = part.getContent();
        if (content instanceof String text) {
            return text.trim();
        }
        if (content instanceof InputStream inputStream) {
            try (inputStream) {
                return readInputStream(inputStream, part.getContentType());
            }
        }
        try (InputStream inputStream = part.getInputStream()) {
            return readInputStream(inputStream, part.getContentType());
        }
    }

    private String readInputStream(InputStream inputStream, String contentType) throws IOException {
        Charset charset = resolveCharset(contentType);
        return new String(inputStream.readAllBytes(), charset).trim();
    }

    private Charset resolveCharset(String rawContentType) {
        if (rawContentType == null || rawContentType.isBlank()) {
            return DEFAULT_CHARSET;
        }
        try {
            ContentType contentType = new ContentType(rawContentType);
            String charsetName = contentType.getParameter("charset");
            if (charsetName == null || charsetName.isBlank()) {
                return DEFAULT_CHARSET;
            }
            return Charset.forName(charsetName);
        } catch (IllegalArgumentException | ParseException e) {
            return DEFAULT_CHARSET;
        }
    }

}
