package com.secure.utils;

import org.springframework.stereotype.Service;

@Service
public class DecryptionProvider {
    public  String decryptString(String encryptedInput) {
        if (encryptedInput == null || encryptedInput.isEmpty()) {
            throw new IllegalArgumentException("Encrypted input must be a non-empty string.");
        }

        String decrypted = encryptedInput;

        for (int round = 3; round >= 1; round--) {
            // Step 1: Halve the string
            int halfLength = decrypted.length() / 2;
            decrypted = decrypted.substring(0, halfLength);

            // Step 2: Unshift each character by its index + 1
            StringBuilder unshifted = new StringBuilder();
            for (int i = 0; i < decrypted.length(); i++) {
                int charCode = (int) decrypted.charAt(i);
                charCode = (charCode - (i + 1)) % 65536;
                if (charCode < 0) charCode += 65536;
                unshifted.append((char) charCode);
            }

            decrypted = unshifted.toString();
        }

        return decrypted;
    }
}
