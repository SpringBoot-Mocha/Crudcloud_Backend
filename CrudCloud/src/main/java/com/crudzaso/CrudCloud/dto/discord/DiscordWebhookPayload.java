package com.crudzaso.CrudCloud.dto.discord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing the complete payload to be sent to a Discord webhook.
 * This is the main object that gets serialized and sent via HTTP POST to the Discord webhook URL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscordWebhookPayload {
    /**
     * Message content (up to 2000 characters).
     * Can be null if embeds are provided.
     */
    private String content;

    /**
     * Array of embed objects for rich message formatting (max 10 embeds)
     */
    private List<DiscordEmbed> embeds;

    /**
     * Override the default webhook username
     */
    private String username;

    /**
     * Override the default webhook avatar URL
     */
    private String avatar_url;

    /**
     * Whether the message should use text-to-speech
     */
    private boolean tts;
}
