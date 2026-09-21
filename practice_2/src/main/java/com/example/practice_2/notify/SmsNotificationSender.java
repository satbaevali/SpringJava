package com.example.practice_2.notify;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "app.feature",
        name = "mode",
        havingValue = "verbose"
)
public class SmsNotificationSender implements NotificationSender {

    @Override
    public void send(String message) {
        System.out.println("[SMS] " + message);
    }
}