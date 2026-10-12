package com.foodshare;

import jakarta.persistence.*;

@Entity
@Table(name = "ngo_notifications",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"ngo_id", "donation_id", "event_type"}
    ))
public class NgoNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ngo_id", nullable = false)
    private Long ngoId;

    @Column(name = "donation_id", nullable = false)
    private Long donationId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private String createdAt;

    protected NgoNotification() {}

    public NgoNotification(Long ngoId, Long donationId,
                           String eventType, String title,
                           String message, String createdAt) {
        this.ngoId = ngoId;
        this.donationId = donationId;
        this.eventType = eventType;
        this.title = title;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getNgoId() { return ngoId; }
    public Long getDonationId() { return donationId; }
    public String getEventType() { return eventType; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getCreatedAt() { return createdAt; }
}
