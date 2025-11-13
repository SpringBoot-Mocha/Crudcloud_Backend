package com.crudzaso.CrudCloud.mapper;

import com.crudzaso.CrudCloud.domain.entity.DatabaseEngine;
import com.crudzaso.CrudCloud.dto.response.DatabaseEngineResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * MapStruct mapper for DatabaseEngine entity to DatabaseEngineResponse DTO conversion
 * Generates type-safe mapping code at compile-time
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DatabaseEngineMapper {

    /**
     * Map DatabaseEngine entity to DatabaseEngineResponse DTO
     *
     * @param databaseEngine the database engine entity to map
     * @return the mapped database engine response DTO
     */
    DatabaseEngineResponse toResponse(DatabaseEngine databaseEngine);

    /**
     * Map list of DatabaseEngine entities to list of DatabaseEngineResponse DTOs
     *
     * @param databaseEngines the list of database engine entities to map
     * @return the list of mapped database engine response DTOs
     */
    List<DatabaseEngineResponse> toResponseList(List<DatabaseEngine> databaseEngines);
}