package com.secure.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.secure.operations.TransactionOperations;
import com.secure.services.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("api/transaction")
public class TransactionController {
    @Autowired
    private TransactionOperations transactionOperations;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/add")
    public Map<String, Object> addTransaction(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        // Extract data from request body
        System.out.println("controller request body"+requestBody);
        Integer receiverId = (Integer) requestBody.get("selectedBeneficiaryID");
        String receiverAccountNumber = (String) requestBody.get("receiverAcc");
        BigDecimal amountTransferred = new BigDecimal(requestBody.get("amount").toString());
        String ifscCode = (String) requestBody.get("ifscCodeUser");
        String desc=(String) requestBody.get("description");
        System.out.println("ifsc code at controller side of transaction"+ifscCode);
        System.out.println("description at controller side of transaction"+desc);

        // Extract senderId from JWT token
        String authToken = jwtService.extractAuthToken(request);
        DecodedJWT decodedJWT = jwtService.extractClaims(authToken);
        if (decodedJWT == null) {
            return Map.of("status", false, "message", "Invalid JWT Token");
        }
        Integer senderId = decodedJWT.getClaim("userId").asInt();
        String userBank = decodedJWT.getClaim("userBank").asString();

        // Call the function in TransactionOperations
        return transactionOperations.addTransaction(senderId, receiverId, receiverAccountNumber, amountTransferred, ifscCode,userBank,desc);
    }

    @GetMapping("/get/{userId}")
    public Map<String, Object> getTransactionsByUserId(@PathVariable Integer userId) {
        return transactionOperations.getTransactionsByUserId(userId);
    }
}
