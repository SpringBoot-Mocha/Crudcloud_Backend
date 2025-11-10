package com.crudzaso.CrudCloud.repository;

import com.crudzaso.CrudCloud.domain.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


/**
 * Repository for Plan entity
 * Provides database operations for Plan records
 */
@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    /**
     * Find a plan by name
     * @param name the plan name to search for
     * @return Optional containing the plan if found
     */
    Optional<Plan> findByName(String name);

}
