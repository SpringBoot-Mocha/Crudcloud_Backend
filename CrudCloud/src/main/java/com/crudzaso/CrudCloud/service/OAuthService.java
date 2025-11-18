package com.crudzaso.CrudCloud.service;

import com.crudzaso.CrudCloud.dto.OAuthUserInfo;

/**
 * Interface for validating and processing OAuth tokens from different providers.
 * Implementations: Google, GitHub, etc.
 */
public interface OAuthService {
    /**
     * Validates a Google token and returns user information.
     *
     * @param googleToken Google JWT token
     * @return OAuthUserInfo with Google user data
     * @throws Exception if token is invalid or expired
     */
    OAuthUserInfo validateGoogleToken(String googleToken);

    /**
     * Validates a GitHub token/code and returns user information.
     *
     * @param githubToken GitHub token (personal access token or authorization code)
     * @return OAuthUserInfo with GitHub user data
     * @throws Exception if token is invalid or expired
     */
    OAuthUserInfo validateGitHubToken(String githubToken);
}
