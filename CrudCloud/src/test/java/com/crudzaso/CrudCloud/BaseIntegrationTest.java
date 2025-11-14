package com.crudzaso.CrudCloud;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for integration tests that interact with the database
 *
 * This class provides:
 * - Spring Boot context loading (@SpringBootTest)
 * - Test profile activation (application-test.properties)
 * - Automatic transaction rollback after each test (@Transactional)
 *
 * By extending this class, child tests automatically:
 * 1. Load the Spring Boot application context
 * 2. Use PostgreSQL configuration from application-test.properties
 * 3. Wrap each test method in a transaction that rolls back after completion
 *    (data is cleaned up automatically, no manual cleanup needed)
 *
 * Usage:
 * {@code
 * @ExtendWith(SpringExtension.class)
 * class UserMapperTest extends BaseIntegrationTest {
 *     @Test
 *     void testSomething() {
 *         // Data inserted here will be rolled back automatically
 *     }
 * }
 * }
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {
    // This base class provides automatic transaction management
    // Child classes inherit @Transactional behavior for data cleanup
}
