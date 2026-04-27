package com.asimov.client.services;

import com.asimov.client.api.ApiClient;
import com.asimov.client.models.Option;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Service de gestion des options académiques de l'établissement.
 */
public class OptionService {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Récupère le catalogue complet des options disponibles.
     */
    public static CompletableFuture<List<Option>> getAllOptionsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = ApiClient.getInstance().get("/options");
                return mapper.readValue(json, new TypeReference<List<Option>>() {});
            } catch (Exception e) {
                throw new CompletionException("Erreur lors de la récupération des options", e);
            }
        });
    }

    /**
     * Crée une nouvelle option dans le catalogue.
     */
    public static CompletableFuture<Void> createOptionAsync(String nom) {
        return CompletableFuture.runAsync(() -> {
            try {
                Map<String, String> payload = Map.of("nom", nom);
                String body = mapper.writeValueAsString(payload);
                ApiClient.getInstance().post("/options", body);
            } catch (Exception e) {
                throw new CompletionException("Erreur lors de la création de l'option", e);
            }
        });
    }

    /**
     * Supprime une option du catalogue.
     */
    public static CompletableFuture<Void> deleteOptionAsync(int id) {
        return CompletableFuture.runAsync(() -> {
            try {
                ApiClient.getInstance().delete("/options/" + id);
            } catch (Exception e) {
                throw new CompletionException("Erreur lors de la suppression de l'option", e);
            }
        });
    }

    /**
     * Récupère les options associées à un élève (via son ID métier).
     */
    public static CompletableFuture<List<Option>> getOptionsByEleveAsync(int eleveId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = ApiClient.getInstance().get("/options/eleve/" + eleveId);
                return mapper.readValue(json, new TypeReference<List<Option>>() {});
            } catch (Exception e) {
                throw new CompletionException("Accès refusé ou élève introuvable", e);
            }
        });
    }

    /**
     * Enregistre un choix d'option pour un élève.
     */
    public static CompletableFuture<Void> assignerOptionAsync(int eleveId, int optionId) {
        return CompletableFuture.runAsync(() -> {
            try {
                Map<String, Integer> payload = Map.of("eleve_id", eleveId, "option_id", optionId);
                String body = mapper.writeValueAsString(payload);
                ApiClient.getInstance().post("/options/choisir", body);
            } catch (Exception e) {
                throw new CompletionException("Erreur lors de l'assignation de l'option", e);
            }
        });
    }

    /**
     * Supprime une option du parcours de l'élève.
     */
    public static CompletableFuture<Void> retirerOptionAsync(int eleveId, int optionId) {
        return CompletableFuture.runAsync(() -> {
            try {
                Map<String, Integer> payload = Map.of("eleve_id", eleveId, "option_id", optionId);
                String body = mapper.writeValueAsString(payload);
                ApiClient.getInstance().post("/options/retirer", body);
            } catch (Exception e) {
                throw new CompletionException("Erreur lors du retrait de l'option", e);
            }
        });
    }
}