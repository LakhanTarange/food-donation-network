package com.foodshare;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FoodExpiryService {

    private static final ZoneId ZONE =
        ZoneId.of("Asia/Kolkata");

    private static final DateTimeFormatter FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final DonationRepository donations;
    private final DonorNotificationService notifications;

    public FoodExpiryService(
        DonationRepository donations,
        DonorNotificationService notifications
    ) {
        this.donations = donations;
        this.notifications = notifications;
    }

    public static String currentDeadline() {
        return LocalDateTime.now(ZONE).format(FORMAT);
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void expireFood() {

        String nowLocal = currentDeadline();

        List<Donation> expired =
            donations.findByStatusAndDeadlineLessThanEqual(
                "AVAILABLE", nowLocal
            );

        for (Donation donation : expired) {

            int updated = donations.expireDonation(
                donation.getId(),
                Instant.now().toString(),
                nowLocal
            );

            if (updated == 1) {
                notifications.notifyDonor(
                    donation,
                    "Food Donation Expired",
                    "Your food donation of " +
                    donation.getFood() +
                    " has reached its pickup deadline " +
                    "without being accepted. " +
                    "Please do not distribute food " +
                    "without confirming its safety."
                );
            }
        }
    }
}
