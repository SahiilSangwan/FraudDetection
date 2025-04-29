
package com.secure.services;

import com.secure.exception.CustomException;
import com.secure.model.*;
import com.secure.repository.AccountRepository;
import com.secure.repository.BeneficiaryRepository;
import com.secure.repository.TransactionRepository;
import com.secure.repository.UserRepository;
import com.secure.utils.EmailProvider;
import com.secure.utils.TemplateProvider;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    // Repositories and utilities required for transaction operations
    private final AccountRepository accountRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final EmailProvider emailProvider;
    private final TemplateProvider templateProvider;

    // Map to handle concurrency for account locking during transactions
    private static final Map<String, Object> accountLocks = new ConcurrentHashMap<>();

    // Constructor injection for dependencies
    public TransactionService(AccountRepository accountRepository, BeneficiaryRepository beneficiaryRepository, TransactionRepository transactionRepository, UserRepository userRepository, EmailProvider emailProvider, TemplateProvider templateProvider) {
        this.accountRepository = accountRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.emailProvider = emailProvider;
        this.templateProvider = templateProvider;
    }

    /**
     * Adds a new transaction after validating OTP, beneficiary, accounts and balances.
     * If 3 or more incorrect OTP attempts, the transaction is flagged as suspicious and saved as pending.
     */
    @Transactional
    public Map<String, Object> addTransaction(Integer senderId, Integer beneficiaryId, String receiverAccountNumber,
                                              BigDecimal amountTransferred, String ifscCode, String userBank, String description, Integer otpAttempt) {
        try {
            System.out.println("🔹 Starting Transaction Process...");

            // Validate beneficiary and amount limit
            Beneficiary beneficiary = validateBeneficiary(senderId, beneficiaryId, amountTransferred);
            if (beneficiary == null) {
                return Map.of("status", false, "message", "Invalid beneficiary or limit exceeded");
            }

            // Fetch sender's account
            Account senderAccount = fetchSenderAccount(senderId, userBank);
            if (senderAccount == null) {
                return Map.of("status", false, "message", "Sender account not found");
            }

            // Check sender's balance
            if (senderAccount.getBalance().compareTo(amountTransferred) < 0) {
                return Map.of("status", false, "message", "Insufficient balance");
            }

            // Fetch receiver's account
            Account receiverAccount = fetchReceiverAccount(receiverAccountNumber, ifscCode);
            if (receiverAccount == null) {
                return Map.of("status", false, "message", "Receiver account not found");
            }

            // If multiple incorrect OTP attempts, flag as suspicious
            if (otpAttempt >= 3) {
                Transaction transaction = new Transaction();
                transaction.setSenderId(senderId);
                transaction.setReceiverId(receiverAccount.getUserId());
                transaction.setSenderAccountNumber(senderAccount.getAccountNumber());
                transaction.setReceiverAccountNumber(receiverAccount.getAccountNumber());
                transaction.setAmountTransferred(amountTransferred);
                transaction.setDescription(description + "\n" + "Multiple incorrect otp attempts");
                transaction.setFlag(Transaction.TransactionFlag.PENDING);
                transaction.setOtpAttempt(otpAttempt);
                transaction.setMarked(Transaction.TransactionMarked.SUSPICIOUS);
                transaction.setCurrentBalanceSender(senderAccount.getBalance());
                transaction.setCurrentBalanceReceiver(receiverAccount.getBalance());
                transactionRepository.save(transaction);

                return Map.of("status", true, "message", "Transaction suspicious: Multiple incorrect OTP attempts");
            }

            // Process a valid transaction
            return processTransaction(senderAccount, receiverAccount, amountTransferred, senderId, beneficiary.getBeneficiaryUserId(), description);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("status", false, "message", "Transaction failed: " + e.getMessage());
        }
    }

    /**
     * Validates if the beneficiary exists and transaction amount is within allowed limit.
     */
    private Beneficiary validateBeneficiary(Integer senderId, Integer receiverId, BigDecimal amountTransferred) {
        Optional<Beneficiary> opt = beneficiaryRepository.findById(receiverId);
        if (opt.isPresent()) {
            Beneficiary beneficiary = opt.get();
            if (amountTransferred.compareTo(beneficiary.getAmount()) <= 0) {
                return beneficiary;
            }
        }
        return null;
    }

    /**
     * Fetches sender's account based on user ID and bank.
     */
    private Account fetchSenderAccount(Integer senderId, String userBank) {
        List<Account> senderAccounts = accountRepository.findByUserIdAndBank(senderId, userBank);
        return senderAccounts.isEmpty() ? null : senderAccounts.get(0);
    }

    /**
     * Fetches receiver's account using account number and IFSC code.
     */
    private Account fetchReceiverAccount(String accountNumber, String ifscCode) {
        return accountRepository.findByAccountNumberAndIfscCode(accountNumber, ifscCode);
    }

    /**
     * Processes the actual transaction:
     *  - Locks sender and receiver accounts
     *  - Verifies balance
     *  - Updates balances
     *  - Records the transaction
     *  - Sends email notifications to both parties
     */
    @Transactional
    public Map<String, Object> processTransaction(Account sender, Account receiver, BigDecimal amount,
                                                  Integer senderId, Integer receiverId, String description) {
        // Create locks for concurrency handling
        Object senderLock = accountLocks.computeIfAbsent(sender.getAccountNumber(), key -> new Object());
        Object receiverLock = accountLocks.computeIfAbsent(receiver.getAccountNumber(), key -> new Object());

        // Determine locking order to prevent deadlocks
        Object firstLock = senderLock.toString().compareTo(receiverLock.toString()) < 0 ? senderLock : receiverLock;
        Object secondLock = firstLock == senderLock ? receiverLock : senderLock;

        synchronized (firstLock) {
            synchronized (secondLock) {
                // Recheck balance after acquiring lock
                if (sender.getBalance().compareTo(amount) < 0) {
                    return Map.of("status", false, "message", "Insufficient balance after locking");
                }

                // Update balances
                sender.setBalance(sender.getBalance().subtract(amount));
                receiver.setBalance(receiver.getBalance().add(amount));

                try {
                    // Save updated balances
                    accountRepository.save(sender);
                    accountRepository.save(receiver);

                    // Create and save transaction
                    Transaction transaction = new Transaction();
                    transaction.setSenderId(senderId);
                    transaction.setReceiverId(receiverId);
                    transaction.setSenderAccountNumber(sender.getAccountNumber());
                    transaction.setReceiverAccountNumber(receiver.getAccountNumber());
                    transaction.setAmountTransferred(amount);
                    transaction.setDescription(description);
                    transaction.setFlag(Transaction.TransactionFlag.COMPLETED);
                    transaction.setCurrentBalanceSender(sender.getBalance());
                    transaction.setCurrentBalanceReceiver(receiver.getBalance());
                    transactionRepository.save(transaction);

                    // Prepare email details
                    Optional<User> user = userRepository.findById(senderId);
                    Optional<User> user2 = userRepository.findById(receiverId);

                    String senderEmail = user.get().getEmail();
                    String receiverEmail = user2.get().getEmail();
                    String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a, dd MMM yyyy"));

                    // Send debit email to sender
                    String senderMessageBody = templateProvider.buildDebitNotificationEmail(transaction, sender, receiver, timestamp);
                    emailProvider.sendEmail(senderEmail, "Account Debited - Transaction Alert", senderMessageBody);

                    // Send credit email to receiver
                    String receiverMessageBody = templateProvider.buildCreditNotificationEmail(transaction, receiver, user.get(), timestamp, description);
                    emailProvider.sendEmail(receiverEmail, "Account Credited - Payment Received", receiverMessageBody);

                    return Map.of("status", true, "message", "Transaction successful", "transactionId", transaction.getTransactionId());
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new CustomException("Transaction failed: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Retrieves all completed transactions (sent and received) for a specific user.
     * Returns a combined, sorted transaction summary list.
     */
    public Map<String, Object> getTransactionsByUserId(Integer userId) {
        try {
            List<Transaction> sentTransactions = transactionRepository.findBySenderIdAndFlag(
                    userId, Transaction.TransactionFlag.COMPLETED);
            List<Transaction> receivedTransactions = transactionRepository.findByReceiverIdAndFlag(
                    userId, Transaction.TransactionFlag.COMPLETED);

            List<TransactionSummary> allTransactions = new ArrayList<>();

            for (Transaction txn : sentTransactions) {
                allTransactions.add(new TransactionSummary(
                        txn.getTransactionId(),
                        txn.getDescription(),
                        txn.getTimestamp(),
                        null,
                        txn.getAmountTransferred(),
                        txn.getCurrentBalanceSender()
                ));
            }

            for (Transaction txn : receivedTransactions) {
                allTransactions.add(new TransactionSummary(
                        txn.getTransactionId(),
                        txn.getDescription(),
                        txn.getTimestamp(),
                        txn.getAmountTransferred(),
                        null,
                        txn.getCurrentBalanceReceiver()
                ));
            }

            // Sort by most recent
            allTransactions = allTransactions.stream()
                    .sorted(Comparator.comparing(TransactionSummary::getTimestamp).reversed())
                    .collect(Collectors.toList());

            return Map.of("status", true, "transactions", allTransactions);

        } catch (Exception e) {
            throw new CustomException("Failed to fetch transactions");
        }
    }
}
