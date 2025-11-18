package com.crudzaso.CrudCloud.dto;

import com.crudzaso.CrudCloud.domain.enums.OAuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO interno para representar información de usuario de OAuth.
 * Se usa para pasar datos del token OAuth validado a través del servicio.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthUserInfo {
    /**
     * ID único del usuario en el proveedor OAuth (e.g., Google subject, GitHub user ID)
     */
    private String providerId;

    /**
     * Email del usuario desde OAuth
     */
    private String email;

    /**
     * Nombre del usuario desde OAuth
     */
    private String firstName;

    /**
     * Apellido del usuario desde OAuth
     */
    private String lastName;

    /**
     * Proveedor OAuth (GOOGLE, GITHUB, etc.)
     */
    private OAuthProvider provider;
}
