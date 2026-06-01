package com.example.jpademo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication -> Spring Boot ilovasini ishga tushiradi
// @EntityScan -> @Entity klasslarni topadi
// @EnableJpaRepositories -> JpaRepository larni topadi
@SpringBootApplication
public class JpaDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(JpaDemoApplication.class, args);
    }
}
