package com.secure.controller;


import com.auth0.jwt.interfaces.DecodedJWT;
import com.secure.model.BlockedUser;
import com.secure.repository.BlockedUserRepository;
import com.secure.services.EmailService;
import com.secure.services.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/alert")
public class BlockedUserController {
    @Autowired
    BlockedUserRepository blockedUserRepository;
    @Autowired
    EmailService emailService;
    @Autowired
    JwtService jwtService;



    @GetMapping("/warning/{purpose}")
    public Map<String, Object> warning(HttpServletRequest request, @PathVariable String purpose) {

        Map<String, Object> response = new HashMap<>();

        // Extract JWT token from the request header
        String token = jwtService.extractAuthToken(request);
        if (token == null) {
            response.put("status", "error");
            response.put("message", "❌ JWT token is missing.");
            return response;
        }

        // Decode the JWT to extract claims
        DecodedJWT jwt = jwtService.extractClaims(token);
        if (jwt == null) {
            response.put("status", "error");
            response.put("message", "❌ Invalid JWT token.");
            return response;
        }

        // Extract user information from JWT claims
        String userBank = jwt.getClaim("userBank").asString();
        String userEmail = jwt.getClaim("email").asString();

        // Validate that necessary information is present in the JWT
        if (userBank == null || userBank.isEmpty() || userEmail == null || userEmail.isEmpty()) {
            response.put("status", "error");
            response.put("message", "❌ userBank or userEmail not found in JWT.");
            return response;
        }


        String subject = "Multiple Incorrect Attempts Detected - SecurePulse Account";

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
                "        .warning-icon { color: #e74c3c; font-size: 48px; margin-bottom: 20px; }" +
                "        .alert-box { background: #fdecea; padding: 15px; border-left: 4px solid #e74c3c; margin: 20px 0; text-align: left; }" +
                "        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; border-radius: 0 0 8px 8px; font-size: 12px; color: #7f8c8d; }" +
                "        .button { background-color: #3498db; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 15px 0; font-weight: bold; }" +
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
                "            <div class='warning-icon'>⚠️</div>" +
                "            <h2 style='color: #2c3e50;'>Multiple Incorrect Attempts Detected</h2>" +
                "            " +
                "            <div class='alert-box'>" +
                "                <p>We detected <strong>multiple unsuccessful attempts</strong> to access your account or perform an action.</p>" +
                "                <p><strong>Date/Time:</strong> " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a, dd MMM yyyy")) + "</p>" +
                "            </div>" +
                "            " +
                "            <p>For your security, we recommend:</p>" +
                "            <ul style='text-align: left; margin-left: 20px;'>" +
                "                <li>Ensure you're using the correct credentials</li>" +
                "                <li>Reset your password if you've forgotten it</li>" +
                "                <li>Contact support if you didn't initiate these attempts</li>" +
                "            </ul>" +
                "            " +
                "            <div class='divider'></div>" +
                "            " +
                "            <a href='' class='button'>Reset Password</a>" +
                "            " +
                "            <p style='font-size: 13px; color: #7f8c8d;'>This is an automated security notification. No action is required if you recognize this activity.</p>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>© 2025 SecurePulse by WISSEN Technology. All rights reserved.</p>" +
                "            <p>123 Tech Park, Innovation City | support@securepulse.com</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";

        emailService.sendEmail(userEmail, subject, messageBody);


        // Send the warning email
        try {
            response.put("status", "success");
            response.put("message", "✅ Warning email sent successfully.");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "❌ Failed to send email: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/block")
    public Map<String, Object> block(HttpServletRequest request,@RequestBody Map<String, Object> requestBody ) {

        Map<String, Object> response = new HashMap<>();
        String reason=requestBody.get("reason").toString();

        // Extract the JWT token from the request
        String token = jwtService.extractAuthToken(request);
        if (token == null) {
            response.put("status", "error");
            response.put("message", "❌ JWT token is missing.");
            return response;
        }

        // Decode the JWT to extract claims
        DecodedJWT jwt = jwtService.extractClaims(token);
        if (jwt == null) {
            response.put("status", "error");
            response.put("message", "❌ Invalid JWT token.");
            return response;
        }

        // Extract user information from the JWT claims
        String userBank = jwt.getClaim("userBank").asString();
        String userEmail = jwt.getClaim("email").asString();

        // Create a new BlockedUser entity
        BlockedUser blockedUser = new BlockedUser();
        blockedUser.setEmail(userEmail);
        blockedUser.setBankName(userBank);
        blockedUser.setReason(reason);

        String subject = "Urgent: Your SecurePulse Account Has Been Blocked";

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
                "        .alert-icon { color: #e74c3c; font-size: 48px; margin-bottom: 20px; }" +
                "        .alert-box { background: #fdecea; padding: 20px; border-left: 4px solid #e74c3c; margin: 20px 0; text-align: left; }" +
                "        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; border-radius: 0 0 8px 8px; font-size: 12px; color: #7f8c8d; }" +
                "        .button { background-color: #3498db; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 15px 0; font-weight: bold; }" +
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
                "            <div class='alert-icon'>⚠️</div>" +
                "            <h2 style='color: #2c3e50;'>Account Access Temporarily Suspended</h2>" +
                "            " +
                "            <div class='alert-box'>" +
                "                <p><strong>Reason for Blocking:</strong></p>" +
                "                <p>" + reason + "</p>" +
                "                <p><strong>Date/Time:</strong> " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a, dd MMM yyyy")) + "</p>" +
                "            </div>" +
                "            " +
                "            <p>To restore your account access, please:</p>" +
                "            <ol style='text-align: left; margin-left: 20px;'>" +
                "                <li>Contact our support team immediately</li>" +
                "                <li>Verify your identity if requested</li>" +
                "                <li>Complete any required security steps</li>" +
                "            </ol>" +
                "            " +
                "            <div class='divider'></div>" +
                "            " +
                "            <a href='https://securepulse.com/support' class='button'>Contact Support Now</a>" +
                "            " +
                "            <p style='font-size: 13px; color: #7f8c8d;'>This is an automated security alert. No action is needed if you've already resolved this issue.</p>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>© 2023 SecurePulse by WISSEN Technology. All rights reserved.</p>" +
                "            <p>123 Tech Park, Innovation City | support@securepulse.com | +1 (800) 123-4567</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";

        // Save the BlockedUser entity in the database
        try {
            blockedUserRepository.save(blockedUser);
            emailService.sendEmail(userEmail,subject,messageBody);
            response.put("status", "success");
            response.put("message", "✅ User successfully blocked.");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "❌ Error occurred while blocking user: " + e.getMessage());
        }

        return response;
    }

}
