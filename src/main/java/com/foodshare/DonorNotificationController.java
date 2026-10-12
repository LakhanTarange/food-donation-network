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


    private Donor currentDonor(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Please login"
            );
        }

        return donors.findByEmail(principal.getName())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.FORBIDDEN, "Donor account required"
            ));
    }

    @GetMapping("/unread-count")
    public java.util.Map<String, Long> unreadCount(
            Principal principal) {

        Donor donor = currentDonor(principal);

        return java.util.Map.of(
            "count",
            notifications.countByDonorIdAndIsReadFalse(donor.getId())
        );
    }

    @PatchMapping("/{id}/read")
    public java.util.Map<String, String> markAsRead(
            @PathVariable Long id,
            Principal principal) {

        Donor donor = currentDonor(principal);

        DonorNotification item = notifications
            .findByIdAndDonorId(id, donor.getId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Notification not found"
            ));

        item.setRead(true);
        notifications.save(item);

        return java.util.Map.of(
            "message", "Notification marked as read"
        );
    }

    @PatchMapping("/read-all")
    @org.springframework.transaction.annotation.Transactional
    public java.util.Map<String, String> markAllRead(
            Principal principal) {

        Donor donor = currentDonor(principal);

        var items = notifications.findByDonorIdAndIsReadFalse(
            donor.getId()
        );

        for (DonorNotification item : items) {
            item.setRead(true);
        }

        notifications.saveAll(items);

        return java.util.Map.of(
            "message", "All notifications marked as read"
        );
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
