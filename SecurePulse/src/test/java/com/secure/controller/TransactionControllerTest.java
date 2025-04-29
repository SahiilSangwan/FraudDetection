package com.secure.controller;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import com.secure.services.TransactionService;
import com.secure.utils.DecryptionProvider;
import com.secure.utils.JwtProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @InjectMocks
    private TransactionController transactionController;

    @Mock
    private TransactionService transactionService;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private DecryptionProvider Decrypt;

    @Test
    void addTransaction_Success() {
        // Test case when everything is fine and the transaction is successful.
        String eSelectedBeneficiaryID = "123";
        String eReceiverAcc = "100";
        String eAmount = "100";
        String description = "Test transaction";
        String eIfscCodeUser = "IFSC123";
        int totpAttempt = 1;

        MockHttpServletRequest request = new MockHttpServletRequest();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("eSelectedBeneficiaryID", eSelectedBeneficiaryID);
        requestBody.put("eReceiverAcc", eReceiverAcc);
        requestBody.put("eAmount", eAmount);
        requestBody.put("description", description);
        requestBody.put("eIfscCodeUser", eIfscCodeUser);
        requestBody.put("totpAttempt", totpAttempt);

        when(jwtProvider.extractAuthToken(request)).thenReturn("token");
        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        when(jwtProvider.extractClaims("token")).thenReturn(decodedJWT);
        when(Decrypt.decryptString(eSelectedBeneficiaryID)).thenReturn("123");
        when(Decrypt.decryptString(eReceiverAcc)).thenReturn("100");
        when(Decrypt.decryptString(eAmount)).thenReturn("100");
        when(Decrypt.decryptString(eIfscCodeUser)).thenReturn(eIfscCodeUser);
        Claim userIdClaim = mock(Claim.class);
        Claim userBankClaim = mock(Claim.class);
        when(userIdClaim.asInt()).thenReturn(1);
        when(userBankClaim.asString()).thenReturn("BankName");
        when(decodedJWT.getClaim("userId")).thenReturn(userIdClaim);
        when(decodedJWT.getClaim("userBank")).thenReturn(userBankClaim);

        Map<String, Object> response = Map.of("status", true, "message", "Transaction successful");
        when(transactionService.addTransaction(1, 123, "100", new BigDecimal("100"), eIfscCodeUser, "BankName", description, totpAttempt))
                .thenReturn(response);

        ResponseEntity<Map<String, Object>> result = transactionController.addTransaction(requestBody, request);

        assertEquals(200, result.getStatusCodeValue());
        assertEquals(true, result.getBody().get("status"));
        assertEquals("Transaction successful", result.getBody().get("message"));
    }



    @Test
    void addTransaction_TransactionServiceFailure() {
        // Test case when the transaction service throws an unexpected exception
        String eSelectedBeneficiaryID = "123";
        String eReceiverAcc = "100";
        String eAmount = "100";
        String description = "Test transaction";
        String eIfscCodeUser = "IFSC123";
        int totpAttempt = 1;

        MockHttpServletRequest request = new MockHttpServletRequest();
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("eSelectedBeneficiaryID", eSelectedBeneficiaryID);
        requestBody.put("eReceiverAcc", eReceiverAcc);
        requestBody.put("eAmount", eAmount);
        requestBody.put("description", description);
        requestBody.put("eIfscCodeUser", eIfscCodeUser);
        requestBody.put("totpAttempt", totpAttempt);

        when(jwtProvider.extractAuthToken(request)).thenReturn("token");
        DecodedJWT decodedJWT = mock(DecodedJWT.class);
        when(jwtProvider.extractClaims("token")).thenReturn(decodedJWT);
        when(Decrypt.decryptString(eSelectedBeneficiaryID)).thenReturn("123");
        when(Decrypt.decryptString(eReceiverAcc)).thenReturn("100");
        when(Decrypt.decryptString(eAmount)).thenReturn("100");
        when(Decrypt.decryptString(eIfscCodeUser)).thenReturn(eIfscCodeUser);
        Claim userIdClaim = mock(Claim.class);
        Claim userBankClaim = mock(Claim.class);
        when(userIdClaim.asInt()).thenReturn(1);
        when(userBankClaim.asString()).thenReturn("BankName");
        when(decodedJWT.getClaim("userId")).thenReturn(userIdClaim);
        when(decodedJWT.getClaim("userBank")).thenReturn(userBankClaim);

        when(transactionService.addTransaction(1, 123, "100", new BigDecimal("100"), eIfscCodeUser, "BankName", description, totpAttempt))
                .thenThrow(new RuntimeException("Service failure"));

        ResponseEntity<Map<String, Object>> result = transactionController.addTransaction(requestBody, request);

        assertEquals(500, result.getStatusCodeValue());
        assertTrue(result.getBody().get("message").toString().contains("An unexpected error occurred"));
    }

    @Test
    void getTransactionsByUserId_Success() {
        Integer userId = 1;
        Map<String, Object> response = new HashMap<>();
        response.put("status", true);
        response.put("message", "Transactions retrieved successfully");

        when(transactionService.getTransactionsByUserId(userId)).thenReturn(response);

        ResponseEntity<Map<String, Object>> result = transactionController.getTransactionsByUserId(userId);
        assertEquals(200, result.getStatusCodeValue());
        assertEquals(true, result.getBody().get("status"));
        assertEquals("Transactions retrieved successfully", result.getBody().get("message"));
    }

    @Test
    void getTransactionsByUserId_ExceptionHandling() {
        // Test case when the getTransactionsByUserId method throws an exception
        Integer userId = 1;

        when(transactionService.getTransactionsByUserId(userId)).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<Map<String, Object>> result = transactionController.getTransactionsByUserId(userId);

        assertEquals(500, result.getStatusCodeValue());
        assertTrue(result.getBody().get("message").toString().contains("An unexpected error occurred"));
    }




}
