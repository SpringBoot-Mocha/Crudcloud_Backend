package com.crudzaso.CrudCloud.controller;

import com.crudzaso.CrudCloud.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin Controller - Testing endpoint for Email Service
 *
 * Proporciona endpoints para testear las notificaciones por email
 * sin necesidad de crear instancias o cambiar planes.
 *
 * ENDPOINTS:
 * POST /api/v1/admin/test-email?email=...&type=...
 *   - instance-created: Test email de instancia creada
 *   - password-rotated: Test email de contraseña rotada
 *   - plan-changed: Test email de cambio de plan
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final EmailService emailService;

    /**
     * Test Email Service - Envía emails de prueba
     *
     * Ejemplo:
     * curl -X POST "http://localhost:8080/api/v1/admin/test-email?email=test@example.com&type=instance-created"
     */
    @PostMapping("/test-email")
    public ResponseEntity<?> testEmail(
            @RequestParam String email,
            @RequestParam String type) {

        log.info("Testing email notification: type={}, email={}", type, email);

        try {
            switch (type.toLowerCase()) {
                case "instance-created":
                    emailService.notifyInstanceCreated(
                            email,
                            "Test User",
                            "test-db-instance",
                            "PostgreSQL",
                            "91.98.225.17",
                            5432,
                            "testdb",
                            "testuser"
                    );
                    return ResponseEntity.ok()
                            .body(new TestEmailResponse("success", "Email enviado: Instance Created"));

                case "password-rotated":
                    emailService.notifyPasswordRotated(
                            email,
                            "Test User",
                            "test-db-instance",
                            "91.98.225.17",
                            5432,
                            "testdb",
                            "testuser",
                            "NewPassword123!"
                    );
                    return ResponseEntity.ok()
                            .body(new TestEmailResponse("success", "Email enviado: Password Rotated"));

                case "plan-changed":
                    emailService.notifyPlanChanged(
                            email,
                            "Test User",
                            "Premium",
                            10,
                            500,
                            "$99.99"
                    );
                    return ResponseEntity.ok()
                            .body(new TestEmailResponse("success", "Email enviado: Plan Changed"));

                default:
                    return ResponseEntity.badRequest()
                            .body(new TestEmailResponse("error", "Tipo de email no válido: " + type));
            }
        } catch (Exception e) {
            log.error("Error sending test email", e);
            return ResponseEntity.status(500)
                    .body(new TestEmailResponse("error", "Error: " + e.getMessage()));
        }
    }

    /**
     * Health check del Email Service
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok()
                .body(new HealthResponse("OK", "Email Service está funcionando"));
    }

    // ====== DTO Classes ======

    public static class TestEmailResponse {
        public String status;
        public String message;

        public TestEmailResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class HealthResponse {
        public String status;
        public String message;

        public HealthResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}
