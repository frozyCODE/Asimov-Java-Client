package com.asimov.client.services;

import com.asimov.client.api.ApiClient;
import com.asimov.client.models.Eleve;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class EleveService {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static CompletableFuture<List<Eleve>> recupererTousAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonString = ApiClient.getInstance().get("/eleves");
                return mapper.readValue(jsonString, new TypeReference<List<Eleve>>(){});
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    public static CompletableFuture<Eleve> createEleveAsync(Eleve eleve) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonBody = mapper.writeValueAsString(eleve);
                String responseJson = ApiClient.getInstance().post("/eleves", jsonBody);
                return mapper.readValue(responseJson, Eleve.class);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    public static CompletableFuture<Eleve> updateEleveAsync(Eleve eleve) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonBody = mapper.writeValueAsString(eleve);

                // LIGNE DE DEBUGGING À AJOUTER :
                System.out.println("JSON envoyé au serveur : " + jsonBody);

                String responseJson = ApiClient.getInstance().put("/eleves/" + eleve.getId(), jsonBody);
                return mapper.readValue(responseJson, Eleve.class);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    public static CompletableFuture<Void> deleteEleveAsync(int id) {
        return CompletableFuture.runAsync(() -> {
            try {
                ApiClient.getInstance().delete("/eleves/" + id);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }
}