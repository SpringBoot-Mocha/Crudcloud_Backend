package com.crudzaso.CrudCloud.repository;

import com.crudzaso.CrudCloud.domain.entity.InstanceLog;
import com.crudzaso.CrudCloud.domain.enums.LogLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for InstanceLog entity.
 */
@Repository
public interface InstanceLogRepository extends JpaRepository<InstanceLog, Long> {

    /**
     * Find all logs for a specific instance, ordered by most recent first.
     */
    List<InstanceLog> findByInstanceIdOrderByCreatedAtDesc(Long instanceId);

    /**
     * Find paginated logs for a specific instance.
     */
    Page<InstanceLog> findByInstanceIdOrderByCreatedAtDesc(Long instanceId, Pageable pageable);

    /**
     * Find logs by level (INFO, ERROR, WARNING, etc.).
     */
    List<InstanceLog> findByInstanceIdAndLevelOrderByCreatedAtDesc(Long instanceId, LogLevel level);

    /**
     * Find logs within a date range.
     */
    List<InstanceLog> findByInstanceIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long instanceId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find error logs for troubleshooting.
     */
    List<InstanceLog> findByInstanceIdAndLevelInOrderByCreatedAtDesc(
            Long instanceId, List<LogLevel> levels);
}
