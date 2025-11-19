package com.crudzaso.CrudCloud.dto.discord;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing a Discord message embed.
 * Embeds allow for rich formatting of messages with titles, descriptions, fields, colors, etc.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscordEmbed {
    /**
     * Title of the embed (max 256 characters)
     */
    private String title;

    /**
     * Description of the embed (max 4096 characters)
     */
    private String description;

    /**
     * Color of the embed sidebar (integer RGB value)
     * Examples: 9498935 (Gray), 3447003 (Blue), 15844367 (Gold)
     */
    private int color;

    /**
     * Array of fields to display in the embed (max 25 fields)
     */
    private List<DiscordEmbedField> fields;

    /**
     * Footer object containing text and optional icon URL
     */
    private DiscordFooter footer;

    /**
     * Unix timestamp for the embed content (in seconds)
     */
    private long timestamp;
}
