package com.secure.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.secure.exception.CustomException;
import com.secure.services.TransactionService;
import com.secure.utils.DecryptionProvider;
import com.secure.utils.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("api/transaction")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private DecryptionProvider Decrypt;

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addTransaction(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        try {
            // Extract data from request body
            Integer receiverId = Integer.parseInt(Decrypt.decryptString(requestBody.get("eSelectedBeneficiaryID").toString()));
            String receiverAccountNumber = Decrypt.decryptString((String) requestBody.get("eReceiverAcc"));
            BigDecimal amountTransferred = new BigDecimal(Decrypt.decryptString(requestBody.get("eAmount").toString()));
            String ifscCode = Decrypt.decryptString((String) requestBody.get("eIfscCodeUser"));
            Integer otpAttempt = (Integer) requestBody.get("totpAttempt");
            String desc = (String) requestBody.get("description");

            // Extract senderId from JWT token
            String authToken = jwtProvider.extractAuthToken(request);
            DecodedJWT decodedJWT = jwtProvider.extractClaims(authToken);
            if (decodedJWT == null) {
                throw new CustomException("Invalid JWT Token");
            }

            Integer senderId = decodedJWT.getClaim("userId").asInt();
            String userBank = decodedJWT.getClaim("userBank").asString();

            // Call the function in TransactionService
            Map<String, Object> response = transactionService.addTransaction(senderId, receiverId, receiverAccountNumber, amountTransferred, ifscCode, userBank, desc, otpAttempt);
            return ResponseEntity.ok(response);
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", false, "message", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @GetMapping("/get/{userId}")
    public ResponseEntity<Map<String, Object>> getTransactionsByUserId(@PathVariable Integer userId) {
        try {
            Map<String, Object> response = transactionService.getTransactionsByUserId(userId);
            return ResponseEntity.ok(response);
        } catch (CustomException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("status", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", false, "message", "An unexpected error occurred: " + e.getMessage()));
        }
    }
}