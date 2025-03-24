package com.secure.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Transaction_ID")
    private Integer transactionId;

    @Column(name = "Sender_ID", nullable = false)
    private Integer senderId;

    @Column(name = "Receiver_ID", nullable = false)
    private Integer receiverId;

    @Column(name = "Current_Balance_Sender", precision = 15, scale = 2, nullable = false)
    private BigDecimal CurrentBalanceSender;

    public BigDecimal getCurrentBalanceReceiver() {
        return CurrentBalanceReceiver;
    }

    public void setCurrentBalanceReceiver(BigDecimal currentBalanceReceiver) {
        CurrentBalanceReceiver = currentBalanceReceiver;
    }

    @Column(name="Current_Balance_Receiver", precision = 15, scale = 2, nullable = false)
    private BigDecimal CurrentBalanceReceiver;

    public BigDecimal getCurrentBalanceSender() {
        return CurrentBalanceSender;
    }

    public void setCurrentBalanceSender(BigDecimal currentBalance) {
        CurrentBalanceSender = currentBalance;
    }

    @Column(name = "Receiver_Account_Number", nullable = false, length = 20)
    private String receiverAccountNumber;

    @Column(name = "Sender_Account_Number", nullable = false, length = 20)
    private String senderAccountNumber;

    @Column(name = "Amount_Transferred", precision = 15, scale = 2, nullable = false)
    private BigDecimal amountTransferred;

    @Column(name = "Description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "Otp_ATTEMPT", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer otpAttempt = 0;

    @Column(name = "Timestamp", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime timestamp = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "Flag", nullable = false)
    private TransactionFlag flag;

    @Enumerated(EnumType.STRING)
    @Column(name = "Marked", nullable = false)
    private TransactionMarked marked = TransactionMarked.NORMAL;

    public enum TransactionFlag {
        PENDING, COMPLETED, FAILED, REJECTED
    }

    public enum TransactionMarked {
        SUSPICIOUS, FRAUD, NORMAL
    }


    // Getters and Setters


    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public Integer getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Integer receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverAccountNumber() {
        return receiverAccountNumber;
    }

    public void setReceiverAccountNumber(String receiverAccountNumber) {
        this.receiverAccountNumber = receiverAccountNumber;
    }

    public String getSenderAccountNumber() {
        return senderAccountNumber;
    }

    public void setSenderAccountNumber(String senderAccountNumber) {
        this.senderAccountNumber = senderAccountNumber;
    }

    public BigDecimal getAmountTransferred() {
        return amountTransferred;
    }

    public void setAmountTransferred(BigDecimal amountTransferred) {
        this.amountTransferred = amountTransferred;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getOtpAttempt() {
        return otpAttempt;
    }

    public void setOtpAttempt(Integer otpAttempt) {
        this.otpAttempt = otpAttempt;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public TransactionFlag getFlag() {
        return flag;
    }

    public void setFlag(TransactionFlag flag) {
        this.flag = flag;
    }

    public TransactionMarked getMarked() {
        return marked;
    }

    public void setMarked(TransactionMarked marked) {
        this.marked = marked;
    }
}
