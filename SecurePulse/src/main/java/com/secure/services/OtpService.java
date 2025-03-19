package com.secure.services;


import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private final ConcurrentHashMap<String, String> otpStorage = new ConcurrentHashMap<>();
    private final Random random = new Random();

    // Generate and store OTP
    public String generateOtp(String email) {
        String otp = String.valueOf(100000 + random.nextInt(900000)); // 6-digit OTP
        otpStorage.put(email, otp);

        // Auto-remove OTP after 5 minutes
        new Thread(() -> {
            try {
                TimeUnit.MINUTES.sleep(5);
                otpStorage.remove(email);
            } catch (InterruptedException ignored) {}
        }).start();

        return otp;
    }

    // Validate OTP
    public boolean validateOtp(String email, String userOtp) {
        String storedOtp = otpStorage.get(email);
        if (storedOtp != null && storedOtp.equals(userOtp)) {
            otpStorage.remove(email); // Remove OTP after successful verification
            return true;
        }
        return false;
    }
}
