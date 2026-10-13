package com.foodshare;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/donations")
public class DonationController {
    private final DonationRepository donations;
    private final DonorRepository donors;
    private final NotificationService notifications;
    private final DonorNotificationService donorNotifications;
    private final NgoNotificationService ngoNotifications;
    private final NgoRepository ngos;

    public DonationController(
            DonationRepository donations,
            DonorRepository donors,
            NotificationService notifications,
            DonorNotificationService donorNotifications,
            NgoNotificationService ngoNotifications,
            NgoRepository ngos
    ) {
        this.donations = donations;
        this.donors = donors;
        this.notifications = notifications;
        this.donorNotifications = donorNotifications;
        this.ngoNotifications = ngoNotifications;
        this.ngos = ngos;
    }

    public record DonationRequest(
            String name,
            String phone,
            String food,
            String type,
            String unit,
            Integer quantity,
            String location,
            String deadline,
            String notes
    ) {}

    public record StatusRequest(String status) {}

    public record DonationView(
            String id,
            String name,
            String phone,
            String food,
            String type,
            String unit,
            Integer quantity,
            String location,
            String deadline,
            String notes,
            String status,
            String createdAt,
            String updatedAt,
            String acceptedNgoName,
            String acceptedNgoPhone,
            String acceptedNgoEmail,
            String acceptedNgoContactPerson
    ) {}

    @GetMapping("/mine")
    public List<DonationView> mine(Principal principal) {
        Donor donor = currentDonor(principal);
        return donations.findByDonorIdOrderByCreatedAtDesc(donor.getId())
                .stream().map(d -> view(d, true)).toList();
    }

    public record PublicDonationView(
        String id,
        String food,
        String type,
        String unit,
        Integer quantity,
        String deadline,
        String status
    ) {}

    @GetMapping("/available")
    public List<PublicDonationView> available() {

        return donations
            .findByStatusOrderByCreatedAtDesc("AVAILABLE")
            .stream()
            .map(d -> new PublicDonationView(
                String.valueOf(d.getId()),
                d.getFood(),
                d.getType(),
                d.getUnit(),
                d.getQuantity(),
                d.getDeadline(),
                d.getStatus()
            ))
            .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DonationView create(
            @RequestBody DonationRequest request,
            Principal principal
    ) {
        validate(request);
        Donor donor = currentDonor(principal);
        String now = Instant.now().toString();

        Donation item = new Donation();
        item.setDonor(donor);
        copy(request, item);
        item.setStatus("AVAILABLE");
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        Donation saved = donations.save(item);
        donorNotifications.notifyDonor(
            saved,
            "Thank You for Donating!",
            "Your donation of " + saved.getQuantity() + " " +
            saved.getUnit() + " " + saved.getFood() +
            " has been published. Thank you for helping reduce food waste!"
        );
        ngoNotifications.notifyAvailable(saved);
        notifications.onDonationCreated(saved, donor);
        return view(saved);
    }

    @PutMapping("/{id}")
    public DonationView update(
            @PathVariable Long id,
            @RequestBody DonationRequest request,
            Principal principal
    ) {
        validate(request);
        Donation item = ownedDonation(id, principal);
        if (!"AVAILABLE".equals(item.getStatus())
                && !"EXPIRED".equals(item.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "This donation can no longer be edited");
        }

        copy(request, item);
        item.setUpdatedAt(Instant.now().toString());
        return view(donations.save(item));
    }

    @PatchMapping("/{id}/status")
    public DonationView updateStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest request,
            Principal principal
    ) {
        Donation item = ownedDonation(id, principal);
        String next = request.status();

        boolean allowed =
                "CANCELLED".equals(next)
                && ("AVAILABLE".equals(item.getStatus())
                    || "EXPIRED".equals(item.getStatus()));

        if (!allowed) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "This status change is not allowed");
        }

        item.setStatus(next);
        item.setUpdatedAt(Instant.now().toString());
        return view(donations.save(item));
    }

    private Donor currentDonor(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Please login");
        }
        return donors.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Account not found"));
    }

    private Donation ownedDonation(Long id, Principal principal) {
        Donor donor = currentDonor(principal);
        Donation item = donations.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Donation not found"));
        if (!item.getDonor().getId().equals(donor.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You cannot change this donation");
        }
        return item;
    }

    private void validate(DonationRequest request) {
        if (request == null
                || blank(request.name()) || request.name().length() > 80
                || request.phone() == null
                || !Pattern.matches("[0-9]{10}", request.phone())
                || blank(request.food()) || request.food().length() > 120
                || blank(request.type()) || request.type().length() > 40
                || blank(request.unit()) || request.unit().length() > 30
                || request.quantity() == null
                || request.quantity() < 1 || request.quantity() > 10000
                || blank(request.location()) || request.location().length() > 200
                || blank(request.deadline()) || request.deadline().length() > 40
                || blank(request.notes()) || request.notes().length() > 1000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Please check the donation details");
        }
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void copy(DonationRequest request, Donation item) {
        item.setName(request.name().trim());
        item.setPhone(request.phone().trim());
        item.setFood(request.food().trim());
        item.setType(request.type().trim());
        item.setUnit(request.unit().trim());
        item.setQuantity(request.quantity());
        item.setLocation(request.location().trim());
        item.setDeadline(request.deadline().trim());
        item.setNotes(request.notes().trim());
    }

    private DonationView view(Donation item) {
        return view(item, false);
    }

    private DonationView view(
            Donation item, boolean includeNgoName) {

        String acceptedNgoName = null;
        String acceptedNgoPhone = null;
        String acceptedNgoEmail = null;
        String acceptedNgoContactPerson = null;

        if (includeNgoName &&
            item.getAcceptedNgoId() != null &&
            ("ACCEPTED".equals(item.getStatus()) ||
             "COLLECTED".equals(item.getStatus()))) {

            var acceptedNgo = ngos.findById(
                item.getAcceptedNgoId()
            );

            acceptedNgoName = acceptedNgo
                .map(Ngo::getName)
                .orElse("NGO account unavailable");

            acceptedNgoPhone = acceptedNgo
                .map(Ngo::getPhone)
                .orElse(null);

            acceptedNgoEmail = acceptedNgo
                .map(Ngo::getEmail)
                .orElse(null);

            acceptedNgoContactPerson = acceptedNgo
                .map(Ngo::getContactPerson)
                .orElse(null);
        }

        return new DonationView(
                String.valueOf(item.getId()),
                item.getName(),
                item.getPhone(),
                item.getFood(),
                item.getType(),
                item.getUnit(),
                item.getQuantity(),
                item.getLocation(),
                item.getDeadline(),
                item.getNotes(),
                item.getStatus(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                acceptedNgoName,
                acceptedNgoPhone,
                acceptedNgoEmail,
                acceptedNgoContactPerson
        );
    }
}
