package com.secure.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.secure.exception.CustomException;
import com.secure.model.Beneficiary;
import com.secure.services.BeneficiaryService;
import com.secure.utils.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/beneficiaries")
public class BeneficiaryController {

    private final JwtProvider jwtProvider;
    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(JwtProvider jwtProvider, BeneficiaryService beneficiaryService) {
        this.jwtProvider = jwtProvider;
        this.beneficiaryService = beneficiaryService;
    }

    /**
     * Retrieves a list of beneficiaries for the specified user,
     * optionally filtered by whether the bank is the same.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<Beneficiary>> getBeneficiaries(HttpServletRequest request,
                                                              @PathVariable Integer userId,
                                                              @RequestParam("same") boolean sameBank) {
        try {
            String token = jwtProvider.extractAuthToken(request);
            if (token == null) {
                throw new CustomException("JWT token is missing.");
            }

            DecodedJWT jwt = jwtProvider.extractClaims(token);
            String userBank = jwt.getClaim("userBank").asString();

            List<Beneficiary> beneficiaries = beneficiaryService.getBeneficiaries(userId, userBank, sameBank);
            return ResponseEntity.ok(beneficiaries);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException("An error occurred while fetching beneficiaries.");
        }
    }

    /**
     * Adds a new beneficiary for the authenticated user.
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addBeneficiary(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        try {
            String token = jwtProvider.extractAuthToken(request);
            if (token == null) {
                throw new CustomException("Unauthorized: Token missing");
            }

            DecodedJWT decodedJWT = jwtProvider.extractClaims(token);
            Integer userId = decodedJWT.getClaim("userId").asInt();
            String userBank = decodedJWT.getClaim("userBank").asString();

            String accountNumber = (String) requestBody.get("accountNumber");
            String ifscCode = (String) requestBody.get("ifscCode");
            BigDecimal amount = new BigDecimal(requestBody.get("amount").toString());
            String name = (String) requestBody.get("name");

            Map<String, Object> response = beneficiaryService.addBeneficiary(userId, userBank, accountNumber, ifscCode, amount, name);
            return ResponseEntity.ok(response);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException("An error occurred while adding the beneficiary.");
        }
    }

    /**
     * Deletes a beneficiary based on the provided beneficiary ID.
     */
    @DeleteMapping("/delete/{beneficiaryId}")
    public ResponseEntity<Map<String, Object>> deleteBeneficiary(@PathVariable Integer beneficiaryId, HttpServletRequest request) {
        try {
            String token = jwtProvider.extractAuthToken(request);
            if (token == null) {
                throw new CustomException("Unauthorized: Token missing");
            }

            DecodedJWT decodedJWT = jwtProvider.extractClaims(token);
            Integer userId = decodedJWT.getClaim("userId").asInt();

            beneficiaryService.deleteBeneficiary(userId, beneficiaryId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException("An error occurred while deleting the beneficiary.");
        }
    }

    /**
     * Updates the transfer limit (amount) for a specific beneficiary.
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateBeneficiaryAmount(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        try {
            String token = jwtProvider.extractAuthToken(request);
            if (token == null) {
                throw new CustomException("Unauthorized: Token missing");
            }

            DecodedJWT decodedJWT = jwtProvider.extractClaims(token);
            Integer userId = decodedJWT.getClaim("userId").asInt();

            Integer beneficiaryId = (Integer) requestBody.get("beneficiaryId");
            BigDecimal newAmount = new BigDecimal(requestBody.get("amount").toString());

            Beneficiary updatedBeneficiary = beneficiaryService.updateBeneficiaryAmount(userId, beneficiaryId, newAmount);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Beneficiary updated successfully.");
            response.put("data", updatedBeneficiary);
            return ResponseEntity.ok(response);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException("An error occurred while updating the beneficiary amount.");
        }
    }

    /**
     * Retrieves beneficiaries eligible for transaction by bank type (same/different).
     */
    @GetMapping("transaction/{userId}")
    public ResponseEntity<List<Beneficiary>> getBeneficiariesTransaction(HttpServletRequest request,
                                                                         @PathVariable Integer userId,
                                                                         @RequestParam("same") boolean sameBank) {
        try {
            String token = jwtProvider.extractAuthToken(request);
            if (token == null) {
                throw new CustomException("JWT token is missing.");
            }

            DecodedJWT jwt = jwtProvider.extractClaims(token);
            String userBank = jwt.getClaim("userBank").asString();

            List<Beneficiary> beneficiaries = beneficiaryService.getBeneficiariesForTransaction(userId, userBank, sameBank);
            return ResponseEntity.ok(beneficiaries);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException("An error occurred while fetching beneficiaries for transactions.");
        }
    }

    /**
     * Compares beneficiary account details with existing records for validation.
     */
    @GetMapping("/compare/{beneficiaryId}")
    public ResponseEntity<Map<String, Object>> compareBeneficiary(HttpServletRequest request, @PathVariable Integer beneficiaryId) {
        try {
            String token = jwtProvider.extractAuthToken(request);
            if (token == null) {
                throw new CustomException("Unauthorized: Token missing");
            }

            DecodedJWT decodedJWT = jwtProvider.extractClaims(token);
            Integer userId = decodedJWT.getClaim("userId").asInt();
            String userBank = decodedJWT.getClaim("userBank").asString();

            Map<String, Object> response = beneficiaryService.compareBeneficiary(userId, beneficiaryId, userBank);
            return ResponseEntity.ok(response);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException("An error occurred while comparing the beneficiary.");
        }
    }
}
