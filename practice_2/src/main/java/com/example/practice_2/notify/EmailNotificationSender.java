package com.example.practice_2.notify;

import org.springframework.stereotype.Component;

@Component
public class EmailNotificationSender implements NotificationSender {

    @Override
    public void send(String message) {
        System.out.println("[EMAIL] " + message);
    }
}