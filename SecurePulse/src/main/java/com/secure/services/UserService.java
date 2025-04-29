package com.secure.services;

import com.secure.exception.CustomException;
import com.secure.repository.AccountRepository;
import com.secure.utils.OtpProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.secure.model.*;
import com.secure.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class UserService {

    // Constants for common error messages
    private static final String USER_NOT_FOUND = "User not found";
    private static final String INVALID_MPIN = "Invalid MPIN";

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final OtpProvider otpProvider;

    // Constructor injection
    public UserService(UserRepository userRepository, AccountRepository accountRepository, BCryptPasswordEncoder passwordEncoder, OtpProvider otpProvider) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpProvider = otpProvider;
    }

    // Fetch all users from the database
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Fetch a user by ID
    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    // Fetch a user by email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Register a new user (encrypts password before saving)
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // Update existing user details by ID
    public User updateUser(Integer id, User userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setFirstName(userDetails.getFirstName());
                    user.setLastName(userDetails.getLastName());
                    user.setEmail(userDetails.getEmail());
                    user.setPhoneNumber(userDetails.getPhoneNumber());
                    user.setAddress(userDetails.getAddress());
                    user.setDateOfBirth(userDetails.getDateOfBirth());
                    user.setAadharCard(userDetails.getAadharCard());
                    user.setPanCard(userDetails.getPanCard());
                    user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
                    user.setFlag(userDetails.getFlag());
                    return userRepository.save(user);
                }).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    // Delete user by ID
    public String deleteUser(Integer id) {
        userRepository.deleteById(id);
        return "User deleted successfully";
    }

    // Validate login credentials and confirm the user has an account with the given bank
    public Optional<User> getUserByEmailAndPassword(String email, String rawPassword, String bank) {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent() && passwordEncoder.matches(rawPassword, user.get().getPassword())) {

            boolean hasAccount = accountRepository.existsByUserIdAndBank(user.get().getUserId(), bank);
            if (hasAccount) {
                return user;
            }
        }

        return Optional.empty();
    }

    // Set new MPIN for a user (only if 6 digits)
    public ResponseEntity<Map<String, Object>> setUserMpin(String email, String mpin) {
        if (mpin.length() != 6) {
            throw new CustomException("MPIN must be 6 digits");
        }

        Optional<User> userOpt = getUserByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setMpin(passwordEncoder.encode(mpin));
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "MPIN set successfully"
            ));
        } else {
            throw new CustomException(USER_NOT_FOUND);
        }
    }

    // Update an existing MPIN after validating length
    public ResponseEntity<Map<String, Object>> updateUserMpin(String email, String newMpin) {
        if (newMpin.length() != 6) {
            throw new CustomException("MPIN must be 6 digits");
        }

        Optional<User> userOpt = getUserByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (user.getMpin() == null) {
                throw new CustomException("MPIN not set. Please set MPIN first");
            }

            user.setMpin(passwordEncoder.encode(newMpin));
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "MPIN updated successfully"
            ));
        } else {
            throw new CustomException(USER_NOT_FOUND);
        }
    }

    // Verifies if the provided MPIN matches the stored encrypted MPIN
    public ResponseEntity<Map<String, Object>> verifyMpin(String email, String mpin) {
        Optional<User> userOptional = getUserByEmail(email);

        if (userOptional.isEmpty()) {
            throw new CustomException(USER_NOT_FOUND);
        }

        User user = userOptional.get();
        if (user.getMpin() == null) {
            throw new CustomException("MPIN not set for this user");
        }

        if (passwordEncoder.matches(mpin, user.getMpin())) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Verification successful"
            ));
        }

        throw new CustomException(INVALID_MPIN);
    }

    // Verifies MPIN and OTP in sequence for high-security actions
    public ResponseEntity<Map<String, Object>> verifyMpinOtp(String email, String otp, String mpin) {
        ResponseEntity<?> response = verifyMpin(email, mpin);
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();

        if (responseBody == null || !(boolean) responseBody.get("success")) {
            throw new CustomException(INVALID_MPIN);
        }

        boolean isValidOtp = otpProvider.validateOtp(email, otp);
        if (!isValidOtp) {
            throw new CustomException("Invalid OTP");
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Verification successful"
        ));
    }
}
