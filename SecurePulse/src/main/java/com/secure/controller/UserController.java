package com.secure.controller;



import com.secure.exception.CustomException;
import com.secure.model.BlockedUser;
import com.secure.repository.BlockedUserRepository;
import com.secure.services.*;
import com.secure.utils.DecryptionProvider;
import com.secure.utils.EmailProvider;
import com.secure.utils.JwtProvider;
import com.secure.utils.OtpProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.secure.model.User;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("api/users")
public class UserController {


	 @Autowired
	 private JwtProvider jwtProvider;

     @Autowired
     private   BlockedUserRepository  blockedUserRepository;

    @Autowired
    private UserService dataOperations;

    @Autowired
    private DecryptionProvider Decrpt;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private OtpProvider otpProvider;

    @Autowired
    private EmailProvider emailProvider;

    @GetMapping
    public List<User> getAllUsers() {
        return dataOperations.getAllUsers();
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {



        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if(cookie.getName().equals("admin_token")) {
                    continue;
                }
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0); // Expire the cookie immediately
                response.addCookie(cookie);
            }
        }

    }


    @GetMapping("/{id}")
    public Optional<User> getUserById(@PathVariable Integer id) {
        return dataOperations.getUserById(id);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return dataOperations.createUser(user);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Integer id, @RequestBody User userDetails) {
        return dataOperations.updateUser(id, userDetails);
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Integer id) {
        return dataOperations.deleteUser(id);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> loginRequest, @RequestParam String bank, HttpServletResponse response) {
        try {
            String email = Decrpt.decryptString(loginRequest.get("encryptedEmail"));
            String password = Decrpt.decryptString(loginRequest.get("encryptedPassword"));

            Optional<User> userOptional = dataOperations.getUserByEmailAndPassword(email, password, bank);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                Optional<BlockedUser> isBlocked = blockedUserRepository.findByEmailAndBankName(email, bank);
                if (isBlocked.isPresent()) {
                    BlockedUser blockedUser = isBlocked.get();

                    Instant createdAt = blockedUser.getCreatedAt().toInstant();
                    Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);

                    if (createdAt.isBefore(oneHourAgo)) {
                        blockedUserRepository.delete(blockedUser);
                    } else {
                        return ResponseEntity.status(200).body(Map.of(
                                "status", false,
                                "message", "User is blocked for malicious activity."
                        ));
                    }
                }

                String token = jwtProvider.generateToken(user, bank);
                // Create HttpOnly cookie
                Cookie cookie = new Cookie("auth_token", token);
                cookie.setHttpOnly(true);
                cookie.setSecure(false); // Enable if using HTTPS
                cookie.setPath("/");
                cookie.setMaxAge(3600); // 1 hour

                response.addCookie(cookie);

                // Return only necessary user fields (excluding password)
                Map<String, Object> userDetails = Map.of(
                        "userId", user.getUserId(),
                        "email", user.getEmail(),
                        "phoneNumber", user.getPhoneNumber(),
                        "name", user.getFirstName() + " " + user.getLastName()
                );

                return ResponseEntity.ok(Map.of(
                        "status", true,
                        "message", "Login successful. JWT token set in cookie.",
                        "utoken", token,
                        "user", userDetails
                ));
            } else {
                return ResponseEntity.status(200).body(Map.of(
                        "status", false,
                        "message", "Invalid email or password or user with selected bank not exists."
                ));
            }
        } catch (CustomException e) {
            return ResponseEntity.status(200).body(Map.of(
                    "status", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(200).body(Map.of(
                    "status", false,
                    "message", "An unexpected error occurred: " + e.getMessage()
            ));
        }
    }
    // 1️⃣ Send OTP to the email
    @PostMapping("/sendotp")
    public Map<String, Object> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isEmpty()) {
            throw new CustomException("Email is required to send OTP.");
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
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, String> request, HttpServletResponse httpResponse) {
        try {
            String email = Decrpt.decryptString(request.get("encryptedEmail"));
            String otp = Decrpt.decryptString(request.get("encryptedOtp"));
            String purpose = request.get("purpose"); // Extracting purpose

            if (email == null || email.isEmpty() || otp == null || otp.isEmpty() || purpose == null || purpose.isEmpty()) {
                throw new CustomException("Email, OTP, and purpose are required for verification.");
            }

            if (otpProvider.validateOtp(email, otp)) {
                if (purpose.toLowerCase().equals("login")) {
                    // Generate JWT token with isVerified = true
                    String otpToken = jwtProvider.generateOtpToken(email);

                    // Store OTP token in HttpOnly cookie
                    Cookie cookie = new Cookie("otp_token", otpToken);
                    cookie.setHttpOnly(true);
                    cookie.setSecure(true); // Set to true in production (for HTTPS)
                    cookie.setPath("/");
                    cookie.setMaxAge(3600); // Expiry time (15 minutes)
                    httpResponse.addCookie(cookie);

                    String subject = "Successful Login Detected - SecurePulse Account";
                    String messageBody = "<!DOCTYPE html>" +
                            "<html>" +
                            "<head>" +
                            "    <style>" +
                            "        body { font-family: 'Arial', sans-serif; background-color: #f5f7fa; margin: 0; padding: 0; }" +
                            "        .container { max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 8px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }" +
                            "        .header { background-color: #2c3e50; padding: 20px; border-radius: 8px 8px 0 0; text-align: center; }" +
                            "        .header h1 { color: #ffffff; margin: 0; }" +
                            "        .content { padding: 30px; text-align: center; }" +
                            "        .success-icon { color: #2ecc71; font-size: 48px; margin-bottom: 20px; }" +
                            "        .login-details { background: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0; text-align: left; }" +
                            "        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; border-radius: 0 0 8px 8px; font-size: 12px; color: #7f8c8d; }" +
                            "    </style>" +
                            "</head>" +
                            "<body>" +
                            "    <div class='container'>" +
                            "        <div class='header'>" +
                            "            <h1>SecurePulse</h1>" +
                            "        </div>" +
                            "        <div class='content'>" +
                            "            <div class='success-icon'>✓</div>" +
                            "            <h2>Login Successful</h2>" +
                            "            <p>You have successfully accessed your SecurePulse account.</p>" +
                            "        </div>" +
                            "        <div class='footer'>" +
                            "            <p>© 2025 SecurePulse by WISSEN Technology. All rights reserved.</p>" +
                            "        </div>" +
                            "    </div>" +
                            "</body>" +
                            "</html>";

                    emailProvider.sendEmail(email, subject, messageBody);

                    return ResponseEntity.ok(Map.of("otpVerified", true, "message", "OTP verification was successful!", "otp_token", otpToken));
                }

                return ResponseEntity.ok(Map.of("otpVerified", true, "message", "OTP verification was successful!"));
            } else {
                throw new CustomException("Invalid OTP. Please check and try again.");
            }
        } catch (CustomException e) {
            return ResponseEntity.status(200).body(Map.of("otpVerified", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(200).body(Map.of("otpVerified", false, "message", "An unexpected error occurred: " + e.getMessage()));
        }
    }






    @PutMapping("/reset-password")
    public Map<String, Object> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("newPassword");

        if (email == null || email.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            throw new CustomException("Email and new password are required.");
        }

        // Find user by email
        Optional<User> user = dataOperations.getUserByEmail(email);

        if (user.isPresent()) {
            User existingUser = user.get();
            existingUser.setPassword(passwordEncoder.encode(newPassword)); // Directly update password
            dataOperations.updateUser(existingUser.getUserId(), existingUser);

            return Map.of("status", true, "message", "Password reset successful.");
        } else {
           throw new CustomException("User not found.");
        }


    }

    @PutMapping("/set-mpin")
    public ResponseEntity<?> setMpin(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String mpin = request.get("mpin");

        if (email == null || email.isEmpty() || mpin == null || mpin.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email and MPIN are required"
            ));
        }

        return dataOperations.setUserMpin(email, mpin);
    }


    @PutMapping("/update-mpin")
    public ResponseEntity<?> updateMpin(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newMpin = request.get("newMpin");

        if (email == null || newMpin == null) {
            return ResponseEntity.status(200).body(Map.of(
                    "success", false,
                    "message", "Email or  new MPIN are required"
            ));
        }

        return dataOperations.updateUserMpin(email, newMpin);
    }

    @PostMapping("/verify-mpin")
    public ResponseEntity<?> verifyMpin(@RequestBody Map<String, String> request) {
        String email =Decrpt.decryptString(request.get("eEmail"));
        String mpin =Decrpt.decryptString(request.get("eMpin"));

        if (email == null || email.isEmpty() || mpin == null || mpin.isEmpty()) {
            throw new CustomException("Email and MPIN are required");
        }

        return dataOperations.verifyMpin(email, mpin);
    }


    @PutMapping("/update-mpin-amount")
    public ResponseEntity<Map<String,Object>> updateMpinAmount(@RequestBody Map<String, Object> request) {
        try {
            // Extract and validate input
            String email = (String) request.get("email");
            Object mpinAmountObj = request.get("mpinAmount");

            if (email == null || email.isEmpty() || mpinAmountObj == null) {
                throw new CustomException("Email and MPIN amount are required.");
            }

            BigDecimal mpinAmount;
            try {
                mpinAmount = new BigDecimal(mpinAmountObj.toString());
            } catch (NumberFormatException e) {
                throw new CustomException("Invalid MPIN amount format.");
            }

            // Fetch user and update MPIN amount
            Optional<User> userOpt = dataOperations.getUserByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setMpinAmount(mpinAmount);
                User updatedUser = dataOperations.updateUser(user.getUserId(), user);

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "MPIN amount updated successfully",
                        "mpinAmount", updatedUser.getMpinAmount()
                ));
            } else {
                throw new CustomException("User not found.");
            }
        } catch (CustomException e) {
            return ResponseEntity.status(200).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(200).body(Map.of(
                    "success", false,
                    "message", "An unexpected error occurred: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/get-mpin-amount")
    public int getMpin(@RequestBody Map<String, Object> request) {
        String email= (String) request.get("email");
        Optional<User> userOpt = dataOperations.getUserByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.getMpinAmount().intValue();
        }
        return 0;
    }

    @PostMapping("/verify-mpin-otp")
    public ResponseEntity<?> verifyMpinOtp(@RequestBody Map<String, Object> request) {
        String email = Decrpt.decryptString((String) request.get("eEmail"));
        String otp = Decrpt.decryptString((String) request.get("eOtp"));
        String mpin =Decrpt.decryptString((String) request.get("eMpin"));

        System.out.println("Email: " + email);
        System.out.println("OTP: " + otp);
        System.out.println("MPIN: " + mpin);

        if(email == null || otp == null || mpin == null) {
            throw new CustomException("Email, OTP, and MPIN are required");
        }else{
            return dataOperations.verifyMpinOtp(email, otp, mpin);
        }
    }

}

