package com.crudzaso.CrudCloud.repository;

import com.crudzaso.CrudCloud.domain.entity.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Credential entity
 * Provides database operations for Credential records
 */
@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {

    /**
     * Find credentials for a specific database instance
     * @param databaseInstanceId the database instance ID to search for
     * @return Optional containing the credential if found
     */
    Optional<Credential> findByDatabaseInstanceId(Long databaseInstanceId);
}
