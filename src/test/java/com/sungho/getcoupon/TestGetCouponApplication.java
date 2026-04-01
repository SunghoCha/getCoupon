package com.sungho.getcoupon;

import org.springframework.boot.SpringApplication;

public class TestGetCouponApplication {

    public static void main(String[] args) {
        SpringApplication.from(GetCouponApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
