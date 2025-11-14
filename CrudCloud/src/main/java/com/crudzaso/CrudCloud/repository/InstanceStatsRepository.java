package com.crudzaso.CrudCloud.repository;

import com.crudzaso.CrudCloud.domain.entity.InstanceStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for InstanceStats entity.
 */
@Repository
public interface InstanceStatsRepository extends JpaRepository<InstanceStats, Long> {

    /**
     * Find all stats for a specific instance.
     */
    List<InstanceStats> findByInstanceIdOrderByRecordedAtDesc(Long instanceId);

    /**
     * Find paginated stats for a specific instance.
     */
    Page<InstanceStats> findByInstanceIdOrderByRecordedAtDesc(Long instanceId, Pageable pageable);

    /**
     * Find stats recorded within a date range.
     */
    List<InstanceStats> findByInstanceIdAndRecordedAtBetweenOrderByRecordedAtDesc(
            Long instanceId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find the latest stats for an instance.
     */
    InstanceStats findFirstByInstanceIdOrderByRecordedAtDesc(Long instanceId);
}
