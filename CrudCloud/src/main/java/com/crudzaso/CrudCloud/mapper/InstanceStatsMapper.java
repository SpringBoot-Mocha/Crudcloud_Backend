package com.crudzaso.CrudCloud.mapper;

import com.crudzaso.CrudCloud.domain.entity.InstanceStats;
import com.crudzaso.CrudCloud.dto.response.InstanceStatsResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * MapStruct mapper for InstanceStats entity to InstanceStatsResponse DTO conversion
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InstanceStatsMapper {

    /**
     * Map InstanceStats entity to InstanceStatsResponse DTO
     * Calculates memory and storage usage percentages
     *
     * @param stats the instance stats entity to map
     * @return the mapped instance stats response DTO
     */
    @Mapping(source = "instance.id", target = "instanceId")
    @Mapping(target = "memoryUsagePercent", expression = "java(stats.getMemoryLimitMb() != null && stats.getMemoryLimitMb() > 0 ? (stats.getMemoryUsageMb() / stats.getMemoryLimitMb()) * 100 : 0)")
    @Mapping(target = "storageUsagePercent", expression = "java(stats.getStorageLimitGb() != null && stats.getStorageLimitGb() > 0 ? (stats.getStorageUsageGb() / stats.getStorageLimitGb()) * 100 : 0)")
    InstanceStatsResponse toResponse(InstanceStats stats);

    /**
     * Map list of InstanceStats entities to list of InstanceStatsResponse DTOs
     *
     * @param statsList the list of instance stats entities to map
     * @return the list of mapped instance stats response DTOs
     */
    List<InstanceStatsResponse> toResponseList(List<InstanceStats> statsList);
}
