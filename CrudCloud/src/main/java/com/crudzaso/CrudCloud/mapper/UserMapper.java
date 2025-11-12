package com.crudzaso.CrudCloud.mapper;

import com.crudzaso.CrudCloud.domain.entity.User;
import com.crudzaso.CrudCloud.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper for User entity to UserResponse DTO conversion
 * Generates type-safe mapping code at compile-time
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    /**
     * Map User entity to UserResponse DTO
     * Explicitly maps User.id to UserResponse.userId
     *
     * @param user the user entity to map
     * @return the mapped user response DTO
     */
    @Mapping(source = "id", target = "userId")
    UserResponse toUserResponse(User user);

    /**
     * Map User entity to UserResponse DTO (alternative method name for semantic clarity)
     *
     * @param user the user entity to map
     * @return the mapped user response DTO
     */
    @Mapping(source = "id", target = "userId")
    UserResponse userToDto(User user);
}
