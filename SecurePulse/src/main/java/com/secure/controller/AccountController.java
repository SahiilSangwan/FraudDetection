package com.secure.controller;

import com.secure.model.Account;
import com.secure.operations.AccountOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/accounts")
public class AccountController {

    private final AccountOperations accountOperations;

    public AccountController(AccountOperations accountOperations) {
        this.accountOperations = accountOperations;
    }

    // Get all accounts for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Account>> getAccountsByUserId(@PathVariable Integer userId) {
        List<Account> accounts = accountOperations.getAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    // Get account by account number
    @GetMapping("/{accountNumber}")
    public ResponseEntity<Account> getAccountByNumber(@PathVariable String accountNumber) {
        Optional<Account> account = accountOperations.getAccountByNumber(accountNumber);
        return account.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get all accounts for a bank
    @GetMapping("/bank/{bank}")
    public ResponseEntity<List<Account>> getAccountsByBank(@PathVariable String bank) {
        List<Account> accounts = accountOperations.getAccountsByBank(bank);
        return ResponseEntity.ok(accounts);
    }

    // Get all active accounts for a user
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<Account>> getActiveAccountsByUserId(@PathVariable Integer userId) {
        List<Account> accounts = accountOperations.getActiveAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    // Get all accounts for a user in a specific bank
    @GetMapping("/user/{userId}/bank/{bank}")
    public ResponseEntity<List<Account>> getAccountsByUserIdAndBank(@PathVariable Integer userId, @PathVariable String bank) {
        List<Account> accounts = accountOperations.getAccountsByUserIdAndBank(userId, bank);
        return ResponseEntity.ok(accounts);
    }

    // Add or update an account
    @PostMapping("/save")
    public ResponseEntity<Account> saveAccount(@RequestBody Account account) {
        Account savedAccount = accountOperations.saveAccount(account);
        return ResponseEntity.ok(savedAccount);
    }

    // Delete an account by ID
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Integer accountId) {
        accountOperations.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }
}
