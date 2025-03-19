package com.secure.operations;
import com.secure.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.secure.model.*;
import com.secure.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Component  
public class UserOperations {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
}

