package com.secure.repository;

import com.secure.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
    Optional<Admin> findByEmail(String email);
    Boolean existsByEmail(String email);
    Optional<Admin> findByAadharCard(String aadharCard);
    Optional<Admin> findByPanCard(String panCard);
    void deleteByEmail(String email);
}