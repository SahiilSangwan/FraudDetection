package com.secure.controller;

import com.secure.model.Admin;
import com.secure.services.AdminService;
import com.secure.utils.EmailProvider;
import com.secure.utils.OtpProvider;
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
    private EmailProvider emailProvider;

    @Autowired
    private OtpProvider otpProvider;


    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String,Object>> login(@RequestBody Map<String, String> credentials, HttpServletResponse response) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        return adminService.authenticateAdmin(email, password, response);
    }

    @PostMapping("/verify-mpin")
    public ResponseEntity<Map<String,Object>> verifyMpin(
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

        ResponseEntity<Map<String,Object>> verificationResponse = adminService.verifyMpin(email, mpin, authToken);

        return verificationResponse;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> registerAdmin(@RequestBody Admin admin) {
        return adminService.createAdmin(admin);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Admin>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String,Object>> getAdminById(@PathVariable Integer id) {
        return adminService.getAdminById(id);
    }

    @GetMapping("/transactions/stats")
    public ResponseEntity<Object> getTransactionStatsByBank() {
        List<Map<String, Object>> stats = adminService.getTransactionStatsByBank();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/transactions/fraud")
    public ResponseEntity<Object> getFraudTransactions() {
        return ResponseEntity.ok(adminService.getFraudTransactions());
    }

    @GetMapping("/transactions/suspicious")
    public ResponseEntity<Object> getSuspiciousTransactions() {
        return ResponseEntity.ok(adminService.getSuspiciousTransactions());
    }

    @GetMapping("/transactions/bank/{bankName}/recent")
    public ResponseEntity<Object> getRecentBankTransactions(@PathVariable String bankName) {
        return ResponseEntity.ok(adminService.getRecentBankTransactions(bankName.toUpperCase()));
    }

    @GetMapping("/transactions/bank/{bankName}/all")
    public ResponseEntity<Object> getAllBankTransactions(@PathVariable String bankName) {
        return ResponseEntity.ok(adminService.getAllBankTransactions(bankName.toUpperCase()));
    }

    @PutMapping("/update-password")
    public ResponseEntity<Map<String,Object>> updatePassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("password");
        return adminService.updatePassword(email, newPassword);
    }

    @PutMapping("/update-mpin")
    public ResponseEntity<Map<String,Object>> updateMpin(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newMpin = request.get("mpin");
        return adminService.updateMpin(email, newMpin);
    }

    @GetMapping("/transactions/latest")
    public ResponseEntity<Object> getLatestTransactions() {
        return ResponseEntity.ok(adminService.getLatestTransactions());
    }

    @GetMapping("/transactions/all-latest")
    public ResponseEntity<Object> getAllLatestTransactions() {
        return ResponseEntity.ok(adminService.getAllLatestTransactions());
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<Object> getTransactionDetails(@PathVariable Integer transactionId) {
        return adminService.getTransactionDetails(transactionId);
    }

    @GetMapping("/blocked-users")
    public ResponseEntity<Object> getBlockedUsers() {
        return adminService.getBlockedUsers();
    }

    @DeleteMapping("/blocked-users/{id}")
    public ResponseEntity<Map<String,Object>> deleteBlockedUser(@PathVariable int id) {
        return adminService.deleteBlockedUser(id);
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
        String otp = otpProvider.generateOtp(email);

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
        emailProvider.sendEmail(email, subject, messageBody);

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

        if (otpProvider.validateOtp(email, otp)) {
            System.out.println("OTP verification successful!");
            return Map.of("otpVerified", true, "message", "OTP verification was successful!");
        } else {
            System.out.println("Invalid OTP for email: " + email);
            return Map.of("otpVerified", false, "message", "Invalid OTP. Please check and try again.");
        }
    }

    @PutMapping("/transactions/{transactionId}/mark-normal")
    public ResponseEntity<Map<String,Object>> markTransactionNormal(@PathVariable Integer transactionId) {
        try {
            boolean updated = adminService.markTransactionAsNormal(transactionId);
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
    public ResponseEntity<Map<String,Object>> markTransactionFraud(@PathVariable Integer transactionId) {
        try {
            boolean updated = adminService.markTransactionAsFraud(transactionId);
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
