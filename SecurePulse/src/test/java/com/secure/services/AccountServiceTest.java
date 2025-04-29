package com.secure.services;

import com.secure.model.Account;
import com.secure.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;


    @Test
    void getAccountsByUserId() {
         //Mock the behavior of accountRepository
         when(accountRepository.findByUserId(1)).thenReturn(List.of(new Account()));

         //Call the method to test
         List<Account> accounts = accountService.getAccountsByUserId(1);

         //Verify the result
         assertNotNull(accounts);
         assertFalse(accounts.isEmpty());
    }

    @Test
    void getAccountByNumber() {
        //Mock the behavior of accountRepository
        when(accountRepository.findByAccountNumber("123456")).thenReturn(java.util.Optional.of(new Account()));

        //Call the method to test
        java.util.Optional<Account> account = accountService.getAccountByNumber("123456");

        //Verify the result
        assertTrue(account.isPresent());
    }

    @Test
    void getAccountsByBank() {
        //Mock the behavior of accountRepository
        when(accountRepository.findByBank("BankName")).thenReturn(List.of(new Account()));

        //Call the method to test
        List<Account> accounts = accountService.getAccountsByBank("BankName");

        //Verify the result
        assertNotNull(accounts);
        assertFalse(accounts.isEmpty());
    }

    @Test
    void getActiveAccountsByUserId() {
        //Mock the behavior of accountRepository
        when(accountRepository.findByUserIdAndStatus(1, Account.AccountStatus.ACTIVE)).thenReturn(List.of(new Account()));

        //Call the method to test
        List<Account> activeAccounts = accountService.getActiveAccountsByUserId(1);

        //Verify the result
        assertNotNull(activeAccounts);
        assertFalse(activeAccounts.isEmpty());
    }

    @Test
    void getAccountsByUserIdAndBank() {
        //Mock the behavior of accountRepository
        when(accountRepository.findByUserIdAndBank(1, "BankName")).thenReturn(List.of(new Account()));

        //Call the method to test
        List<Account> accounts = accountService.getAccountsByUserIdAndBank(1, "BankName");

        //Verify the result
        assertNotNull(accounts);
        assertFalse(accounts.isEmpty());
    }

    @Test
    void saveAccount() {
        //Mock the behavior of accountRepository
        Account account = new Account();
        when(accountRepository.save(account)).thenReturn(account);

        //Call the method to test
        Account savedAccount = accountService.saveAccount(account);

        //Verify the result
        assertNotNull(savedAccount);
    }

    @Test
    void deleteAccount() {
        //Mock the behavior of accountRepository
        Integer accountId = 1;
        doNothing().when(accountRepository).deleteById(accountId);
        accountService.deleteAccount(accountId);
        assertDoesNotThrow(() -> accountService.deleteAccount(accountId));
        //Verify the interaction with the repository
        // (In this case, we don't have a return value to assert)
        // You can use verify(accountRepository).deleteById(accountId) if you want to check the interaction.
    }
}