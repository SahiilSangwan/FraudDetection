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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.*;
import java.time.format.DateTimeFormatter;

@Service
public class BeneficiaryService {

    @Autowired
    private EmailProvider emailProvider;

    private final AccountRepository accountRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;

    public BeneficiaryService(AccountRepository accountRepository, BeneficiaryRepository beneficiaryRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.userRepository = userRepository;
    }

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

        String messageBody = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <style>" +
                "        body { font-family: 'Arial', sans-serif; background-color: #f5f7fa; margin: 0; padding: 0; }" +
                "        .container { max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 8px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }" +
                "        .header { background-color: #2c3e50; padding: 20px; border-radius: 8px 8px 0 0; text-align: center; }" +
                "        .header h1 { color: #ffffff; margin: 0; }" +
                "        .header p { color: #ecf0f1; margin: 5px 0 0; font-size: 14px; }" +
                "        .content { padding: 30px; text-align: center; }" +
                "        .success-icon { color: #2ecc71; font-size: 48px; margin-bottom: 20px; }" +
                "        .details-box { background: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0; text-align: left; }" +
                "        .details-box p { margin: 8px 0; }" +
                "        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; border-radius: 0 0 8px 8px; font-size: 12px; color: #7f8c8d; }" +
                "        .note { color: #e74c3c; font-size: 13px; margin-top: 20px; font-style: italic; }" +
                "        .divider { border-top: 1px solid #eee; margin: 25px 0; }" +
                "        .cooling-period { background: #fff8e1; padding: 10px; border-left: 4px solid #ffc107; margin: 15px 0; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>SecurePulse</h1>" +
                "            <p>by WISSEN Technology</p>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <div class='success-icon'>✓</div>" +
                "            <h2 style='color: #2c3e50;'>Beneficiary Added Successfully</h2>" +
                "            <p>Your new beneficiary has been registered and will be activated after the security cooling period.</p>" +
                "            " +
                "            <div class='details-box'>" +
                "                <p><strong>Beneficiary Name:</strong> " + beneficiary.getBeneficiaryName() + "</p>" +
                "                <p><strong>Account Number:</strong> " + beneficiary.getBeneficiaryAccountNumber() + "</p>" +
                "                <p><strong>IFSC Code:</strong> " + beneficiary.getIfscCode() + "</p>" +
                "                <p><strong>Transfer Limit:</strong> ₹" + beneficiary.getAmount() + "</p>" +
                "            </div>" +
                "            " +
                "            <div class='cooling-period'>" +
                "            <p><strong>⏳ Security Cooling Period:</strong> For your safety, transfers to this beneficiary will be enabled after <strong>1 hour</strong> (approx. " + formattedTime + ").</p>" +
                "            </div>" +
                "            " +
                "            <div class='divider'></div>" +
                "            " +
                "            <div class='note'>" +
                "                <p>If you did not authorize this action, please <a href='' style='color: #3498db;'>contact support</a> immediately.</p>" +
                "            </div>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>© 2023 SecurePulse by WISSEN Technology. All rights reserved.</p>" +
                "            <p>123 Embassey Tech Park, Bengaluru | support@securepulse.com</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";

        emailProvider.sendEmail(emailUser, subject, messageBody);

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

        String emailUser = userRepository.getById(userId).getEmail();

        String subject = "Beneficiary Transfer Limit Updated - SecurePulse";

        LocalDateTime activationTime = LocalDateTime.now().plusHours(1);
        String formattedTime = activationTime.format(DateTimeFormatter.ofPattern("hh:mm a, dd MMM yyyy"));

        String messageBody = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <style>" +
                "        body { font-family: 'Arial', sans-serif; background-color: #f5f7fa; margin: 0; padding: 0; }" +
                "        .container { max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 8px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }" +
                "        .header { background-color: #2c3e50; padding: 20px; border-radius: 8px 8px 0 0; text-align: center; }" +
                "        .header h1 { color: #ffffff; margin: 0; }" +
                "        .header p { color: #ecf0f1; margin: 5px 0 0; font-size: 14px; }" +
                "        .content { padding: 30px; text-align: center; }" +
                "        .success-icon { color: #2ecc71; font-size: 48px; margin-bottom: 20px; }" +
                "        .details-box { background: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0; text-align: left; }" +
                "        .details-box p { margin: 8px 0; }" +
                "        .footer { background-color: #f8f9fa; padding: 20px; text-align: center; border-radius: 0 0 8px 8px; font-size: 12px; color: #7f8c8d; }" +
                "        .note { color: #e74c3c; font-size: 13px; margin-top: 20px; font-style: italic; }" +
                "        .divider { border-top: 1px solid #eee; margin: 25px 0; }" +
                "        .cooling-period { background: #fff8e1; padding: 10px; border-left: 4px solid #ffc107; margin: 15px 0; }" +
                "        .changes { background: #e8f5e9; padding: 10px; border-left: 4px solid #4caf50; margin: 15px 0; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>SecurePulse</h1>" +
                "            <p>by WISSEN Technology</p>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <div class='success-icon'>✓</div>" +
                "            <h2 style='color: #2c3e50;'>Transfer Limit Updated Successfully</h2>" +
                "            <p>The transfer limit for your beneficiary has been updated and will be activated after the security cooling period.</p>" +
                "            " +
                "            <div class='details-box'>" +
                "                <p><strong>Beneficiary Name:</strong> " + beneficiary.getBeneficiaryName() + "</p>" +
                "                <p><strong>Account Number:</strong> " + beneficiary.getBeneficiaryAccountNumber() + "</p>" +
                "                <p><strong>IFSC Code:</strong> " + beneficiary.getIfscCode() + "</p>" +
                "            </div>" +
                "            " +
                "            <div class='changes'>" +
                "                <p><strong>New Transfer Limit:</strong> ₹" + beneficiary.getAmount() + "</p>" +
                "            </div>" +
                "            " +
                "            <div class='cooling-period'>" +
                "                <p><strong>⏳ Security Cooling Period:</strong> The updated limit will be effective after <strong>1 hour</strong> (approx. " + formattedTime + ").</p>" +
                "            </div>" +
                "            " +
                "            <div class='divider'></div>" +
                "            " +
                "            <div class='note'>" +
                "                <p>If you did not authorize this change, please <a href='' style='color: #3498db;'>contact support</a> immediately.</p>" +
                "            </div>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>© 2023 SecurePulse by WISSEN Technology. All rights reserved.</p>" +
                "            <p>123 Embassey Tech Park, Bengaluru | support@securepulse.com</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";

            emailProvider.sendEmail(emailUser, subject, messageBody);

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
            throw e; // Let the global exception handler handle this
        } catch (Exception e) {
            throw new CustomException("An error occurred while comparing the beneficiary");
        }
    }
}