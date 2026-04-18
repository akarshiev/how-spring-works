package org.example.springcore.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationManager {

    private final List<Notification> notifications;

    @Autowired
    public NotificationManager(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public void notifyAll(String message) {
        if (notifications.isEmpty()) {
            System.out.println("Hech qanday servis topilmadi!");
            return;
        }
        notifications.forEach(notification -> notification.send(message));
    }
}