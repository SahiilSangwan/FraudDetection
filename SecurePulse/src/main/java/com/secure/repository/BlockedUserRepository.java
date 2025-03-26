package com.secure.repository;

import com.secure.model.BlockedUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlockedUserRepository extends JpaRepository<BlockedUser, Integer> {
    Optional<BlockedUser> findByEmailAndBankName(String email, String bankName);
}
