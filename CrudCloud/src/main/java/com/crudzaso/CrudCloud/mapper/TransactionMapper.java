package com.crudzaso.CrudCloud.mapper;

import com.crudzaso.CrudCloud.domain.entity.Transaction;
import com.crudzaso.CrudCloud.dto.response.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * MapStruct mapper for Transaction entity to TransactionResponse DTO conversion
 * Generates type-safe mapping code at compile-time
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {UserMapper.class})
public interface TransactionMapper {

    /**
     * Map Transaction entity to TransactionResponse DTO
     * Maps user relationship to userId
     *
     * @param transaction the transaction entity to map
     * @return the mapped transaction response DTO
     */
    @Mapping(source = "user.id", target = "userId")
    TransactionResponse toResponse(Transaction transaction);

    /**
     * Map list of Transaction entities to list of TransactionResponse DTOs
     *
     * @param transactions the list of transaction entities to map
     * @return the list of mapped transaction response DTOs
     */
    List<TransactionResponse> toResponseList(List<Transaction> transactions);
}