package org.example.springcore.WithJavaConfig;

import org.example.springcore.WithJavaConfig.config.JavaConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class Main {
    static void main() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(JavaConfig.class);
        Store bean = context.getBean(Store.class);
        bean.print();
    }
}