package com.secure.services;

import com.secure.model.Account;
import com.secure.repository.AccountRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

   
    public List<Account> getAccountsByUserId(Integer userId) {
        return accountRepository.findByUserId(userId);
    }

    
    public Optional<Account> getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    
    public List<Account> getAccountsByBank(String bank) {
        return accountRepository.findByBank(bank);
    }

   
    public List<Account> getActiveAccountsByUserId(Integer userId) {
        return accountRepository.findByUserIdAndStatus(userId, Account.AccountStatus.ACTIVE);
    }

    
    public List<Account> getAccountsByUserIdAndBank(Integer userId, String bank) {
        return accountRepository.findByUserIdAndBank(userId, bank);
    }

    
    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    
    public void deleteAccount(Integer accountId) {
        accountRepository.deleteById(accountId);
    }
}
