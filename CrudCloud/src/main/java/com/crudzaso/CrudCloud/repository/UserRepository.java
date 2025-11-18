package com.crudzaso.CrudCloud.repository;

import com.crudzaso.CrudCloud.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity
 * Provides database operations for User records
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by email address
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with the given email exists
     * @param email the email to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find a user by OAuth provider ID
     * @param oauthProviderId the OAuth provider ID (e.g., Google subject ID or GitHub user ID)
     * @return Optional containing the user if found
     */
    Optional<User> findByOauthProviderId(String oauthProviderId);
}
