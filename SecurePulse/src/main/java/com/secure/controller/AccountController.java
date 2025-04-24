package com.secure.controller;

import com.secure.exception.CustomException;
import com.secure.model.Account;
import com.secure.services.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// AccountController handles all account-related operations such as retrieving, adding, etc.
@RestController
@RequestMapping("api/accounts")
public class AccountController {

    private final AccountService accountService;

    // Constructor for AccountController.
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // Retrieves all accounts associated with a specific user.
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Account>> getAccountsByUserId(@PathVariable Integer userId) {
        try {
            List<Account> accounts = accountService.getAccountsByUserId(userId);
            if (accounts.isEmpty()) {
                throw new CustomException("No accounts found for the given user ID");
            }
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            throw new CustomException("An error occurred while retrieving accounts for the user");
        }
    }

    // Retrieves account details by account number.
    @GetMapping("/{accountNumber}")
    public ResponseEntity<Account> getAccountByNumber(@PathVariable String accountNumber) {
        try {
            Account account = accountService.getAccountByNumber(accountNumber)
                    .orElseThrow(() -> new CustomException("Account not found for the given account number"));
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            throw new CustomException("An error occurred while retrieving the account details");
        }
    }

    // Retrieves all accounts associated with a specific bank.
    @GetMapping("/bank/{bank}")
    public ResponseEntity<List<Account>> getAccountsByBank(@PathVariable String bank) {
        try {
            List<Account> accounts = accountService.getAccountsByBank(bank);
            if (accounts.isEmpty()) {
                throw new CustomException("No accounts found for the given bank");
            }
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            throw new CustomException("An error occurred while retrieving accounts for the bank");
        }
    }

    // Retrieves all active accounts for a specific user.
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<Account>> getActiveAccountsByUserId(@PathVariable Integer userId) {
        try {
            List<Account> accounts = accountService.getActiveAccountsByUserId(userId);
            if (accounts.isEmpty()) {
                throw new CustomException("No active accounts found for the given user ID");
            }
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            throw new CustomException("An error occurred while retrieving active accounts for the user");
        }
    }

    // Retrieves all accounts for a specific user in a specific bank.
    @GetMapping("/user/{userId}/bank/{bank}")
    public ResponseEntity<List<Account>> getAccountsByUserIdAndBank(@PathVariable Integer userId, @PathVariable String bank) {
        try {
            List<Account> accounts = accountService.getAccountsByUserIdAndBank(userId, bank);
            if (accounts.isEmpty()) {
                throw new CustomException("No accounts found for the given user ID and bank");
            }
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            throw new CustomException("An error occurred while retrieving accounts for the user and bank");
        }
    }

    // Adds or updates an account.
    @PostMapping("/save")
    public ResponseEntity<Account> saveAccount(@RequestBody Account account) {
        try {
            if (account == null) {
                throw new CustomException("Account details cannot be null");
            }
            Account savedAccount = accountService.saveAccount(account);
            return ResponseEntity.ok(savedAccount);
        } catch (Exception e) {
            throw new CustomException("An error occurred while saving the account");
        }
    }

    // Deletes an account by its ID.
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Integer accountId) {
        try {

            accountService.deleteAccount(accountId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            throw new CustomException("An error occurred while deleting the account");
        }
    }
}