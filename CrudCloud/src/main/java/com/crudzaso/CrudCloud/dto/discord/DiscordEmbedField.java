package com.crudzaso.CrudCloud.dto.discord;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a field in a Discord embed.
 * Used to construct rich embeds in Discord messages.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscordEmbedField {
    /**
     * Field name (max 256 characters)
     */
    private String name;

    /**
     * Field value (max 1024 characters)
     */
    private String value;

    /**
     * Whether this field should appear inline with other fields
     */
    @JsonProperty("inline")
    private boolean inline;
}
