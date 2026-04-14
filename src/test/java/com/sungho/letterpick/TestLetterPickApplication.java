package com.sungho.letterpick;

import org.springframework.boot.SpringApplication;

public class TestLetterPickApplication {

    public static void main(String[] args) {
        SpringApplication.from(LetterPickApplication::main).with(LetterPickTestConfiguration.class).run(args);
    }

}
