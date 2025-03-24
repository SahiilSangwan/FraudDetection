package com.secure.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.secure.model.Beneficiary;
import com.secure.operations.BeneficiaryOperations;
import com.secure.services.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import com.secure.exception.BeneficiaryException;

@RestController
@RequestMapping("api/beneficiaries")
public class BeneficiaryController {

    private final JwtService jwtService;
    private final BeneficiaryOperations beneficiaryOperations;


    public BeneficiaryController(JwtService jwtService, BeneficiaryOperations beneficiaryOperations) {
        this.jwtService = jwtService;
        this.beneficiaryOperations = beneficiaryOperations;
    }

    @GetMapping("/{userId}")
    public List<Beneficiary> getBeneficiaries(HttpServletRequest request,
                                              @PathVariable Integer userId,
                                              @RequestParam("same") boolean sameBank) {
        // ✅ Extract JWT token from cookies
        String token = jwtService.extractAuthToken(request);
        if (token == null) {
            throw new RuntimeException("❌ JWT token is missing.");
        }

        // ✅ Decode the JWT
        DecodedJWT jwt = jwtService.extractClaims(token);
        if (jwt == null) {
            throw new RuntimeException("❌ Invalid JWT token.");
        }

        // ✅ Extract `userBank` from JWT
        String userBank = jwt.getClaim("userBank").asString();
        if (userBank == null || userBank.isEmpty()) {
            throw new RuntimeException("❌ userBank not found in JWT.");
        }

        // ✅ Fetch beneficiaries based on bank filter
        return beneficiaryOperations.getBeneficiaries(userId, userBank, sameBank);
    }

    // ✅ Add Beneficiary
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addBeneficiary(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // ✅ Extract Token & Decode JWT
            String token = jwtService.extractAuthToken(request);

            if (token == null) {
                response.put("success", false);
                response.put("message", "Unauthorized: Token missing");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            System.out.println(token);
            DecodedJWT decodedJWT = jwtService.extractClaims(token);
            Integer userId = decodedJWT.getClaim("userId").asInt();
            String userBank = decodedJWT.getClaim("userBank").asString();

            // ✅ Extract Request Body
            String accountNumber = (String) requestBody.get("accountNumber");
            String ifscCode = (String) requestBody.get("ifscCode");
            BigDecimal amount = new BigDecimal(requestBody.get("amount").toString());
            String name=(String) requestBody.get("name");

            // ✅ Add Beneficiary
            response = beneficiaryOperations.addBeneficiary(userId, userBank, accountNumber, ifscCode, amount,name);
            return ResponseEntity.ok(response);

        } catch (BeneficiaryException e) { // ✅ Handle custom errors
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) { // ✅ Handle unexpected errors
            response.put("success", false);
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ✅ Delete Beneficiary
    @DeleteMapping("/delete/{beneficiaryId}")
    public ResponseEntity<?> deleteBeneficiary(@PathVariable Integer beneficiaryId, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String token = jwtService.extractAuthToken(request);
            if (token == null) {
                return ResponseEntity.status(401).body("Unauthorized: Token missing");
            }



            DecodedJWT decodedJWT = jwtService.extractClaims(token);

            Integer userId = decodedJWT.getClaim("userId").asInt();

            beneficiaryOperations.deleteBeneficiary(userId, beneficiaryId);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ✅ Update Beneficiary Amount
    @PutMapping("/update")
    public ResponseEntity<?> updateBeneficiaryAmount(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String token = jwtService.extractAuthToken(request);
            if (token == null) {
                return ResponseEntity.status(401).body("Unauthorized: Token missing");
            }

            DecodedJWT decodedJWT = jwtService.extractClaims(token);
            Integer userId = decodedJWT.getClaim("userId").asInt();

            Integer beneficiaryId = (Integer) requestBody.get("beneficiaryId");
            BigDecimal newAmount = new BigDecimal(requestBody.get("amount").toString());

            Beneficiary updatedBeneficiary = beneficiaryOperations.updateBeneficiaryAmount(userId, beneficiaryId, newAmount);
            response.put("success", true);
            response.put("message", "Beneficiary updated successfully.");
            response.put("data", updatedBeneficiary);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("transaction/{userId}")
    public List<Beneficiary> getBeneficiariesTransaction(HttpServletRequest request,
                                              @PathVariable Integer userId,
                                              @RequestParam("same") boolean sameBank) {
        // ✅ Extract JWT token from cookies
        String token = jwtService.extractAuthToken(request);
        if (token == null) {
            throw new RuntimeException("❌ JWT token is missing.");
        }

        // ✅ Decode the JWT
        DecodedJWT jwt = jwtService.extractClaims(token);
        if (jwt == null) {
            throw new RuntimeException("❌ Invalid JWT token.");
        }

        // ✅ Extract `userBank` from JWT
        String userBank = jwt.getClaim("userBank").asString();
        if (userBank == null || userBank.isEmpty()) {
            throw new RuntimeException("❌ userBank not found in JWT.");
        }

        // ✅ Fetch beneficiaries based on bank filter
        return beneficiaryOperations.getBeneficiariesForTransaction(userId, userBank,sameBank);
    }

    @GetMapping("/compare/{beneficiaryId}")
    public Map<String,Object> compareBeneficiary(HttpServletRequest request,@PathVariable Integer beneficiaryId){
        Map<String,Object> response=new HashMap<>();
        try{
            String token = jwtService.extractAuthToken(request);
            if (token == null) {
                response.put("success", false);
                response.put("message", "Unauthorized: Token missing");
                return response;
            }

            DecodedJWT decodedJWT = jwtService.extractClaims(token);
            Integer userId = decodedJWT.getClaim("userId").asInt();
            String userBank = decodedJWT.getClaim("userBank").asString();
            response=beneficiaryOperations.compareBeneficiary(userId,beneficiaryId,userBank);
            return response;
        }catch (Exception e){
            response.put("success", false);
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return response;
        }
    }
}