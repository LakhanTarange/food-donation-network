package com.foodshare;

import jakarta.persistence.*;

@Entity
@Table(name = "donations")
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donor_id", nullable = false)
    private Donor donor;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 10)
    private String phone;

    @Column(nullable = false, length = 120)
    private String food;

    @Column(nullable = false, length = 40)
    private String type;

    @Column(nullable = false, length = 30)
    private String unit;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, length = 200)
    private String location;

    @Column(nullable = false, length = 40)
    private String deadline;

    @Column(nullable = false, length = 1000)
    private String notes;

    private Long acceptedNgoId;

    @Column(nullable = false, length = 20)
    private String status = "AVAILABLE";

    @Column(nullable = false, length = 40)
    private String createdAt;

    @Column(nullable = false, length = 40)
    private String updatedAt;

    protected Donation() {
    }

    public Long getId() { return id; }
    public Donor getDonor() { return donor; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getFood() { return food; }
    public String getType() { return type; }
    public String getUnit() { return unit; }
    public Integer getQuantity() { return quantity; }
    public String getLocation() { return location; }
    public String getDeadline() { return deadline; }
    public String getNotes() { return notes; }
    public Long getAcceptedNgoId() { return acceptedNgoId; }
    public void setAcceptedNgoId(Long id) { acceptedNgoId = id; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    public void setDonor(Donor donor) { this.donor = donor; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setFood(String food) { this.food = food; }
    public void setType(String type) { this.type = type; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setLocation(String location) { this.location = location; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
