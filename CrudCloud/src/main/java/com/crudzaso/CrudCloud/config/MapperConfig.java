package com.crudzaso.CrudCloud.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for mapper beans
 * - ModelMapper: Generic entity-to-DTO mappings for Plan, DatabaseEngine, etc.
 * - MapStruct: User entity mappings (handled via UserMapper interface)
 */
@Configuration
public class MapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}
