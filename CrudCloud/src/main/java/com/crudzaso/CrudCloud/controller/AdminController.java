package com.crudzaso.CrudCloud.controller;

import com.crudzaso.CrudCloud.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Admin endpoints for testing and diagnostics.
 *
 * Provides endpoints for testing email notifications and other admin functions.
 * These endpoints should be restricted to admin users in production.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin testing and diagnostics endpoints")
public class AdminController {

    private final EmailService emailService;

    /**
     * Test email notifications.
     *
     * @param type Type of email to send: "instance-created", "password-rotated", or "plan-updated"
     * @param email Email address to send to
     * @return Success/failure status
     */
    @PostMapping("/test-email")
    @Operation(
        summary = "Test email notification",
        description = "Sends a test email notification. Types: instance-created, password-rotated, plan-updated"
    )
    public ResponseEntity<Map<String, Object>> testEmail(
            @RequestParam String type,
            @RequestParam String email) {

        Map<String, Object> response = new HashMap<>();

        try {
            switch (type.toLowerCase()) {
                case "instance-created":
                    log.info("📧 Testing instance-created email to: {}", email);
                    emailService.notifyInstanceCreated(
                            email,
                            "Test User",
                            "test-postgres-db",
                            "PostgreSQL 15",
                            "db.example.com",
                            5432,
                            "testdb",
                            "dbadmin"
                    );
                    response.put("status", "success");
                    response.put("message", "instance-created email sent");
                    response.put("type", "instance-created");
                    break;

                case "password-rotated":
                    log.info("📧 Testing password-rotated email to: {}", email);
                    emailService.notifyPasswordRotated(
                            email,
                            "Test User",
                            "test-postgres-db",
                            "db.example.com",
                            5432,
                            "testdb",
                            "dbadmin",
                            "NewSecurePassword123!@#"
                    );
                    response.put("status", "success");
                    response.put("message", "password-rotated email sent");
                    response.put("type", "password-rotated");
                    break;

                case "plan-updated":
                    log.info("📧 Testing plan-updated email to: {}", email);
                    emailService.notifyPlanChanged(
                            email,
                            "Test User",
                            "Premium Plan",
                            10,
                            500,
                            "$29.99/month"
                    );
                    response.put("status", "success");
                    response.put("message", "plan-updated email sent");
                    response.put("type", "plan-updated");
                    break;

                default:
                    log.warn("❌ Invalid email type requested: {}", type);
                    response.put("status", "error");
                    response.put("message", "Invalid email type. Use: instance-created, password-rotated, or plan-updated");
                    return ResponseEntity.badRequest().body(response);
            }

            log.info("✅ Test email sent successfully: type={}, email={}", type, email);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error sending test email: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "Failed to send test email: " + e.getMessage());
            response.put("type", type);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
