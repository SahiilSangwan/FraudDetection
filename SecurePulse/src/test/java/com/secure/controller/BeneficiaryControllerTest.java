package com.secure.controller;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.secure.model.Beneficiary;
import com.secure.repository.AccountRepository;
import com.secure.repository.BeneficiaryRepository;
import com.secure.repository.UserRepository;
import com.secure.services.BeneficiaryService;
import com.secure.utils.EmailProvider;
import com.secure.utils.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficiaryControllerTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private BeneficiaryService beneficiaryService;

    @Mock
    private EmailProvider emailProvider;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BeneficiaryController beneficiaryController;

    @Test
    void addBeneficiary() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "token";

        // Mock JWT claims
        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim userIdClaim = mock(Claim.class);
        Claim userBankClaim = mock(Claim.class);
        when(userIdClaim.asInt()).thenReturn(123);
        when(userBankClaim.asString()).thenReturn("HDFC");
        when(jwt.getClaim("userId")).thenReturn(userIdClaim);
        when(jwt.getClaim("userBank")).thenReturn(userBankClaim);

        when(jwtProvider.extractAuthToken(request)).thenReturn(token);
        when(jwtProvider.extractClaims(token)).thenReturn(jwt);

        Map<String, Object> requestBody = Map.of(
                "accountNumber", "1234567890",
                "ifscCode", "HDFC0001234",
                "amount", new BigDecimal("1000"),
                "name", "John Doe"
        );

        Map<String, Object> serviceResponse = Map.of(
                "success", true,
                "data", new Object(),
                "message", "Successfully added beneficiary"
        );

        when(beneficiaryService.addBeneficiary(123, "HDFC", "1234567890", "HDFC0001234", new BigDecimal("1000"), "John Doe"))
                .thenReturn(serviceResponse);

        ResponseEntity<Map<String, Object>> response = beneficiaryController.addBeneficiary(requestBody, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertNotNull(response.getBody().get("data"));
        assertEquals("Successfully added beneficiary", response.getBody().get("message"));
    }

    @Test
    void getBeneficiaries() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "token";

        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim userBankClaim = mock(Claim.class);
        when(userBankClaim.asString()).thenReturn("HDFC");
        when(jwt.getClaim("userBank")).thenReturn(userBankClaim);

        when(jwtProvider.extractAuthToken(request)).thenReturn(token);
        when(jwtProvider.extractClaims(token)).thenReturn(jwt);

        List<Beneficiary> beneficiaries = List.of(new Beneficiary());
        when(beneficiaryService.getBeneficiaries(1, "HDFC", true)).thenReturn(beneficiaries);

        ResponseEntity<List<Beneficiary>> response = beneficiaryController.getBeneficiaries(request, 1, true);
        assertEquals(1, response.getBody().size());
    }

    @Test
    void deleteBeneficiary() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "token";

        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim userIdClaim = mock(Claim.class);
        when(userIdClaim.asInt()).thenReturn(10);
        when(jwt.getClaim("userId")).thenReturn(userIdClaim);

        when(jwtProvider.extractAuthToken(request)).thenReturn(token);
        when(jwtProvider.extractClaims(token)).thenReturn(jwt);

        ResponseEntity<Map<String, Object>> response = beneficiaryController.deleteBeneficiary(5, request);
        assertTrue((Boolean) response.getBody().get("success"));
    }

    @Test
    void updateBeneficiaryAmount() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "token";

        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim userIdClaim = mock(Claim.class);
        when(userIdClaim.asInt()).thenReturn(1);
        when(jwt.getClaim("userId")).thenReturn(userIdClaim);

        when(jwtProvider.extractAuthToken(request)).thenReturn(token);
        when(jwtProvider.extractClaims(token)).thenReturn(jwt);

        Beneficiary updated = new Beneficiary();
        updated.setAmount(new BigDecimal("2000"));

        Map<String, Object> requestBody = Map.of(
                "beneficiaryId", 99,
                "amount", new BigDecimal("2000")
        );

        when(beneficiaryService.updateBeneficiaryAmount(1, 99, new BigDecimal("2000"))).thenReturn(updated);

        ResponseEntity<Map<String, Object>> response = beneficiaryController.updateBeneficiaryAmount(requestBody, request);
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals(updated, response.getBody().get("data"));
    }

    @Test
    void getBeneficiariesTransaction() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "token";

        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim userBankClaim = mock(Claim.class);
        when(userBankClaim.asString()).thenReturn("HDFC");
        when(jwt.getClaim("userBank")).thenReturn(userBankClaim);

        when(jwtProvider.extractAuthToken(request)).thenReturn(token);
        when(jwtProvider.extractClaims(token)).thenReturn(jwt);

        List<Beneficiary> beneficiaries = List.of(new Beneficiary());
        when(beneficiaryService.getBeneficiariesForTransaction(1, "HDFC", false)).thenReturn(beneficiaries);

        ResponseEntity<List<Beneficiary>> response = beneficiaryController.getBeneficiariesTransaction(request, 1, false);
        assertEquals(1, response.getBody().size());
    }

    @Test
    void compareBeneficiary() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "token";

        DecodedJWT jwt = mock(DecodedJWT.class);
        Claim userIdClaim = mock(Claim.class);
        Claim userBankClaim = mock(Claim.class);
        when(userIdClaim.asInt()).thenReturn(42);
        when(userBankClaim.asString()).thenReturn("HDFC");
        when(jwt.getClaim("userId")).thenReturn(userIdClaim);
        when(jwt.getClaim("userBank")).thenReturn(userBankClaim);

        when(jwtProvider.extractAuthToken(request)).thenReturn(token);
        when(jwtProvider.extractClaims(token)).thenReturn(jwt);

        Map<String, Object> responseMap = Map.of("success", true, "data", new Object());
        when(beneficiaryService.compareBeneficiary(42, 100, "HDFC")).thenReturn(responseMap);

        ResponseEntity<Map<String, Object>> response = beneficiaryController.compareBeneficiary(request, 100);
        assertTrue((Boolean) response.getBody().get("success"));
        assertNotNull(response.getBody().get("data"));
    }
}
