package com.crudzaso.CrudCloud.domain.enums;

/**
 * Enum for supported OAuth providers.
 * Represents the external authentication services that the platform accepts.
 */
public enum OAuthProvider {
    GOOGLE("google"),
    GITHUB("github");

    private final String value;

    OAuthProvider(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static OAuthProvider fromValue(String value) {
        for (OAuthProvider provider : OAuthProvider.values()) {
            if (provider.value.equalsIgnoreCase(value)) {
                return provider;
            }
        }
        return null;
    }
}
