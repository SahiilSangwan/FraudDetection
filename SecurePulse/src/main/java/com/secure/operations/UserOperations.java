package com.secure.operations;
import com.secure.repository.AccountRepository;
import com.secure.services.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.secure.model.*;
import com.secure.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Component  
public class UserOperations {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private OtpService otpService;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User createUser(User user) {
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        return userRepository.save(user);
    }

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
                    user.setPassword(userDetails.getPassword());
                    user.setFlag(userDetails.getFlag());
                    return userRepository.save(user);
                }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public String deleteUser(Integer id) {
        userRepository.deleteById(id);
        return "User deleted successfully";
    }

    public Optional<User> getUserByEmailAndPassword(String email, String rawPassword) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent() && passwordEncoder.matches(rawPassword, user.get().getPassword())) {
            return user;
        }
        return Optional.empty();
    }

    public Optional<User> getUserByEmailAndPassword(String email, String rawPassword, String bank) {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent() && passwordEncoder.matches(rawPassword, user.get().getPassword())) {
            // Check if the user has an account in the specified bank
            List<Account> account = accountRepository.findByUserIdAndBank(user.get().getUserId(), bank);

            if (account.size()>=1) {
                return user; // User exists and belongs to the given bank
            }
        }

        return Optional.empty(); // User does not exist or bank mismatch
    }


    public ResponseEntity<?> setUserMpin(String email, String mpin) {
        if (mpin.length() != 6) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "MPIN must be 6 digits"
            ));
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
        }

        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "User not found"
        ));
    }

    public ResponseEntity<?> updateUserMpin(String email,  String newMpin) {
        if (newMpin.length() != 6) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "MPIN must be 6 digits"
            ));
        }

        Optional<User> userOpt = getUserByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (user.getMpin() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "MPIN not set. Please set MPIN first"
                ));
            }

//            if (!passwordEncoder.matches(oldMpin, user.getMpin())) {
//                return ResponseEntity.badRequest().body(Map.of(
//                        "success", false,
//                        "message", "Old MPIN is incorrect"
//                ));
//            }

            user.setMpin(passwordEncoder.encode(newMpin));
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "MPIN updated successfully"
            ));
        }

        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "User not found"
        ));
    }

    public ResponseEntity<?> verifyMpin(String email, String mpin) {
        Optional<User> userOptional = getUserByEmail(email);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "User not found"
            ));
        }

        User user = userOptional.get();
        if (user.getMpin() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "MPIN not set for this user"
            ));
        }

        if (passwordEncoder.matches(mpin, user.getMpin())) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Verification successful"
            ));
        }

        return ResponseEntity.status(200).body(Map.of(
                "success", false,
                "message", "Invalid MPIN"
        ));
    }

    public ResponseEntity<?> verifyMpinOtp(String email, String otp, String mpin) {

            ResponseEntity<?> response = verifyMpin(email, mpin);
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();

            if (responseBody != null && !(boolean) responseBody.get("success")) {
                return ResponseEntity.status(200).body(Map.of(
                        "success", false,
                        "message", "Invalid MPIN"
                ));
            } else {
                boolean isValidOtp = otpService.validateOtp(email, otp);
                if (!isValidOtp) {
                    return ResponseEntity.status(200).body(Map.of(
                            "success", false,
                            "message", "Invalid OTP"
                    ));
                }
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Verification successful"
                ));
            }

    }


}

