package com.crudzaso.CrudCloud.dto.discord;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Represents a Discord embed
 * Embeds are used to display rich content in Discord messages
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscordEmbed {

    /**
     * The title of the embed
     */
    private String title;

    /**
     * The description of the embed
     */
    private String description;

    /**
     * Color of the embed (as RGB integer)
     * Examples: 16711680 for red, 65280 for green, 255 for blue
     */
    private Integer color;

    /**
     * List of fields in the embed
     */
    private List<DiscordEmbedField> fields;

    /**
     * Footer of the embed
     */
    private DiscordFooter footer;

    /**
     * Timestamp of the embed (ISO 8601 format)
     */
    private String timestamp;
}
