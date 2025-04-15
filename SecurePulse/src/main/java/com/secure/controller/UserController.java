package com.secure.controller;



import com.secure.model.BlockedUser;
import com.secure.repository.BlockedUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.secure.model.User;
import com.secure.operations.UserOperations;
import com.secure.services.EmailService;
import com.secure.services.JwtService;
import com.secure.services.OtpService;

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
	 private JwtService jwtService;

     @Autowired
    BlockedUserRepository  blockedUserRepository;

    @Autowired
    private UserOperations dataOperations;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

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
    public Map<String, Object> loginUser(@RequestBody Map<String, String> loginRequest,@RequestParam String bank ,HttpServletResponse response) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        Optional<User> userOptional = dataOperations.getUserByEmailAndPassword(email, password,bank);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Optional<BlockedUser> isBlocked = blockedUserRepository.findByEmailAndBankName(email,bank);
            if (isBlocked.isPresent()) {
                BlockedUser blockedUser = isBlocked.get();

                Instant createdAt = blockedUser.getCreatedAt().toInstant();
                Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);

                if (createdAt.isBefore(oneHourAgo)) {
                    blockedUserRepository.delete(blockedUser);
                } else {

                    return Map.of(
                            "status", false,
                            "message", "User is blocked for malicious activity."
                    );
                }
            }
            String token = jwtService.generateToken(user, bank);

            // Create HttpOnly cookie
            Cookie cookie = new Cookie("auth_token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // Enable if using HTTPS
            cookie.setPath("/");
            cookie.setMaxAge(3600 ); // 1 hour

            response.addCookie(cookie);

            // Return only necessary user fields (excluding password)
            Map<String, Object> userDetails = Map.of(
                    "userId", user.getUserId(),
                    "email", user.getEmail(),
                    "phoneNumber", user.getPhoneNumber(),
                    "name",user.getFirstName()+" "+user.getLastName()
            );

            return Map.of(
                    "status", true,
                    "message", "Login successful. JWT token set in cookie.",
                    "utoken", token,
                    "user", userDetails
            );
        } else {
            return Map.of(
                    "status", false,
                    "message", "Invalid email or password or user with selected bank not exists."
            );
        }
    }

    // 1️⃣ Send OTP to the email
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
        String purpose = request.get("purpose"); // Extracting purpose

        System.out.println("Received Request: " + request);

        if (email == null || email.isEmpty() || otp == null || otp.isEmpty() || purpose == null || purpose.isEmpty()) {
            return Map.of("otpVerified", false, "message", "Email, OTP, and purpose are required for verification.");
        }

        if (otpService.validateOtp(email, otp)) {
            System.out.println("OTP verification successful!");
            if(purpose.toLowerCase().equals("login")) {

                // Generate JWT token with isVerified = true
                String otpToken = jwtService.generateOtpToken(email); // Implement this method

                // Store OTP token in HttpOnly cookie
                Cookie cookie = new Cookie("otp_token", otpToken);
                cookie.setHttpOnly(true);
                cookie.setSecure(true); // Set to true in production (for HTTPS)
                cookie.setPath("/");
                cookie.setMaxAge(3600 ); // Expiry time (15 minutes)
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
                        "        .header p { color: #ecf0f1; margin: 5px 0 0; font-size: 14px; }" +
                        "        .content { padding: 30px; text-align: center; }" +
                        "        .success-icon { color: #2ecc71; font-size: 48px; margin-bottom: 20px; }" +
                        "        .login-details { background: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0; text-align: left; }" +
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
                        "            <div class='success-icon'>✓</div>" +
                        "            <h2 style='color: #2c3e50;'>Login Successful</h2>" +
                        "            <p>You have successfully accessed your SecurePulse account.</p>" +
                        "            " +
                        "            <div class='login-details'>" +
                        "                <p><strong>Date & Time:</strong> " + new java.util.Date() + "</p>" +
                        "            </div>" +
                        "            " +
                        "            <p>If this was you, no further action is required.</p>" +
                        "            " +
                        "            <div class='divider'></div>" +
                        "            " +
                        "            <a href='' class='button'>Go to My Account</a>" +
                        "            " +
                        "            <div class='note'>" +
                        "                <p>If you didn't perform this login, please secure your account immediately by changing your password and contact our support team.</p>" +
                        "            </div>" +
                        "        </div>" +
                        "        <div class='footer'>" +
                        "            <p>© 2025 SecurePulse by WISSEN Technology. All rights reserved.</p>" +
                        "            <p>123 Embassey Tech Park, Bengaluru | support@securepulse.com</p>" +
                        "        </div>" +
                        "    </div>" +
                        "</body>" +
                        "</html>";

                emailService.sendEmail(email, subject, messageBody);



                return Map.of("otpVerified", true, "message", "OTP verification was successful!", "otp_token", otpToken);
            }

            return Map.of("otpVerified", true, "message", "OTP verification was successful!");
        } else {
            System.out.println("Invalid OTP for email: " + email);
            return Map.of("otpVerified", false, "message", "Invalid OTP. Please check and try again.");
        }
    }






    @PutMapping("/reset-password")
    public Map<String, Object> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("newPassword");

        if (email == null || email.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            return Map.of("status", false, "message", "Email and new password are required.");
        }

        // Find user by email
        Optional<User> user = dataOperations.getUserByEmail(email);

        if (user.isPresent()) {
            User existingUser = user.get();
            existingUser.setPassword(newPassword); // Directly update password
            dataOperations.updateUser(existingUser.getUserId(), existingUser);

            return Map.of("status", true, "message", "Password reset successful.");
        } else {
            return Map.of("status", false, "message", "User not found.");
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
        String email = request.get("email");
        String mpin = request.get("mpin");

        if (email == null || email.isEmpty() || mpin == null || mpin.isEmpty()) {
            return ResponseEntity.status(200).body(Map.of(
                    "success", false,
                    "message", "Email and MPIN are required"
            ));
        }

        return dataOperations.verifyMpin(email, mpin);
    }


    @PutMapping("/update-mpin-amount")
    public ResponseEntity<?> updateMpinAmount(@RequestBody Map<String, Object> request) {
        try {
            String email = (String) request.get("email");
            BigDecimal mpinAmount = new BigDecimal(request.get("mpinAmount").toString());

            if (email == null || email.isEmpty() || mpinAmount == null) {
                return ResponseEntity.status(200).body(Map.of(
                        "success", false,
                        "message", "Email and MPIN amount are required"
                ));
            }

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
            }

            return ResponseEntity.status(200).body(Map.of(
                    "success", false,
                    "message", "User not found"
            ));

        } catch (NumberFormatException e) {
            return ResponseEntity.status(200).body(Map.of(
                    "success", false,
                    "message", "Invalid MPIN amount format"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error updating MPIN amount"
            ));
        }
    }

    @PostMapping("/get-mpin-amount")
    public int getMpin(@RequestBody Map<String, Object> request) {
        String email= (String) request.get("email");
        System.out.println(email);
        Optional<User> userOpt = dataOperations.getUserByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("MPIN amount: " + user.getMpinAmount());
            return user.getMpinAmount().intValue();
        }
        return 0;
    }

    @PostMapping("/verify-mpin-otp")
    public ResponseEntity<?> verifyMpinOtp(@RequestBody Map<String, Object> request) {
        String email = (String) request.get("email");
        String otp = (String) request.get("otp");
        String mpin = (String) request.get("mpin");

        System.out.println("Email: " + email);
        System.out.println("OTP: " + otp);
        System.out.println("MPIN: " + mpin);

        if(email == null || otp == null || mpin == null) {
            return ResponseEntity.status(200).body(Map.of(
                    "success", false,
                    "message", "Email, OTP, and MPIN are required"
            ));
        }else{
            return dataOperations.verifyMpinOtp(email, otp, mpin);
        }
    }





}

