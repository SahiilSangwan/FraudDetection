package com.secure.model;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "BlockedUser")
public class BlockedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; // Corresponding to the `id` column in the table

    @Column(name = "userEmail", nullable = false)
    private String email; // The email field

    @Column(name = "bank_name", nullable = false)
    private String bankName; // Corresponds to the `bank_name` column in the table

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt; // Corresponds to the `created_at` column in the table

    // Default constructor
    public BlockedUser() {}

    // PrePersist lifecycle callback to automatically set createdAt before persisting the entity
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = new Timestamp(System.currentTimeMillis());  // Set current timestamp if not already set
        }
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

}
