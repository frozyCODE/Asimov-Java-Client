package com.asimov.client.api;

import com.asimov.client.exceptions.ApiAuthException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Logger;

/**
 * Client HTTP singleton chargé de communiquer avec l'API Node.js.
 * Gère l'injection automatique du token JWT et l'interception des erreurs HTTP.
 */
public class ApiClient {
    private static final Logger LOGGER = Logger.getLogger(ApiClient.class.getName());
    private static ApiClient instance;
    private final HttpClient httpClient;
    private String jwtToken;
    private final String BASE_URL = "http://localhost:3000/api";

    private ApiClient() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public static ApiClient getInstance() {
        if (instance == null) instance = new ApiClient();
        return instance;
    }

    public void setToken(String token) {
        this.jwtToken = token;
        LOGGER.info("Token JWT mis à jour dans ApiClient.");
    }

    /**
     * Vérifie le code de statut HTTP de la réponse.
     * Sépare l'erreur 401 (Session expirée) de l'erreur 403 (Accès interdit).
     */
    private void checkResponseCode(HttpResponse<String> response) {
        if (response.statusCode() == 401) {
            throw new ApiAuthException("Session expirée ou non valide.");
        }
        if (response.statusCode() == 403) {
            throw new RuntimeException("Accès interdit (403) : Votre rôle ne permet pas cette action.");
        }
        if (response.statusCode() >= 400) {
            throw new RuntimeException("Erreur API : " + response.statusCode() + " - " + response.body());
        }
    }

    public String get(String endpoint) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Accept", "application/json");

        if (jwtToken != null && !jwtToken.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + jwtToken);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        checkResponseCode(response);
        return response.body();
    }

    public String post(String endpoint, String jsonBody) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

        if (jwtToken != null && !jwtToken.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + jwtToken);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        checkResponseCode(response);
        return response.body();
    }

    public String put(String endpoint, String jsonBody) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody));

        if (jwtToken != null && !jwtToken.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + jwtToken);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        checkResponseCode(response);
        return response.body();
    }

    /**
     * Envoie une requête HTTP PATCH (Modification partielle).
     *
     * @param endpoint L'URL de la route (ex: /moyennes/1/valider).
     * @param jsonBody Le corps de la requête en JSON.
     * @return La réponse de l'API sous forme de chaîne.
     */
    public String patch(String endpoint, String jsonBody) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody));

        if (jwtToken != null && !jwtToken.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + jwtToken);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        checkResponseCode(response);
        return response.body();
    }

    public String delete(String endpoint) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Accept", "application/json")
                .DELETE();

        if (jwtToken != null && !jwtToken.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + jwtToken);
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        checkResponseCode(response);
        return response.body();
    }
}