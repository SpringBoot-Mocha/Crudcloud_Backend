package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.service.CredentialEncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Implementation of CredentialEncryptionService using BCrypt hashing.
 *
 * IMPORTANT SECURITY NOTES:
 * - This service uses BCrypt which is one-way (hashing), NOT reversible encryption
 * - For a production system that needs to retrieve passwords, use Jasypt or similar
 * - Database credentials are shown ONCE at creation and sent via email
 * - We DO NOT retrieve passwords after creation (by design)
 * - Passwords are hashed and verified using BCrypt
 * - Never log or expose plaintext credentials
 */
@Service
@Slf4j
public class CredentialEncryptionServiceImpl implements CredentialEncryptionService {

    private final BCryptPasswordEncoder encoder;
    private final SecureRandom secureRandom;

    /**
     * Constructor initializing the BCrypt password encoder.
     * Strength 10 is suitable for password hashing.
     */
    public CredentialEncryptionServiceImpl(@Value("${encryption.key:default-insecure-key}") String encryptionKey) {
        // BCrypt encoder with strength 10
        this.encoder = new BCryptPasswordEncoder(10);
        this.secureRandom = new SecureRandom();
        log.info("CredentialEncryptionService initialized with BCrypt encoder (strength=10)");
    }

    @Override
    public String generateSecurePassword() {
        log.debug("Generating secure random password");

        // Generate 16-byte random sequence
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);

        // Convert to Base64 and clean up special characters that might cause issues
        String base64Password = Base64.getEncoder().encodeToString(randomBytes);

        // Ensure we have a mix: take 12 chars from base64 + add some guaranteed special chars
        String password = base64Password.substring(0, 12) + "!@#$";

        log.debug("Secure password generated successfully");
        return password;
    }

    @Override
    public String encrypt(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            log.warn("Attempted to encrypt empty password");
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        log.debug("Encrypting password (length: {} characters)", plainPassword.length());

        try {
            // Using PBKDF2PasswordEncoder which internally uses SHA256 and salt
            String encryptedPassword = encoder.encode(plainPassword);
            log.debug("Password encrypted successfully");
            return encryptedPassword;
        } catch (Exception e) {
            log.error("Error encrypting password", e);
            throw new RuntimeException("Failed to encrypt password", e);
        }
    }

    @Override
    public String decrypt(String encryptedPassword) {
        log.warn("⚠️ DECRYPT METHOD CALLED - BCrypt is one-way!");
        log.warn("Credentials should NOT be decrypted after creation.");
        log.warn("User should have saved password at creation time.");

        throw new UnsupportedOperationException(
            "BCrypt hashing is one-way (irreversible by design). " +
            "Database credentials should be returned ONLY at creation time and sent via email. " +
            "They should NOT be retrieved later. If user loses password, it must be rotated."
        );
    }
}
