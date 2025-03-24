
package com.secure.operations;

import com.secure.model.Beneficiary;
import com.secure.model.Transaction;
import com.secure.model.TransactionSummary;
import com.secure.repository.AccountRepository;
import com.secure.repository.BeneficiaryRepository;
import com.secure.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.secure.model.Account;

@Service
public class TransactionOperations {
    private final AccountRepository accountRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final TransactionRepository transactionRepository;

    private static final Map<String, Object> accountLocks = new ConcurrentHashMap<>();

    public TransactionOperations(AccountRepository accountRepository, BeneficiaryRepository beneficiaryRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Map<String, Object> addTransaction(Integer senderId, Integer beneficiaryId, String receiverAccountNumber,
                                              BigDecimal amountTransferred, String ifscCode, String userBank, String description) {
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
                    System.out.println("✅ Transaction Recorded: " + transaction);

                    return Map.of("status", true, "message", "Transaction successful", "transactionId", transaction.getTransactionId());
                } catch (Exception e) {
                    System.out.println("❌ Error saving transaction: " + e.getMessage());
                    e.printStackTrace();
                    return Map.of("status", false, "message", "Transaction failed: " + e.getMessage());
                }
            }
        }
    }

    public Map<String, Object> getTransactionsByUserId(Integer userId) {
        System.out.println("🔹 Fetching transactions for User ID: " + userId);

        List<Transaction> sentTransactions = transactionRepository.findBySenderId(userId);
        List<Transaction> receivedTransactions = transactionRepository.findByReceiverId(userId);


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
                    txn.getAmountTransferred(), // Credited amount
                    (null),
                    txn.getCurrentBalanceReceiver()// No debited amount
            ));
        }

        // Sort transactions by timestamp (latest first)
        allTransactions = allTransactions.stream()
                .sorted(Comparator.comparing(TransactionSummary::getTimestamp).reversed())
                .collect(Collectors.toList());

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("status", true);
        response.put("transactions", allTransactions);

        return response;
    }
}


