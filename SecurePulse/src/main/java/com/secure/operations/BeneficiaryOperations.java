package com.secure.operations;

import com.secure.model.Account;
import com.secure.model.Beneficiary;
import com.secure.model.Compare;
import com.secure.model.User;
import com.secure.repository.AccountRepository;
import com.secure.repository.BeneficiaryRepository;
import com.secure.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

        System.out.print(beneficiaryUserId+" "+userId);
        System.out.println(userBank + " " + beneficiaryBank);
        if(Objects.equals(userOptional.get().getUserId(), userId) && userBank.equals(beneficiaryBank)){
            response.put("success", false);
            response.put("message", "Can not add the same person as beneficiary  : " + beneficiaryUserId);
            response.put("data",null);
            return response;

        }


        // ✅ Create new Beneficiary entry
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setUserId(userId);
        beneficiary.setBeneficiaryUserId(beneficiaryUserId);
        beneficiary.setBeneficiaryAccountNumber(accountNumber);
        beneficiary.setBeneficiaryBank(beneficiaryBank);
        beneficiary.setBeneficiaryName(name);
        beneficiary.setAmount(amount);
        beneficiary.setIfscCode(ifscCode);
        beneficiaryRepository.save(beneficiary);
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


    public List<Beneficiary> getBeneficiariesForTransaction(Integer userId, String userBank, boolean same) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        if (same) {
            // Fetch beneficiaries from the same bank
            return beneficiaryRepository.findByUserIdAndBeneficiaryBankAndUpdatedAtLessThanEqual(userId, userBank, oneHourAgo);
        } else {
            // Fetch beneficiaries from different banks
            return beneficiaryRepository.findByUserIdAndBeneficiaryBankNotAndUpdatedAtLessThanEqual(userId, userBank, oneHourAgo);
        }
    }
    public Map<String,Object> compareBeneficiary(Integer userId, Integer beneficiaryId,String userBank) {
        Map<String, Object> response = new HashMap<>();
        Optional<Beneficiary> beneficiaryOptional = beneficiaryRepository.findById(beneficiaryId);

        if (beneficiaryOptional.isEmpty()) {
            response.put("success", false);
            response.put("message", "Beneficiary not found");
            response.put("data", null);
            return response;
        }
        Account useraccount = accountRepository.findByUserIdAndBank(userId,userBank).get(0);
        Beneficiary beneficiary = beneficiaryOptional.get();
        Account beneficiaryAccount = accountRepository.findByUserIdAndBank(beneficiary.getBeneficiaryUserId(),beneficiary.getBeneficiaryBank()).get(0);
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<User> beneficiaryUserOptional = userRepository.findById(beneficiary.getBeneficiaryUserId());
        if (userOptional.isEmpty() || beneficiaryUserOptional.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found");
            response.put("data", null);
            return response;
        }
        String beneficiaryIfsc = beneficiaryAccount.getIfscCode();
        String userIfscCode = useraccount.getIfscCode();
        String userName = userOptional.get().getFirstName() + " " + userOptional.get().getLastName();
        String beneficiaryName = beneficiaryUserOptional.get().getFirstName() + " " + beneficiaryUserOptional.get().getLastName();
        String userAccountNumber = useraccount.getAccountNumber();
        String beneficiaryAccountNumber = beneficiaryAccount.getAccountNumber();
        Compare compare = new Compare(userName, beneficiaryName, userAccountNumber, beneficiaryAccountNumber, userIfscCode, beneficiaryIfsc);
        response.put("success", true);
        response.put("data", compare);
        return response;



    }

}