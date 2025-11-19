package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.User;
import com.crudzaso.CrudCloud.dto.discord.DiscordEmbed;
import com.crudzaso.CrudCloud.dto.discord.DiscordEmbedField;
import com.crudzaso.CrudCloud.dto.discord.DiscordFooter;
import com.crudzaso.CrudCloud.dto.discord.DiscordWebhookPayload;
import com.crudzaso.CrudCloud.service.DiscordNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;

/**
 * Implementation of Discord notification service.
 * Sends webhook messages to Discord channel when users register.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DiscordNotificationServiceImpl implements DiscordNotificationService {

    private final RestTemplate restTemplate;

    @Value("${discord.webhook.url:}")
    private String webhookUrl;

    /**
     * Color codes for different subscription plans
     */
    private static final int COLOR_FREE = 9498935;      // Gray
    private static final int COLOR_STANDARD = 3447003;   // Blue
    private static final int COLOR_PREMIUM = 15844367;   // Gold

    @Override
    public void sendRegistrationNotification(User user, String planName) {
        // Skip if webhook URL is not configured
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("Discord webhook URL is not configured. Skipping notification.");
            return;
        }

        try {
            DiscordWebhookPayload payload = buildRegistrationEmbed(user, planName);
            restTemplate.postForObject(webhookUrl, payload, String.class);
            log.info("Registration notification sent to Discord for user: {}", user.getEmail());
        } catch (RestClientException e) {
            log.error("Error sending notification to Discord for user: {}. Exception: {}", user.getEmail(), e.getMessage());
            // Don't throw exception - failure to notify Discord shouldn't block user registration
        } catch (Exception e) {
            log.error("Unexpected error sending Discord notification for user: {}", user.getEmail(), e);
            // Don't throw exception - failure to notify Discord shouldn't block user registration
        }
    }

    /**
     * Builds the Discord embed payload for a registration notification.
     *
     * @param user The registered user
     * @param planName The subscription plan
     * @return DiscordWebhookPayload ready to send
     */
    private DiscordWebhookPayload buildRegistrationEmbed(User user, String planName) {
        DiscordWebhookPayload payload = new DiscordWebhookPayload();
        payload.setUsername("CrudCloud Bot");

        DiscordEmbed embed = new DiscordEmbed();
        embed.setTitle("🎉 Nuevo Usuario Registrado");
        embed.setColor(getColorByPlan(planName));
        embed.setTimestamp(System.currentTimeMillis() / 1000);

        // Build embed fields
        embed.setFields(Arrays.asList(
            DiscordEmbedField.builder()
                .name("Email")
                .value(user.getEmail())
                .inline(false)
                .build(),
            DiscordEmbedField.builder()
                .name("Plan")
                .value(planName.toUpperCase())
                .inline(true)
                .build(),
            DiscordEmbedField.builder()
                .name("Estado")
                .value("✅ Activo")
                .inline(true)
                .build()
        ));

        // Set footer
        embed.setFooter(DiscordFooter.builder()
            .text("CrudCloud Registration System")
            .build());

        payload.setEmbeds(Arrays.asList(embed));
        return payload;
    }

    /**
     * Determines the embed color based on the subscription plan.
     *
     * @param planName The subscription plan name
     * @return RGB color code as integer
     */
    private int getColorByPlan(String planName) {
        if (planName == null) {
            return COLOR_FREE;
        }

        return switch (planName.toUpperCase()) {
            case "PREMIUM" -> COLOR_PREMIUM;
            case "STANDARD" -> COLOR_STANDARD;
            default -> COLOR_FREE;
        };
    }
}
