package com.asimov.client.services;

import com.asimov.client.api.ApiClient;
import com.asimov.client.models.Eleve;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Service gérant les opérations métiers et la communication avec l'API pour les entités Élève.
 * <p>
 * Il fournit les méthodes nécessaires pour la pagination, la création, la modification,
 * la suppression et la récupération du profil sécurisé.
 * </p>
 */
public class EleveService {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Récupère les informations de profil de l'élève actuellement authentifié.
     *
     * @return Un CompletableFuture contenant l'objet Eleve.
     */
    public static CompletableFuture<Eleve> recupererProfilEleveConnecte() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = ApiClient.getInstance().get("/eleves/profile");
                return mapper.readValue(json, Eleve.class);
            } catch (Exception e) {
                throw new CompletionException("Impossible de récupérer le profil élève", e);
            }
        });
    }

    /**
     * Récupère une liste paginée d'élèves pour l'affichage dans les tableaux de bord.
     *
     * @param page  Le numéro de la page souhaitée.
     * @param limit Le nombre d'enregistrements par page.
     * @return Un CompletableFuture contenant une Map avec la liste des élèves et le nombre total de pages.
     */
    public static CompletableFuture<Map<String, Object>> recupererPaginesAsync(int page, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = String.format("/eleves?page=%d&limit=%d", page, limit);
                String json = ApiClient.getInstance().get(url);
                JsonNode root = mapper.readTree(json);

                Map<String, Object> resultat = new HashMap<>();
                if (root.has("data")) {
                    List<Eleve> liste = mapper.convertValue(root.get("data"), new TypeReference<List<Eleve>>() {});
                    resultat.put("liste", liste);
                }
                if (root.has("meta")) {
                    resultat.put("totalPages", root.get("meta").get("totalPages").asInt());
                }
                return resultat;
            } catch (Exception e) {
                throw new CompletionException("Erreur lors de la pagination des élèves", e);
            }
        });
    }

    /**
     * Récupère une liste exhaustive des élèves (pour peupler des listes déroulantes).
     *
     * @return Un CompletableFuture contenant la liste complète des élèves.
     */
    public static CompletableFuture<List<Eleve>> recupererToutPourSelectionAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = ApiClient.getInstance().get("/eleves?page=1&limit=1000");
                JsonNode root = mapper.readTree(json);
                return mapper.convertValue(root.get("data"), new TypeReference<List<Eleve>>() {});
            } catch (Exception e) {
                throw new CompletionException("Erreur lors de la récupération globale des élèves", e);
            }
        });
    }

    /**
     * Crée un nouvel élève via l'API Node.js.
     *
     * @param eleve L'objet Élève contenant les données saisies par l'utilisateur.
     * @return Un CompletableFuture contenant l'élève créé, mis à jour avec son ID généré.
     */
    public static CompletableFuture<Eleve> createEleveAsync(Eleve eleve) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String body = mapper.writeValueAsString(eleve);
                String response = ApiClient.getInstance().post("/eleves", body);
                JsonNode root = mapper.readTree(response);
                if (root.has("id")) {
                    eleve.setId(root.get("id").asInt());
                }
                return eleve;
            } catch (Exception e) {
                throw new CompletionException("Erreur lors de la création de l'élève", e);
            }
        });
    }

    /**
     * Met à jour les données d'un élève existant.
     *
     * @param eleve L'objet Élève contenant les modifications.
     * @return Un CompletableFuture vide indiquant le succès de l'opération.
     */
    public static CompletableFuture<Void> updateEleveAsync(Eleve eleve) {
        return CompletableFuture.runAsync(() -> {
            try {
                String body = mapper.writeValueAsString(eleve);
                ApiClient.getInstance().put("/eleves/" + eleve.getId(), body);
            } catch (Exception e) {
                throw new CompletionException("Erreur lors de la modification de l'élève", e);
            }
        });
    }

    /**
     * Supprime définitivement un élève et son compte utilisateur associé.
     *
     * @param id L'identifiant métier de l'élève à supprimer.
     * @return Un CompletableFuture vide indiquant le succès de l'opération.
     */
    public static CompletableFuture<Void> deleteEleveAsync(int id) {
        return CompletableFuture.runAsync(() -> {
            try {
                ApiClient.getInstance().delete("/eleves/" + id);
            } catch (Exception e) {
                throw new CompletionException("Erreur lors de la suppression de l'élève", e);
            }
        });
    }
}