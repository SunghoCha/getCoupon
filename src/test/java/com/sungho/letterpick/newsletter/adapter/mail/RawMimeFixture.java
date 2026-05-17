package com.sungho.letterpick.newsletter.adapter.mail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

final class RawMimeFixture {

    private RawMimeFixture() {
    }

    static InputStream rawMimeInputStream(String rawMime) {
        return new ByteArrayInputStream(rawMime.getBytes(StandardCharsets.UTF_8));
    }
}
