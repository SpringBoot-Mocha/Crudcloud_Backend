package com.crudzaso.CrudCloud.dto.discord;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the footer of a Discord embed
 * Contains footer text and optional icon URL
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscordFooter {

    /**
     * The footer text
     */
    private String text;

    /**
     * URL of the footer icon
     */
    @JsonProperty("icon_url")
    private String iconUrl;
}
