package com.secure.controller;

import com.secure.model.Admin;
import com.secure.services.AdminService;
import com.secure.utils.ApplicationCache;
import com.secure.utils.EmailProvider;
import com.secure.utils.OtpProvider;
import com.secure.utils.TemplateProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final EmailProvider emailProvider;
    private final OtpProvider otpProvider;
    private final AdminService adminService;
    private final TemplateProvider templateProvider;

    public AdminController(EmailProvider emailProvider,
                           OtpProvider otpProvider,
                           AdminService adminService,
                           TemplateProvider templateProvider) {
        this.emailProvider = emailProvider;
        this.otpProvider = otpProvider;
        this.adminService = adminService;
        this.templateProvider = templateProvider;
    }

    // Authenticates admin using email and password, sets JWT token in response
    @PostMapping("/login")
    public ResponseEntity<Map<String,Object>> login(@RequestBody Map<String, String> credentials, HttpServletResponse response) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        return adminService.authenticateAdmin(email, password, response);
    }

    // Verifies MPIN for authenticated admin using token
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

        if (email == null || mpin == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Email and MPIN are required",
                    "success", false
            ));
        }

        return adminService.verifyMpin(email, mpin, authToken);
    }


    // Retrieves admin details by ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String,Object>> getAdminById(@PathVariable Integer id) {
        return adminService.getAdminById(id);
    }

    // Retrieves transaction statistics grouped by bank
    @GetMapping("/transactions/stats")
    public ResponseEntity<List<Map<String, Object>>> getTransactionStatsByBank() {
        List<Map<String, Object>> stats = adminService.getTransactionStatsByBank();
        return ResponseEntity.ok(stats);
    }

    // Returns all transactions flagged as fraud
    @GetMapping("/transactions/fraud")
    public ResponseEntity<List<Map<String, Object>>> getFraudTransactions() {
        return ResponseEntity.ok(adminService.getFraudTransactions());
    }

    // Returns all transactions flagged as suspicious
    @GetMapping("/transactions/suspicious")
    public ResponseEntity<List<Map<String, Object>>> getSuspiciousTransactions() {
        return ResponseEntity.ok(adminService.getSuspiciousTransactions());
    }

    // Returns the most recent transactions for a specific bank
    @GetMapping("/transactions/bank/{bankName}/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecentBankTransactions(@PathVariable String bankName) {
        return ResponseEntity.ok(adminService.getRecentBankTransactions(bankName.toUpperCase()));
    }

    // Returns all transactions for a specific bank
    @GetMapping("/transactions/bank/{bankName}/all")
    public ResponseEntity<List<Map<String, Object>>> getAllBankTransactions(@PathVariable String bankName) {
        return ResponseEntity.ok(adminService.getAllBankTransactions(bankName.toUpperCase()));
    }


    // Updates admin MPIN
    @PutMapping("/update-mpin")
    public ResponseEntity<Map<String,Object>> updateMpin(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newMpin = request.get("mpin");
        return adminService.updateMpin(email, newMpin);
    }

    // Retrieves latest transactions across all banks
    @GetMapping("/transactions/latest")
    public ResponseEntity<List<Map<String, Object>>> getLatestTransactions() {
        return ResponseEntity.ok(adminService.getLatestTransactions());
    }

    // Retrieves all latest transactions (expanded scope)
    @GetMapping("/transactions/all-latest")
    public ResponseEntity<List<Map<String, Object>>> getAllLatestTransactions() {
        return ResponseEntity.ok(adminService.getAllLatestTransactions());
    }

    // Retrieves detailed information for a specific transaction
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<Object> getTransactionDetails(@PathVariable Integer transactionId) {
        return adminService.getTransactionDetails(transactionId);
    }

    // Returns a list of all blocked users
    @GetMapping("/blocked-users")
    public ResponseEntity<List<Map<String, Object>>> getBlockedUsers() {
        return adminService.getBlockedUsers();
    }

    // Deletes a blocked user by ID
    @DeleteMapping("/blocked-users/{id}")
    public ResponseEntity<Map<String,Object>> deleteBlockedUser(@PathVariable int id) {
        return adminService.deleteBlockedUser(id);
    }

    // Clears the admin_token cookie to log out the admin
    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("admin_token")) {
                    ApplicationCache.remove(cookie.getValue());
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                    break;
                }
            }
        }
    }

    // Sends OTP to the admin's email for verification purposes
    @PostMapping("/sendotp")
    public Map<String, Object> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isEmpty()) {
            return Map.of("status", false, "message", "Email is required to send OTP.");
        }

        String otp = otpProvider.generateOtp(email);

        String subject = "Your SecurePulse OTP - Verify Your Identity";
        String messageBody = templateProvider.buildOtpEmailContent(otp);

        emailProvider.sendEmail(email, subject, messageBody);

        return Map.of("status", true, "message", "OTP has been sent successfully to your email.");
    }

    // Verifies the received OTP for the specified purpose
    @PostMapping("/verifyotp")
    public Map<String, Object> verifyOtp(@RequestBody Map<String, String> request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String email = request.get("email");
        String otp = request.get("otp");
        String purpose = request.get("purpose");

        if (email == null || email.isEmpty() || otp == null || otp.isEmpty() || purpose == null || purpose.isEmpty()) {
            return Map.of("otpVerified", false, "message", "Email, OTP, and purpose are required for verification.");
        }

        if (otpProvider.validateOtp(email, otp)) {
            return Map.of("otpVerified", true, "message", "OTP verification was successful!");
        } else {
            return Map.of("otpVerified", false, "message", "Invalid OTP. Please check and try again.");
        }
    }

    // Marks a transaction as normal (not fraudulent)
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

    // Marks a transaction as fraudulent
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
