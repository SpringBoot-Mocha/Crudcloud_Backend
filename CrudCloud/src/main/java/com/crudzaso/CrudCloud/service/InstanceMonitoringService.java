package com.crudzaso.CrudCloud.service;

import com.crudzaso.CrudCloud.dto.response.InstanceLogResponse;
import com.crudzaso.CrudCloud.dto.response.InstanceStatsResponse;
import com.crudzaso.CrudCloud.domain.enums.LogLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for instance monitoring (stats and logs).
 */
public interface InstanceMonitoringService {

    /**
     * Get all stats for an instance.
     */
    List<InstanceStatsResponse> getInstanceStats(Long instanceId);

    /**
     * Get paginated stats for an instance.
     */
    Page<InstanceStatsResponse> getInstanceStatsPaginated(Long instanceId, Pageable pageable);

    /**
     * Get latest stats for an instance.
     */
    InstanceStatsResponse getLatestStats(Long instanceId);

    /**
     * Get stats within a date range.
     */
    List<InstanceStatsResponse> getStatsInRange(Long instanceId, LocalDateTime start, LocalDateTime end);

    /**
     * Record new stats for an instance (called by monitoring system).
     */
    InstanceStatsResponse recordStats(Long instanceId, InstanceStatsResponse stats);

    /**
     * Get all logs for an instance.
     */
    List<InstanceLogResponse> getInstanceLogs(Long instanceId);

    /**
     * Get paginated logs for an instance.
     */
    Page<InstanceLogResponse> getInstanceLogsPaginated(Long instanceId, Pageable pageable);

    /**
     * Get logs by level (INFO, ERROR, WARNING, etc.).
     */
    List<InstanceLogResponse> getLogsByLevel(Long instanceId, LogLevel level);

    /**
     * Get logs within a date range.
     */
    List<InstanceLogResponse> getLogsInRange(Long instanceId, LocalDateTime start, LocalDateTime end);

    /**
     * Add a log entry for an instance.
     */
    InstanceLogResponse addLog(Long instanceId, LogLevel level, String title, String message, String source);

    /**
     * Get error logs for troubleshooting.
     */
    List<InstanceLogResponse> getErrorLogs(Long instanceId);
}
