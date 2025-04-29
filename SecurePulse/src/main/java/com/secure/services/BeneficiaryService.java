package com.secure.services;

import com.secure.exception.CustomException;
import com.secure.model.Account;
import com.secure.model.Beneficiary;
import com.secure.model.Compare;
import com.secure.model.User;
import com.secure.repository.AccountRepository;
import com.secure.repository.BeneficiaryRepository;
import com.secure.repository.UserRepository;
import com.secure.utils.EmailProvider;
import com.secure.utils.TemplateProvider;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.time.format.DateTimeFormatter;

/**
 * Service layer for managing beneficiaries, including creation, deletion,
 * updates, and data retrieval related to transfer validations.
 */
@Service
public class BeneficiaryService {

    private final EmailProvider emailProvider;
    private final AccountRepository accountRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;
    private final TemplateProvider templateProvider;

    public BeneficiaryService(AccountRepository accountRepository, BeneficiaryRepository beneficiaryRepository, UserRepository userRepository,
                              EmailProvider emailProvider, TemplateProvider templateProvider) {
        this.accountRepository = accountRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.userRepository = userRepository;
        this.emailProvider = emailProvider;
        this.templateProvider = templateProvider;
    }

    /**
     * Adds a new beneficiary to a user's account after validating account details and IFSC.
     * Sends a confirmation email after successful addition.
     *
     * @param userId        the ID of the user adding the beneficiary
     * @param userBank      the bank of the user
     * @param accountNumber the account number of the beneficiary
     * @param ifscCode      the IFSC code of the beneficiary's account
     * @param amount        the transfer limit for the beneficiary
     * @param name          the name of the beneficiary
     * @return response map containing success flag, beneficiary data, and a message
     */
    public Map<String, Object> addBeneficiary(Integer userId, String userBank, String accountNumber, String ifscCode, BigDecimal amount, String name) {
        Map<String, Object> response = new HashMap<>();

        Optional<Beneficiary> existingBeneficiary = beneficiaryRepository.findByUserIdAndBeneficiaryAccountNumber(userId, accountNumber);
        if (existingBeneficiary.isPresent()) {
            throw new CustomException("Beneficiary already exists");
        }

        Optional<Account> accountOptional = accountRepository.findByAccountNumber(accountNumber);
        if (accountOptional.isEmpty()) {
            throw new CustomException("Account does not exist");
        }

        Account beneficiaryAccount = accountOptional.get();
        if (!beneficiaryAccount.getIfscCode().equals(ifscCode)) {
            throw new CustomException("Account does not match IFSC code");
        }

        Integer beneficiaryUserId = beneficiaryAccount.getUserId();
        String beneficiaryBank = beneficiaryAccount.getBank();

        Optional<User> userOptional = userRepository.findById(beneficiaryUserId);
        if (userOptional.isEmpty()) {
            throw new CustomException("User does not exist");
        }

        if (Objects.equals(userOptional.get().getUserId(), userId) && userBank.equals(beneficiaryBank)) {
            throw new CustomException("Cannot add the same person as beneficiary: " + beneficiaryUserId);
        }

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
        response.put("data", beneficiary);
        response.put("message", "Successfully added beneficiary");

        String emailUser = userRepository.getById(userId).getEmail();
        String subject = "New Beneficiary Added Successfully - SecurePulse";
        LocalDateTime activationTime = LocalDateTime.now().plusHours(1);
        String formattedTime = activationTime.format(DateTimeFormatter.ofPattern("hh:mm a, dd MMM yyyy"));

        String messageBody = templateProvider.buildBeneficiaryAddedEmail(beneficiary, formattedTime);
        emailProvider.sendEmail(emailUser, subject, messageBody);

        return response;
    }

    /**
     * Deletes a beneficiary only if it belongs to the user making the request.
     *
     * @param userId        the ID of the user
     * @param beneficiaryId the ID of the beneficiary to delete
     */
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

    /**
     * Updates the transfer amount limit for a beneficiary and sends a notification email.
     *
     * @param userId        the ID of the user
     * @param beneficiaryId the ID of the beneficiary
     * @param newAmount     the new transfer limit
     * @return updated Beneficiary object
     */
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

