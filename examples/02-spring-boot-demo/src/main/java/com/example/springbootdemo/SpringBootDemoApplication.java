package com.example.springbootdemo;

// @SpringBootApplication = 3 ta annotationni birlashtiradi:
//   1. @Configuration       -> "Bu klass bean config larini oz ichiga oladi"
//   2. @EnableAutoConfiguration -> "classpath dagi dependency larga qarab, avtomatik sozla"
//   3. @ComponentScan        -> "@Service, @Controller, @Component larni top"

// Quyida @SpringBootApplication ni 3 ga ajratib korsatilgan:
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// Yuqoridagi 1 satr = pastdagi 3 satr:
// @Configuration
// @EnableAutoConfiguration
// @ComponentScan(basePackages = "com.example.springbootdemo")
public class SpringBootDemoApplication {

    public static void main(String[] args) {
        // SpringApplication.run() ishga tushganda:
        // 1. ApplicationContext yaratiladi
        // 2. @ComponentScan -> com.example.springbootdemo ichidagi beanlarni topadi
        // 3. @EnableAutoConfiguration -> spring.factories dagi 100+ AutoConfiguration larni tekshiradi
        // 4. @ConditionalOnClass -> faqat classpath da kerakli klass bolsa ishga tushiradi
        // 5. Tomcat server 8080 portda ishga tushadi
        // 6. DispatcherServlet so'rovlarni qabul qilishga tayyor
        SpringApplication.run(SpringBootDemoApplication.class, args);
    }
}
