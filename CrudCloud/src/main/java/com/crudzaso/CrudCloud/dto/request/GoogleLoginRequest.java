package com.crudzaso.CrudCloud.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Google OAuth2 login.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginRequest {
    
    /**
     * Google ID token obtained from Google Sign-In.
     * Accepts both "token" and "idToken" as field names for compatibility.
     */
    @NotBlank(message = "Google ID token is required")
    @JsonProperty(value = "idToken", access = JsonProperty.Access.WRITE_ONLY)
    private String idToken;
    
    /**
     * Alternative field name for the Google ID token.
     * This allows the frontend to send either "token" or "idToken".
     */
    @JsonProperty("token")
    public void setToken(String token) {
        this.idToken = token;
    }
}
