package com.crudzaso.CrudCloud.mapper;

import com.crudzaso.CrudCloud.domain.entity.InstanceLog;
import com.crudzaso.CrudCloud.dto.response.InstanceLogResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * MapStruct mapper for InstanceLog entity to InstanceLogResponse DTO conversion
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InstanceLogMapper {

    /**
     * Map InstanceLog entity to InstanceLogResponse DTO
     *
     * @param log the instance log entity to map
     * @return the mapped instance log response DTO
     */
    @Mapping(source = "instance.id", target = "instanceId")
    InstanceLogResponse toResponse(InstanceLog log);

    /**
     * Map list of InstanceLog entities to list of InstanceLogResponse DTOs
     *
     * @param logs the list of instance log entities to map
     * @return the list of mapped instance log response DTOs
     */
    List<InstanceLogResponse> toResponseList(List<InstanceLog> logs);
}
