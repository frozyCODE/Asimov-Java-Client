package com.asimov.client.services;

import com.asimov.client.api.ApiClient;
import com.asimov.client.models.AuthRequest;
import com.asimov.client.models.AuthResponse;
import com.asimov.client.models.Utilisateur;
import com.asimov.client.utils.UserSession;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Service gérant l'authentification avec l'API Node.js.
 */
public class AuthService {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static CompletableFuture<String> loginAsync(String email, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                AuthRequest request = new AuthRequest(email, password);
                String jsonBody = mapper.writeValueAsString(request);

                String responseJson = ApiClient.getInstance().post("/auth/login", jsonBody);
                AuthResponse authResponse = mapper.readValue(responseJson, AuthResponse.class);

                String token = null;
                if (authResponse != null && authResponse.getData() != null) {
                    token = authResponse.getData().getToken();
                    Utilisateur utilisateur = authResponse.getData().getUtilisateur();

                    if (token != null && utilisateur != null) {
                        // On passe maintenant l'ID de l'utilisateur
                        UserSession.getInstance().login(
                                utilisateur.getId(),
                                token,
                                utilisateur.getEmail(),
                                utilisateur.getNom(),
                                utilisateur.getPrenom(),
                                utilisateur.getRole()
                        );
                        ApiClient.getInstance().setToken(token);
                    }
                }
                return token;
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }
}