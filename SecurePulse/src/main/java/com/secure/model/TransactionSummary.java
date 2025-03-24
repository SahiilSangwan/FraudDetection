package com.secure.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionSummary {
    private Integer transactionId;
    private String description;
    private LocalDateTime timestamp;
    private BigDecimal creditedAmount;
    private BigDecimal debitedAmount;
    private BigDecimal currentBalance;


    public TransactionSummary(Integer transactionId, String description, LocalDateTime timestamp,
                              BigDecimal creditedAmount, BigDecimal debitedAmount,BigDecimal currentBalance) {
        this.transactionId = transactionId;
        this.description = description;
        this.timestamp = timestamp;
        this.creditedAmount = creditedAmount;
        this.debitedAmount = debitedAmount;
        this.currentBalance = currentBalance;

    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public BigDecimal getCreditedAmount() {
        return creditedAmount;
    }

    public BigDecimal getDebitedAmount() {
        return debitedAmount;
    }

    public BigDecimal getCurrentBalance() {return currentBalance;}

}
