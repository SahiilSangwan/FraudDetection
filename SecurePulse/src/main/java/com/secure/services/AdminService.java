package com.secure.services;


import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.secure.exception.CustomException;
import com.secure.model.*;
import com.secure.repository.*;
import com.secure.utils.ApplicationCache;
import com.secure.utils.EmailProvider;
import com.secure.utils.JwtProvider;
import com.secure.utils.TemplateProvider;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.servlet.http.Cookie;

@Component
public class AdminService {

    private final EmailProvider emailProvider;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AdminRepository adminRepository;
    private final BlockedUserRepository blockedUserRepository;
    private final JwtProvider jwtProvider;
    private final TransactionService transactionService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TemplateProvider templateProvider;

    public AdminService(EmailProvider emailProvider,
                        AccountRepository accountRepository,
                        TransactionRepository transactionRepository,
                        AdminRepository adminRepository,
                        BlockedUserRepository blockedUserRepository,
                        JwtProvider jwtProvider,
                        TransactionService transactionService,
                        UserRepository userRepository,
                        BCryptPasswordEncoder passwordEncoder,
                        TemplateProvider templateProvider) {
        this.emailProvider = emailProvider;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.adminRepository = adminRepository;
        this.blockedUserRepository = blockedUserRepository;
        this.jwtProvider = jwtProvider;
        this.transactionService = transactionService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.templateProvider = templateProvider;
    }

    public ResponseEntity<Map<String,Object>> authenticateAdmin(String email, String password, HttpServletResponse response) {
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);

        if (adminOpt.isEmpty() || !passwordEncoder.matches(password, adminOpt.get().getPassword())) {
            throw new CustomException("Invalid credentials");
        }

        Admin admin = adminOpt.get();
        String token = jwtProvider.generateAdminToken(admin);

        Cookie authCookie = new Cookie("admin_token", token);
        authCookie.setHttpOnly(false);
        authCookie.setSecure(false); // for HTTPS
        authCookie.setPath("/"); // cookie will be available across the entire application
        authCookie.setMaxAge(3600); // 1 hour (in seconds)
        response.addCookie(authCookie);
        ApplicationCache.put(token,true);

        List<BlockedUser> blockedUsers = blockedUserRepository.findAll(Sort.by("createdAt").descending());
        Timestamp twoHoursAgo = new Timestamp(System.currentTimeMillis() - (2 * 60 * 60 * 1000));

        List<BlockedUser> usersToUnblock = blockedUsers.stream()
                .filter(user -> user.getCreatedAt().before(twoHoursAgo))
                .collect(Collectors.toList());

        if (!usersToUnblock.isEmpty()) {
            blockedUserRepository.deleteAll(usersToUnblock);
            System.out.println("🔹 Unblocked " + usersToUnblock.size() + " users who were blocked for more than 2 hours");
        }

        String subject = "Admin Login Detected - SecurePulse Dashboard";
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a, dd MMM yyyy"));

        String messageBody = templateProvider.buildAdminLoginSuccessEmail(timestamp);
        emailProvider.sendEmail(email, subject, messageBody);

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "requireMpin", true,
                "success", true,
                "id", admin.getAdminId(),
                "email", admin.getEmail(),
                "token", token
        ));
    }

    public ResponseEntity<Map<String,Object>> verifyMpin(String email, String mpin, String authToken) {
        try {
            DecodedJWT jwt = jwtProvider.extractClaims(authToken);

            String tokenEmail = jwt.getSubject();
            if (!email.equals(tokenEmail)) {
                throw new CustomException("Invalid token");
            }

            Optional<Admin> adminOpt = adminRepository.findByEmail(email);
            if (adminOpt.isEmpty() || !passwordEncoder.matches(mpin, adminOpt.get().getMpin())) {
                throw new CustomException("Invalid MPIN");
            }

            return ResponseEntity.ok(Map.of(
                    "message", "MPIN verification successful",
                    "success", true
            ));
        } catch (JWTVerificationException e) {
            throw new CustomException("Invalid or expired token");
        }
    }

