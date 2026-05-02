package com.sungho.letterpick.member.application;

import com.sungho.letterpick.member.domain.NewsletterInboxAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class NewsletterInboxAddressGenerator {
    private static final char[] TOKEN_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private final SecureRandom secureRandom = new SecureRandom();
    private final String domain;

    public NewsletterInboxAddressGenerator(@Value("${newsletter.inbox-address.domain}") String domain) {
        this.domain = domain;
    }

    public NewsletterInboxAddress generate() {
        StringBuilder token = new StringBuilder(NewsletterInboxAddress.TOKEN_LENGTH);
        for (int i = 0; i < NewsletterInboxAddress.TOKEN_LENGTH; i++) {
            token.append(TOKEN_CHARS[secureRandom.nextInt(TOKEN_CHARS.length)]);
        }
        return new NewsletterInboxAddress(token + "@" + domain);
    }
}
