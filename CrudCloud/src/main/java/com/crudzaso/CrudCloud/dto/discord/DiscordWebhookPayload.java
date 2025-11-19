package com.crudzaso.CrudCloud.dto.discord;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Represents the payload sent to a Discord webhook
 * Contains message content and embed information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscordWebhookPayload {

    /**
     * Plain text content of the message
     */
    private String content;

    /**
     * List of embeds to include in the message
     */
    private List<DiscordEmbed> embeds;

    /**
     * Username that will be displayed for the webhook message
     */
    private String username;

    /**
     * Avatar URL for the webhook message
     */
    @JsonProperty("avatar_url")
    private String avatarUrl;

    /**
     * Whether the message should be text-to-speech
     */
    @JsonProperty("tts")
    private Boolean textToSpeech;
}
