package com.crudzaso.CrudCloud.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for EmailServiceImpl
 *
 * Tests all email scenarios including success cases and error handling
 * Verifies that email failures don't block application operations
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Email Service Implementation Tests")
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_NAME = "Test User";
    private static final String DATABASE_NAME = "test_db";
    private static final String ENGINE_NAME = "PostgreSQL";
    private static final String HOST = "localhost";
    private static final int PORT = 5432;
    private static final String USERNAME = "test_user";
    private static final String NEW_PASSWORD = "new_password123";
    private static final String OLD_PLAN = "Free";
    private static final String NEW_PLAN = "Pro";
    private static final int NEW_LIMIT = 10;

    @BeforeEach
    void setUp() {
        // EmailServiceImpl uses @Value injection for configuration
        // We need to set the fromEmail value for tests
        // Using reflection to set the private field
        try {
            var field = emailService.getClass().getDeclaredField("fromEmail");
            field.setAccessible(true);
            field.set(emailService, "noreply@crudcloud.com");
        } catch (Exception e) {
            throw new RuntimeException("Failed to set fromEmail for test", e);
        }
    }

    @Test
    @DisplayName("Should send welcome email successfully")
    void sendWelcomeEmail_WithValidParameters_ShouldSendEmail() {
        // When
        emailService.sendWelcomeEmail(TEST_EMAIL, TEST_NAME);

        // Then
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should handle welcome email failure gracefully")
    void sendWelcomeEmail_WhenEmailFails_ShouldNotThrowException() {
        // Given
        doThrow(new MailSendException("SMTP connection failed"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        emailService.sendWelcomeEmail(TEST_EMAIL, TEST_NAME);

        // Should not throw exception - email failure shouldn't block registration
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should send instance creation email successfully")
    void sendInstanceCreatedEmail_WithValidParameters_ShouldSendEmail() {
        // When
        emailService.sendInstanceCreatedEmail(
                TEST_EMAIL, TEST_NAME, DATABASE_NAME, ENGINE_NAME,
                HOST, PORT, USERNAME
        );

        // Then
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should handle instance creation email failure gracefully")
    void sendInstanceCreatedEmail_WhenEmailFails_ShouldNotThrowException() {
        // Given
        doThrow(new MailSendException("SMTP connection failed"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        emailService.sendInstanceCreatedEmail(
                TEST_EMAIL, TEST_NAME, DATABASE_NAME, ENGINE_NAME,
                HOST, PORT, USERNAME
        );

        // Should not throw exception - email failure shouldn't block instance creation
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should send password rotation email successfully")
    void sendPasswordRotatedEmail_WithValidParameters_ShouldSendEmail() {
        // When
        emailService.sendPasswordRotatedEmail(TEST_EMAIL, DATABASE_NAME, NEW_PASSWORD);

        // Then
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should handle password rotation email failure gracefully")
    void sendPasswordRotatedEmail_WhenEmailFails_ShouldNotThrowException() {
        // Given
        doThrow(new MailSendException("SMTP connection failed"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        emailService.sendPasswordRotatedEmail(TEST_EMAIL, DATABASE_NAME, NEW_PASSWORD);

        // Should not throw exception
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should send plan upgrade email successfully")
    void sendPlanUpgradeEmail_WithValidParameters_ShouldSendEmail() {
        // When
        emailService.sendPlanUpgradeEmail(TEST_EMAIL, OLD_PLAN, NEW_PLAN, NEW_LIMIT);

        // Then
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should handle plan upgrade email failure gracefully")
    void sendPlanUpgradeEmail_WhenEmailFails_ShouldNotThrowException() {
        // Given
        doThrow(new MailSendException("SMTP connection failed"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        emailService.sendPlanUpgradeEmail(TEST_EMAIL, OLD_PLAN, NEW_PLAN, NEW_LIMIT);

        // Should not throw exception
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should send error notification email successfully")
    void sendErrorNotification_WithValidParameters_ShouldSendEmail() {
        // When
        emailService.sendErrorNotification(TEST_EMAIL, "Database Connection Failed",
                "Unable to connect to database instance");

        // Then
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should handle error notification email failure gracefully")
    void sendErrorNotification_WhenEmailFails_ShouldNotThrowException() {
        // Given
        doThrow(new MailSendException("SMTP connection failed"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        emailService.sendErrorNotification(TEST_EMAIL, "Database Connection Failed",
                "Unable to connect to database instance");

        // Should not throw exception
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should use correct email configuration")
    void sendWelcomeEmail_ShouldUseConfiguredFromAddress() {
        // When
        emailService.sendWelcomeEmail(TEST_EMAIL, TEST_NAME);

        // Then
        verify(mailSender).send(argThat((SimpleMailMessage message) ->
                message.getFrom() != null &&
                message.getFrom().equals("noreply@crudcloud.com") &&
                message.getTo() != null &&
                message.getTo()[0].equals(TEST_EMAIL)
        ));
    }

    @Test
    @DisplayName("Should include all required information in instance creation email")
    void sendInstanceCreatedEmail_ShouldIncludeAllDetails() {
        // When
        emailService.sendInstanceCreatedEmail(
                TEST_EMAIL, TEST_NAME, DATABASE_NAME, ENGINE_NAME,
                HOST, PORT, USERNAME
        );

        // Then
        verify(mailSender).send(argThat((SimpleMailMessage message) ->
                message.getSubject() != null &&
                message.getSubject().contains("Database Instance Has Been Created") &&
                message.getText() != null &&
                message.getText().contains(DATABASE_NAME) &&
                message.getText().contains(ENGINE_NAME) &&
                message.getText().contains(HOST) &&
                message.getText().contains(String.valueOf(PORT)) &&
                message.getText().contains(USERNAME)
        ));
    }

    @Test
    @DisplayName("Should include password in rotation email")
    void sendPasswordRotatedEmail_ShouldIncludeNewPassword() {
        // When
        emailService.sendPasswordRotatedEmail(TEST_EMAIL, DATABASE_NAME, NEW_PASSWORD);

        // Then
        verify(mailSender).send(argThat((SimpleMailMessage message) ->
                message.getText() != null &&
                message.getText().contains(NEW_PASSWORD) &&
                message.getText().contains(DATABASE_NAME)
        ));
    }

    @Test
    @DisplayName("Should include plan details in upgrade email")
    void sendPlanUpgradeEmail_ShouldIncludePlanDetails() {
        // When
        emailService.sendPlanUpgradeEmail(TEST_EMAIL, OLD_PLAN, NEW_PLAN, NEW_LIMIT);

        // Then
        verify(mailSender).send(argThat((SimpleMailMessage message) ->
                message.getText() != null &&
                message.getText().contains(OLD_PLAN) &&
                message.getText().contains(NEW_PLAN) &&
                message.getText().contains(String.valueOf(NEW_LIMIT))
        ));
    }

    @Test
    @DisplayName("Should include error details in notification email")
    void sendErrorNotification_ShouldIncludeErrorDetails() {
        // Given
        String errorTitle = "Database Connection Failed";
        String errorMessage = "Unable to connect to database instance";

        // When
        emailService.sendErrorNotification(TEST_EMAIL, errorTitle, errorMessage);

        // Then
        verify(mailSender).send(argThat((SimpleMailMessage message) ->
                message.getSubject() != null &&
                message.getSubject().contains(errorTitle) &&
                message.getText() != null &&
                message.getText().contains(errorTitle) &&
                message.getText().contains(errorMessage)
        ));
    }

    @Test
    @DisplayName("Should handle null email addresses gracefully")
    void sendWelcomeEmail_WithNullEmail_ShouldNotFail() {
        // When
        emailService.sendWelcomeEmail(null, TEST_NAME);

        // Then
        // Should not throw exception - the mail sender will handle null addresses
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should handle empty email addresses gracefully")
    void sendWelcomeEmail_WithEmptyEmail_ShouldNotFail() {
        // When
        emailService.sendWelcomeEmail("", TEST_NAME);

        // Then
        // Should not throw exception - the mail sender will handle empty addresses
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should handle null names gracefully")
    void sendWelcomeEmail_WithNullName_ShouldNotFail() {
        // When
        emailService.sendWelcomeEmail(TEST_EMAIL, null);

        // Then
        // Should not throw exception - the email template handles null names
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}