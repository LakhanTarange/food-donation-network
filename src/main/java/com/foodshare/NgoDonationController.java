package com.foodshare;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ngo/donations")
public class NgoDonationController {

    private final NgoRepository ngos;
    private final DonationRepository donations;

    public NgoDonationController(
            NgoRepository ngos,
            DonationRepository donations) {
        this.ngos = ngos;
        this.donations = donations;
    }

    private Ngo currentNgo(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Please login");
        }

        return ngos.findByEmail(principal.getName())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.FORBIDDEN, "NGO account required"));
    }

    private Ngo verifiedNgo(Principal principal) {
        Ngo ngo = currentNgo(principal);

        if (!ngo.isVerified()) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Admin verification required");
        }
        return ngo;
    }

    public record FoodView(
        Long id,
        String food,
        Integer quantity,
        String unit,
        String location,
        String deadline,
        String status,
        String donorName,
        String donorPhone
    ) {}

    private FoodView view(Donation d) {
        return new FoodView(
            d.getId(),
            d.getFood(),
            d.getQuantity(),
            d.getUnit(),
            d.getLocation(),
            d.getDeadline(),
            d.getStatus(),
            d.getName(),
            d.getPhone()
        );
    }

    @GetMapping("/available")
    public List<Map<String, Object>> available(
            Principal principal) {

        verifiedNgo(principal);

        return donations
            .findByStatusOrderByCreatedAtDesc("AVAILABLE")
            .stream()
            .map(d -> Map.<String, Object>of(
                "id", d.getId(),
                "food", d.getFood(),
                "quantity", d.getQuantity(),
                "unit", d.getUnit(),
                "location", d.getLocation(),
                "deadline", d.getDeadline(),
                "status", d.getStatus()
            ))
            .toList();
    }

    @GetMapping("/mine")
    public List<FoodView> mine(Principal principal) {
        Ngo ngo = currentNgo(principal);

        return donations
            .findByAcceptedNgoIdOrderByCreatedAtDesc(ngo.getId())
            .stream()
            .map(this::view)
            .toList();
    }

    @Transactional
    @PostMapping("/{id}/accept")
    public Map<String, String> accept(
            @PathVariable Long id,
            Principal principal) {

        Ngo ngo = verifiedNgo(principal);

        int updated = donations.acceptAvailableDonation(
            id, ngo.getId(), Instant.now().toString()
        );

        if (updated == 0) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Donation unavailable or already accepted");
        }

        return Map.of(
            "message", "Food donation accepted successfully",
            "status", "ACCEPTED"
        );
    }

    @Transactional
    @PostMapping("/{id}/complete")
    public Map<String, String> complete(
            @PathVariable Long id,
            Principal principal) {

        Ngo ngo = verifiedNgo(principal);

        int updated = donations.completePickup(
            id, ngo.getId(), Instant.now().toString()
        );

        if (updated == 0) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Donation not accepted by your NGO");
        }

        return Map.of(
            "message", "Food pickup completed successfully",
            "status", "COLLECTED"
        );
    }
}
