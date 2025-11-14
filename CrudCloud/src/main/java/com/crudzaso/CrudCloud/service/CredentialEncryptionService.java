package com.crudzaso.CrudCloud.service;

/**
 * Service interface for encrypting and decrypting database credentials.
 * Uses AES-256 encryption to securely store database passwords.
 */
public interface CredentialEncryptionService {

    /**
     * Generate a secure random password for database access.
     * Creates a 16-character password with alphanumeric and special characters.
     *
     * @return A randomly generated secure password
     */
    String generateSecurePassword();

    /**
     * Encrypt a plaintext password using AES-256.
     * IMPORTANT: This is encryption (reversible), NOT hashing.
     *
     * @param plainPassword The plaintext password to encrypt
     * @return The encrypted password (encoded in Base64 or similar)
     */
    String encrypt(String plainPassword);

    /**
     * Decrypt an encrypted password back to plaintext.
     * IMPORTANT: Only used when returning credentials to user (one time).
     * NEVER store the plaintext result.
     *
     * @param encryptedPassword The encrypted password to decrypt
     * @return The decrypted plaintext password
     */
    String decrypt(String encryptedPassword);
}
