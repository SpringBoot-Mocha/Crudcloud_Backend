package com.crudzaso.CrudCloud.service;

import com.crudzaso.CrudCloud.domain.entity.User;

/**
 * Service interface for sending Discord webhook notifications.
 * Handles sending user registration notifications and other events to Discord channels.
 */
public interface DiscordNotificationService {

    /**
     * Sends a registration notification to Discord when a new user signs up.
     *
     * @param user The newly registered user
     * @param planName The subscription plan name (FREE, STANDARD, PREMIUM)
     */
    void sendRegistrationNotification(User user, String planName);
}
