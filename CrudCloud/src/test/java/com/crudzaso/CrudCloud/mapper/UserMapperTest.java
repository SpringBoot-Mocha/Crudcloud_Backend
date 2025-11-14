package com.crudzaso.CrudCloud.mapper;

import com.crudzaso.CrudCloud.BaseIntegrationTest;
import com.crudzaso.CrudCloud.domain.entity.User;
import com.crudzaso.CrudCloud.dto.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserMapper using Spring Boot Test
 * Tests the actual MapStruct implementation with Spring context
 * Specifically tests the id to userId field mapping
 * 
 * Extends BaseIntegrationTest for:
 * - Automatic Spring Boot context loading
 * - PostgreSQL database configuration (test profile)
 * - Automatic transaction rollback after each test (data cleanup)
 */
@ExtendWith(SpringExtension.class)
class UserMapperTest extends BaseIntegrationTest {

    @Autowired
    private UserMapper userMapper;

    private User user;

    @BeforeEach
    void setUp() {
        // Setup test entity
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .isOrganization(false)
                .build();
    }

    @Test
    void toResponse_ValidUser_ReturnsCorrectResponse() {
        // When
        UserResponse response = userMapper.toResponse(user);

        // Then
        assertNotNull(response);
        assertEquals(user.getId(), response.getUserId()); // Tests the field mapping from id to userId
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getName(), response.getName());
        assertEquals(user.getIsOrganization(), response.getIsOrganization());
    }

    @Test
    void toResponse_NullUser_ReturnsNull() {
        // When
        UserResponse response = userMapper.toResponse(null);

        // Then
        assertNull(response);
    }

    @Test
    void toResponse_UserWithNullFields_ReturnsResponseWithNullFields() {
        // Given
        User userWithNullFields = User.builder()
                .id(2L)
                .email("test2@example.com")
                .name(null)
                .isOrganization(true)
                .build();

        // When
        UserResponse response = userMapper.toResponse(userWithNullFields);

        // Then
        assertNotNull(response);
        assertEquals(userWithNullFields.getId(), response.getUserId());
        assertEquals(userWithNullFields.getEmail(), response.getEmail());
        assertNull(response.getName());
        assertEquals(userWithNullFields.getIsOrganization(), response.getIsOrganization());
    }

    @Test
    void toResponse_UserWithEmptyFields_ReturnsResponseWithEmptyFields() {
        // Given
        User userWithEmptyFields = User.builder()
                .id(3L)
                .email("")
                .name("")
                .isOrganization(false)
                .build();

        // When
        UserResponse response = userMapper.toResponse(userWithEmptyFields);

        // Then
        assertNotNull(response);
        assertEquals(userWithEmptyFields.getId(), response.getUserId());
        assertEquals(userWithEmptyFields.getEmail(), response.getEmail());
        assertEquals(userWithEmptyFields.getName(), response.getName());
        assertEquals(userWithEmptyFields.getIsOrganization(), response.getIsOrganization());
    }

    @Test
    void toResponse_UserWithOrganizationTrue_ReturnsResponseWithOrganizationTrue() {
        // Given
        User organizationUser = User.builder()
                .id(4L)
                .email("org@example.com")
                .name("Organization User")
                .isOrganization(true)
                .build();

        // When
        UserResponse response = userMapper.toResponse(organizationUser);

        // Then
        assertNotNull(response);
        assertEquals(organizationUser.getId(), response.getUserId());
        assertEquals(organizationUser.getEmail(), response.getEmail());
        assertEquals(organizationUser.getName(), response.getName());
        assertTrue(response.getIsOrganization());
    }

    @Test
    void toResponse_UserWithOrganizationFalse_ReturnsResponseWithOrganizationFalse() {
        // Given
        User individualUser = User.builder()
                .id(5L)
                .email("individual@example.com")
                .name("Individual User")
                .isOrganization(false)
                .build();

        // When
        UserResponse response = userMapper.toResponse(individualUser);

        // Then
        assertNotNull(response);
        assertEquals(individualUser.getId(), response.getUserId());
        assertEquals(individualUser.getEmail(), response.getEmail());
        assertEquals(individualUser.getName(), response.getName());
        assertFalse(response.getIsOrganization());
    }

    @Test
    void toResponse_UserWithSpecialCharacters_ReturnsCorrectResponse() {
        // Given
        User userWithSpecialChars = User.builder()
                .id(6L)
                .email("user+test@example-domain.com")
                .name("User with Special Chars!@#$%^&*()")
                .isOrganization(false)
                .build();

        // When
        UserResponse response = userMapper.toResponse(userWithSpecialChars);

        // Then
        assertNotNull(response);
        assertEquals(userWithSpecialChars.getId(), response.getUserId());
        assertEquals(userWithSpecialChars.getEmail(), response.getEmail());
        assertEquals(userWithSpecialChars.getName(), response.getName());
        assertEquals(userWithSpecialChars.getIsOrganization(), response.getIsOrganization());
    }

    @Test
    void toResponse_UserWithLongValues_ReturnsCorrectResponse() {
        // Given
        String longEmail = "very.long.email.address.with.many.parts.and.subdomains@example-domain-with-long-name.com";
        String longName = "Very Long User Name With Many Words And Special Characters That Should Be Properly Handled By The Mapper";

        User userWithLongValues = User.builder()
                .id(7L)
                .email(longEmail)
                .name(longName)
                .isOrganization(true)
                .build();

        // When
        UserResponse response = userMapper.toResponse(userWithLongValues);

        // Then
        assertNotNull(response);
        assertEquals(userWithLongValues.getId(), response.getUserId());
        assertEquals(userWithLongValues.getEmail(), response.getEmail());
        assertEquals(userWithLongValues.getName(), response.getName());
        assertEquals(userWithLongValues.getIsOrganization(), response.getIsOrganization());
    }

    @Test
    void toResponse_UserWithMaxId_ReturnsCorrectResponse() {
        // Given
        User userWithMaxId = User.builder()
                .id(Long.MAX_VALUE)
                .email("max@example.com")
                .name("Max ID User")
                .isOrganization(false)
                .build();

        // When
        UserResponse response = userMapper.toResponse(userWithMaxId);

        // Then
        assertNotNull(response);
        assertEquals(Long.MAX_VALUE, response.getUserId());
        assertEquals(userWithMaxId.getEmail(), response.getEmail());
        assertEquals(userWithMaxId.getName(), response.getName());
        assertEquals(userWithMaxId.getIsOrganization(), response.getIsOrganization());
    }

    @Test
    void toResponse_UserWithMinId_ReturnsCorrectResponse() {
        // Given
        User userWithMinId = User.builder()
                .id(Long.MIN_VALUE)
                .email("min@example.com")
                .name("Min ID User")
                .isOrganization(false)
                .build();

        // When
        UserResponse response = userMapper.toResponse(userWithMinId);

        // Then
        assertNotNull(response);
        assertEquals(Long.MIN_VALUE, response.getUserId());
        assertEquals(userWithMinId.getEmail(), response.getEmail());
        assertEquals(userWithMinId.getName(), response.getName());
        assertEquals(userWithMinId.getIsOrganization(), response.getIsOrganization());
    }

    @Test
    void toResponse_UserWithZeroId_ReturnsCorrectResponse() {
        // Given
        User userWithZeroId = User.builder()
                .id(0L)
                .email("zero@example.com")
                .name("Zero ID User")
                .isOrganization(false)
                .build();

        // When
        UserResponse response = userMapper.toResponse(userWithZeroId);

        // Then
        assertNotNull(response);
        assertEquals(0L, response.getUserId());
        assertEquals(userWithZeroId.getEmail(), response.getEmail());
        assertEquals(userWithZeroId.getName(), response.getName());
        assertEquals(userWithZeroId.getIsOrganization(), response.getIsOrganization());
    }
}