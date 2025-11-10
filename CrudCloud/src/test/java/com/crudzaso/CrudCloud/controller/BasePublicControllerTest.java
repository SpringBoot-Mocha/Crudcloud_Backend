package com.crudzaso.CrudCloud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base class for PUBLIC controller integration tests (no authentication required).
 *
 * Provides common configuration for MockMvc and ObjectMapper WITHOUT @WithMockUser.
 * Use this for tests that target public endpoints like /api/v1/auth/**, /api/v1/engines/**, etc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BasePublicControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;
}
