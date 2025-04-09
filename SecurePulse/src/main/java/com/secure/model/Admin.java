
package com.secure.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "admin")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer adminId;

    private String firstName;
    private String lastName;
    private String email;
    private String aadharCard;
    private String panCard;

    @JsonIgnore
    private String password;

    @JsonIgnore
    private String mpin;
    private String Address;
    private String dateOfBirth;
    private String phoneNumber;
    // Stored as encrypted string of integer digits


    // Default constructor
    public Admin() {
    }

    // Getters


    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
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

    // Setters
    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
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
}