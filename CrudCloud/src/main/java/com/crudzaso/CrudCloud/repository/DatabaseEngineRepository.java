package com.crudzaso.CrudCloud.repository;

import com.crudzaso.CrudCloud.domain.entity.DatabaseEngine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for DatabaseEngine entity
 * Provides database operations for DatabaseEngine records
 */
@Repository
public interface DatabaseEngineRepository extends JpaRepository<DatabaseEngine, Long> {

    /**
     * Find a database engine by name and version
     * @param name the engine name (e.g., "PostgreSQL")
     * @param version the engine version (e.g., "14.5")
     * @return Optional containing the engine if found
     */
    Optional<DatabaseEngine> findByNameAndVersion(String name, String version);
}
