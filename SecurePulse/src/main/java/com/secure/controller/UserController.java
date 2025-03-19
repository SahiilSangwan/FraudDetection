package com.secure.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.secure.model.User;
import com.secure.operations.UserOperations;
import com.secure.services.EmailService;
import com.secure.services.JwtService;
import com.secure.services.OtpService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;



import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("api/users")
public class UserController {


	 @Autowired
	 private JwtService jwtService;

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
        // Invalidate the session
        request.getSession().invalidate();

        // Get all cookies and set them to expire
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0); // Expire the cookie immediately
                response.addCookie(cookie);
            }
        }

        // Optionally, clear authentication tokens if using JWT
        response.setHeader("Authorization", ""); // Clear Bearer token (if stored in headers)
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
            String token = jwtService.generateToken(user, bank);

            // Create HttpOnly cookie
            Cookie cookie = new Cookie("auth_token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // Enable if using HTTPS
            cookie.setPath("/");
            cookie.setMaxAge(3600 * 10); // 1 hour

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
        String subject = "Your Secure OTP for Verification";
        String messageBody = "<div style='font-family: Arial, sans-serif; text-align: center;'>" +
                             "<h2 style='color: #333;'>Your One-Time Password (OTP)</h2>" +
                             "<p>Please use the following OTP to verify your identity:</p>" +
                             "<h1 style='color: #007BFF; font-size: 36px;'>" + otp + "</h1>" +
                             "<p>This OTP is valid for only 5 minutes. Do not share it with anyone.</p>" +
                             "<p>Thank you,<br><strong>SecurePulse Team</strong></p>" +
                             "</div>";

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
                cookie.setMaxAge(3600 * 10); // Expiry time (15 minutes)
                httpResponse.addCookie(cookie);
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

}

