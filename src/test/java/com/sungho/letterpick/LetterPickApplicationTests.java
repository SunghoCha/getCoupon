package com.sungho.letterpick;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(LetterPickTestConfiguration.class)
@SpringBootTest
class LetterPickApplicationTests {

    @Test
    void contextLoads() {
    }

}
