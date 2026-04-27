package com.asimov.client.services;

import com.asimov.client.api.ApiClient;
import com.asimov.client.models.Classe;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Service de communication avec l'API pour la gestion des classes physiques.
 */
public class ClasseService {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Récupère la liste de toutes les classes.
     */
    public static CompletableFuture<List<Classe>> getAllClassesAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = ApiClient.getInstance().get("/classes");
                return mapper.readValue(json, new TypeReference<List<Classe>>() {});
            } catch (Exception e) {
                throw new CompletionException("Erreur récupération classes", e);
            }
        });
    }

    /**
     * Crée une nouvelle classe.
     */
    public static CompletableFuture<Void> createClasseAsync(Classe classe) {
        return CompletableFuture.runAsync(() -> {
            try {
                String body = mapper.writeValueAsString(classe);
                ApiClient.getInstance().post("/classes", body);
            } catch (Exception e) {
                throw new CompletionException("Erreur création classe", e);
            }
        });
    }

    /**
     * Supprime une classe.
     */
    public static CompletableFuture<Void> deleteClasseAsync(int id) {
        return CompletableFuture.runAsync(() -> {
            try {
                ApiClient.getInstance().delete("/classes/" + id);
            } catch (Exception e) {
                throw new CompletionException("Erreur suppression classe", e);
            }
        });
    }
}