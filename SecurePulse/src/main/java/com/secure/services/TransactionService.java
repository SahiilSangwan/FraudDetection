
package com.secure.services;

import com.secure.exception.CustomException;
import com.secure.model.*;
import com.secure.repository.AccountRepository;
import com.secure.repository.BeneficiaryRepository;
import com.secure.repository.TransactionRepository;
import com.secure.repository.UserRepository;
import com.secure.utils.EmailProvider;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final AccountRepository accountRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final TransactionRepository transactionRepository;
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private EmailProvider emailProvider;

    private static final Map<String, Object> accountLocks = new ConcurrentHashMap<>();

    public TransactionService(AccountRepository accountRepository, BeneficiaryRepository beneficiaryRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Map<String, Object> addTransaction(Integer senderId, Integer beneficiaryId, String receiverAccountNumber,
                                              BigDecimal amountTransferred, String ifscCode, String userBank, String description,Integer otpAttempt) {
        try {
            System.out.println("🔹 Starting Transaction Process...");

            // Validate beneficiary
            Beneficiary beneficiary = validateBeneficiary(senderId, beneficiaryId, amountTransferred);
            if (beneficiary == null) {
                System.out.println("❌ Beneficiary validation failed.");
                return Map.of("status", false, "message", "Invalid beneficiary or limit exceeded");
            }

            // Fetch and validate accounts
            Account senderAccount = fetchSenderAccount(senderId, userBank);
            if (senderAccount == null) {
                System.out.println("❌ Sender account not found.");
                return Map.of("status", false, "message", "Sender account not found");
            }

            if (senderAccount.getBalance().compareTo(amountTransferred) < 0) {
                System.out.println("❌ Insufficient balance in sender's account.");
                return Map.of("status", false, "message", "Insufficient balance");
            }

            Account receiverAccount = fetchReceiverAccount(receiverAccountNumber, ifscCode);
            if (receiverAccount == null) {
                System.out.println("❌ Receiver account not found.");
                return Map.of("status", false, "message", "Receiver account not found");
            }

            System.out.println(otpAttempt);
            if(otpAttempt >=3){
                Transaction transaction = new Transaction();
                transaction.setSenderId(senderId);
                transaction.setReceiverId(receiverAccount.getUserId());
                transaction.setSenderAccountNumber(senderAccount.getAccountNumber());
                transaction.setReceiverAccountNumber(receiverAccount.getAccountNumber());
                transaction.setAmountTransferred(amountTransferred);
                transaction.setDescription(description +"\n" +"Multiple incorrect otp attempts");
                transaction.setFlag(Transaction.TransactionFlag.PENDING);
                transaction.setOtpAttempt(otpAttempt);
                transaction.setMarked(Transaction.TransactionMarked.SUSPICIOUS);
                transaction.setCurrentBalanceSender(senderAccount.getBalance());
                transaction.setCurrentBalanceReceiver(receiverAccount.getBalance());
                transactionRepository.save(transaction);

                return Map.of("status", true, "message", "Transaction suspicious: Multiple incorrect OTP attempts");

            }

            // Process transaction
            return processTransaction(senderAccount, receiverAccount, amountTransferred, senderId, beneficiary.getBeneficiaryUserId(), description);
        } catch (Exception e) {
            System.out.println("❌ Transaction failed: " + e.getMessage());
            e.printStackTrace();
            return Map.of("status", false, "message", "Transaction failed: " + e.getMessage());
        }
    }

    private Beneficiary validateBeneficiary(Integer senderId, Integer receiverId, BigDecimal amountTransferred) {
        System.out.println("🔎 Validating Beneficiary: Sender ID: " + senderId + ", Receiver ID: " + receiverId);
        Optional<Beneficiary> opt = beneficiaryRepository.findById(receiverId);
        if (opt.isPresent()) {
            Beneficiary beneficiary = opt.get();
            System.out.println("✅ Beneficiary Found: " + beneficiary);
            if (amountTransferred.compareTo(beneficiary.getAmount()) <= 0) {
                System.out.println("✅ Beneficiary validation passed.");
                return beneficiary;
            }
        }
        System.out.println("❌ Beneficiary validation failed.");
        return null;
    }

    private Account fetchSenderAccount(Integer senderId, String userBank) {
        System.out.println("🔎 Fetching sender account for User ID: " + senderId + ", Bank: " + userBank);
        List<Account> senderAccounts = accountRepository.findByUserIdAndBank(senderId, userBank);
        if (senderAccounts.isEmpty()) {
            System.out.println("❌ No sender account found.");
            return null;
        }
        Account senderAccount = senderAccounts.get(0);
        System.out.println("✅ Sender Account Found: " + senderAccount);
        return senderAccount;
    }

    private Account fetchReceiverAccount(String accountNumber, String ifscCode) {
        System.out.println("🔎 Fetching receiver account: " + accountNumber + " | IFSC: " + ifscCode);
        Account receiverAccount = accountRepository.findByAccountNumberAndIfscCode(accountNumber, ifscCode);
        if (receiverAccount == null) {
            System.out.println("❌ Receiver account not found.");
        } else {
            System.out.println("✅ Receiver Account Found: " + receiverAccount);
        }
        return receiverAccount;
    }

    @Transactional
    public Map<String, Object> processTransaction(Account sender, Account receiver, BigDecimal amount,
                                                  Integer senderId, Integer receiverId, String description) {
        System.out.println("🔹 Processing Transaction...");


        Object senderLock = accountLocks.computeIfAbsent(sender.getAccountNumber(), key -> new Object());
        Object receiverLock = accountLocks.computeIfAbsent(receiver.getAccountNumber(), key -> new Object());

        Object firstLock, secondLock;
        if (senderLock.toString().compareTo(receiverLock.toString()) < 0) {
            firstLock = senderLock;
            secondLock = receiverLock;
        } else {
            firstLock = receiverLock;
            secondLock = senderLock;
        }

        synchronized (firstLock) {
            synchronized (secondLock) {
                System.out.println("🔹 Acquired Locks, Performing Transaction...");

                // Double-check sender's balance within the lock
                if (sender.getBalance().compareTo(amount) < 0) {
                    System.out.println("❌ Insufficient balance after locking.");
                    return Map.of("status", false, "message", "Insufficient balance after locking");
                }

                // Deduct from sender, credit to receiver
                sender.setBalance(sender.getBalance().subtract(amount));
                receiver.setBalance(receiver.getBalance().add(amount));

                try {
                    System.out.println("💾 Saving Updated Account Balances...");
                    accountRepository.save(sender);
                    accountRepository.save(receiver);
                    System.out.println("✅ Account Balances Updated!");


                    // Create transaction record
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
                    System.out.println(transaction);
                    System.out.println("✅ Transaction Recorded: " + transaction);

                    Optional<User> user=userRepository.findById(senderId);
                    Optional<User> user2=userRepository.findById(receiverId);
                    String senderEmail=user.get().getEmail();
                    String recieverEmail=user2.get().getEmail();

                    String senderSubject = "Account Debited - Transaction Alert";

                    String senderMessageBody = "<!DOCTYPE html>" +
                            "<html>" +
                            "<head>" +
                            "    <style>" +
                            "        body { font-family: 'Arial', sans-serif; background-color: #f5f7fa; margin: 0; padding: 0; }" +
                            "        .container { max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 8px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }" +
                            "        .header { background-color: #2c3e50; padding: 20px; border-radius: 8px 8px 0 0; text-align: center; }" +
                            "        .header h1 { color: #ffffff; margin: 0; }" +
                            "        .header p { color: #ecf0f1; margin: 5px 0 0; font-size: 14px; }" +
                            "        .content { padding: 30px; text-align: center; }" +
                            "        .transaction-icon { color: #e74c3c; font-size: 48px; margin-bottom: 20px; }" +
                            "        .details-box { background: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0; text-align: left; }" +
                            "        .details-box p { margin: 10px 0; }" +
                            "        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; border-radius: 0 0 8px 8px; font-size: 12px; color: #7f8c8d; }" +
                            "        .button { background-color: #3498db; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 15px 0; font-weight: bold; }" +
                            "        .note { color: #e74c3c; font-size: 13px; margin-top: 20px; font-style: italic; }" +
                            "        .divider { border-top: 1px solid #eee; margin: 25px 0; }" +
                            "    </style>" +
                            "</head>" +
                            "<body>" +
                            "    <div class='container'>" +
                            "        <div class='header'>" +
                            "            <h1>SecurePulse</h1>" +
                            "            <p>by WISSEN Technology</p>" +
                            "        </div>" +
                            "        <div class='content'>" +
                            "            <div class='transaction-icon'>↓</div>" +
                            "            <h2 style='color: #2c3e50;'>Account Debit Notification</h2>" +
                            "            " +
                            "            <div class='details-box'>" +
                            "                <p><strong>Transaction Amount:</strong> <span style='color: #e74c3c;'>-₹" + amount + "</span></p>" +
                            "                <p><strong>Date & Time:</strong> " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a, dd MMM yyyy")) + "</p>" +
                            "                <p><strong>Available Balance:</strong> ₹" + sender.getBalance() + "</p>" +
                            "                <p><strong>Transaction ID:</strong> " + transaction.getTransactionId() + "</p>" +
                            "                <p><strong>Recipient:</strong> " + receiver.getAccountNumber() + "</p>" +
                            "            </div>" +
                            "            " +
                            "            <p>If you recognize this transaction, no further action is needed.</p>" +
                            "            " +
                            "            <div class='divider'></div>" +
                            "            " +
                            "            <a href='https://securepulse.com/transactions' class='button'>View Transaction Details</a>" +
                            "            " +
                            "            <div class='note'>" +
                            "                <p>If you did not authorize this transaction, please contact us immediately at <a href='mailto:support@securepulse.com' style='color: #3498db;'>support@securepulse.com</a> or call +1 (800) 123-4567.</p>" +
                            "            </div>" +
                            "        </div>" +
                            "        <div class='footer'>" +
                            "            <p>© 2023 SecurePulse by WISSEN Technology. All rights reserved.</p>" +
                            "            <p>123 Tech Park, Innovation City | support@securepulse.com</p>" +
                            "        </div>" +
                            "    </div>" +
                            "</body>" +
                            "</html>";

                    emailProvider.sendEmail(senderEmail,senderSubject,senderMessageBody);




                    String subject = "Account Credited - Payment Received";

                    String messageBody = "<!DOCTYPE html>" +
                            "<html>" +
                            "<head>" +
                            "    <style>" +
                            "        body { font-family: 'Arial', sans-serif; background-color: #f5f7fa; margin: 0; padding: 0; }" +
                            "        .container { max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 8px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }" +
                            "        .header { background-color: #2c3e50; padding: 20px; border-radius: 8px 8px 0 0; text-align: center; }" +
                            "        .header h1 { color: #ffffff; margin: 0; }" +
                            "        .header p { color: #ecf0f1; margin: 5px 0 0; font-size: 14px; }" +
                            "        .content { padding: 30px; text-align: center; }" +
                            "        .credit-icon { color: #2ecc71; font-size: 48px; margin-bottom: 20px; }" +
                            "        .details-box { background: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0; text-align: left; }" +
                            "        .details-box p { margin: 10px 0; }" +
                            "        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; border-radius: 0 0 8px 8px; font-size: 12px; color: #7f8c8d; }" +
                            "        .button { background-color: #3498db; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 15px 0; font-weight: bold; }" +
                            "        .divider { border-top: 1px solid #eee; margin: 25px 0; }" +
                            "    </style>" +
                            "</head>" +
                            "<body>" +
                            "    <div class='container'>" +
                            "        <div class='header'>" +
                            "            <h1>SecurePulse</h1>" +
                            "            <p>by WISSEN Technology</p>" +
                            "        </div>" +
                            "        <div class='content'>" +
                            "            <div class='credit-icon'>↑</div>" +
                            "            <h2 style='color: #2c3e50;'>Payment Received</h2>" +
                            "            " +
                            "            <div class='details-box'>" +
                            "                <p><strong>Amount Credited:</strong> <span style='color: #2ecc71;'>+₹" + amount + "</span></p>" +
                            "                <p><strong>Date & Time:</strong> " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a, dd MMM yyyy")) + "</p>" +
                            "                <p><strong>Available Balance:</strong> ₹" + receiver.getBalance() + "</p>" +
                            "                <p><strong>Transaction ID:</strong> " + transaction.getTransactionId() + "</p>" +
                            "                <p><strong>Sender:</strong> " + user.get().getFirstName()+" "+user.get().getLastName() + "</p>" +
                            "                <p><strong>Reference:</strong> " + description + "</p>" +
                            "            </div>" +
                            "            " +
                            "            <p>This amount is now available in your account.</p>" +
                            "            " +
                            "            <div class='divider'></div>" +
                            "            " +
                            "            <a href='https://securepulse.com/transactions' class='button'>View Transaction History</a>" +
                            "            " +
                            "            <p style='font-size: 13px; color: #7f8c8d;'>Thank you for using SecurePulse services.</p>" +
                            "        </div>" +
                            "        <div class='footer'>" +
                            "            <p>© 2023 SecurePulse by WISSEN Technology. All rights reserved.</p>" +
                            "            <p>123 Tech Park, Innovation City | support@securepulse.com</p>" +
                            "        </div>" +
                            "    </div>" +
                            "</body>" +
                            "</html>";

                            emailProvider.sendEmail(recieverEmail,subject,messageBody);



                    return Map.of("status", true, "message", "Transaction successful", "transactionId", transaction.getTransactionId());
                } catch (Exception e) {
                    System.out.println("❌ Error saving transaction: " + e.getMessage());
                    e.printStackTrace();
                    throw new CustomException("Transaction failed: " + e.getMessage());
                }
            }
        }
    }



    public Map<String, Object> getTransactionsByUserId(Integer userId) {
        try {
            System.out.println("🔹 Fetching transactions for User ID: " + userId);

            // Get all completed transactions
            List<Transaction> sentTransactions = transactionRepository.findBySenderIdAndFlag(
                    userId, Transaction.TransactionFlag.COMPLETED);
            List<Transaction> receivedTransactions = transactionRepository.findByReceiverIdAndFlag(
                    userId, Transaction.TransactionFlag.COMPLETED);

            List<TransactionSummary> allTransactions = new ArrayList<>();

            // Process sent (debited) transactions
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

            // Process received (credited) transactions
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

            // Sort transactions by timestamp (latest first)
            allTransactions = allTransactions.stream()
                    .sorted(Comparator.comparing(TransactionSummary::getTimestamp).reversed())
                    .collect(Collectors.toList());

            return Map.of(
                    "status", true,
                    "transactions", allTransactions
            );

        } catch (Exception e) {
            System.out.println("❌ Error fetching transactions: " + e.getMessage());
            throw new CustomException("Failed to fetch transactions");
        }
    }
}


