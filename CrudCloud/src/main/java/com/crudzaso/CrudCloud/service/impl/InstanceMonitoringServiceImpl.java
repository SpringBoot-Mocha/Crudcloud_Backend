package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.DatabaseInstance;
import com.crudzaso.CrudCloud.domain.entity.InstanceLog;
import com.crudzaso.CrudCloud.domain.entity.InstanceStats;
import com.crudzaso.CrudCloud.domain.enums.LogLevel;
import com.crudzaso.CrudCloud.dto.response.InstanceLogResponse;
import com.crudzaso.CrudCloud.dto.response.InstanceStatsResponse;
import com.crudzaso.CrudCloud.exception.ResourceNotFoundException;
import com.crudzaso.CrudCloud.mapper.InstanceLogMapper;
import com.crudzaso.CrudCloud.mapper.InstanceStatsMapper;
import com.crudzaso.CrudCloud.repository.DatabaseInstanceRepository;
import com.crudzaso.CrudCloud.repository.InstanceLogRepository;
import com.crudzaso.CrudCloud.repository.InstanceStatsRepository;
import com.crudzaso.CrudCloud.service.InstanceMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of InstanceMonitoringService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InstanceMonitoringServiceImpl implements InstanceMonitoringService {

    private final InstanceStatsRepository statsRepository;
    private final InstanceLogRepository logRepository;
    private final DatabaseInstanceRepository instanceRepository;
    private final InstanceStatsMapper statsMapper;
    private final InstanceLogMapper logMapper;

    @Override
    public List<InstanceStatsResponse> getInstanceStats(Long instanceId) {
        log.debug("Fetching all stats for instance ID: {}", instanceId);
        validateInstanceExists(instanceId);

        List<InstanceStats> stats = statsRepository.findByInstanceIdOrderByRecordedAtDesc(instanceId);
        return stats.stream()
                .map(statsMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<InstanceStatsResponse> getInstanceStatsPaginated(Long instanceId, Pageable pageable) {
        log.debug("Fetching paginated stats for instance ID: {}", instanceId);
        validateInstanceExists(instanceId);

        Page<InstanceStats> stats = statsRepository.findByInstanceIdOrderByRecordedAtDesc(instanceId, pageable);
        return stats.map(statsMapper::toResponse);
    }

    @Override
    public InstanceStatsResponse getLatestStats(Long instanceId) {
        log.debug("Fetching latest stats for instance ID: {}", instanceId);
        validateInstanceExists(instanceId);

        InstanceStats stats = statsRepository.findFirstByInstanceIdOrderByRecordedAtDesc(instanceId);
        if (stats == null) {
            log.warn("No stats found for instance ID: {}", instanceId);
            return null;
        }
        return statsMapper.toResponse(stats);
    }

    @Override
    public List<InstanceStatsResponse> getStatsInRange(Long instanceId, LocalDateTime start, LocalDateTime end) {
        log.debug("Fetching stats for instance ID: {} between {} and {}", instanceId, start, end);
        validateInstanceExists(instanceId);

        List<InstanceStats> stats = statsRepository.findByInstanceIdAndRecordedAtBetweenOrderByRecordedAtDesc(
                instanceId, start, end);
        return stats.stream()
                .map(statsMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public InstanceStatsResponse recordStats(Long instanceId, InstanceStatsResponse statsRequest) {
        log.info("Recording stats for instance ID: {}", instanceId);
        validateInstanceExists(instanceId);

        DatabaseInstance instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Database Instance", instanceId));

        InstanceStats stats = InstanceStats.builder()
                .instance(instance)
                .cpuUsagePercent(statsRequest.getCpuUsagePercent())
                .memoryUsageMb(statsRequest.getMemoryUsageMb())
                .memoryLimitMb(statsRequest.getMemoryLimitMb())
                .storageUsageGb(statsRequest.getStorageUsageGb())
                .storageLimitGb(statsRequest.getStorageLimitGb())
                .activeConnections(statsRequest.getActiveConnections())
                .totalQueries(statsRequest.getTotalQueries())
                .recordedAt(LocalDateTime.now())
                .build();

        InstanceStats savedStats = statsRepository.save(stats);
        log.info("Stats recorded successfully for instance ID: {}", instanceId);

        return statsMapper.toResponse(savedStats);
    }

    @Override
    public List<InstanceLogResponse> getInstanceLogs(Long instanceId) {
        log.debug("Fetching all logs for instance ID: {}", instanceId);
        validateInstanceExists(instanceId);

        List<InstanceLog> logs = logRepository.findByInstanceIdOrderByCreatedAtDesc(instanceId);
        return logs.stream()
                .map(logMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<InstanceLogResponse> getInstanceLogsPaginated(Long instanceId, Pageable pageable) {
        log.debug("Fetching paginated logs for instance ID: {}", instanceId);
        validateInstanceExists(instanceId);

        Page<InstanceLog> logs = logRepository.findByInstanceIdOrderByCreatedAtDesc(instanceId, pageable);
        return logs.map(logMapper::toResponse);
    }

    @Override
    public List<InstanceLogResponse> getLogsByLevel(Long instanceId, LogLevel level) {
        log.debug("Fetching {} logs for instance ID: {}", level, instanceId);
        validateInstanceExists(instanceId);

        List<InstanceLog> logs = logRepository.findByInstanceIdAndLevelOrderByCreatedAtDesc(instanceId, level);
        return logs.stream()
                .map(logMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<InstanceLogResponse> getLogsInRange(Long instanceId, LocalDateTime start, LocalDateTime end) {
        log.debug("Fetching logs for instance ID: {} between {} and {}", instanceId, start, end);
        validateInstanceExists(instanceId);

        List<InstanceLog> logs = logRepository.findByInstanceIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                instanceId, start, end);
        return logs.stream()
                .map(logMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public InstanceLogResponse addLog(Long instanceId, LogLevel level, String title, String message, String source) {
        log.info("Adding {} log for instance ID: {}", level, instanceId);
        validateInstanceExists(instanceId);

        DatabaseInstance instance = instanceRepository.findById(instanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Database Instance", instanceId));

        InstanceLog instanceLog = InstanceLog.builder()
                .instance(instance)
                .level(level)
                .title(title)
                .message(message)
                .source(source)
                .build();

        InstanceLog savedLog = logRepository.save(instanceLog);
        log.info("Log added successfully for instance ID: {}", instanceId);

        return logMapper.toResponse(savedLog);
    }

    @Override
    public List<InstanceLogResponse> getErrorLogs(Long instanceId) {
        log.debug("Fetching error logs for instance ID: {}", instanceId);
        validateInstanceExists(instanceId);

        List<LogLevel> errorLevels = Arrays.asList(LogLevel.ERROR, LogLevel.WARNING);
        List<InstanceLog> logs = logRepository.findByInstanceIdAndLevelInOrderByCreatedAtDesc(
                instanceId, errorLevels);
        return logs.stream()
                .map(logMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void validateInstanceExists(Long instanceId) {
        if (!instanceRepository.existsById(instanceId)) {
            throw new ResourceNotFoundException("Database Instance", instanceId);
        }
    }
}