//    public ResponseEntity<Object> createAdmin(Admin admin) {
//        if (adminRepository.findByEmail(admin.getEmail()).isPresent()) {
//            return ResponseEntity.badRequest().body(Map.of("message", "Email already exists"));
//        }
//
//        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
//        admin.setMpin(passwordEncoder.encode(admin.getMpin()));
//        Admin savedAdmin = adminRepository.save(admin);
//        return ResponseEntity.ok(savedAdmin); // ✅ Admin is returned without type conflict
//    }
//
//    public List<Admin> getAllAdmins() {
//        return adminRepository.findAll();
//    }

    public ResponseEntity<Map<String, Object>> getAdminById(Integer id) {
        Optional<Admin> admin = adminRepository.findById(id);

        if (admin.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "admin", admin.get()
            ));
        } else {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Admin not found"
            ));
        }
    }

    public List<Map<String, Object>> getTransactionStatsByBank() {
        List<Transaction> allTransactions = transactionRepository.findAll();
        Map<String, Map<String, Long>> bankStatsMap = new HashMap<>();

        // Initialize stats for each bank
        String[] banks = {"HERITAGE", "FINOVA", "WISSEN"};
        for (String bank : banks) {
            Map<String, Long> stats = new HashMap<>();
            stats.put("incoming", 0L);
            stats.put("outgoing", 0L);
            stats.put("suspicious", 0L);
            stats.put("failed", 0L);
            stats.put("success", 0L);
            stats.put("fraud", 0L);
            bankStatsMap.put(bank, stats);
        }

        // Process transactions
        for (Transaction transaction : allTransactions) {
            Optional<Account> senderAccount = accountRepository.findByAccountNumber(transaction.getSenderAccountNumber());
            Optional<Account> receiverAccount = accountRepository.findByAccountNumber(transaction.getReceiverAccountNumber());

            senderAccount.ifPresent(sender -> {
                String senderBank = sender.getBank();
                if (bankStatsMap.containsKey(senderBank)) {
                    bankStatsMap.get(senderBank).merge("outgoing", 1L, Long::sum);

                    // Update status counts
                    if (transaction.getFlag() == Transaction.TransactionFlag.COMPLETED) {
                        bankStatsMap.get(senderBank).merge("success", 1L, Long::sum);
                    } else if (transaction.getFlag() == Transaction.TransactionFlag.FAILED) {
                        bankStatsMap.get(senderBank).merge("failed", 1L, Long::sum);
                    }

                    // Update marked counts
                    if (transaction.getMarked() == Transaction.TransactionMarked.SUSPICIOUS) {
                        bankStatsMap.get(senderBank).merge("suspicious", 1L, Long::sum);
                    } else if (transaction.getMarked() == Transaction.TransactionMarked.FRAUD) {
                        bankStatsMap.get(senderBank).merge("fraud", 1L, Long::sum);
                    }
                }
            });

            receiverAccount.ifPresent(receiver -> {
                String receiverBank = receiver.getBank();
                if (bankStatsMap.containsKey(receiverBank)) {
                    bankStatsMap.get(receiverBank).merge("incoming", 1L, Long::sum);
                }
            });
        }

        // Convert to desired format
        return Arrays.stream(banks)
                .map(bank -> {
                    Map<String, Object> bankStats = new LinkedHashMap<>();
                    bankStats.put("name", bank);
                    bankStats.putAll(bankStatsMap.get(bank).entrySet().stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    e -> e.getValue().intValue() // Convert Long to Integer
                            )));
                    return bankStats;
                })
                .collect(Collectors.toList());
    }

    // Returns all transactions marked as fraud
    public List<Map<String, Object>> getFraudTransactions() {
        List<Transaction> fraudTransactions = transactionRepository
                .findByMarked(Transaction.TransactionMarked.FRAUD);

        return fraudTransactions.stream()
                .map(transaction -> {
                    Map<String, Object> transactionDetails = new LinkedHashMap<>();
                    transactionDetails.put("transactionId", transaction.getTransactionId());
                    transactionDetails.put("senderAccountNumber", transaction.getSenderAccountNumber());
                    transactionDetails.put("receiverAccountNumber", transaction.getReceiverAccountNumber());
                    transactionDetails.put("amount", transaction.getAmountTransferred());
                    transactionDetails.put("timestamp", transaction.getTimestamp());
                    transactionDetails.put("description", transaction.getDescription());
                    transactionDetails.put("status", transaction.getFlag());
                    transactionDetails.put("marked", transaction.getMarked());
                    transactionDetails.put("currentBalanceSender", transaction.getCurrentBalanceSender());
                    transactionDetails.put("currentBalanceReceiver", transaction.getCurrentBalanceReceiver());
                    return transactionDetails;
                })
                .collect(Collectors.toList());
    }

    // Returns all transactions marked as suspicious
    public List<Map<String, Object>> getSuspiciousTransactions() {
        List<Transaction> suspiciousTransactions = transactionRepository
                .findByMarked(Transaction.TransactionMarked.SUSPICIOUS);

        return suspiciousTransactions.stream()
                .map(transaction -> {
                    Map<String, Object> transactionDetails = new LinkedHashMap<>();
                    transactionDetails.put("transactionId", transaction.getTransactionId());
                    transactionDetails.put("senderAccountNumber", transaction.getSenderAccountNumber());
                    transactionDetails.put("receiverAccountNumber", transaction.getReceiverAccountNumber());
                    transactionDetails.put("amount", transaction.getAmountTransferred());
                    transactionDetails.put("timestamp", transaction.getTimestamp());
                    transactionDetails.put("description", transaction.getDescription());
                    transactionDetails.put("status", transaction.getFlag());
                    transactionDetails.put("otpAttempts", transaction.getOtpAttempt());
                    transactionDetails.put("marked", transaction.getMarked());
                    transactionDetails.put("currentBalanceSender", transaction.getCurrentBalanceSender());
                    transactionDetails.put("currentBalanceReceiver", transaction.getCurrentBalanceReceiver());
                    return transactionDetails;
                })
                .collect(Collectors.toList());
    }

    // Returns the most recent transactions for a specific bank

    public List<Map<String, Object>> getRecentBankTransactions(String bankName) {
        List<Transaction> transactions = transactionRepository
                .findRecentByBank(bankName, PageRequest.of(0, 10));
        return formatTransactions(transactions);
    }

    // Returns all transactions for a specific bank
    public List<Map<String, Object>> getAllBankTransactions(String bankName) {
        List<Transaction> transactions = transactionRepository
                .findRecentByBank(bankName, PageRequest.of(0, 150));
        return formatTransactions(transactions);
    }


    // Returns all transactions for a specific user
    private List<Map<String, Object>> formatTransactions(List<Transaction> transactions) {
        return transactions.stream()
                .map(transaction -> {
                    Map<String, Object> details = new LinkedHashMap<>();
                    details.put("transactionId", transaction.getTransactionId());
                    details.put("senderAccountNumber", transaction.getSenderAccountNumber());
                    details.put("receiverAccountNumber", transaction.getReceiverAccountNumber());
                    details.put("amount", transaction.getAmountTransferred());
                    details.put("timestamp", transaction.getTimestamp());
                    details.put("status", transaction.getFlag());
                    details.put("description", transaction.getDescription());
                    details.put("currentBalanceSender", transaction.getCurrentBalanceSender());
                    details.put("currentBalanceReceiver", transaction.getCurrentBalanceReceiver());
                    details.put("marked", transaction.getMarked());
                    return details;
                })
                .collect(Collectors.toList());
    }

