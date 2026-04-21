package com.asimov.client.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse {
    private AuthData data; // Le token est maintenant à l'intérieur de l'objet 'data'
    // Vous pouvez aussi ajouter 'message' et 'success' si vous voulez les récupérer
    // private boolean success;
    // private String message;
}
