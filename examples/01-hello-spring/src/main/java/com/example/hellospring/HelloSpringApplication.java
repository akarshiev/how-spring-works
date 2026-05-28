package com.example.hellospring;

// @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
// Springga: "Bu ilonvani ishga tushir, hamma narsani avtomatik sozla"
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HelloSpringApplication {

    // Dastur mana shu yerdan boshlanadi
    public static void main(String[] args) {
        // Spring ni ishga tushiradi:
        // 1. Tomcat server 8080 portda ochiladi
        // 2. @Controller, @Service larni topadi
        // 3. /hello URL ga javob beradigan controller ni yuklaydi
        SpringApplication.run(HelloSpringApplication.class, args);
    }
}
