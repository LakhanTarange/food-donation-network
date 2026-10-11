package com.foodshare;

import jakarta.persistence.*;

@Entity
@Table(name = "donors")
public class Donor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 10)
    private String phone;

    @Column(nullable = false, length = 30)
    private String donorType;

    @Column(length = 120)
    private String organisation;

    @Column(nullable = false, length = 300)
    private String address;

    protected Donor() {
    }

    public Donor(
            String name,
            String email,
            String passwordHash,
            String phone,
            String donorType,
            String organisation,
            String address
    ) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.donorType = donorType;
        this.organisation = organisation;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public String getDonorType() {
        return donorType;
    }

    public String getOrganisation() {
        return organisation;
    }

    public String getAddress() {
        return address;
    }
}