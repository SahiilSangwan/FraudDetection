package com.secure.repository;

import com.secure.model.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Integer> {

    // Find all beneficiaries by the logged-in user's ID
    List<Beneficiary> findByUserId(Integer userId);

    // Find all beneficiaries where a specific user is the beneficiary
    List<Beneficiary> findByBeneficiaryUserId(Integer beneficiaryUserId);

    // Find a beneficiary by account number
    Optional<Beneficiary> findByBeneficiaryAccountNumber(String beneficiaryAccountNumber);

    // Check if a beneficiary exists for a specific user
    boolean existsByUserIdAndBeneficiaryAccountNumber(Integer userId, String beneficiaryAccountNumber);

    // Find all beneficiaries for a specific bank
    List<Beneficiary> findByBeneficiaryBank(String beneficiaryBank);

    // Delete a beneficiary by ID
    void deleteByBeneficiaryId(Integer beneficiaryId);

    List<Beneficiary> findByUserIdAndBeneficiaryBank(Integer userId, String bank);

    // ✅ Find beneficiaries from a different bank
    List<Beneficiary> findByUserIdAndBeneficiaryBankNot(Integer userId, String bank);

    Optional<Beneficiary> findByUserIdAndBeneficiaryAccountNumber(Integer userId, String accountNumber);
}
