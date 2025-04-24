package com.secure.repository;

import com.secure.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    // Find all accounts belonging to a specific user
    List<Account> findByUserId(Integer userId);



    // Find an account by account number
    Optional<Account> findByAccountNumber(String accountNumber);

    // Find all accounts by bank name
    List<Account> findByBank(String bank);

    // Find all active accounts for a user
    List<Account> findByUserIdAndStatus(Integer userId, Account.AccountStatus status);
    Account findByAccountNumberAndIfscCode(String accountNumber, String ifscCode);


    // Find all accounts for a user in a specific bank
    List<Account> findByUserIdAndBank(Integer userId, String bank);

    boolean existsByUserIdAndBank(Integer userId, String bank);

}
