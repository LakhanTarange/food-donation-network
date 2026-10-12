package com.foodshare;

import java.security.Principal;
import java.util.Locale;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ngo")
public class NgoController {

    private final NgoRepository ngos;
    private final DonorRepository donors;
    private final PasswordEncoder encoder;

    public NgoController(
            NgoRepository ngos,
            DonorRepository donors,
            PasswordEncoder encoder) {
        this.ngos = ngos;
        this.donors = donors;
        this.encoder = encoder;
    }

    public record RegisterRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 64) String password,
        @NotBlank @Pattern(regexp = "[0-9]{10}") String phone,
        @NotBlank String registrationNumber,
        @NotBlank String contactPerson,
        @NotBlank String address
    ) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request) {

        String email = request.email()
                .trim().toLowerCase(Locale.ROOT);

        if (ngos.existsByEmail(email)
                || donors.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "Email already registered"));
        }

        if (ngos.existsByRegistrationNumber(
                request.registrationNumber().trim())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                    "message", "NGO registration number already exists"));
        }

        if (request.password().getBytes(
                java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Password is too long"));
        }

        Ngo ngo = new Ngo(
            request.name().trim(),
            email,
            encoder.encode(request.password()),
            request.phone(),
            request.registrationNumber().trim(),
            request.contactPerson().trim(),
            request.address().trim()
        );

        ngos.save(ngo);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of(
                "message",
                "NGO registration successful. Admin verification pending."
            ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> profile(Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Please login"));
        }

        return ngos.findByEmail(principal.getName())
            .<ResponseEntity<?>>map(ngo -> ResponseEntity.ok(
                Map.of(
                    "id", ngo.getId(),
                    "name", ngo.getName(),
                    "email", ngo.getEmail(),
                    "phone", ngo.getPhone(),
                    "registrationNumber", ngo.getRegistrationNumber(),
                    "contactPerson", ngo.getContactPerson(),
                    "address", ngo.getAddress(),
                    "verified", ngo.isVerified()
                )
            ))
            .orElseGet(() -> ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "NGO account required")));
    }
}
