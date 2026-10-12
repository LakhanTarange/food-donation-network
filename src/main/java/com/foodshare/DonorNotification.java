package com.foodshare;

import jakarta.persistence.*;

@Entity
@Table(name = "donor_notifications")
public class DonorNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long donorId;

    @Column(nullable = false)
    private Long donationId;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false, length = 40)
    private String createdAt;


    @Column(nullable = false)
    private boolean isRead = false;

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        this.isRead = read;
    }

    protected DonorNotification() {}

    public DonorNotification(Long donorId, Long donationId,
                             String title, String message,
                             String createdAt) {
        this.donorId = donorId;
        this.donationId = donationId;
        this.title = title;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getDonorId() { return donorId; }
    public Long getDonationId() { return donationId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getCreatedAt() { return createdAt; }
}
