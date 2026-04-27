package com.sungho.letterpick;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import(LetterPickTestConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
class LetterPickApplicationTests {

    @Test
    void contextLoads() {
    }

}
