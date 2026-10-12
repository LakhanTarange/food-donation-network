package com.foodshare;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
public class AdminBootstrap implements CommandLineRunner {
    private final AdminRepository admins;
    private final DonorRepository donors;
    private final NgoRepository ngos;
    private final PasswordEncoder encoder;

    @Value("${foodshare.admin.email:}")
    private String email;

    @Value("${foodshare.admin.password:}")
    private String password;

    public AdminBootstrap(
        AdminRepository admins,
        DonorRepository donors,
        NgoRepository ngos,
        PasswordEncoder encoder
    ) {
        this.admins = admins;
        this.donors = donors;
        this.ngos = ngos;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (email.isBlank() || password.isBlank()) return;

        String normalized = email.trim().toLowerCase(Locale.ROOT);

        if (admins.findByEmail(normalized).isPresent()) return;

        if (donors.existsByEmail(normalized)
                || ngos.existsByEmail(normalized)) {
            throw new IllegalStateException(
                "Admin email already belongs to another account");
        }

        if (password.length() < 12 ||
            password.getBytes(
                java.nio.charset.StandardCharsets.UTF_8
            ).length > 72) {
            throw new IllegalStateException(
                "Admin password must contain 12-72 UTF-8 bytes");
        }

        admins.save(new Admin(normalized, encoder.encode(password)));
    }
}
