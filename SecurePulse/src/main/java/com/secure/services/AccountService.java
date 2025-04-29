package com.secure.services;

import com.secure.model.Account;
import com.secure.repository.AccountRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Service layer responsible for handling operations related to Account entities.
 */
@Component
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Retrieves all accounts associated with a specific user ID.
     *
     * @param userId the ID of the user
     * @return list of accounts belonging to the user
     */
    public List<Account> getAccountsByUserId(Integer userId) {
        return accountRepository.findByUserId(userId);
    }

    /**
     * Retrieves an account by its account number.
     *
     * @param accountNumber the account number
     * @return optional account if found
     */
    public Optional<Account> getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    /**
     * Retrieves all accounts associated with a specific bank.
     *
     * @param bank the name of the bank
     * @return list of accounts from the specified bank
     */
    public List<Account> getAccountsByBank(String bank) {
        return accountRepository.findByBank(bank);
    }

    /**
     * Retrieves all active accounts for a given user.
     *
     * @param userId the ID of the user
     * @return list of active accounts
     */
    public List<Account> getActiveAccountsByUserId(Integer userId) {
        return accountRepository.findByUserIdAndStatus(userId, Account.AccountStatus.ACTIVE);
    }

    /**
     * Retrieves accounts for a user that belong to a specific bank.
     *
     * @param userId the ID of the user
     * @param bank the name of the bank
     * @return list of user's accounts in the given bank
     */
    public List<Account> getAccountsByUserIdAndBank(Integer userId, String bank) {
        return accountRepository.findByUserIdAndBank(userId, bank);
    }

    /**
     * Saves or updates an account in the database.
     *
     * @param account the account entity to be saved
     * @return the saved account entity
     */
    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    /**
     * Deletes an account based on its ID.
     *
     * @param accountId the ID of the account to be deleted
     */
    public void deleteAccount(Integer accountId) {
        accountRepository.deleteById(accountId);
    }
}
