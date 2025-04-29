package com.secure.controller;



import com.secure.exception.CustomException;
import com.secure.model.BlockedUser;
import com.secure.repository.BlockedUserRepository;
import com.secure.services.*;
import com.secure.utils.*;
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

/**
 * Controller handling user-related operations like login, registration, OTP verification, MPIN, and password management.
 */
@RestController
@RequestMapping("api/users")
public class UserController {

    private final JwtProvider jwtProvider;
    private final BlockedUserRepository blockedUserRepository;
    private final UserService dataOperations;
    private final DecryptionProvider Decrpt;
    private final OtpProvider otpProvider;
    private final EmailProvider emailProvider;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final TemplateProvider templateProvider;

    public UserController(JwtProvider jwtProvider,
                          BlockedUserRepository blockedUserRepository,
                          UserService dataOperations,
                          DecryptionProvider decrpt,
                          OtpProvider otpProvider,
                          EmailProvider emailProvider,
                          TemplateProvider templateProvider) {
        this.jwtProvider = jwtProvider;
        this.blockedUserRepository = blockedUserRepository;
        this.dataOperations = dataOperations;
        this.Decrpt = decrpt;
        this.otpProvider = otpProvider;
        this.emailProvider = emailProvider;
        this.templateProvider = templateProvider;
    }

    /**
     * Get list of all users.
     */
//    @GetMapping
//    public List<User> getAllUsers() {
//        return dataOperations.getAllUsers();
//    }

    /**
     * Logs out the user by clearing all cookies except 'admin_token'.
     */
    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if(cookie.getName().equals("admin_token")) {
                    continue;
                }
                if(cookie.getName().equals("auth_token") || cookie.getName().equals("otp_token")) {
                    ApplicationCache.remove(cookie.getValue());
                }
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);

            }
        }
    }

    /**
     * Get user by ID.
     */
    @GetMapping("/{id}")
    public Optional<User> getUserById(@PathVariable Integer id) {
        return dataOperations.getUserById(id);
    }

    /**
     * Register new user.
     */
//    @PostMapping
//    public User createUser(@RequestBody User user) {
//        return dataOperations.createUser(user);
//    }

    /**
     * Update user details by ID.
     */
//    @PutMapping("/{id}")
//    public User updateUser(@PathVariable Integer id, @RequestBody User userDetails) {
//        return dataOperations.updateUser(id, userDetails);
//    }

    /**
     * Delete user by ID.
     */
