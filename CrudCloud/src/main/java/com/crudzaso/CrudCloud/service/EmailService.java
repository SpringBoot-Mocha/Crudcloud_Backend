package com.crudzaso.CrudCloud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

/**
 * Email Service - Maneja notificaciones por correo electrónico
 * para eventos de CrudCloud:
 * - Creación de instancia
 * - Rotación de contraseña
 * - Cambio de plan
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    /**
     * Notificación: Instancia de BD creada exitosamente
     */
    public void notifyInstanceCreated(
            String userEmail,
            String userName,
            String instanceName,
            String databaseEngine,
            String host,
            Integer port,
            String databaseName,
            String username) {

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", userName);
            variables.put("instanceName", instanceName);
            variables.put("databaseEngine", databaseEngine);
            variables.put("host", host);
            variables.put("port", port);
            variables.put("databaseName", databaseName);
            variables.put("username", username);

            String htmlContent = renderTemplate("emails/instance-created", variables);
            sendEmail(userEmail, "🚀 Tu instancia de base de datos está lista", htmlContent);

            log.info("✅ Email de instancia creada enviado a: {}", userEmail);
        } catch (Exception e) {
            log.error("❌ Error enviando email de instancia creada a {}: {}", userEmail, e.getMessage(), e);
        }
    }

    /**
     * Notificación: Contraseña de BD rotada
     */
    public void notifyPasswordRotated(
            String userEmail,
            String userName,
            String instanceName,
            String host,
            Integer port,
            String databaseName,
            String username,
            String newPassword) {

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", userName);
            variables.put("instanceName", instanceName);
            variables.put("host", host);
            variables.put("port", port);
            variables.put("databaseName", databaseName);
            variables.put("username", username);
            variables.put("newPassword", newPassword);
            variables.put("expiresIn", "No expira (válida indefinidamente)");

            String htmlContent = renderTemplate("emails/password-rotated", variables);
            sendEmail(userEmail, "🔄 Tu contraseña de base de datos ha sido rotada", htmlContent);

            log.info("✅ Email de contraseña rotada enviado a: {}", userEmail);
        } catch (Exception e) {
            log.error("❌ Error enviando email de rotación a {}: {}", userEmail, e.getMessage(), e);
        }
    }

    /**
     * Notificación: Plan actualizado
     */
    public void notifyPlanChanged(
            String userEmail,
            String userName,
            String newPlanName,
            Integer maxInstances,
            Integer storageMB,
            String price) {

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", userName);
            variables.put("newPlanName", newPlanName);
            variables.put("maxInstances", maxInstances);
            variables.put("storageMB", storageMB);
            variables.put("price", price);

            String htmlContent = renderTemplate("emails/plan-updated", variables);
            sendEmail(userEmail, "📊 Tu plan ha sido actualizado", htmlContent);

            log.info("✅ Email de cambio de plan enviado a: {}", userEmail);
        } catch (Exception e) {
            log.error("❌ Error enviando email de cambio de plan a {}: {}", userEmail, e.getMessage(), e);
        }
    }

    private String renderTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("noreply@crudcloud.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        message.addHeader("X-Service", "CrudCloud");
        message.addHeader("X-Priority", "3");

        mailSender.send(message);
    }
}