        String emailUser = userRepository.getById(userId).getEmail();
        String subject = "Beneficiary Transfer Limit Updated - SecurePulse";

        LocalDateTime activationTime = LocalDateTime.now().plusHours(1);
        String formattedTime = activationTime.format(DateTimeFormatter.ofPattern("hh:mm a, dd MMM yyyy"));

        String messageBody = templateProvider.buildTransferLimitUpdatedEmail(beneficiary, formattedTime);
        emailProvider.sendEmail(emailUser, subject, messageBody);

        return beneficiaryRepository.save(beneficiary);
    }

    /**
     * Fetches beneficiaries based on bank relation (same or different bank).
     *
     * @param userId   the ID of the user
     * @param userBank the bank of the user
     * @param sameBank whether to fetch from the same bank or not
     * @return list of beneficiaries
     */
    public List<Beneficiary> getBeneficiaries(Integer userId, String userBank, boolean sameBank) {
        if (sameBank) {
            return beneficiaryRepository.findByUserIdAndBeneficiaryBank(userId, userBank);
        } else {
            return beneficiaryRepository.findByUserIdAndBeneficiaryBankNot(userId, userBank);
        }
    }

    /**
     * Retrieves beneficiaries eligible for transactions (added/updated more than an hour ago).
     *
     * @param userId   the ID of the user
     * @param userBank the user's bank
     * @param same     whether the beneficiary is from the same bank
     * @return list of valid beneficiaries
     */
    public List<Beneficiary> getBeneficiariesForTransaction(Integer userId, String userBank, boolean same) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        if (same) {
            return beneficiaryRepository.findByUserIdAndBeneficiaryBankAndUpdatedAtLessThanEqual(userId, userBank, oneHourAgo);
        } else {
            return beneficiaryRepository.findByUserIdAndBeneficiaryBankNotAndUpdatedAtLessThanEqual(userId, userBank, oneHourAgo);
        }
    }

    /**
     * Compares user and beneficiary details for validation or UI display.
     *
     * @param userId        the user ID
     * @param beneficiaryId the beneficiary ID
     * @param userBank      the bank of the user
     * @return map containing success flag and comparison data
     */
    public Map<String, Object> compareBeneficiary(Integer userId, Integer beneficiaryId, String userBank) {
        try {
            Optional<Beneficiary> beneficiaryOptional = beneficiaryRepository.findById(beneficiaryId);
            if (beneficiaryOptional.isEmpty()) {
                throw new CustomException("Beneficiary not found");
            }

            Account userAccount = accountRepository.findByUserIdAndBank(userId, userBank)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new CustomException("User account not found for the given bank"));

            Beneficiary beneficiary = beneficiaryOptional.get();

            Account beneficiaryAccount = accountRepository.findByUserIdAndBank(beneficiary.getBeneficiaryUserId(), beneficiary.getBeneficiaryBank())
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new CustomException("Beneficiary account not found for the given bank"));

            Optional<User> userOptional = userRepository.findById(userId);
            Optional<User> beneficiaryUserOptional = userRepository.findById(beneficiary.getBeneficiaryUserId());

            if (userOptional.isEmpty() || beneficiaryUserOptional.isEmpty()) {
                throw new CustomException("User or Beneficiary user not found");
            }

            String beneficiaryIfsc = beneficiaryAccount.getIfscCode();
            String userIfscCode = userAccount.getIfscCode();
            String userName = userOptional.get().getFirstName() + " " + userOptional.get().getLastName();
            String beneficiaryName = beneficiaryUserOptional.get().getFirstName() + " " + beneficiaryUserOptional.get().getLastName();
            String userAccountNumber = userAccount.getAccountNumber();
            String beneficiaryAccountNumber = beneficiaryAccount.getAccountNumber();

            Compare compare = new Compare(userName, beneficiaryName, userAccountNumber, beneficiaryAccountNumber, userIfscCode, beneficiaryIfsc);

            return Map.of(
                    "success", true,
                    "data", compare
            );
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException("An error occurred while comparing the beneficiary");
        }
    }
}
