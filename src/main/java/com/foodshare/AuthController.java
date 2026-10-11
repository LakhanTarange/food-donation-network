package com.foodshare;

import java.security.Principal;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final DonorRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            DonorRepository repository,
            PasswordEncoder passwordEncoder
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public record RegistrationRequest(
            @NotBlank @Size(max = 80) String name,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 8, max = 64) String password,
            @NotBlank @Pattern(regexp = "[0-9]{10}") String phone,
            @NotBlank @Size(max = 30) String donorType,
            @Size(max = 120) String organisation,
            @NotBlank @Size(max = 300) String address
    ) {
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of(
                "token", token.getToken(),
                "headerName", token.getHeaderName()
        );
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @Valid @RequestBody RegistrationRequest request
    ) {
        Set<String> allowedTypes = Set.of(
                "Individual",
                "Restaurant",
                "Hotel",
                "Catering Service",
                "Organisation"
        );

        if (!allowedTypes.contains(request.donorType())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Select a valid donor type"));
        }

        // BCrypt accepts at most 72 bytes.
        if (request.password().getBytes(
                java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Password is too long"));
        }

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (repository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message", "This email is already registered"));
        }

        Donor donor = new Donor(
                request.name().trim(),
                email,
                passwordEncoder.encode(request.password()),
                request.phone(),
                request.donorType(),
                request.organisation() == null
                        ? "" : request.organisation().trim(),
                request.address().trim()
        );

        try {
            repository.saveAndFlush(donor);
        } catch (DataIntegrityViolationException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message", "This email is already registered"));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "Registration successful. Please login."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> currentDonor(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please login"));
        }

        return repository.findByEmail(principal.getName())
                .<ResponseEntity<?>>map(donor -> ResponseEntity.ok(Map.of(
                        "id", donor.getId(),
                        "name", donor.getName(),
                        "email", donor.getEmail(),
                        "phone", donor.getPhone(),
                        "donorType", donor.getDonorType(),
                        "organisation", donor.getOrganisation(),
                        "address", donor.getAddress()
                )))
                .orElseGet(() -> ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Account not found")));
    }
}