package com.foodshare;

import jakarta.persistence.*;

@Entity
@Table(name = "ngos")
public class Ngo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 10)
    private String phone;

    @Column(nullable = false, length = 100)
    private String registrationNumber;

    @Column(nullable = false, length = 80)
    private String contactPerson;

    @Column(nullable = false, length = 300)
    private String address;

    @Column(nullable = false)
    private boolean verified = false;

    protected Ngo() {}

    public Ngo(String name, String email, String passwordHash,
               String phone, String registrationNumber,
               String contactPerson, String address) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.registrationNumber = registrationNumber;
        this.contactPerson = contactPerson;
        this.address = address;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getPhone() { return phone; }
    public String getRegistrationNumber() {
        return registrationNumber;
    }
    public String getContactPerson() { return contactPerson; }
    public String getAddress() { return address; }
    public boolean isVerified() { return verified; }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
