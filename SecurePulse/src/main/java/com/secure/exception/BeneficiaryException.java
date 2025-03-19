package com.secure.exception; // ✅ Make sure this matches your package structure

public class BeneficiaryException extends RuntimeException {
    public BeneficiaryException(String message) {
        super(message);
    }
}
