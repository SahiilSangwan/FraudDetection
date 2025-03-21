package com.secure.operations;

import com.secure.model.Account;
import com.secure.model.Beneficiary;
import com.secure.model.User;
import com.secure.repository.AccountRepository;
import com.secure.repository.BeneficiaryRepository;
import com.secure.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.*;
import com.secure.exception.BeneficiaryException;

@Service
public class BeneficiaryOperations {

    private final AccountRepository accountRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;

    public BeneficiaryOperations(AccountRepository accountRepository, BeneficiaryRepository beneficiaryRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.userRepository = userRepository;
    }

    public Map<String,Object> addBeneficiary(Integer userId, String userBank, String accountNumber, String ifscCode, BigDecimal amount,String name) throws BeneficiaryException {
        // ✅ Check if beneficiary already exists
        Map<String,Object> response = new HashMap<>();
        Optional<Beneficiary> existingBeneficiary = beneficiaryRepository.findByUserIdAndBeneficiaryAccountNumber(userId, accountNumber);
        if (existingBeneficiary.isPresent()) {
            response.put("success", false);
            response.put("message", "Beneficiary already exists");
            response.put("data",null);
            return response;
        }

        // ✅ Fetch beneficiary account details
        Optional<Account> accountOptional = accountRepository.findByAccountNumber(accountNumber);
        if (accountOptional.isEmpty()) {
            response.put("success", false);
            response.put("message", "Account does not exist");
            response.put("data",null);
            return response;
        }

        Account beneficiaryAccount = accountOptional.get();
        if (!beneficiaryAccount.getIfscCode().equals(ifscCode)) {
            response.put("success", false);
            response.put("message", "Account does not match ifsc code");
            response.put("data",null);
            return response;

        }

        Integer beneficiaryUserId = beneficiaryAccount.getUserId();
        String beneficiaryBank = beneficiaryAccount.getBank();

        // ✅ Fetch beneficiary user details
        Optional<User> userOptional = userRepository.findById(beneficiaryUserId);
        if (userOptional.isEmpty()) {
            response.put("success", false);
            response.put("message", "User does not exist");
            response.put("data",null);
            return response;

        }

        if(userOptional.get().getUserId()==userId && userBank.equals(beneficiaryBank)){
            response.put("success", false);
            response.put("message", "Can not add the same person as beneficiary  : " + beneficiaryUserId);
            response.put("data",null);

        }
        User beneficiaryUser = userOptional.get();
       // String beneficiaryName = beneficiaryUser.getFirstName() + " " + beneficiaryUser.getLastName();

        // ✅ Create new Beneficiary entry
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setUserId(userId);
        beneficiary.setBeneficiaryUserId(beneficiaryUserId);
        beneficiary.setBeneficiaryAccountNumber(accountNumber);
        beneficiary.setBeneficiaryBank(beneficiaryBank);
        beneficiary.setBeneficiaryName(name);
        beneficiary.setAmount(amount);
        response.put("success", true);
        response.put("data",beneficiary);
        response.put("message", "Successfully added beneficiary");

        return response;
    }


    public void deleteBeneficiary(Integer userId, Integer beneficiaryId) {
        Optional<Beneficiary> beneficiaryOptional = beneficiaryRepository.findById(beneficiaryId);

        if (beneficiaryOptional.isEmpty()) {
            throw new RuntimeException("Beneficiary not found");
        }

        Beneficiary beneficiary = beneficiaryOptional.get();
        if (!beneficiary.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You cannot delete this beneficiary");
        }

        beneficiaryRepository.delete(beneficiary);
    }

    // ✅ Update Beneficiary Amount
    public Beneficiary updateBeneficiaryAmount(Integer userId, Integer beneficiaryId, BigDecimal newAmount) {
        Optional<Beneficiary> beneficiaryOptional = beneficiaryRepository.findById(beneficiaryId);

        if (beneficiaryOptional.isEmpty()) {
            throw new RuntimeException("Beneficiary not found");
        }

        Beneficiary beneficiary = beneficiaryOptional.get();
        if (!beneficiary.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You cannot update this beneficiary");
        }

        beneficiary.setAmount(newAmount);

        return beneficiaryRepository.save(beneficiary);
    }

    public List<Beneficiary> getBeneficiaries(Integer userId, String userBank, boolean sameBank) {
        if (sameBank) {
            // ✅ Fetch beneficiaries from the same bank
            return beneficiaryRepository.findByUserIdAndBeneficiaryBank(userId, userBank);
        } else {
            // ✅ Fetch beneficiaries from a different bank
            return beneficiaryRepository.findByUserIdAndBeneficiaryBankNot(userId, userBank);
        }
    }
    
}
