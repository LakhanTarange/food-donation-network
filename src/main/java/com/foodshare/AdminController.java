package com.foodshare;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final NgoRepository ngos;
    private final DonorRepository donors;
    private final DonationRepository donations;

    public AdminController(
        NgoRepository ngos,
        DonorRepository donors,
        DonationRepository donations
    ) {
        this.ngos = ngos;
        this.donors = donors;
        this.donations = donations;
    }

    public record NgoView(
        Long id,
        String name,
        String email,
        String phone,
        String registrationNumber,
        String contactPerson,
        String address,
        boolean verified
    ) {}

    private NgoView view(Ngo ngo) {
        return new NgoView(
            ngo.getId(),
            ngo.getName(),
            ngo.getEmail(),
            ngo.getPhone(),
            ngo.getRegistrationNumber(),
            ngo.getContactPerson(),
            ngo.getAddress(),
            ngo.isVerified()
        );
    }

    @GetMapping("/me")
    public Map<String, String> me(Principal principal) {
        return Map.of(
            "email", principal.getName(),
            "role", "ADMIN"
        );
    }

    @GetMapping("/ngos")
    public List<NgoView> allNgos() {
        return ngos.findAll().stream().map(this::view).toList();
    }

    @PatchMapping("/ngos/{id}/verification")
    public NgoView verify(
        @PathVariable Long id,
        @RequestBody Map<String, Boolean> request
    ) {
        if (request.get("verified") == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "verified field is required"
            );
        }

        Ngo ngo = ngos.findById(id).orElseThrow(() ->
            new ResponseStatusException(
                HttpStatus.NOT_FOUND, "NGO not found"
            )
        );

        ngo.setVerified(request.get("verified"));
        return view(ngos.save(ngo));
    }

    @GetMapping("/stats")
    public Map<String, Long> statistics() {
        return Map.of(
            "totalDonors", donors.count(),
            "totalNgos", ngos.count(),
            "totalDonations", donations.count()
        );
    }
}
