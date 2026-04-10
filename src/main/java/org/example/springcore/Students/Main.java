package org.example.springcore.Students;


import org.example.springcore.Students.config.AppConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        Student bean = context.getBean(Student.class);
        bean.print();
    }
}
