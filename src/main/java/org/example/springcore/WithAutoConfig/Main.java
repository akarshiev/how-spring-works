package org.example.springcore.WithAutoConfig;

import org.example.springcore.WithAutoConfig.config.AppConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    static void main() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        Store bean = context.getBean(Store.class);
        bean.print();
    }
}
