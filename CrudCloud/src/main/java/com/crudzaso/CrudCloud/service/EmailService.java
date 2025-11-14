package com.crudzaso.CrudCloud.service;

/**
 * Service for sending email notifications to users.
 *
 * Handles:
 * - Welcome emails on registration
 * - Database instance created notifications
 * - Password rotation notifications
 * - Plan upgrade confirmations
 */
public interface EmailService {

    /**
     * Send welcome email to new user
     *
     * @param userEmail Email address of the user
     * @param userName Name of the user
     */
    void sendWelcomeEmail(String userEmail, String userName);

    /**
     * Send email notification when database instance is created.
     * Includes host, port, username (but NOT password for security).
     * Password shown only ONCE in response body.
     *
     * @param userEmail Email address of the user
     * @param userName Name of the user
     * @param databaseName Name of the created database
     * @param engineName Database engine type (PostgreSQL, MySQL, etc.)
     * @param host VPS host address
     * @param port Database port
     * @param username Database username
     */
    void sendInstanceCreatedEmail(
        String userEmail,
        String userName,
        String databaseName,
        String engineName,
        String host,
        int port,
        String username
    );

    /**
     * Send email notification when database password is rotated.
     * Includes the NEW password (only time it's shown).
     *
     * @param userEmail Email address of the user
     * @param databaseName Name of the database
     * @param newPassword The new database password
     */
    void sendPasswordRotatedEmail(
        String userEmail,
        String databaseName,
        String newPassword
    );

    /**
     * Send email notification when plan is upgraded.
     *
     * @param userEmail Email address of the user
     * @param oldPlan Old plan name
     * @param newPlan New plan name
     * @param newLimit New instance limit
     */
    void sendPlanUpgradeEmail(
        String userEmail,
        String oldPlan,
        String newPlan,
        int newLimit
    );

    /**
     * Send error notification to user
     *
     * @param userEmail Email address of the user
     * @param errorTitle Title of the error
     * @param errorMessage Description of the error
     */
    void sendErrorNotification(
        String userEmail,
        String errorTitle,
        String errorMessage
    );
}
