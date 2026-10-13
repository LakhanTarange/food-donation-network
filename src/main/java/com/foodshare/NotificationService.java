package com.foodshare;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log =
        LoggerFactory.getLogger(NotificationService.class);

    private final ObjectProvider<JavaMailSender> mailProvider;

    @Value("${foodshare.ngo-notification-emails:}")
    private String ngoEmails;

    @Value("${foodshare.mail.from:}")
    private String fromAddress;

    public NotificationService(
        ObjectProvider<JavaMailSender> mailProvider
    ) {
        this.mailProvider = mailProvider;
    }

    public void onDonationCreated(Donation donation, Donor donor) {

        String details =
            "Donation ID: " + donation.getId() +
            "\\nFood: " + donation.getFood() +
            "\\nQuantity: " + donation.getQuantity() +
            " " + donation.getUnit() +
            "\\nLocation: " + donation.getLocation() +
            "\\nPickup Deadline: " + donation.getDeadline() +
            "\\nStatus: Available";

        String donorMessage =
            "Dear " + donor.getName() + ",\\n\\n" +
            "Thank you for your generous food donation!\\n\\n" +
            "Your donation has been registered successfully.\\n\\n" +
            details + "\\n\\n" +
            "Your kindness helps reduce food waste and " +
            "support people in need.\\n\\n" +
            "An NGO may contact you to arrange collection.\\n\\n" +
            "Share Food, Spread Happiness!\\n" +
            "Team FoodShareConnect";

        send(
            donor.getEmail(),
            "Thank You for Your Food Donation",
            donorMessage
        );

        String ngoMessage =
            "Dear NGO Team,\n\n" +
            "A new food donation is available.\n\n" +
            "Donation ID: " + donation.getId() +
            "\nFood: " + donation.getFood() +
            "\nQuantity: " + donation.getQuantity() +
            " " + donation.getUnit() +
            "\nPickup Deadline: " + donation.getDeadline() +
            "\n\nLog in to the NGO Dashboard to review the donation." +
            "\nDonor contact and pickup address are available " +
            "only after acceptance.\n\n" +
            "Team FoodShareConnect";

        if (ngoEmails == null || ngoEmails.isBlank()) {
            log.info("NGO email list is not configured.");
            return;
        }

        Set<String> recipients = new LinkedHashSet<>(
            Arrays.asList(ngoEmails.split(","))
        );

        for (String email : recipients) {
            email = email.trim();

            if (!email.isBlank()) {
                send(
                    email,
                    "New Food Donation Available - Pickup Request",
                    ngoMessage
                );
            }
        }
    }

    private void send(
        String to,
        String subject,
        String body
    ) {
        JavaMailSender sender = mailProvider.getIfAvailable();

        if (sender == null ||
            fromAddress == null ||
            fromAddress.isBlank()) {

            log.info(
                "Email not sent: SMTP configuration is incomplete."
            );
            return;
        }

        try {
            SimpleMailMessage message =
                new SimpleMailMessage();

            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            sender.send(message);
            log.info("Donation notification email sent.");

        } catch (Exception ex) {
            log.warn(
                "Donation email delivery failed: {}",
                ex.getClass().getSimpleName()
            );
        }
    }
}
