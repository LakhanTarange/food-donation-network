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

    public record NgoNotificationView(
        Long id,
        Long donationId,
        String eventType,
        String title,
        String message,
        String createdAt
    ) {}

    @GetMapping("/mine")
    public List<NgoNotificationView> mine(Principal principal) {

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

        return notifications
            .findByNgoIdOrderByIdDesc(ngo.getId())
            .stream()
            .map(n -> new NgoNotificationView(
                n.getId(),
                n.getDonationId(),
                n.getEventType(),
                n.getTitle(),
                "A food donation update is available. " +
                "Open your NGO Dashboard to view details.",
                n.getCreatedAt()
            ))
            .toList();
    }
}
