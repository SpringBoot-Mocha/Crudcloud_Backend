package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Implementation of EmailService using Gmail SMTP.
 *
 * Sends email notifications for:
 * - User registration
 * - Instance creation
 * - Password rotation
 * - Plan upgrades
 * - Errors
 *
 * CONFIGURATION:
 * Set environment variables (not hardcoded):
 * - GMAIL_USERNAME: Gmail address
 * - GMAIL_APP_PASSWORD: App-specific password (not regular password)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.from:noreply@crudcloud.com}")
    private String fromEmail;

    @Value("${mail.from-name:CrudCloud}")
    private String fromName;

    @Override
    public void sendWelcomeEmail(String userEmail, String userName) {
        log.info("📧 Sending welcome email to: {}", userEmail);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(userEmail);
            message.setSubject("🎉 Welcome to CrudCloud!");

            String body = String.format("""
                    Hello %s,

                    Welcome to CrudCloud! 🚀

                    You've successfully registered. Now you can:
                    - Create up to 2 free database instances
                    - Test with PostgreSQL, MySQL, MongoDB, Redis, and more
                    - Upgrade your plan anytime to get more instances

                    Get started: https://crudcloud.com/dashboard

                    If you have any questions, contact us at support@crudcloud.com

                    Happy coding!
                    CrudCloud Team
                    """, userName);

            message.setText(body);
            mailSender.send(message);

            log.info("✅ Welcome email sent successfully to: {}", userEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send welcome email to {}: {}", userEmail, e.getMessage(), e);
            // Don't throw - email failure shouldn't block user registration
        }
    }

    @Override
    public void sendInstanceCreatedEmail(
            String userEmail,
            String userName,
            String databaseName,
            String engineName,
            String host,
            int port,
            String username) {

        log.info("📧 Sending instance creation email to: {}", userEmail);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(userEmail);
            message.setSubject("✅ Your Database Instance Has Been Created");

            String body = String.format("""
                    Hello %s,

                    Your database instance has been successfully created! 🎉

                    DATABASE DETAILS:
                    ────────────────────────────────────────
                    Name:     %s
                    Engine:   %s
                    Host:     %s
                    Port:     %d
                    Username: %s

                    ⚠️  PASSWORD:
                    Your password was shown in the response when you created this instance.
                    It will NOT be shown again - save it now!
                    If you lose it, you can rotate the password in your dashboard.

                    CONNECTION EXAMPLES:
                    ────────────────────────────────────────
                    PostgreSQL: psql -h %s -p %d -U %s -d %s
                    MySQL:      mysql -h %s -p %d -u %s -p
                    MongoDB:    mongosh --host %s:%d -u %s --db %s

                    NEXT STEPS:
                    1. Save your password somewhere secure
                    2. Download your credentials as PDF from the dashboard
                    3. Connect to your database using the details above
                    4. Start building! 🚀

                    Support: https://docs.crudcloud.com

                    CrudCloud Team
                    """,
                    userName,
                    databaseName, engineName, host, port, username,
                    host, port, username, databaseName,
                    host, port, username,
                    host, port, username, databaseName
            );

            message.setText(body);
            mailSender.send(message);

            log.info("✅ Instance creation email sent successfully to: {}", userEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send instance creation email to {}: {}", userEmail, e.getMessage(), e);
            // Don't throw - email failure shouldn't block instance creation
        }
    }

    @Override
    public void sendPasswordRotatedEmail(String userEmail, String databaseName, String newPassword) {
        log.info("📧 Sending password rotation email to: {}", userEmail);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(userEmail);
            message.setSubject("🔐 Your Database Password Has Been Rotated");

            String body = String.format("""
                    Hello,

                    Your database password has been successfully rotated! 🔄

                    DATABASE: %s
                    NEW PASSWORD: %s

                    ⚠️  IMPORTANT:
                    - This is the ONLY time this password will be displayed
                    - Save it immediately in a secure location
                    - Update your application connection strings
                    - The old password is now invalid

                    If you did not perform this action, please change your password immediately!

                    CrudCloud Team
                    """, databaseName, newPassword);

            message.setText(body);
            mailSender.send(message);

            log.info("✅ Password rotation email sent successfully to: {}", userEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send password rotation email to {}: {}", userEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendPlanUpgradeEmail(String userEmail, String oldPlan, String newPlan, int newLimit) {
        log.info("📧 Sending plan upgrade email to: {}", userEmail);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(userEmail);
            message.setSubject("🎊 Plan Upgraded Successfully!");

            String body = String.format("""
                    Hello,

                    Congratulations! Your plan has been successfully upgraded! 🎉

                    PLAN DETAILS:
                    ────────────────────────────────────────
                    Previous Plan: %s
                    New Plan:      %s
                    Instance Limit: %d

                    You can now create up to %d database instances!

                    Start creating: https://crudcloud.com/dashboard

                    Thank you for trusting CrudCloud! 🚀

                    CrudCloud Team
                    """, oldPlan, newPlan, newLimit, newLimit);

            message.setText(body);
            mailSender.send(message);

            log.info("✅ Plan upgrade email sent successfully to: {}", userEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send plan upgrade email to {}: {}", userEmail, e.getMessage(), e);
        }
    }

    @Override
    public void sendErrorNotification(String userEmail, String errorTitle, String errorMessage) {
        log.warn("📧 Sending error notification to: {}", userEmail);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(userEmail);
            message.setSubject("❌ Error: " + errorTitle);

            String body = String.format("""
                    Hello,

                    We encountered an issue with your CrudCloud account:

                    ERROR: %s
                    ────────────────────────────────────────
                    %s

                    If this persists, please contact support@crudcloud.com

                    CrudCloud Team
                    """, errorTitle, errorMessage);

            message.setText(body);
            mailSender.send(message);

            log.info("✅ Error notification sent successfully to: {}", userEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send error notification to {}: {}", userEmail, e.getMessage(), e);
        }
    }
}
