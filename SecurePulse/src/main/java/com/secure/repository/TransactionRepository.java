package com.secure.repository;

import com.secure.model.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findBySenderId(Integer senderId);
    List<Transaction> findByReceiverId(Integer receiverId);
    List<Transaction> findByMarked(Transaction.TransactionMarked marked);

    List<Transaction> findBySenderIdAndFlag(Integer senderId,Transaction.TransactionFlag flag);
    List<Transaction> findByReceiverIdAndFlag(Integer recieverId,Transaction.TransactionFlag flag);


    @Query("SELECT t FROM Transaction t WHERE " +
            "(t.senderAccountNumber IN (SELECT a.accountNumber FROM Account a WHERE a.bank = :bank) OR " +
            "t.receiverAccountNumber IN (SELECT a.accountNumber FROM Account a WHERE a.bank = :bank)) " +
            "ORDER BY t.timestamp DESC")
    List<Transaction> findAllByBank(@Param("bank") String bank);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(t.senderAccountNumber IN (SELECT a.accountNumber FROM Account a WHERE a.bank = :bank) OR " +
            "t.receiverAccountNumber IN (SELECT a.accountNumber FROM Account a WHERE a.bank = :bank)) " +
            "ORDER BY t.timestamp DESC")
    List<Transaction> findRecentByBank(@Param("bank") String bank, Pageable pageable);
}


