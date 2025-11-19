package com.crudzaso.CrudCloud.dto.discord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing the footer of a Discord embed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscordFooter {
    /**
     * Footer text (max 2048 characters)
     */
    private String text;

    /**
     * URL of the footer icon (optional)
     */
    private String icon_url;
}
