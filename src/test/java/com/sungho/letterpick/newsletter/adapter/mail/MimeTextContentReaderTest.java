package com.sungho.letterpick.newsletter.adapter.mail;

import jakarta.mail.Part;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class MimeTextContentReaderTest {

    @Test
    @DisplayName("텍스트 content가 String이 아니면 InputStream과 charset으로 본문을 복원한다")
    void read_restores_text_from_input_stream_when_content_is_not_string() throws Exception {
        // given
        Charset charset = Charset.forName("EUC-KR");
        Part part = mock(Part.class);
        given(part.getContent()).willReturn(new Object());
        given(part.getInputStream()).willReturn(new ByteArrayInputStream("뉴스레터 본문".getBytes(charset)));
        given(part.getContentType()).willReturn("text/plain; charset=EUC-KR");

        MimeTextContentReader reader = new MimeTextContentReader();

        // when
        String content = reader.read(part);

        // then
        assertThat(content).isEqualTo("뉴스레터 본문");
    }
}
