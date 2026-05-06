package com.asimov.client.services;

import com.asimov.client.api.ApiClient;
import com.asimov.client.models.Eleve;
import com.asimov.client.models.Parent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Service métier pour la gestion des parents et de leurs relations.
 * <p>
 * Communique avec l'API Node.js pour récupérer les listes de parents,
 * les profils individuels et les enfants associés.
 * </p>
 */
public class ParentService {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Récupère la liste de tous les parents inscrits (Vue Administration).
     * 
     * @return Un CompletableFuture contenant la liste des parents.
     */
    public static CompletableFuture<List<Parent>> recupererTousLesParentsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = ApiClient.getInstance().get("/parents");
                JsonNode root = mapper.readTree(json);
                return mapper.convertValue(root.get("data"), new TypeReference<List<Parent>>() {
                });
            } catch (Exception e) {
                throw new CompletionException("Erreur lors de la récupération des parents", e);
            }
        });
    }

    /**
     * Récupère le profil du parent actuellement connecté.
     * 
     * @return Un CompletableFuture contenant l'objet Parent.
     */
    public static CompletableFuture<Parent> recupererMonProfilAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = ApiClient.getInstance().get("/parents/profile");
                JsonNode root = mapper.readTree(json);
                return mapper.convertValue(root.get("data"), Parent.class);
            } catch (Exception e) {
                throw new CompletionException("Impossible de récupérer le profil parent", e);
            }
        });
    }

    /**
     * Récupère la liste des enfants associés à un parent spécifique.
     * 
     * @param parentId L'identifiant unique du parent.
     * @return Un CompletableFuture contenant la liste des élèves (enfants).
     */
    public static CompletableFuture<List<Eleve>> recupererMesEnfantsAsync(int parentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = ApiClient.getInstance().get("/parents/" + parentId + "/eleves");
                JsonNode root = mapper.readTree(json);
                return mapper.convertValue(root.get("data"), new TypeReference<List<Eleve>>() {
                });
            } catch (Exception e) {
                throw new CompletionException("Erreur lors de la récupération des enfants", e);
            }
        });
    }

    /**
     * Supprime un parent de la base de données.
     * 
     * @param parentId L'ID du parent à supprimer.
     * @return Un CompletableFuture de type Void.
     */
    public static CompletableFuture<Void> supprimerParentAsync(int parentId) {
        return CompletableFuture.runAsync(() -> {
            try {
                ApiClient.getInstance().delete("/parents/" + parentId);
            } catch (Exception e) {
                throw new CompletionException("Erreur lors de la suppression du parent", e);
            }
        });
    }

    /**
     * Crée un nouveau parent dans le système.
     * 
     * @param nom      Le nom de famille.
     * @param prenom   Le prénom.
     * @param email    L'adresse email.
     * @param password Le mot de passe initial.
     * @return Un CompletableFuture de type Void.
     */
    public static CompletableFuture<Void> creerParentAsync(String nom, String prenom, String email, String password) {
        return CompletableFuture.runAsync(() -> {
            try {
                String body = String.format("{\"nom\":\"%s\", \"prenom\":\"%s\", \"email\":\"%s\", \"password\":\"%s\"}",
                        nom, prenom, email, password);
                ApiClient.getInstance().post("/parents", body);
            } catch (Exception e) {
                throw new CompletionException("Erreur lors de la création du parent", e);
            }
        });
    }
}
