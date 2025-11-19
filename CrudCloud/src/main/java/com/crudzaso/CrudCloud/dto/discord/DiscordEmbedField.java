package com.crudzaso.CrudCloud.dto.discord;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a field in a Discord embed
 * Fields are used to add additional information to embeds
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscordEmbedField {

    /**
     * The name of the field
     */
    private String name;

    /**
     * The value of the field
     */
    private String value;

    /**
     * Whether the field should be displayed inline
     */
    @JsonProperty("inline")
    private Boolean inline;
}
