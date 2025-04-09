package com.secure.operations;


import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.secure.model.*;
import com.secure.repository.*;
import com.secure.services.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.servlet.http.Cookie;
import com.secure.services.EmailService;

@Component
public class AdminOperation {

    @Autowired
    private EmailService emailService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private BlockedUserRepository blockedUserRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public ResponseEntity<?> authenticateAdmin(String email, String password, HttpServletResponse response) {
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);


        if (adminOpt.isPresent() && passwordEncoder.matches(password, adminOpt.get().getPassword())) {
            Admin admin = adminOpt.get();
            String token = jwtService.generateAdminToken(admin);

            Cookie authCookie = new Cookie("admin_token", token);
            authCookie.setHttpOnly(false);
            authCookie.setSecure(false); // for HTTPS
            authCookie.setPath("/"); // cookie will be available across the entire application
            authCookie.setMaxAge(3600); // 1 hour (in seconds)
            response.addCookie(authCookie);

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
                    "        .success-icon { color: #2ecc71; font-size: 48px; margin-bottom: 20px; }" +
                    "        .login-details { background: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0; text-align: left; }" +
                    "        .login-details p { margin: 10px 0; }" +
                    "        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; border-radius: 0 0 8px 8px; font-size: 12px; color: #7f8c8d; }" +
                    "        .button { background-color: #3498db; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 15px 0; font-weight: bold; }" +
                    "        .security-note { background: #fff8e1; padding: 15px; border-left: 4px solid #ffc107; margin: 20px 0; text-align: left; }" +
                    "    </style>" +
                    "</head>" +
                    "<body>" +
                    "    <div class='container'>" +
                    "        <div class='header'>" +
                    "            <h1>SecurePulse Admin</h1>" +
                    "            <p>by WISSEN Technology</p>" +
                    "        </div>" +
                    "        <div class='content'>" +
                    "            <div class='success-icon'>✓</div>" +
                    "            <h2 style='color: #2c3e50;'>Administrator Login Successful</h2>" +
                    "            " +
                    "            <div class='login-details'>" +
                    "                <p><strong>Login Time:</strong> " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a, dd MMM yyyy")) + "</p>" +
                    "            </div>" +
                    "            " +
                    "            <div class='security-note'>" +
                    "                <p><strong>Security Recommendation:</strong> Always log out from shared devices and enable two-factor authentication for enhanced security.</p>" +
                    "            </div>" +
                    "            " +
                    "            <a href='#' class='button'>View Login Activity</a>" +
                    "            " +
                    "            <p style='font-size: 13px; color: #7f8c8d;'>If this wasn't you, please secure your account immediately.</p>" +
                    "        </div>" +
                    "        <div class='footer'>" +
                    "            <p>© 2025 SecurePulse by WISSEN Technology. All rights reserved.</p>" +
                    "            <p>24/7 Security Team: security@securepulse.com | Emergency: +91 9876543210</p>" +
                    "        </div>" +
                    "    </div>" +
                    "</body>" +
                    "</html>";

            emailService.sendEmail(email, subject, messageBody);

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "requireMpin", true,
                    "success", true,
                    "id", admin.getAdminId(),
                    "email",admin.getEmail(),
                    "token",token
            ));
        }

        return ResponseEntity.status(200).body(Map.of(
                "message", "Invalid credentials",
                "success", false
        ));
    }
    public ResponseEntity<?> verifyMpin(String email, String mpin, String authToken) {
        try {
            DecodedJWT jwt = jwtService.extractClaims(authToken);

            String tokenEmail = jwt.getSubject();
            if (!email.equals(tokenEmail)) {
                return ResponseEntity.status(401).body(Map.of(
                        "message", "Invalid token",
                        "success", false
                ));
            }

            Optional<Admin> adminOpt = adminRepository.findByEmail(email);
            if (adminOpt.isPresent() && passwordEncoder.matches(mpin, adminOpt.get().getMpin())) {
                Admin admin = adminOpt.get();



                return ResponseEntity.ok(Map.of(
                        "message", "MPIN verification successful",
                        "success", true
                ));
            }

            return ResponseEntity.status(200).body(Map.of(
                    "message", "Invalid MPIN",
                    "success", false
            ));

        } catch (JWTVerificationException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "message", "Invalid or expired token",
                    "success", false
            ));
        }
    }

    public ResponseEntity<?> createAdmin(Admin admin) {
        if (adminRepository.findByEmail(admin.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already exists"));
        }

        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setMpin(passwordEncoder.encode(admin.getMpin()));
        Admin savedAdmin = adminRepository.save(admin);
        return ResponseEntity.ok(savedAdmin);
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public ResponseEntity<?> getAdminById(Integer id) {
        Optional<Admin> admin = adminRepository.findById(id);
        return admin.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }





    public List<Map<String, Object>> getTransactionStatsByBank() {
        List<Transaction> allTransactions = transactionRepository.findAll();
        Map<String, Map<String, Long>> bankStatsMap = new HashMap<>();

        // Initialize stats for each bank
        String[] banks = {"HDFC", "ICICI", "SBI"};
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

    public List<Map<String, Object>> getRecentBankTransactions(String bankName) {
        List<Transaction> transactions = transactionRepository
                .findRecentByBank(bankName, PageRequest.of(0, 10));
        return formatTransactions(transactions);
    }

    public List<Map<String, Object>> getAllBankTransactions(String bankName) {
        List<Transaction> transactions = transactionRepository
                .findRecentByBank(bankName, PageRequest.of(0, 150));
        return formatTransactions(transactions);
    }

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

    public ResponseEntity<?> updatePassword(String email, String newPassword) {
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);

        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            admin.setPassword(passwordEncoder.encode(newPassword));
            adminRepository.save(admin);
            return ResponseEntity.ok(Map.of(
                    "message", "Password updated successfully",
                    "success", true
            ));
        }

        return ResponseEntity.badRequest().body(Map.of(
                "message", "Admin not found",
                "success", false
        ));
    }

    public ResponseEntity<?> updateMpin(String email, String newMpin) {
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);

        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            admin.setMpin(passwordEncoder.encode(newMpin));
            adminRepository.save(admin);

            String subject = "MPIN Successfully Updated - SecurePulse";

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
                    "        .success-icon { color: #2ecc71; font-size: 48px; margin-bottom: 20px; }" +
                    "        .details-box { background: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0; text-align: left; }" +
                    "        .details-box p { margin: 10px 0; }" +
                    "        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; border-radius: 0 0 8px 8px; font-size: 12px; color: #7f8c8d; }" +
                    "        .security-note { background: #fff8e1; padding: 15px; border-left: 4px solid #ffc107; margin: 20px 0; text-align: left; }" +
                    "    </style>" +
                    "</head>" +
                    "<body>" +
                    "    <div class='container'>" +
                    "        <div class='header'>" +
                    "            <h1>SecurePulse</h1>" +
                    "            <p>by WISSEN Technology</p>" +
                    "        </div>" +
                    "        <div class='content'>" +
                    "            <div class='success-icon'>✓</div>" +
                    "            <h2 style='color: #2c3e50;'>MPIN Update Confirmation</h2>" +
                    "            " +
                    "            <div class='details-box'>" +
                    "                <p><strong>Status:</strong> <span style='color: #2ecc71;'>Successfully Changed</span></p>" +
                    "                <p><strong>Changed On:</strong> " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a, dd MMM yyyy")) + "</p>" +
                    "            </div>" +
                    "            " +
                    "            <div class='security-note'>" +
                    "                <p><strong>Important Security Note:</strong></p>" +
                    "                <ul style='margin-top: 5px; padding-left: 20px;'>" +
                    "                    <li>Never share your MPIN with anyone</li>" +
                    "                    <li>Change your MPIN regularly for security</li>" +
                    "                    <li>Contact support immediately if you didn't make this change</li>" +
                    "                </ul>" +
                    "            </div>" +
                    "            " +
                    "            <p style='font-size: 13px; color: #7f8c8d;'>This is an automated confirmation. No reply is needed.</p>" +
                    "        </div>" +
                    "        <div class='footer'>" +
                    "            <p>© 2023 SecurePulse by WISSEN Technology. All rights reserved.</p>" +
                    "            <p>24/7 Security Helpline: +91 9876543210 | security@securepulse.com</p>" +
                    "        </div>" +
                    "    </div>" +
                    "</body>" +
                    "</html>";

            emailService.sendEmail(email, subject, messageBody);

            return ResponseEntity.ok(Map.of(
                    "message", "MPIN updated successfully",
                    "success", true
            ));
        }

        return ResponseEntity.badRequest().body(Map.of(
                "message", "Admin not found",
                "success", false
        ));
    }


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

    public ResponseEntity<?> getTransactionDetails(Integer transactionId) {
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

    public ResponseEntity<?> getBlockedUsers() {
        try {
            List<BlockedUser> blockedUsers = blockedUserRepository.findAll(Sort.by("createdAt").descending());

            List<Map<String, Object>> formattedUsers = blockedUsers.stream()
                    .map(user -> {
                        Map<String, Object> userMap = new LinkedHashMap<>();
                        Optional<User> userOpt = userRepository.findByEmail(user.getEmail());
                        List<Account> accounts = accountRepository.findByUserIdAndBank(userOpt.get().getUserId(), user.getBankName());

                        if (userOpt.isPresent() && !accounts.isEmpty()) {
                            User userData = userOpt.get();
                            userMap.put("name", userData.getFirstName() + " " + userData.getLastName());
                            userMap.put("id", user.getId());
                            userMap.put("accountNumber", accounts.get(0).getAccountNumber());
                            userMap.put("email", user.getEmail());
                            userMap.put("bankName", user.getBankName());
                            userMap.put("blockedAt", user.getCreatedAt());
                            userMap.put("reason",user.getReason());
                            return userMap;
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(formattedUsers);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Error fetching blocked users",
                    "success", false
            ));
        }
    }

    public ResponseEntity<?> deleteBlockedUser(int id) {
        if (!blockedUserRepository.existsById(id)) {
            return ResponseEntity.status(200).body(Map.of(
                    "message", "Blocked user not found",
                    "success", false
            ));
        }

        try {
            String userEmail = blockedUserRepository.findById(id).get().getEmail();
            blockedUserRepository.deleteById(id);

            String subject = "Account Access Restored - SecurePulse";

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
                    "        .success-icon { color: #2ecc71; font-size: 48px; margin-bottom: 20px; }" +
                    "        .details-box { background: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0; text-align: left; }" +
                    "        .details-box p { margin: 10px 0; }" +
                    "        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; border-radius: 0 0 8px 8px; font-size: 12px; color: #7f8c8d; }" +
                    "        .button { background-color: #3498db; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 15px 0; font-weight: bold; }" +
                    "        .security-tip { background: #e8f5e9; padding: 15px; border-left: 4px solid #2ecc71; margin: 20px 0; text-align: left; }" +
                    "    </style>" +
                    "</head>" +
                    "<body>" +
                    "    <div class='container'>" +
                    "        <div class='header'>" +
                    "            <h1>SecurePulse</h1>" +
                    "            <p>by WISSEN Technology</p>" +
                    "        </div>" +
                    "        <div class='content'>" +
                    "            <div class='success-icon'>✓</div>" +
                    "            <h2 style='color: #2c3e50;'>Your Account Has Been Restored</h2>" +
                    "            " +
                    "            <div class='details-box'>" +
                    "                <p><strong>Status:</strong> <span style='color: #2ecc71;'>Active</span></p>" +
                    "                <p><strong>Unblocked By:</strong> Administrator</p>" +
                    "                <p><strong>Date/Time:</strong> " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a, dd MMM yyyy")) + "</p>" +
                    "            </div>" +
                    "            " +
                    "            <div class='security-tip'>" +
                    "                <p><strong>✔️ You can now:</strong></p>" +
                    "                <ul style='margin-top: 5px; padding-left: 20px;'>" +
                    "                    <li>Access all account features</li>" +
                    "                    <li>Perform transactions normally</li>" +
                    "                    <li>Log in from your usual devices</li>" +
                    "                </ul>" +
                    "            </div>" +
                    "            " +
                    "            <a href='https://securepulse.com/login' class='button'>Login to Your Account</a>" +
                    "            " +
                    "            <p style='font-size: 13px; color: #7f8c8d;'>If you still experience issues, please contact our support team.</p>" +
                    "        </div>" +
                    "        <div class='footer'>" +
                    "            <p>© 2023 SecurePulse by WISSEN Technology. All rights reserved.</p>" +
                    "            <p>123 Tech Park, Innovation City | support@securepulse.com | +91 9876543210 </p>" +
                    "        </div>" +
                    "    </div>" +
                    "</body>" +
                    "</html>";

            emailService.sendEmail(userEmail, subject, messageBody);

            return ResponseEntity.ok(Map.of(
                    "message", "User unblocked successfully",
                    "success", true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Error while unblocking user",
                    "success", false
            ));
        }
    }

    public boolean markTransactionAsNormal(Integer transactionId) {
        Optional<Transaction> txnOpt = transactionRepository.findById(transactionId);
        if (txnOpt.isPresent()) {
            Transaction txn = txnOpt.get();
            if (txn.getMarked() == Transaction.TransactionMarked.SUSPICIOUS) {
                txn.setFlag(Transaction.TransactionFlag.COMPLETED);
                txn.setMarked(Transaction.TransactionMarked.NORMAL);
                transactionRepository.save(txn);
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