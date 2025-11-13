package com.crudzaso.CrudCloud.mapper;

import com.crudzaso.CrudCloud.domain.entity.Plan;
import com.crudzaso.CrudCloud.dto.response.PlanResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * MapStruct mapper for Plan entity to PlanResponse DTO conversion
 * Generates type-safe mapping code at compile-time
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PlanMapper {

    /**
     * Map Plan entity to PlanResponse DTO
     * Maps pricePerMonth to priceMonth
     *
     * @param plan the plan entity to map
     * @return the mapped plan response DTO
     */
    @Mapping(source = "pricePerMonth", target = "priceMonth")
    PlanResponse toResponse(Plan plan);

    /**
     * Map list of Plan entities to list of PlanResponse DTOs
     *
     * @param plans the list of plan entities to map
     * @return the list of mapped plan response DTOs
     */
    List<PlanResponse> toResponseList(List<Plan> plans);
}