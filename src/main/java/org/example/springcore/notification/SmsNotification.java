package org.example.springcore.notification;

import org.example.springcore.notification.Notification;
import org.springframework.stereotype.Component;

@Component
public class SmsNotification implements Notification {
    @Override
    public void send(String message) {
        System.out.println("SMS yuborildi: " + message);
    }
}