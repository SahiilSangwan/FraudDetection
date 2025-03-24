package com.secure.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "beneficiary")
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer beneficiaryId;

    @Column(name = "user_id", nullable = false)
    private Integer userId; // Foreign Key - Storing User ID instead of User object

    @Column(name = "beneficiary_name", nullable = false, length = 255)
    private String beneficiaryName;

    @Column(name = "beneficiary_bank", nullable = false, length = 20)
    private String beneficiaryBank; // Directly storing the bank name

    @Column(name = "beneficiary_userid", nullable = false)
    private Integer beneficiaryUserId; // Foreign Key - Storing Beneficiary User ID

    @Column(name = "beneficiary_account_number", unique = false, nullable = false, length = 20)
    private String beneficiaryAccountNumber;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount; // Using BigDecimal for financial accuracy

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name="ifsc_code",nullable = false ,length = 20)
    private String ifscCode;

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }



    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getBeneficiaryId() { return beneficiaryId; }
    public void setBeneficiaryId(Integer beneficiaryId) { this.beneficiaryId = beneficiaryId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getBeneficiaryName() { return beneficiaryName; }
    public void setBeneficiaryName(String beneficiaryName) { this.beneficiaryName = beneficiaryName; }

    public String getBeneficiaryBank() { return beneficiaryBank; }
    public void setBeneficiaryBank(String beneficiaryBank) { this.beneficiaryBank = beneficiaryBank; }

    public Integer getBeneficiaryUserId() { return beneficiaryUserId; }
    public void setBeneficiaryUserId(Integer beneficiaryUserId) { this.beneficiaryUserId = beneficiaryUserId; }

    public String getBeneficiaryAccountNumber() { return beneficiaryAccountNumber; }
    public void setBeneficiaryAccountNumber(String beneficiaryAccountNumber) { 
        this.beneficiaryAccountNumber = beneficiaryAccountNumber; 
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
