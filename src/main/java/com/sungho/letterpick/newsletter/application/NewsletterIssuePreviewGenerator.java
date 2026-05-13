package com.sungho.letterpick.newsletter.application;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class NewsletterIssuePreviewGenerator {

    private static final int MAX_PREVIEW_LENGTH = 120;

    public String generate(String content) {
        String previewText = Jsoup.parse(requireNonNull(content)).text();
        if (previewText.codePointCount(0, previewText.length()) <= MAX_PREVIEW_LENGTH) {
            return previewText;
        }
        int endIndex = previewText.offsetByCodePoints(0, MAX_PREVIEW_LENGTH);
        return previewText.substring(0, endIndex);
    }
}
