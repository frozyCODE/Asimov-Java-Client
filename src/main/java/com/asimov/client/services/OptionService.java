package com.asimov.client.services;

import com.asimov.client.api.ApiClient;
import com.asimov.client.models.Option;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Service gérant la communication avec l'API concernant les options des élèves et le catalogue.
 */
public class OptionService {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static CompletableFuture<List<Option>> getAllOptionsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = ApiClient.getInstance().get("/options");
                return mapper.readValue(json, new TypeReference<List<Option>>() {});
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    public static CompletableFuture<List<Option>> getOptionsByEleveAsync(int eleveId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = ApiClient.getInstance().get("/options/eleve/" + eleveId);
                return mapper.readValue(json, new TypeReference<List<Option>>() {});
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    public static CompletableFuture<Void> choisirOptionAsync(int eleveId, int optionId) {
        return CompletableFuture.runAsync(() -> {
            try {
                String body = String.format("{\"eleve_id\": %d, \"option_id\": %d}", eleveId, optionId);
                ApiClient.getInstance().post("/options/choisir", body);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    public static CompletableFuture<Void> desisterOptionAsync(int eleveId, int optionId) {
        return CompletableFuture.runAsync(() -> {
            try {
                String body = String.format("{\"eleve_id\": %d, \"option_id\": %d}", eleveId, optionId);
                ApiClient.getInstance().post("/options/retirer", body);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Crée une nouvelle option dans le catalogue.
     */
    public static CompletableFuture<Void> createOptionAsync(String nom) {
        return CompletableFuture.runAsync(() -> {
            try {
                String body = String.format("{\"nom\": \"%s\"}", nom);
                ApiClient.getInstance().post("/options", body);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Supprime définitivement une option du catalogue.
     */
    public static CompletableFuture<Void> deleteOptionAsync(int id) {
        return CompletableFuture.runAsync(() -> {
            try {
                ApiClient.getInstance().delete("/options/" + id);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }
}