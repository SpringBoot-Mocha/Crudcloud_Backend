package com.crudzaso.CrudCloud.service.impl;

import com.crudzaso.CrudCloud.domain.entity.User;
import com.crudzaso.CrudCloud.dto.discord.DiscordEmbed;
import com.crudzaso.CrudCloud.dto.discord.DiscordEmbedField;
import com.crudzaso.CrudCloud.dto.discord.DiscordFooter;
import com.crudzaso.CrudCloud.dto.discord.DiscordWebhookPayload;
import com.crudzaso.CrudCloud.service.DiscordNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Arrays;

/**
 * Implementation of Discord notification service
 * Sends webhook notifications to Discord for user registration events
 */
@Service
@Slf4j
public class DiscordNotificationServiceImpl implements DiscordNotificationService {

    @Value("${discord.webhook.url:}")
    private String discordWebhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Send a registration notification to Discord
     * @param user The registered user
     * @param planName The subscription plan name
     */
    @Override
    public void sendRegistrationNotification(User user, String planName) {
        if (discordWebhookUrl == null || discordWebhookUrl.isBlank()) {
            log.warn("Discord webhook URL not configured, skipping notification");
            return;
        }

        try {
            DiscordWebhookPayload payload = buildRegistrationEmbed(user, planName);
            restTemplate.postForObject(discordWebhookUrl, payload, String.class);
            log.info("Discord notification sent successfully for user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error sending Discord notification for user: {}", user.getEmail(), e);
        }
    }

    /**
     * Build the Discord embed payload for registration
     * @param user The registered user
     * @param planName The subscription plan name
     * @return Discord webhook payload with embed
     */
    private DiscordWebhookPayload buildRegistrationEmbed(User user, String planName) {
        Integer embedColor = getColorByPlan(planName);
        String timestamp = Instant.now().toString();

        DiscordEmbedField emailField = DiscordEmbedField.builder()
                .name("📧 Email")
                .value(user.getEmail())
                .inline(true)
                .build();

        DiscordEmbedField planField = DiscordEmbedField.builder()
                .name("📋 Plan")
                .value(planName)
                .inline(true)
                .build();

        DiscordEmbedField userField = DiscordEmbedField.builder()
                .name("👤 Usuario")
                .value(user.getFirstName() + " " + user.getLastName())
                .inline(false)
                .build();

        DiscordFooter footer = DiscordFooter.builder()
                .text("CrudCloud - Nuevo registro")
                .build();

        DiscordEmbed embed = DiscordEmbed.builder()
                .title("✨ Nuevo usuario registrado")
                .description("Un nuevo usuario se ha registrado en CrudCloud")
                .color(embedColor)
                .fields(Arrays.asList(emailField, planField, userField))
                .footer(footer)
                .timestamp(timestamp)
                .build();

        return DiscordWebhookPayload.builder()
                .content(null)
                .embeds(Arrays.asList(embed))
                .username("CrudCloud Bot")
                .textToSpeech(false)
                .build();
    }

    /**
     * Get color code for a plan type
     * @param planName The plan name
     * @return RGB color code
     */
    private Integer getColorByPlan(String planName) {
        return switch (planName) {
            case "Free" -> 9498935;      // Gray
            case "Standard" -> 3447003;  // Blue
            case "Premium" -> 15844367;  // Gold
            default -> 9498935;          // Default gray
        };
    }
}
