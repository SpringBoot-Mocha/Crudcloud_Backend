package com.crudzaso.CrudCloud.mapper;

import com.crudzaso.CrudCloud.domain.entity.Subscription;
import com.crudzaso.CrudCloud.dto.response.SubscriptionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * MapStruct mapper for Subscription entity to SubscriptionResponse DTO conversion
 * Generates type-safe mapping code at compile-time
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {UserMapper.class, PlanMapper.class})
public interface SubscriptionMapper {

    /**
     * Map Subscription entity to SubscriptionResponse DTO
     * Maps relationships to their IDs and plan name
     *
     * @param subscription the subscription entity to map
     * @return the mapped subscription response DTO
     */
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "plan.id", target = "planId")
    @Mapping(source = "plan.name", target = "planName")
    SubscriptionResponse toResponse(Subscription subscription);

    /**
     * Map list of Subscription entities to list of SubscriptionResponse DTOs
     *
     * @param subscriptions the list of subscription entities to map
     * @return the list of mapped subscription response DTOs
     */
    List<SubscriptionResponse> toResponseList(List<Subscription> subscriptions);
}