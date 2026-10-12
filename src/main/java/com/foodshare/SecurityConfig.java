package com.foodshare;

import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(
            DonorRepository repository,
            NgoRepository ngoRepository,
            AdminRepository adminRepository) {
        return email -> {
            String normalisedEmail =
                    email.trim().toLowerCase(Locale.ROOT);

            var donor = repository.findByEmail(normalisedEmail);

            if (donor.isPresent()) {
                Donor account = donor.get();

                return User.withUsername(account.getEmail())
                        .password(account.getPasswordHash())
                        .roles("DONOR")
                        .build();
            }

            var ngo = ngoRepository.findByEmail(normalisedEmail);
            if (ngo.isPresent()) {
                Ngo account = ngo.get();
                return User.withUsername(account.getEmail())
                        .password(account.getPasswordHash())
                        .roles("NGO")
                        .build();
            }

            Admin admin = adminRepository.findByEmail(normalisedEmail)
                    .orElseThrow(() ->
                            new UsernameNotFoundException(
                                    "Account not found"));

            return User.withUsername(admin.getEmail())
                    .password(admin.getPasswordHash())
                    .roles("ADMIN")
                    .build();
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http.authorizeHttpRequests(authorise -> authorise
                .requestMatchers(
                        "/",
                        "/index.html",
                        "/style.css",
                        "/script.js",
                        "/favicon.ico",
                        "/error",
                        "/api/auth/csrf",
                        "/api/auth/register",
                        "/api/ngo/register",
                        "/api/health",
                        "/api/donations/available"
                ).permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/ngo/donations/**").hasRole("NGO")
                .requestMatchers("/api/ngo/notifications/**").hasRole("NGO")
                .requestMatchers("/api/ngo/me").hasRole("NGO")
                .requestMatchers("/api/notifications/**").hasRole("DONOR")
                .requestMatchers("/api/auth/me").hasRole("DONOR")
                .requestMatchers("/api/donations/**").hasRole("DONOR")
                .anyRequest().authenticated()
        );

        // CSRF protection stays enabled for session-based authentication.
        http.formLogin(login -> login
                .loginPage("/index.html")
                .loginProcessingUrl("/api/auth/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"message\":\"Login successful\"}");
                })
                .failureHandler((request, response, exception) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"message\":\"Invalid email or password\"}");
                })
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"message\":\"Logged out successfully\"}");
                })
        );

        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
        );

        return http.build();
    }
}