package com.secure.controller;

import com.secure.exception.CustomException;
import com.secure.model.Account;
import com.secure.model.Account.AccountStatus;
import com.secure.repository.AccountRepository;
import com.secure.services.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountService accountService;

    private AccountController accountController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        accountService = new AccountService(accountRepository);
        accountController = new AccountController(accountService);
    }

    @Test
    void getAccountsByUserId_shouldReturnAccounts() {
        List<Account> mockAccounts = List.of(new Account());
        when(accountRepository.findByUserId(1)).thenReturn(mockAccounts);

        ResponseEntity<List<Account>> response = accountController.getAccountsByUserId(1);

        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void getAccountByNumber_shouldReturnAccount() {
        Account mockAccount = new Account();
        when(accountRepository.findByAccountNumber("123456")).thenReturn(Optional.of(mockAccount));

        ResponseEntity<Account> response = accountController.getAccountByNumber("123456");

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void getAccountsByBank_shouldReturnAccounts() {
        List<Account> mockAccounts = List.of(new Account());
        when(accountRepository.findByBank("HDFC")).thenReturn(mockAccounts);

        ResponseEntity<List<Account>> response = accountController.getAccountsByBank("HDFC");

        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void getActiveAccountsByUserId_shouldReturnActiveAccounts() {
        List<Account> activeAccounts = List.of(new Account());
        when(accountRepository.findByUserIdAndStatus(1, AccountStatus.ACTIVE)).thenReturn(activeAccounts);

        ResponseEntity<List<Account>> response = accountController.getActiveAccountsByUserId(1);

        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void getAccountsByUserIdAndBank_shouldReturnAccounts() {
        List<Account> accounts = List.of(new Account());
        when(accountRepository.findByUserIdAndBank(1, "HDFC")).thenReturn(accounts);

        ResponseEntity<List<Account>> response = accountController.getAccountsByUserIdAndBank(1, "HDFC");

        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void saveAccount_shouldSaveAndReturnAccount() {
        Account inputAccount = new Account();
        when(accountRepository.save(inputAccount)).thenReturn(inputAccount);

        ResponseEntity<Account> response = accountController.saveAccount(inputAccount);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void deleteAccount_shouldInvokeRepositoryDelete() {
        doNothing().when(accountRepository).deleteById(1);

        ResponseEntity<Void> response = accountController.deleteAccount(1);

        verify(accountRepository, times(1)).deleteById(1);
        assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    void getAccountsByUserId_shouldThrowExceptionWhenNoAccounts() {
        when(accountRepository.findByUserId(1)).thenReturn(Collections.emptyList());

        CustomException exception = assertThrows(CustomException.class, () -> {
            accountController.getAccountsByUserId(1);
        });

        assertEquals("An error occurred while retrieving accounts for the user", exception.getMessage());
    }

    @Test
    void getAccountByNumber_shouldThrowExceptionWhenAccountNotFound() {
        when(accountRepository.findByAccountNumber("123456")).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> {
            accountController.getAccountByNumber("123456");
        });

        assertEquals("An error occurred while retrieving the account details", exception.getMessage());
    }

    @Test
    void getAccountsByBank_shouldThrowExceptionWhenNoAccounts() {
        when(accountRepository.findByBank("HDFC")).thenReturn(Collections.emptyList());

        CustomException exception = assertThrows(CustomException.class, () -> {
            accountController.getAccountsByBank("HDFC");
        });

        assertEquals("An error occurred while retrieving accounts for the bank", exception.getMessage());
    }

    @Test
    void getActiveAccountsByUserId_shouldThrowExceptionWhenNoActiveAccounts() {
        when(accountRepository.findByUserIdAndStatus(1, AccountStatus.ACTIVE)).thenReturn(Collections.emptyList());

        CustomException exception = assertThrows(CustomException.class, () -> {
            accountController.getActiveAccountsByUserId(1);
        });

        assertEquals("An error occurred while retrieving active accounts for the user", exception.getMessage());
    }

    @Test
    void getAccountsByUserIdAndBank_shouldThrowExceptionWhenNoAccounts() {
        when(accountRepository.findByUserIdAndBank(1, "HDFC")).thenReturn(Collections.emptyList());

        CustomException exception = assertThrows(CustomException.class, () -> {
            accountController.getAccountsByUserIdAndBank(1, "HDFC");
        });

        assertEquals("An error occurred while retrieving accounts for the user and bank", exception.getMessage());
    }

    @Test
    void saveAccount_shouldThrowExceptionWhenAccountIsNull() {
        CustomException exception = assertThrows(CustomException.class, () -> {
            accountController.saveAccount(null);
        });

        assertEquals("An error occurred while saving the account", exception.getMessage());
    }

    @Test
    void deleteAccount_shouldThrowExceptionOnRepositoryError() {
        doThrow(new RuntimeException("Database error")).when(accountRepository).deleteById(1);

        CustomException exception = assertThrows(CustomException.class, () -> {
            accountController.deleteAccount(1);
        });

        assertEquals("An error occurred while deleting the account", exception.getMessage());
    }







}