//    @DeleteMapping("/{id}")
//    public String deleteUser(@PathVariable Integer id) {
//        return dataOperations.deleteUser(id);
//    }

    /**
     * Logs in user, checks if blocked, and issues JWT token in HttpOnly cookie.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> loginRequest, @RequestParam String bank, HttpServletResponse response) {
        try {
             String email = Decrpt.decryptString(loginRequest.get("encryptedEmail"));
             String password = Decrpt.decryptString(loginRequest.get("encryptedPassword"));
             System.out.println(password);
            Optional<User> userOptional = dataOperations.getUserByEmailAndPassword(email, password, bank);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // Check if user is blocked
                Optional<BlockedUser> isBlocked = blockedUserRepository.findByEmailAndBankName(email, bank);
                if (isBlocked.isPresent()) {
                    BlockedUser blockedUser = isBlocked.get();
                    Instant createdAt = blockedUser.getCreatedAt().toInstant();
                    Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);

                    if (createdAt.isBefore(oneHourAgo)) {
                        blockedUserRepository.delete(blockedUser);
                    } else {
                        return ResponseEntity.ok(Map.of("status", false, "message", "User is blocked for malicious activity."));
                    }
                }

                // Issue token
                String token = jwtProvider.generateToken(user, bank);
                Cookie cookie = new Cookie("auth_token", token);
                cookie.setHttpOnly(true);
                cookie.setSecure(false);
                cookie.setPath("/");
                cookie.setMaxAge(3600);
                response.addCookie(cookie);

                Map<String, Object> userDetails = Map.of(
                        "userId", user.getUserId(),
                        "email", user.getEmail(),
                        "phoneNumber", user.getPhoneNumber(),
                        "name", user.getFirstName() + " " + user.getLastName()
                );
                ApplicationCache.put(token,true);

                return ResponseEntity.ok(Map.of(
                        "status", true,
                        "message", "Login successful. JWT token set in cookie.",
                        "utoken", token,
                        "user", userDetails
                ));
            } else {
                return ResponseEntity.ok(Map.of("status", false, "message", "Invalid email or password or user with selected bank not exists."));
            }
        } catch (CustomException e) {
            return ResponseEntity.ok(Map.of("status", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("status", false, "message", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    /**
     * Sends OTP to user's email.
     */
    @PostMapping("/sendotp")
    public Map<String, Object> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            throw new CustomException("Email is required to send OTP.");
        }

        String otp = otpProvider.generateOtp(email);
        String subject = "Your SecurePulse OTP - Verify Your Identity";
        String messageBody = templateProvider.buildOtpEmailContent(otp);
        emailProvider.sendEmail(email, subject, messageBody);

        return Map.of("status", true, "message", "OTP has been sent successfully to your email.");
    }

    /**
     * Verifies OTP for login or other actions.
     */
    @PostMapping("/verifyotp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, String> request, HttpServletResponse httpResponse) {
        try {
            String email = Decrpt.decryptString(request.get("encryptedEmail"));
            String otp = Decrpt.decryptString(request.get("encryptedOtp"));
            String purpose = request.get("purpose");

            if (email == null || email.isEmpty() || otp == null || otp.isEmpty() || purpose == null || purpose.isEmpty()) {
                throw new CustomException("Email, OTP, and purpose are required for verification.");
            }

            if (otpProvider.validateOtp(email, otp)) {
                if (purpose.equalsIgnoreCase("login")) {
                    String otpToken = jwtProvider.generateOtpToken(email);
                    Cookie cookie = new Cookie("otp_token", otpToken);
                    cookie.setHttpOnly(true);
                    cookie.setSecure(true);
                    cookie.setPath("/");
                    cookie.setMaxAge(3600);
                    httpResponse.addCookie(cookie);
                    ApplicationCache.put(otpToken, true);

                    emailProvider.sendEmail(email, "Successful Login Detected - SecurePulse Account", templateProvider.buildLoginSuccessEmailContent());

                    return ResponseEntity.ok(Map.of("otpVerified", true, "message", "OTP verification was successful!", "otp_token", otpToken));
                }

                return ResponseEntity.ok(Map.of("otpVerified", true, "message", "OTP verification was successful!"));
            } else {
                throw new CustomException("Invalid OTP. Please check and try again.");
            }
        } catch (CustomException e) {
            return ResponseEntity.ok(Map.of("otpVerified", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("otpVerified", false, "message", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    /**
     * Resets user's password using email.
     */
//    @PutMapping("/reset-password")
//    public Map<String, Object> resetPassword(@RequestBody Map<String, String> request) {
//        String email = request.get("email");
//        String newPassword = request.get("newPassword");
//
//        if (email == null || email.isEmpty() || newPassword == null || newPassword.isEmpty()) {
//            throw new CustomException("Email and new password are required.");
//        }
//
//        Optional<User> user = dataOperations.getUserByEmail(email);
//        if (user.isPresent()) {
//            User existingUser = user.get();
//            existingUser.setPassword(passwordEncoder.encode(newPassword));
//            dataOperations.updateUser(existingUser.getUserId(), existingUser);
//            return Map.of("status", true, "message", "Password reset successful.");
//        } else {
//            throw new CustomException("User not found.");
//        }
//    }

    /**
     * Sets MPIN for the user.
     */
    @PutMapping("/set-mpin")
    public ResponseEntity<?> setMpin(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String mpin = request.get("mpin");

        if (email == null || email.isEmpty() || mpin == null || mpin.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email and MPIN are required"));
        }

        return dataOperations.setUserMpin(email, mpin);
    }

    /**
     * Updates user's MPIN.
     */
    @PutMapping("/update-mpin")
    public ResponseEntity<?> updateMpin(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newMpin = request.get("newMpin");

        if (email == null || newMpin == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Email or new MPIN are required"));
        }

        return dataOperations.updateUserMpin(email, newMpin);
    }

    /**
     * Verifies user's MPIN.
     */
    @PostMapping("/verify-mpin")
    public ResponseEntity<?> verifyMpin(@RequestBody Map<String, String> request) {
        String email = Decrpt.decryptString(request.get("eEmail"));
        String mpin = Decrpt.decryptString(request.get("eMpin"));

        if (email == null || email.isEmpty() || mpin == null || mpin.isEmpty()) {
            throw new CustomException("Email and MPIN are required");
        }

        return dataOperations.verifyMpin(email, mpin);
    }

    /**
     * Updates MPIN amount limit for user.
     */
    @PutMapping("/update-mpin-amount")
    public ResponseEntity<Map<String,Object>> updateMpinAmount(@RequestBody Map<String, Object> request) {
        try {
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
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    /**
     * Gets user's MPIN amount limit.
     */
    @PostMapping("/get-mpin-amount")
    public int getMpin(@RequestBody Map<String, Object> request) {
        String email = (String) request.get("email");
        Optional<User> userOpt = dataOperations.getUserByEmail(email);
        return userOpt.map(user -> user.getMpinAmount().intValue()).orElse(0);
    }

    /**
     * Verifies MPIN and OTP together for sensitive operations.
     */
    @PostMapping("/verify-mpin-otp")
    public ResponseEntity<?> verifyMpinOtp(@RequestBody Map<String, Object> request) {
        String email = Decrpt.decryptString((String) request.get("eEmail"));
        String otp = Decrpt.decryptString((String) request.get("eOtp"));
        String mpin = Decrpt.decryptString((String) request.get("eMpin"));

        if(email == null || otp == null || mpin == null) {
            throw new CustomException("Email, OTP, and MPIN are required");
        } else {
            return dataOperations.verifyMpinOtp(email, otp, mpin);
        }
    }
}



