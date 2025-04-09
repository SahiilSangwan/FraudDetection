package com.secure.controller;

import com.secure.model.Admin;
import com.secure.operations.AdminOperation;
import com.secure.services.EmailService;
import com.secure.services.OtpService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpService otpService;


    private final AdminOperation adminOperation;

    @Autowired
    public AdminController(AdminOperation adminOperation) {
        this.adminOperation = adminOperation;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, HttpServletResponse response) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        return adminOperation.authenticateAdmin(email, password, response);
    }

    @PostMapping("/verify-mpin")
    public ResponseEntity<?> verifyMpin(
            @RequestBody Map<String, String> request,
            @CookieValue(name = "admin_token", required = false) String authToken,
            HttpServletResponse response) {

        if (authToken == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "message", "Authentication required",
                    "success", false
            ));
        }

        String email = request.get("email");
        String mpin = request.get("mpin");
        System.out.println(email);
        System.out.println(mpin);

        if (email == null || mpin == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Email and MPIN are required",
                    "success", false
            ));
        }

        ResponseEntity<?> verificationResponse = adminOperation.verifyMpin(email, mpin, authToken);

        return verificationResponse;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerAdmin(@RequestBody Admin admin) {
        return adminOperation.createAdmin(admin);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Admin>> getAllAdmins() {
        return ResponseEntity.ok(adminOperation.getAllAdmins());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAdminById(@PathVariable Integer id) {
        return adminOperation.getAdminById(id);
    }

    @GetMapping("/transactions/stats")
    public ResponseEntity<?> getTransactionStatsByBank() {
        List<Map<String, Object>> stats = adminOperation.getTransactionStatsByBank();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/transactions/fraud")
    public ResponseEntity<?> getFraudTransactions() {
        return ResponseEntity.ok(adminOperation.getFraudTransactions());
    }

    @GetMapping("/transactions/suspicious")
    public ResponseEntity<?> getSuspiciousTransactions() {
        return ResponseEntity.ok(adminOperation.getSuspiciousTransactions());
    }

    @GetMapping("/transactions/bank/{bankName}/recent")
    public ResponseEntity<?> getRecentBankTransactions(@PathVariable String bankName) {
        return ResponseEntity.ok(adminOperation.getRecentBankTransactions(bankName.toUpperCase()));
    }

    @GetMapping("/transactions/bank/{bankName}/all")
    public ResponseEntity<?> getAllBankTransactions(@PathVariable String bankName) {
        return ResponseEntity.ok(adminOperation.getAllBankTransactions(bankName.toUpperCase()));
    }

    @PutMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("password");
        return adminOperation.updatePassword(email, newPassword);
    }

    @PutMapping("/update-mpin")
    public ResponseEntity<?> updateMpin(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newMpin = request.get("mpin");
        return adminOperation.updateMpin(email, newMpin);
    }

    @GetMapping("/transactions/latest")
    public ResponseEntity<?> getLatestTransactions() {
        return ResponseEntity.ok(adminOperation.getLatestTransactions());
    }

    @GetMapping("/transactions/all-latest")
    public ResponseEntity<?> getAllLatestTransactions() {
        return ResponseEntity.ok(adminOperation.getAllLatestTransactions());
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<?> getTransactionDetails(@PathVariable Integer transactionId) {
        return adminOperation.getTransactionDetails(transactionId);
    }

    @GetMapping("/blocked-users")
    public ResponseEntity<?> getBlockedUsers() {
        return adminOperation.getBlockedUsers();
    }

    @DeleteMapping("/blocked-users/{id}")
    public ResponseEntity<?> deleteBlockedUser(@PathVariable int id) {
        return adminOperation.deleteBlockedUser(id);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("admin_token")) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                    break;
                }

            }
        }
    }




    @PostMapping("/sendotp")
    public Map<String, Object> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isEmpty()) {
            return Map.of("status", false, "message", "Email is required to send OTP.");
        }

        // Generate OTP
        String otp = otpService.generateOtp(email);

        // Construct OTP message
        String subject = "Your SecurePulse OTP - Verify Your Identity";

        String messageBody = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <style>" +
                "        body { font-family: 'Arial', sans-serif; background-color: #f5f7fa; margin: 0; padding: 0; }" +
                "        .container { max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 8px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }" +
                "        .header { background-color: #2c3e50; padding: 20px; border-radius: 8px 8px 0 0; text-align: center; }" +
                "        .header img { max-width: 180px; }" +
                "        .content { padding: 30px; text-align: center; }" +
                "        .otp-code { font-size: 36px; font-weight: bold; color: #3498db; letter-spacing: 3px; margin: 20px 0; padding: 10px 20px; background: #f0f8ff; display: inline-block; border-radius: 5px; }" +
                "        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; border-radius: 0 0 8px 8px; font-size: 12px; color: #7f8c8d; }" +
                "        .company-info { margin-top: 10px; font-size: 14px; color: #555; }" +
                "        .note { color: #e74c3c; font-size: 13px; margin-top: 20px; font-style: italic; }" +
                "        .divider { border-top: 1px solid #eee; margin: 25px 0; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1 style='color: #ffffff; margin: 0;'>SecurePulse</h1>" +
                "            <p style='color: #ecf0f1; margin: 5px 0 0; font-size: 14px;'>by WISSEN Technology</p>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <h2 style='color: #2c3e50;'>Your One-Time Password</h2>" +
                "            <p>To complete your verification, please enter the following OTP code:</p>" +
                "            <div class='otp-code'>" + otp + "</div>" +
                "            <p>This code will expire in <strong>5 minutes</strong>.</p>" +
                "            <div class='note'>For your security, please do not share this code with anyone.</div>" +
                "            <div class='divider'></div>" +
                "            <p>If you didn't request this OTP, please ignore this email or contact our support team immediately.</p>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>© 2025 SecurePulse by WISSEN Technology. All rights reserved.</p>" +
                "            <div class='company-info'>" +
                "                <p>123 Embassey Tech Park, Bengaluru</p>" +
                "                <p>support@securepulse.com | +91 12389-04567</p>" +
                "            </div>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";

        // Send email with formatted OTP
        emailService.sendEmail(email, subject, messageBody);

        return Map.of("status", true, "message", "OTP has been sent successfully to your email.");
    }

    @PostMapping("/verifyotp")
    public Map<String, Object> verifyOtp(@RequestBody Map<String, String> request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String email = request.get("email");
        String otp = request.get("otp");
        String purpose = request.get("purpose");

        System.out.println("Received Request: " + request);

        if (email == null || email.isEmpty() || otp == null || otp.isEmpty() || purpose == null || purpose.isEmpty()) {
            return Map.of("otpVerified", false, "message", "Email, OTP, and purpose are required for verification.");
        }

        if (otpService.validateOtp(email, otp)) {
            System.out.println("OTP verification successful!");
            return Map.of("otpVerified", true, "message", "OTP verification was successful!");
        } else {
            System.out.println("Invalid OTP for email: " + email);
            return Map.of("otpVerified", false, "message", "Invalid OTP. Please check and try again.");
        }
    }

    @PutMapping("/transactions/{transactionId}/mark-normal")
    public ResponseEntity<?> markTransactionNormal(@PathVariable Integer transactionId) {
        try {
            boolean updated = adminOperation.markTransactionAsNormal(transactionId);
            if (updated) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Transaction marked as normal"
                ));
            }
            return ResponseEntity.status(200).body(Map.of(
                "success", false,
                "message", "Transaction not found or already marked as normal"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error updating transaction status"
            ));
        }
    }

    @PutMapping("/transactions/{transactionId}/mark-fraud")
    public ResponseEntity<?> markTransactionFraud(@PathVariable Integer transactionId) {
        try {
            boolean updated = adminOperation.markTransactionAsFraud(transactionId);
            if (updated) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Transaction marked as fraud"
                ));
            }
            return ResponseEntity.status(200).body(Map.of(
                "success", false,
                "message", "Transaction not found or already marked as fraud"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error updating transaction status"
            ));
        }
    }


}
