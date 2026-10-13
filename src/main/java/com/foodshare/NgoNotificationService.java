package com.foodshare;

import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class NgoNotificationService {

    private final NgoRepository ngos;
    private final NgoNotificationRepository notifications;

    public NgoNotificationService(
            NgoRepository ngos,
            NgoNotificationRepository notifications) {
        this.ngos = ngos;
        this.notifications = notifications;
    }

    public void notifyAvailable(Donation donation) {

        for (Ngo ngo : ngos.findByVerifiedTrue()) {

            String message =
                "New food available: " + donation.getFood() +
                ". Quantity: " + donation.getQuantity() +
                " " + donation.getUnit() +
                ". Pickup deadline: " + donation.getDeadline();

            notifications.save(new NgoNotification(
                ngo.getId(),
                donation.getId(),
                "FOOD_AVAILABLE",
                "New Food Donation Available",
                message,
                Instant.now().toString()
            ));
        }
    }
}