//    public ResponseEntity<Map<String,Object>> updatePassword(String email, String newPassword) {
//        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
//
//        if (adminOpt.isEmpty()) {
//            throw new CustomException("Admin not found");
//        }
//
//        Admin admin = adminOpt.get();
//        admin.setPassword(passwordEncoder.encode(newPassword));
//        adminRepository.save(admin);
//
//        return ResponseEntity.ok(Map.of(
//                "message", "Password updated successfully",
//                "success", true
//        ));
//    }

    // Updates the MPIN for the admin
    public ResponseEntity<Map<String,Object>> updateMpin(String email, String newMpin) {
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);

        if (adminOpt.isEmpty()) {
            throw new CustomException("Admin not found");
        }

        Admin admin = adminOpt.get();
        admin.setMpin(passwordEncoder.encode(newMpin));
        adminRepository.save(admin);

        String subject = "MPIN Successfully Updated - SecurePulse";
        String timeStamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a, dd MMM yyyy"));

        String messageBody =templateProvider.buildMpinUpdateConfirmationEmail(timeStamp);

        emailProvider.sendEmail(email, subject, messageBody);

        return ResponseEntity.ok(Map.of(
                "message", "MPIN updated successfully",
                "success", true
        ));
    }

    // Returns the most recent transactions
    public List<Map<String, Object>> getLatestTransactions() {
        List<Transaction> transactions = transactionRepository.findAll(
                PageRequest.of(0, 10, Sort.by("timestamp").descending())
        ).getContent();

        return transactions.stream().map(transaction -> {
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("transactionId", transaction.getTransactionId());
            details.put("amount", transaction.getAmountTransferred());
            details.put("timestamp", transaction.getTimestamp());
            details.put("status", transaction.getFlag());
            details.put("description", transaction.getDescription());
            details.put("marked", transaction.getMarked());

            // Fetch sender account and user details
            Optional<Account> senderAccountOpt = accountRepository.findByAccountNumber(transaction.getSenderAccountNumber());
            senderAccountOpt.ifPresent(senderAccount -> {
                details.put("senderAccountNumber", senderAccount.getAccountNumber());
                details.put("senderBank", senderAccount.getBank());
                Optional<User> senderUserOpt = userRepository.findById(senderAccount.getUserId());
                senderUserOpt.ifPresent(senderUser -> {
                    details.put("senderName", senderUser.getFirstName()+" " +senderUser.getLastName());
                });
            });

            // Fetch receiver account and user details
            Optional<Account> receiverAccountOpt = accountRepository.findByAccountNumber(transaction.getReceiverAccountNumber());
            receiverAccountOpt.ifPresent(receiverAccount -> {
                details.put("receiverAccountNumber", receiverAccount.getAccountNumber());
                details.put("receiverBank", receiverAccount.getBank());
                Optional<User> receiverUserOpt = userRepository.findById(receiverAccount.getUserId());
                receiverUserOpt.ifPresent(receiverUser -> {
                    details.put("receiverName", receiverUser.getFirstName()+" " + receiverUser.getLastName());
                });
            });

            return details;
        }).collect(Collectors.toList());
    }


    // Returns all transactions
    public List<Map<String, Object>> getAllLatestTransactions() {
        List<Transaction> transactions = transactionRepository.findAll(
                PageRequest.of(0, 150, Sort.by("timestamp").descending())
        ).getContent();

        return transactions.stream().map(transaction -> {
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("transactionId", transaction.getTransactionId());
            details.put("amount", transaction.getAmountTransferred());
            details.put("timestamp", transaction.getTimestamp());
            details.put("status", transaction.getFlag());
            details.put("description", transaction.getDescription());
            details.put("marked", transaction.getMarked());

            // Fetch sender account and user details
            Optional<Account> senderAccountOpt = accountRepository.findByAccountNumber(transaction.getSenderAccountNumber());
            senderAccountOpt.ifPresent(senderAccount -> {
                details.put("senderAccountNumber", senderAccount.getAccountNumber());
                details.put("senderBank", senderAccount.getBank());
                Optional<User> senderUserOpt = userRepository.findById(senderAccount.getUserId());
                senderUserOpt.ifPresent(senderUser -> {
                    details.put("senderName", senderUser.getFirstName()+" " +senderUser.getLastName());
                });
            });

            // Fetch receiver account and user details
            Optional<Account> receiverAccountOpt = accountRepository.findByAccountNumber(transaction.getReceiverAccountNumber());
            receiverAccountOpt.ifPresent(receiverAccount -> {
                details.put("receiverAccountNumber", receiverAccount.getAccountNumber());
                details.put("receiverBank", receiverAccount.getBank());
                Optional<User> receiverUserOpt = userRepository.findById(receiverAccount.getUserId());
                receiverUserOpt.ifPresent(receiverUser -> {
                    details.put("receiverName", receiverUser.getFirstName()+" " + receiverUser.getLastName());
                });
            });

            return details;
        }).collect(Collectors.toList());
    }

    public ResponseEntity<Object> getTransactionDetails(Integer transactionId) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);

        if (transactionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Transaction transaction = transactionOpt.get();

        Optional<Account> senderAccount = accountRepository.findByAccountNumber(transaction.getSenderAccountNumber());
        Optional<Account> receiverAccount = accountRepository.findByAccountNumber(transaction.getReceiverAccountNumber());

        Optional<User> senderUser = senderAccount.map(account -> userRepository.findById(account.getUserId())).orElse(Optional.empty());
        Optional<User> receiverUser = receiverAccount.map(account -> userRepository.findById(account.getUserId())).orElse(Optional.empty());

        Map<String, Object> transactionDetails = new LinkedHashMap<>();

        // Basic transaction details
        transactionDetails.put("transactionId", transaction.getTransactionId());
        transactionDetails.put("amountTransferred", transaction.getAmountTransferred());
        transactionDetails.put("timestamp", transaction.getTimestamp());
        transactionDetails.put("status", transaction.getFlag());
        transactionDetails.put("description", transaction.getDescription());
        transactionDetails.put("marked", transaction.getMarked());

        // Sender details
        Map<String, Object> senderDetails = new LinkedHashMap<>();
        senderAccount.ifPresent(sender -> {
            senderDetails.put("accountNumber", sender.getAccountNumber());
            senderDetails.put("ifscCode", sender.getIfscCode());
            senderDetails.put("bank", sender.getBank());
        });
        senderUser.ifPresent(user -> {
            senderDetails.put("firstName", user.getFirstName());
            senderDetails.put("lastName", user.getLastName());
        });
        transactionDetails.put("sender", senderDetails);

        // Receiver details
        Map<String, Object> receiverDetails = new LinkedHashMap<>();
        receiverAccount.ifPresent(receiver -> {
            receiverDetails.put("accountNumber", receiver.getAccountNumber());
            receiverDetails.put("ifscCode", receiver.getIfscCode());
            receiverDetails.put("bank", receiver.getBank());

        });
        receiverUser.ifPresent(user -> {
            receiverDetails.put("firstName", user.getFirstName());
            receiverDetails.put("lastName", user.getLastName());
        });
        transactionDetails.put("receiver", receiverDetails);


        return ResponseEntity.ok(transactionDetails);
    }


    // Returns all blocked users
    public ResponseEntity<List<Map<String, Object>>> getBlockedUsers() {
        try {
            List<BlockedUser> blockedUsers = blockedUserRepository.findAll(Sort.by("createdAt").descending());

            List<Map<String, Object>> formattedUsers = blockedUsers.stream()
                    .map(user -> {
                        Map<String, Object> userMap = new LinkedHashMap<>();
                        Optional<User> userOpt = userRepository.findByEmail(user.getEmail());
                        if (userOpt.isEmpty()) {
                            throw new CustomException("User not found for email: " + user.getEmail());
                        }

                        List<Account> accounts = accountRepository.findByUserIdAndBank(userOpt.get().getUserId(), user.getBankName());
                        if (accounts.isEmpty()) {
                            throw new CustomException("No accounts found for user: " + user.getEmail());
                        }

                        User userData = userOpt.get();
                        userMap.put("name", userData.getFirstName() + " " + userData.getLastName());
                        userMap.put("id", user.getId());
                        userMap.put("accountNumber", accounts.get(0).getAccountNumber());
                        userMap.put("email", user.getEmail());
                        userMap.put("bankName", user.getBankName());
                        userMap.put("blockedAt", user.getCreatedAt());
                        userMap.put("reason", user.getReason());
                        return userMap;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(formattedUsers);

        } catch (CustomException e) {
            throw e; // Let the global exception handler handle this
        } catch (Exception e) {
            throw new CustomException("Error fetching blocked users");
        }
    }

    // Deletes a blocked user by ID
    public ResponseEntity<Map<String,Object>> deleteBlockedUser(int id) {
        if (!blockedUserRepository.existsById(id)) {
            throw new CustomException("Blocked user not found");
        }

        try {
            String userEmail = blockedUserRepository.findById(id)
                    .orElseThrow(() -> new CustomException("Blocked user not found"))
                    .getEmail();

            blockedUserRepository.deleteById(id);

            String subject = "Account Access Restored - SecurePulse";
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a, dd MMM yyyy"));
            String messageBody = templateProvider.buildAccountRestoredEmail(timestamp);

            emailProvider.sendEmail(userEmail, subject, messageBody);

            return ResponseEntity.ok(Map.of(
                    "message", "User unblocked successfully",
                    "success", true
            ));
        } catch (Exception e) {
            throw new CustomException("Error while unblocking user");
        }
    }

    @Transactional
    public boolean markTransactionAsNormal(Integer transactionId) {
        Optional<Transaction> txnOpt = transactionRepository.findById(transactionId);
        if (txnOpt.isPresent()) {
            Transaction txn = txnOpt.get();
            Account sender = accountRepository.findByAccountNumber(txn.getSenderAccountNumber()).orElse(null);
            Account receiver = accountRepository.findByAccountNumber(txn.getReceiverAccountNumber()).orElse(null);

            if (sender == null || receiver == null) {
                return false;
            }

            Map<String, Object> isDone = transactionService.processTransaction(
                    sender,
                    receiver,
                    txn.getAmountTransferred(),
                    txn.getSenderId(),
                    txn.getReceiverId(),
                    txn.getDescription()
            );

            if (isDone != null && Boolean.TRUE.equals(isDone.get("status"))) {
                transactionRepository.delete(txn);
                try{
                    Thread.sleep(2000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }

                return true;
            }
        }
        return false;
    }

    public boolean markTransactionAsFraud(Integer transactionId) {
        Optional<Transaction> txnOpt = transactionRepository.findById(transactionId);
        if (txnOpt.isPresent()) {
            Transaction txn = txnOpt.get();
            if (txn.getMarked() == Transaction.TransactionMarked.SUSPICIOUS) {
                txn.setMarked(Transaction.TransactionMarked.FRAUD);
                transactionRepository.save(txn);
                return true;
            }
        }
        return false;
    }

}