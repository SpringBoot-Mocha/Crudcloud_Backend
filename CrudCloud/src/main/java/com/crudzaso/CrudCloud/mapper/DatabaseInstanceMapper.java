package com.crudzaso.CrudCloud.mapper;

import com.crudzaso.CrudCloud.domain.entity.DatabaseInstance;
import com.crudzaso.CrudCloud.dto.response.DatabaseInstanceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * MapStruct mapper for DatabaseInstance entity to DatabaseInstanceResponse DTO conversion
 * Generates type-safe mapping code at compile-time
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {UserMapper.class, DatabaseEngineMapper.class})
public interface DatabaseInstanceMapper {

    /**
     * Map DatabaseInstance entity to DatabaseInstanceResponse DTO
     * Maps relationships to their IDs
     *
     * @param databaseInstance the database instance entity to map
     * @return the mapped database instance response DTO
     */
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "subscription.id", target = "subscriptionId")
    @Mapping(source = "databaseEngine.id", target = "databaseEngine")
    DatabaseInstanceResponse toResponse(DatabaseInstance databaseInstance);

    /**
     * Map list of DatabaseInstance entities to list of DatabaseInstanceResponse DTOs
     *
     * @param databaseInstances the list of database instance entities to map
     * @return the list of mapped database instance response DTOs
     */
    List<DatabaseInstanceResponse> toResponseList(List<DatabaseInstance> databaseInstances);
}