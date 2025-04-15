package com.secure.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private String dateOfBirth;
    private String aadharCard;
    private String panCard;
    private String password;
    private String mpin;
    private Boolean flag;
    @Column(nullable = false)
    private BigDecimal mpinAmount = new BigDecimal("2000.00");

    public BigDecimal getMpinAmount() {
        return mpinAmount;
    }

    public void setMpinAmount(BigDecimal mpinAmount) {
        this.mpinAmount = mpinAmount;
    }

    // Default Constructor
    public User() {
    }

    // Getters
    public Integer getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getAadharCard() {
        return aadharCard;
    }

    public String getPanCard() {
        return panCard;
    }

    public String getPassword() {
        return password;
    }

    public String getMpin() {
        return mpin;
    }

    public Boolean getFlag() {
        return flag;
    }

    // Setters
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setAadharCard(String aadharCard) {
        this.aadharCard = aadharCard;
    }

    public void setPanCard(String panCard) {
        this.panCard = panCard;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMpin(String mpin) {
        this.mpin = mpin;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }
}