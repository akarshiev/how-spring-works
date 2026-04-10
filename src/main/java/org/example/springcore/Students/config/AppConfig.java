package org.example.springcore.Students.config;

import org.example.springcore.Students.Student;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
public class AppConfig {

    @Bean
    public List<String> subjects() {
        return Arrays.asList("Matematika", "Fizika", "Tarix");
    }

    @Bean
    public Map<String, Integer> grades() {
        return Map.of(
                "Matematika", 5,
                "Fizika", 4,
                "Tarix", 5
        );
    }

    @Bean
    public Student student() {
        // Bu yerda biz yuqoridagi beanlarni parametr sifatida beramiz
        return new Student("Ali Valiyev", subjects(), grades());
    }
}