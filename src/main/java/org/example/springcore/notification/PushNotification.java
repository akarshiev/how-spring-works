package org.example.springcore.notification;

import org.springframework.stereotype.Component;

@Component
public class PushNotification implements Notification {
    @Override
    public void send(String message) {
        System.out.println("SMS yuborildi: " + message);
    }
}