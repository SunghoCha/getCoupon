package com.sungho.letterpick.newsletter.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NewsletterIssuePreviewGeneratorTest {

    private final NewsletterIssuePreviewGenerator previewGenerator = new NewsletterIssuePreviewGenerator();

    @Test
    @DisplayName("HTML 태그와 줄바꿈이 포함된 본문을 plain text 미리보기로 변환한다")
    void generate_converts_html_and_line_breaks_to_plain_text_preview() {
        // given
        String content = """
                <p>첫 번째 줄<br>두 번째 줄</p>
                <p>세 번째 줄</p>
                """;

        // when
        String previewText = previewGenerator.generate(content);

        // then
        assertThat(previewText).isEqualTo("첫 번째 줄 두 번째 줄 세 번째 줄");
    }

    @Test
    @DisplayName("plain text 미리보기가 120자를 넘으면 120자까지만 반환한다")
    void generate_limits_plain_text_preview_to_120_characters() {
        // given
        String content = "가".repeat(121);

        // when
        String previewText = previewGenerator.generate(content);

        // then
        assertThat(previewText).isEqualTo("가".repeat(120));
    }
}
