package com.foodshare;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/notifications")
public class DonorNotificationController {

    private final DonorNotificationRepository notifications;
    private final DonorRepository donors;

    public DonorNotificationController(
            DonorNotificationRepository notifications,
            DonorRepository donors) {
        this.notifications = notifications;
        this.donors = donors;
    }

    @GetMapping("/mine")
    public List<DonorNotification> mine(Principal principal) {

        if (principal == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Please login");
        }

        Donor donor = donors.findByEmail(principal.getName())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Donor account required"));

        return notifications.findByDonorIdOrderByIdDesc(
            donor.getId()
        );
    }
}
