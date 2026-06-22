package com.example.jwtauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication -> @Configuration + @EnableAutoConfiguration + @ComponentScan
// Spring by default komponentlarni JwtAuthApplication dan boshlab qidiradi
// (com.example.jwtauth va uning ichidagi hamma papkalar)
@SpringBootApplication
public class JwtAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(JwtAuthApplication.class, args);
    }
}
