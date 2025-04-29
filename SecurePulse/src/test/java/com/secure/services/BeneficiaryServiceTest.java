package com.secure.services;

import com.secure.exception.CustomException;
import com.secure.model.*;
import com.secure.repository.*;
import com.secure.utils.EmailProvider;
import com.secure.utils.TemplateProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BeneficiaryServiceTest {

    @InjectMocks
    private BeneficiaryService beneficiaryService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailProvider emailProvider;

    @Mock
    private TemplateProvider templateProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        beneficiaryService = new BeneficiaryService(accountRepository, beneficiaryRepository, userRepository, emailProvider, templateProvider);
    }

    @Test
    void addBeneficiary_success() {
        Integer userId = 1;
        String userBank = "BankA";
        String accountNumber = "123456";
        String ifscCode = "BANK0001";
        BigDecimal amount = new BigDecimal("5000");
        String name = "John";

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setIfscCode(ifscCode);
        account.setUserId(2);
        account.setBank("BankB");

        User user = new User();
        user.setUserId(2);

        when(beneficiaryRepository.findByUserIdAndBeneficiaryAccountNumber(userId, accountNumber)).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        when(userRepository.findById(2)).thenReturn(Optional.of(user));
        when(userRepository.getById(userId)).thenReturn(new User() {{ setEmail("user@example.com"); }});
        when(templateProvider.buildBeneficiaryAddedEmail(any(), any())).thenReturn("Email Body");
        doNothing().when(emailProvider).sendEmail(any(), any(), any());

        Map<String, Object> response = beneficiaryService.addBeneficiary(userId, userBank, accountNumber, ifscCode, amount, name);

        assertTrue((Boolean) response.get("success"));
        assertEquals("Successfully added beneficiary", response.get("message"));
        verify(beneficiaryRepository, times(1)).save(any(Beneficiary.class));
    }


    @Test
    void deleteBeneficiary_success() {
        Integer userId = 1;
        Integer beneficiaryId = 2;
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setUserId(userId);

        when(beneficiaryRepository.findById(beneficiaryId)).thenReturn(Optional.of(beneficiary));

        beneficiaryService.deleteBeneficiary(userId, beneficiaryId);

        verify(beneficiaryRepository, times(1)).delete(beneficiary);
    }


    @Test
    void updateBeneficiaryAmount_success() {
        Integer userId = 1;
        Integer beneficiaryId = 2;
        BigDecimal newAmount = new BigDecimal("7000");

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setUserId(userId);
        beneficiary.setAmount(new BigDecimal("5000"));

        when(beneficiaryRepository.findById(beneficiaryId)).thenReturn(Optional.of(beneficiary));
        when(userRepository.getById(userId)).thenReturn(new User() {{ setEmail("user@example.com"); }});
        when(templateProvider.buildTransferLimitUpdatedEmail(any(), any())).thenReturn("Email Body");
        doNothing().when(emailProvider).sendEmail(any(), any(), any());
        when(beneficiaryRepository.save(any())).thenReturn(beneficiary);

        Beneficiary result = beneficiaryService.updateBeneficiaryAmount(userId, beneficiaryId, newAmount);

        assertEquals(newAmount, result.getAmount());
        verify(emailProvider, times(1)).sendEmail(any(), any(), any());
    }

    @Test
    void getBeneficiaries_sameBank() {
        Integer userId = 1;
        String userBank = "BankA";

        when(beneficiaryRepository.findByUserIdAndBeneficiaryBank(userId, userBank)).thenReturn(Collections.emptyList());

        List<Beneficiary> result = beneficiaryService.getBeneficiaries(userId, userBank, true);

        assertNotNull(result);
    }

    @Test
    void getBeneficiaries_differentBank() {
        Integer userId = 1;
        String userBank = "BankA";

        when(beneficiaryRepository.findByUserIdAndBeneficiaryBankNot(userId, userBank)).thenReturn(Collections.emptyList());

        List<Beneficiary> result = beneficiaryService.getBeneficiaries(userId, userBank, false);

        assertNotNull(result);
    }


    @Test
    void getBeneficiariesForTransaction_sameBank() {
        Integer userId = 1;
        String userBank = "BankA";

        when(beneficiaryRepository.findByUserIdAndBeneficiaryBankAndUpdatedAtLessThanEqual(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<Beneficiary> result = beneficiaryService.getBeneficiariesForTransaction(userId, userBank, true);

        assertNotNull(result);
    }

    @Test
    void getBeneficiariesForTransaction_differentBank() {
        Integer userId = 1;
        String userBank = "BankA";

        when(beneficiaryRepository.findByUserIdAndBeneficiaryBankNotAndUpdatedAtLessThanEqual(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<Beneficiary> result = beneficiaryService.getBeneficiariesForTransaction(userId, userBank, false);

        assertNotNull(result);
    }


    @Test
    void compareBeneficiary_success() {
        Integer userId = 1;
        Integer beneficiaryId = 2;
        String userBank = "BankA";

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setUserId(userId);
        beneficiary.setBeneficiaryUserId(3);
        beneficiary.setBeneficiaryBank("BankB");

        Account userAccount = new Account();
        userAccount.setIfscCode("IFSC123");
        userAccount.setAccountNumber("1111");

        Account beneficiaryAccount = new Account();
        beneficiaryAccount.setIfscCode("IFSC456");
        beneficiaryAccount.setAccountNumber("2222");

        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");

        User beneficiaryUser = new User();
        beneficiaryUser.setFirstName("Jane");
        beneficiaryUser.setLastName("Smith");

        when(beneficiaryRepository.findById(beneficiaryId)).thenReturn(Optional.of(beneficiary));
        when(accountRepository.findByUserIdAndBank(userId, userBank)).thenReturn(List.of(userAccount));
        when(accountRepository.findByUserIdAndBank(3, "BankB")).thenReturn(List.of(beneficiaryAccount));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(3)).thenReturn(Optional.of(beneficiaryUser));

        Map<String, Object> result = beneficiaryService.compareBeneficiary(userId, beneficiaryId, userBank);

        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("data"));
    }
}



