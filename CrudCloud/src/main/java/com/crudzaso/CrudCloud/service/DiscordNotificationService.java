package com.crudzaso.CrudCloud.service;

import com.crudzaso.CrudCloud.domain.entity.User;

/**
 * Service interface for Discord webhook notifications
 * Handles sending registration notifications to Discord channels
 */
public interface DiscordNotificationService {

    /**
     * Send a registration notification to Discord
     * @param user The registered user
     * @param planName The name of the subscription plan (e.g., "Free", "Standard", "Premium")
     */
    void sendRegistrationNotification(User user, String planName);
}
