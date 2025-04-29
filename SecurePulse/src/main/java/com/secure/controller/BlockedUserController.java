package com.secure.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.secure.exception.CustomException;
import com.secure.model.BlockedUser;
import com.secure.repository.BlockedUserRepository;
import com.secure.utils.EmailProvider;
import com.secure.utils.JwtProvider;
import com.secure.utils.TemplateProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller responsible for handling alert-related operations such as
 * sending warning emails and blocking users based on suspicious activities.
 */
@RestController
@RequestMapping("/api/alert")
public class BlockedUserController {

    private final BlockedUserRepository blockedUserRepository;
    private final EmailProvider emailProvider;
    private final JwtProvider jwtProvider;
    private final TemplateProvider templateProvider;

    @Autowired
    public BlockedUserController(BlockedUserRepository blockedUserRepository,
                                 EmailProvider emailProvider,
                                 JwtProvider jwtProvider,
                                 TemplateProvider templateProvider) {
        this.blockedUserRepository = blockedUserRepository;
        this.emailProvider = emailProvider;
        this.jwtProvider = jwtProvider;
        this.templateProvider = templateProvider;
    }

    /**
     * Sends a warning email to the user when multiple incorrect attempts are detected.
     *
     * @param request The HTTP request containing the JWT token in the header.
     * @param purpose The reason for the warning (e.g., "login").
     * @return ResponseEntity containing success or error message.
     */
    @GetMapping("/warning/{purpose}")
    public ResponseEntity<Map<String, Object>> warning(HttpServletRequest request, @PathVariable String purpose) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Extract JWT token from Authorization header
            String token = jwtProvider.extractAuthToken(request);
            if (token == null) {
                throw new CustomException("JWT token is missing.");
            }

            // Parse token to get user claims
            DecodedJWT jwt = jwtProvider.extractClaims(token);
            if (jwt == null) {
                throw new CustomException("Invalid JWT token.");
            }

            // Get user email and bank info from JWT claims, handle null claims
            String userBank = jwt.getClaim("userBank") != null ? jwt.getClaim("userBank").asString() : null;
            String userEmail = jwt.getClaim("email") != null ? jwt.getClaim("email").asString() : null;

            // Validate extracted info
            if (userBank == null || userEmail == null) {
                throw new CustomException("userBank or userEmail not found in JWT.");
            }

            // Prepare and send warning email
            String subject = "Multiple Incorrect Attempts Detected - SecurePulse Account";
            String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a, dd MMM yyyy"));
            String messageBody = templateProvider.buildMultipleAttemptWarningEmail(timeStamp);
            emailProvider.sendEmail(userEmail, subject, messageBody);

            response.put("status", "success");
            response.put("message", "✅ Warning email sent successfully.");
            return ResponseEntity.ok(response);

        } catch (CustomException e) {
            // Handle known exceptions
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(200).body(response);

        } catch (Exception e) {
            // Handle unexpected exceptions
            response.put("status", "error");
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(200).body(response);
        }
    }


    /**
     * Blocks a user account and sends an account suspension email.
     *
     * @param request      The HTTP request containing the JWT token.
     * @param requestBody  JSON body containing the block reason.
     * @return ResponseEntity containing success or error message.
     */
    @PostMapping("/block")
    public ResponseEntity<Map<String, Object>> block(HttpServletRequest request, @RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Extract block reason from request body
            String reason = requestBody.get("reason").toString();

            // Extract JWT token from Authorization header
            String token = jwtProvider.extractAuthToken(request);
            if (token == null) {
                throw new CustomException("JWT token is missing.");
            }

            // Parse token to get user claims
            DecodedJWT jwt = jwtProvider.extractClaims(token);
            if (jwt == null) {
                throw new CustomException("Invalid JWT token.");
            }

            // Extract user details
            String userBank = jwt.getClaim("userBank").asString();
            String userEmail = jwt.getClaim("email").asString();

            if (userBank == null || userEmail == null) {
                throw new CustomException("Required user information is missing in the JWT.");
            }

            // Create blocked user record
            BlockedUser blockedUser = new BlockedUser();
            blockedUser.setEmail(userEmail);
            blockedUser.setBankName(userBank);
            blockedUser.setReason(reason);

            // Prepare and send block notification email
            String subject = "Urgent: Your SecurePulse Account Has Been Blocked";
            String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a, dd MMM yyyy"));
            String messageBody = templateProvider.buildAccountSuspendedEmail(reason, timeStamp);

            blockedUserRepository.save(blockedUser);
            emailProvider.sendEmail(userEmail, subject, messageBody);

            response.put("status", "success");
            response.put("message", "✅ User successfully blocked.");
            return ResponseEntity.ok(response);

        } catch (CustomException e) {
            // Handle known exceptions
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(200).body(response);

        } catch (Exception e) {
            // Handle unexpected exceptions
            response.put("status", "error");
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(200).body(response);
        }
    }
}
