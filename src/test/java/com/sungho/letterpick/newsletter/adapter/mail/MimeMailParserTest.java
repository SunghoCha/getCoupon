package com.sungho.letterpick.newsletter.adapter.mail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.sungho.letterpick.newsletter.adapter.mail.RawMimeFixture.rawMimeInputStream;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MimeMailParserTest {

    @Test
    @DisplayName("HTML MIME 메일에서 발신자, 제목, 본문을 추출한다")
    void test1() {
        // given
        String rawMime = """
                From: Newsletter <newsletter@example.com>
                To: abcd1234efgh@inbound.letterpick.test
                Subject: Weekly Newsletter
                MIME-Version: 1.0
                Content-Type: text/html; charset=UTF-8

                <html><body><h1>뉴스레터 본문</h1></body></html>
                """;
        MimeMailParser parser = new MimeMailParser();

        // when
        ParsedMimeMail parsedMimeMail = parser.parse(rawMimeInputStream(rawMime));

        // then
        assertThat(parsedMimeMail.senderEmail()).isEqualTo("newsletter@example.com");
        assertThat(parsedMimeMail.subject()).isEqualTo("Weekly Newsletter");
        assertThat(parsedMimeMail.content()).isEqualTo("<html><body><h1>뉴스레터 본문</h1></body></html>");
    }

    @Test
    @DisplayName("InputStream으로 받은 raw MIME 메일을 파싱한다")
    void parse_parses_raw_mime_input_stream() {
        // given
        String rawMime = """
                From: Newsletter <newsletter@example.com>
                To: abcd1234efgh@inbound.letterpick.test
                Subject: Weekly Newsletter
                MIME-Version: 1.0
                Content-Type: text/html; charset=UTF-8

                <html><body><h1>뉴스레터 본문</h1></body></html>
                """;
        MimeMailParser parser = new MimeMailParser();

        // when
        ParsedMimeMail parsedMimeMail = parser.parse(rawMimeInputStream(rawMime));

        // then
        assertThat(parsedMimeMail.senderEmail()).isEqualTo("newsletter@example.com");
        assertThat(parsedMimeMail.subject()).isEqualTo("Weekly Newsletter");
        assertThat(parsedMimeMail.content()).isEqualTo("<html><body><h1>뉴스레터 본문</h1></body></html>");
    }

    @Test
    @DisplayName("plain text MIME 메일에서 발신자, 제목, 본문을 추출한다")
    void parse_extracts_content_from_plain_text_mime_mail() {
        // given
        String rawMime = """
                From: Newsletter <newsletter@example.com>
                To: abcd1234efgh@inbound.letterpick.test
                Subject: Weekly Newsletter
                MIME-Version: 1.0
                Content-Type: text/plain; charset=UTF-8

                뉴스레터 평문 본문
                """;
        MimeMailParser parser = new MimeMailParser();

        // when
        ParsedMimeMail parsedMimeMail = parser.parse(rawMimeInputStream(rawMime));

        // then
        assertThat(parsedMimeMail.senderEmail()).isEqualTo("newsletter@example.com");
        assertThat(parsedMimeMail.subject()).isEqualTo("Weekly Newsletter");
        assertThat(parsedMimeMail.content()).isEqualTo("뉴스레터 평문 본문");
    }

    @Test
    @DisplayName("multipart alternative에 plain과 HTML이 함께 있으면 HTML 본문을 선택한다")
    void parse_selects_html_when_alternative_contains_plain_and_html() {
        // given
        String rawMime = """
                From: Newsletter <newsletter@example.com>
                To: abcd1234efgh@inbound.letterpick.test
                Subject: Weekly Newsletter
                MIME-Version: 1.0
                Content-Type: multipart/alternative; boundary="alternative-boundary"

                --alternative-boundary
                Content-Type: text/plain; charset=UTF-8

                plain newsletter body
                --alternative-boundary
                Content-Type: text/html; charset=UTF-8

                <html><body><h1>HTML 뉴스레터 본문</h1></body></html>
                --alternative-boundary--
                """;
        MimeMailParser parser = new MimeMailParser();

        // when
        ParsedMimeMail parsedMimeMail = parser.parse(rawMimeInputStream(rawMime));

        // then
        assertThat(parsedMimeMail.content()).isEqualTo("<html><body><h1>HTML 뉴스레터 본문</h1></body></html>");
    }

    @Test
    @DisplayName("multipart alternative에 HTML이 없으면 plain 본문을 선택한다")
    void parse_selects_plain_when_alternative_does_not_contain_html() {
        // given
        String rawMime = """
                From: Newsletter <newsletter@example.com>
                To: abcd1234efgh@inbound.letterpick.test
                Subject: Weekly Newsletter
                MIME-Version: 1.0
                Content-Type: multipart/alternative; boundary="alternative-boundary"

                --alternative-boundary
                Content-Type: text/plain; charset=UTF-8

                plain newsletter body
                --alternative-boundary--
                """;
        MimeMailParser parser = new MimeMailParser();

        // when
        ParsedMimeMail parsedMimeMail = parser.parse(rawMimeInputStream(rawMime));

        // then
        assertThat(parsedMimeMail.content()).isEqualTo("plain newsletter body");
    }

    @Test
    @DisplayName("multipart alternative에서 HTML 본문이 비어 있으면 plain 본문을 선택한다")
    void parse_falls_back_to_plain_when_alternative_html_is_blank() {
        // given
        String rawMime = """
                From: Newsletter <newsletter@example.com>
                To: abcd1234efgh@inbound.letterpick.test
                Subject: Weekly Newsletter
                MIME-Version: 1.0
                Content-Type: multipart/alternative; boundary="alternative-boundary"

                --alternative-boundary
                Content-Type: text/plain; charset=UTF-8

                plain newsletter body
                --alternative-boundary
                Content-Type: text/html; charset=UTF-8


                --alternative-boundary--
                """;
        MimeMailParser parser = new MimeMailParser();

        // when
        ParsedMimeMail parsedMimeMail = parser.parse(rawMimeInputStream(rawMime));

        // then
        assertThat(parsedMimeMail.content()).isEqualTo("plain newsletter body");
    }

    @Test
    @DisplayName("multipart 내부의 alternative 본문에서 HTML을 추출한다")
    void parse_extracts_html_from_nested_alternative_multipart() {
        // given
        String rawMime = """
                From: Newsletter <newsletter@example.com>
                To: abcd1234efgh@inbound.letterpick.test
                Subject: Weekly Newsletter
                MIME-Version: 1.0
                Content-Type: multipart/mixed; boundary="mixed-boundary"

                --mixed-boundary
                Content-Type: multipart/alternative; boundary="alternative-boundary"

                --alternative-boundary
                Content-Type: text/plain; charset=UTF-8

                plain newsletter body
                --alternative-boundary
                Content-Type: text/html; charset=UTF-8

                <html><body><h1>HTML 뉴스레터 본문</h1></body></html>
                --alternative-boundary--

                --mixed-boundary
                Content-Type: application/pdf
                Content-Disposition: attachment; filename="sample.pdf"

                attachment body
                --mixed-boundary--
                """;
        MimeMailParser parser = new MimeMailParser();

        // when
        ParsedMimeMail parsedMimeMail = parser.parse(rawMimeInputStream(rawMime));

        // then
        assertThat(parsedMimeMail.content()).isEqualTo("<html><body><h1>HTML 뉴스레터 본문</h1></body></html>");
    }

    @Test
    @DisplayName("일반 multipart에 본문 조각이 여러 개이면 단락 간격으로 이어붙인다")
    void parse_joins_multiple_body_parts_with_blank_line() {
        // given
        String rawMime = """
                From: Newsletter <newsletter@example.com>
                To: abcd1234efgh@inbound.letterpick.test
                Subject: Weekly Newsletter
                MIME-Version: 1.0
                Content-Type: multipart/mixed; boundary="mixed-boundary"

                --mixed-boundary
                Content-Type: text/html; charset=UTF-8

                <section>첫 번째 본문</section>
                --mixed-boundary
                Content-Type: text/html; charset=UTF-8

                <section>두 번째 본문</section>
                --mixed-boundary
                Content-Type: application/pdf
                Content-Disposition: attachment; filename="sample.pdf"

                attachment body
                --mixed-boundary--
                """;
        MimeMailParser parser = new MimeMailParser();

        // when
        ParsedMimeMail parsedMimeMail = parser.parse(rawMimeInputStream(rawMime));

        // then
        assertThat(parsedMimeMail.content()).isEqualTo("""
                <section>첫 번째 본문</section>

                <section>두 번째 본문</section>
                """.trim());
    }

    @Test
    @DisplayName("text plain 첨부파일은 본문에 섞지 않는다")
    void parse_excludes_plain_text_attachment_from_content() {
        // given
        String rawMime = """
                From: Newsletter <newsletter@example.com>
                To: abcd1234efgh@inbound.letterpick.test
                Subject: Weekly Newsletter
                MIME-Version: 1.0
                Content-Type: multipart/mixed; boundary="mixed-boundary"

                --mixed-boundary
                Content-Type: text/html; charset=UTF-8

                <section>뉴스레터 본문</section>
                --mixed-boundary
                Content-Type: text/plain; charset=UTF-8
                Content-Disposition: attachment; filename="memo.txt"

                첨부파일 내용
                --mixed-boundary--
                """;
        MimeMailParser parser = new MimeMailParser();

        // when
        ParsedMimeMail parsedMimeMail = parser.parse(rawMimeInputStream(rawMime));

        // then
        assertThat(parsedMimeMail.content()).isEqualTo("<section>뉴스레터 본문</section>");
    }

    @Test
    @DisplayName("From 헤더가 없으면 MIME 메일 파싱에 실패한다")
    void parse_fails_when_from_header_is_missing() {
        // given
        String rawMime = """
                To: abcd1234efgh@inbound.letterpick.test
                Subject: Weekly Newsletter
                MIME-Version: 1.0
                Content-Type: text/html; charset=UTF-8

                <html><body><h1>뉴스레터 본문</h1></body></html>
                """;
        MimeMailParser parser = new MimeMailParser();

        // when & then
        assertThatThrownBy(() -> parser.parse(rawMimeInputStream(rawMime)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("From 주소가 여러 개이면 MIME 메일 파싱에 실패한다")
    void parse_fails_when_from_header_has_multiple_addresses() {
        // given
        String rawMime = """
                From: Newsletter One <newsletter-one@example.com>, Newsletter Two <newsletter-two@example.com>
                To: abcd1234efgh@inbound.letterpick.test
                Subject: Weekly Newsletter
                MIME-Version: 1.0
                Content-Type: text/html; charset=UTF-8

                <html><body><h1>뉴스레터 본문</h1></body></html>
                """;
        MimeMailParser parser = new MimeMailParser();

        // when & then
        assertThatThrownBy(() -> parser.parse(rawMimeInputStream(rawMime)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Subject 헤더가 없으면 MIME 메일 파싱에 실패한다")
    void parse_fails_when_subject_header_is_missing() {
        // given
        String rawMime = """
                From: Newsletter <newsletter@example.com>
                To: abcd1234efgh@inbound.letterpick.test
                MIME-Version: 1.0
                Content-Type: text/html; charset=UTF-8

                <html><body><h1>뉴스레터 본문</h1></body></html>
                """;
        MimeMailParser parser = new MimeMailParser();

        // when & then
        assertThatThrownBy(() -> parser.parse(rawMimeInputStream(rawMime)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("본문이 없으면 MIME 메일 파싱에 실패한다")
    void parse_fails_when_content_is_missing() {
        // given
        String rawMime = """
                From: Newsletter <newsletter@example.com>
                To: abcd1234efgh@inbound.letterpick.test
                Subject: Weekly Newsletter
                MIME-Version: 1.0
                Content-Type: text/html; charset=UTF-8


                """;
        MimeMailParser parser = new MimeMailParser();

        // when & then
        assertThatThrownBy(() -> parser.parse(rawMimeInputStream(rawMime)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

