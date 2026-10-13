package com.foodshare;

import java.util.Locale;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
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
    private final Environment environment;

    @Value("${foodshare.admin.email:}")
    private String email;

    @Value("${foodshare.admin.password:}")
    private String password;

    @Value("${ADMIN_DEMO_MODE:false}")
    private boolean demoMode;

    @Value("${ADMIN_RESET_PASSWORD_ONCE:false}")
    private boolean resetPasswordOnce;


    public AdminBootstrap(
        AdminRepository admins,
        DonorRepository donors,
        NgoRepository ngos,
        PasswordEncoder encoder,
        Environment environment
    ) {
        this.admins = admins;
        this.donors = donors;
        this.ngos = ngos;
        this.encoder = encoder;
        this.environment = environment;
    }


    @Override
    public void run(String... args) {

        boolean production =
            environment.acceptsProfiles(Profiles.of("prod"));

        if (production && (demoMode || resetPasswordOnce)) {
            throw new IllegalStateException(
                "Demo Admin settings are not allowed in production"
            );
        }

        if (email == null || email.isBlank() ||
            password == null || password.isBlank()) {

            if (production) {
                throw new IllegalStateException(
                    "ADMIN_EMAIL and ADMIN_PASSWORD are required in production"
                );
            }

            return;
        }

        String normalized =
            email.trim().toLowerCase(Locale.ROOT);

        boolean adminDemo =
            !production && demoMode && "admin".equals(normalized);

        int minLength = production
            ? 16
            : (adminDemo ? 10 : 12);

        int bytes = password.getBytes(
            java.nio.charset.StandardCharsets.UTF_8
        ).length;

        if (password.length() < minLength || bytes > 72) {
            throw new IllegalStateException(
                "Invalid admin password length"
            );
        }

        var existing = admins.findByEmail(normalized);

        if (existing.isPresent()) {

            if (resetPasswordOnce) {

                if (!adminDemo) {
                    throw new IllegalStateException(
                        "Password reset is limited to demo Admin"
                    );
                }

                Admin admin = existing.get();

                admin.setPasswordHash(
                    encoder.encode(password)
                );

                admins.save(admin);

                System.out.println(
                    "DEMO ADMIN PASSWORD RESET SUCCESSFUL"
                );
            }

            return;
        }

        if (donors.existsByEmail(normalized)
                || ngos.existsByEmail(normalized)) {
            throw new IllegalStateException(
                "Admin ID already used by another account"
            );
        }

        admins.save(
            new Admin(normalized, encoder.encode(password))
        );

        System.out.println(
            "DEMO ADMIN ACCOUNT CREATED SUCCESSFULLY"
        );
    }
}
