package com.secure.model;

public class Compare {
    private String userName;
    private String beneficiaryName;
    private String userAccountNumber;
    private String beneficiaryAccountNumber;
    private String userIfscCode;
    private String beneficiaryIfscCode;

    public Compare(String userName, String beneficiaryName, String userAccountNumber, String beneficiaryAccountNumber, String userIfscCode, String beneficiaryIfscCode) {
        this.userName = userName;
        this.beneficiaryName = beneficiaryName;
        this.userAccountNumber = userAccountNumber;
        this.beneficiaryAccountNumber = beneficiaryAccountNumber;
        this.userIfscCode = userIfscCode;
        this.beneficiaryIfscCode = beneficiaryIfscCode;

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getUserAccountNumber() {
        return userAccountNumber;
    }

    public void setUserAccountNumber(String userAccountNumber) {
        this.userAccountNumber = userAccountNumber;
    }

    public String getBeneficiaryAccountNumber() {
        return beneficiaryAccountNumber;
    }

    public void setBeneficiaryAccountNumber(String beneficiaryAccountNumber) {
        this.beneficiaryAccountNumber = beneficiaryAccountNumber;
    }

    public String getUserIfscCode() {
        return userIfscCode;
    }

    public void setUserIfscCode(String userIfscCode) {
        this.userIfscCode = userIfscCode;
    }

    public String getBeneficiaryIfscCode() {
        return beneficiaryIfscCode;
    }

    public void setBeneficiaryIfscCode(String beneficiaryIfscCode) {
        this.beneficiaryIfscCode = beneficiaryIfscCode;
    }
}
