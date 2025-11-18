package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.service.CredentialEncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Implementation of CredentialEncryptionService using AES-256 encryption.
 *
 * IMPORTANT SECURITY NOTES:
 * - Uses AES-256 symmetric encryption for reversible password storage
 * - Each encrypted password includes a random IV (initialization vector) for added security
 * - IV is prepended to ciphertext and stored as single Base64 string
 * - Encryption key is derived from configuration or default key
 * - This allows passwords to be retrieved when needed (production requirement)
 * - Never log plaintext credentials
 */
@Service
@Slf4j
public class CredentialEncryptionServiceImpl implements CredentialEncryptionService {

    private final SecretKey secretKey;
    private final SecureRandom secureRandom;
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 16;

    /**
     * Constructor initializing AES-256 encryption key.
     * Derives key from configuration value using SHA-256 hashing.
     */
    public CredentialEncryptionServiceImpl(@Value("${encryption.key:default-secure-production-key-2025}") String encryptionKeyValue) {
        this.secureRandom = new SecureRandom();
        this.secretKey = deriveKeyFromString(encryptionKeyValue);
        log.info("✅ CredentialEncryptionService initialized with AES-256 encryption");
    }

    /**
     * Derives a 256-bit key from a string using SHA-256.
     */
    private SecretKey deriveKeyFromString(String keyString) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(keyString.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
        } catch (Exception e) {
            log.error("❌ Error deriving encryption key", e);
            throw new RuntimeException("Failed to derive encryption key", e);
        }
    }

    @Override
    public String generateSecurePassword() {
        log.debug("🔐 Generating secure random password");

        try {
            // Generate 32 random bytes for a strong password
            byte[] randomBytes = new byte[32];
            secureRandom.nextBytes(randomBytes);

            // Convert to Base64 and create password with special characters
            String base64 = Base64.getEncoder().encodeToString(randomBytes);

            // Take 16 chars from base64 and add special characters for complexity
            String password = base64.substring(0, 16).replaceAll("[+/=]", "x") + "!@#$";

            log.debug("✅ Secure password generated (length: {} characters)", password.length());
            return password;
        } catch (Exception e) {
            log.error("❌ Error generating secure password", e);
            throw new RuntimeException("Failed to generate secure password", e);
        }
    }

    @Override
    public String encrypt(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            log.warn("⚠️ Attempted to encrypt empty password");
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        log.debug("🔐 Encrypting password (length: {} characters)", plainPassword.length());

        try {
            // Generate random IV
            byte[] iv = new byte[IV_SIZE];
            secureRandom.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Create cipher with IV
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            // Encrypt the password
            byte[] encryptedBytes = cipher.doFinal(plainPassword.getBytes(StandardCharsets.UTF_8));

            // Combine IV + encrypted data
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encryptedBytes.length);
            buffer.put(iv);
            buffer.put(encryptedBytes);

            // Encode as Base64
            String encryptedPassword = Base64.getEncoder().encodeToString(buffer.array());

            log.debug("✅ Password encrypted successfully");
            return encryptedPassword;
        } catch (Exception e) {
            log.error("❌ Error encrypting password", e);
            throw new RuntimeException("Failed to encrypt password", e);
        }
    }

    @Override
    public String decrypt(String encryptedPassword) {
        if (encryptedPassword == null || encryptedPassword.isEmpty()) {
            log.warn("⚠️ Attempted to decrypt empty password");
            throw new IllegalArgumentException("Encrypted password cannot be null or empty");
        }

        log.debug("🔐 Decrypting password");

        try {
            // Decode Base64
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedPassword);

            // Extract IV and ciphertext
            ByteBuffer buffer = ByteBuffer.wrap(decodedBytes);
            byte[] iv = new byte[IV_SIZE];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            // Create cipher with IV
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

            // Decrypt
            byte[] decryptedBytes = cipher.doFinal(ciphertext);
            String decryptedPassword = new String(decryptedBytes, StandardCharsets.UTF_8);

            log.debug("✅ Password decrypted successfully");
            return decryptedPassword;
        } catch (Exception e) {
            log.error("❌ Error decrypting password: {}", e.getMessage());
            throw new RuntimeException("Failed to decrypt password: " + e.getMessage(), e);
        }
    }
}
