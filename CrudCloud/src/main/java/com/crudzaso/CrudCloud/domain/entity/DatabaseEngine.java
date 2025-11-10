package com.crudzaso.CrudCloud.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DatabaseEngine entity representing an available database engine
 *
 * Maps to the 'database_engines' table and defines the database engines
 * (PostgreSQL, MySQL, MongoDB, etc.) available for users to create instances.
 * This is static data created by administrators.
 */
@Entity
@Table(name = "database_engines", indexes = {
        @Index(name = "idx_database_engines_name", columnList = "name"),
        @Index(name = "idx_database_engine_name_version", columnList = "name, version")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatabaseEngine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String version;

    /**
     * Default port for this database engine
     * Examples: 5432 for PostgreSQL, 3306 for MySQL
     * Required field
     */
    @Column(nullable = false)
    private Integer defaultPort;

    /**
     * Docker image to use for this engine
     * Examples: "postgres:14", "mysql:8.0", "mongo:6.0"
     * Required field
     */
    @Column(nullable = false)
    private String dockerImage;

    /**
     * Description of the engine features and capabilities
     * Optional field
     */
    @Column(columnDefinition = "TEXT")
    private String description;
}
