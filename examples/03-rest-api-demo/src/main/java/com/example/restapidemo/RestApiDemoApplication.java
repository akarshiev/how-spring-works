package com.example.restapidemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication = 3 ta annotation ni birlashtiradi:
//   @Configuration -> bean config borligini bildiradi
//   @EnableAutoConfiguration -> dependency lar boyicha avtomatik sozlash
//   @ComponentScan -> @Service, @Repository, @Controller larni topadi
@SpringBootApplication
public class RestApiDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestApiDemoApplication.class, args);
    }
}
