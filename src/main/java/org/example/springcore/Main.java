package org.example.springcore;

import org.example.springcore.notification.AppConfig;
import org.example.springcore.notification.NotificationManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    static void main() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        NotificationManager manager = context.getBean(NotificationManager.class);
        System.out.println("::::::::: List :::::::::");
        manager.notifyAll("Hello World");
        context.close();
    }
}
