package com.foodshare;

import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class DonorNotificationService {

    private final DonorNotificationRepository repository;

    public DonorNotificationService(
            DonorNotificationRepository repository) {
        this.repository = repository;
    }

    public void notifyDonor(Donation donation,
                            String title,
                            String message) {

        repository.save(new DonorNotification(
            donation.getDonor().getId(),
            donation.getId(),
            title,
            message,
            Instant.now().toString()
        ));
    }
}
