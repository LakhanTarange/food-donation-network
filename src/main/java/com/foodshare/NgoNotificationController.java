package com.foodshare;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ngo/notifications")
public class NgoNotificationController {

    private final NgoRepository ngos;
    private final NgoNotificationRepository notifications;

    public NgoNotificationController(
            NgoRepository ngos,
            NgoNotificationRepository notifications) {
        this.ngos = ngos;
        this.notifications = notifications;
    }

    @GetMapping("/mine")
    public List<NgoNotification> mine(Principal principal) {

        if (principal == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Please login");
        }

        Ngo ngo = ngos.findByEmail(principal.getName())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.FORBIDDEN, "NGO account required"));

        if (!ngo.isVerified()) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Admin verification required");
        }

        return notifications.findByNgoIdOrderByIdDesc(
            ngo.getId()
        );
    }
}
