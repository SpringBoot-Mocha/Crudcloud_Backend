package com.crudzaso.CrudCloud.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitHubLoginRequest {
    
    @NotBlank(message = "GitHub access token is required")
    @JsonProperty(value = "accessToken", access = JsonProperty.Access.WRITE_ONLY)
    private String accessToken;
    
    // Allow both "token" and "accessToken" field names for frontend compatibility
    @JsonProperty("token")
    public void setToken(String token) {
        this.accessToken = token;
    }
}
