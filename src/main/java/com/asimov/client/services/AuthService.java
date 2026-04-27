package com.asimov.client.services;

import com.asimov.client.api.ApiClient;
import com.asimov.client.models.AuthRequest;
import com.asimov.client.models.AuthResponse;
import com.asimov.client.models.Utilisateur;
import com.asimov.client.models.Eleve;
import com.asimov.client.utils.UserSession;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Service gérant l'authentification et l'initialisation du profil utilisateur.
 */
public class AuthService {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Authentifie l'utilisateur et récupère le profil métier si nécessaire.
     * <p>
     * Après réception du jeton JWT, si l'utilisateur est un élève,
     * un appel vers /api/eleves/profile est effectué pour obtenir l'ID élève réel.
     * </p>
     *
     * @param email    L'email de l'utilisateur.
     * @param password Le mot de passe.
     * @return Un CompletableFuture contenant le token JWT.
     */
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
                        UserSession.getInstance().login(
                                utilisateur.getId(),
                                token,
                                utilisateur.getEmail(),
                                utilisateur.getNom(),
                                utilisateur.getPrenom(),
                                utilisateur.getRole()
                        );
                        ApiClient.getInstance().setToken(token);

                        if ("ELEVE".equalsIgnoreCase(utilisateur.getRole())) {
                            Eleve eleveProfile = EleveService.recupererProfilEleveConnecte().join();
                            if (eleveProfile != null) {
                                UserSession.getInstance().setEleveId(eleveProfile.getId());
                            }
                        }
                    }
                }
                return token;
            } catch (Exception e) {
                throw new CompletionException("Identifiants incorrects ou erreur serveur", e);
            }
        });
    }
}