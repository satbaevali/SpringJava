package com.example.practice_2.service;

import com.example.practice_2.notify.NotificationSender;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    private final NotificationSender sender;

    public BookingService(@Qualifier("emailNotificationSender") NotificationSender sender) {
        this.sender = sender;
    }

    public void confirmBooking(String details) {
        sender.send("Booking confirmed: " + details);
    }
}