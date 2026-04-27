package com.asimov.client.services;

import com.asimov.client.api.ApiClient;
import com.asimov.client.models.Moyenne;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Service de gestion des résultats académiques.
 * <p>
 * Ce service permet de consulter, créer, valider ou supprimer les moyennes semestrielles.
 * Il assure la communication avec les points de terminaison sécurisés de l'API Node.js.
 * </p>
 */
public class MoyenneService {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Récupère la liste des moyennes pour une inscription spécifique.
     * <p>
     * Cette méthode est utilisée pour afficher le bulletin ou le relevé de notes
     * associé à une période scolaire donnée.
     * </p>
     *
     * @param inscriptionId L'identifiant technique de l'inscription (clé primaire métier).
     * @return Un CompletableFuture contenant la liste des objets Moyenne.
     * @throws CompletionException En cas d'erreur de réseau ou de désérialisation JSON.
     */
    public static CompletableFuture<List<Moyenne>> getMoyennesByInscriptionAsync(int inscriptionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = ApiClient.getInstance().get("/moyennes/inscription/" + inscriptionId);
                return mapper.readValue(json, new TypeReference<List<Moyenne>>() {});
            } catch (Exception e) {
                throw new CompletionException("Échec de la récupération des moyennes pour l'inscription : " + inscriptionId, e);
            }
        });
    }

    /**
     * Enregistre une nouvelle évaluation semestrielle en base de données.
     *
     * @param moyenne L'objet Moyenne dûment complété (inscription_id, semestre, note).
     * @return Un CompletableFuture vide indiquant la fin de l'opération.
     */
    public static CompletableFuture<Void> createMoyenneAsync(Moyenne moyenne) {
        return CompletableFuture.runAsync(() -> {
            try {
                String body = mapper.writeValueAsString(moyenne);
                ApiClient.getInstance().post("/moyennes", body);
            } catch (Exception e) {
                throw new CompletionException("Impossible d'enregistrer la moyenne", e);
            }
        });
    }

    /**
     * Valide de manière définitive une moyenne semestrielle.
     * <p>
     * Cette opération est une action d'administration réservée aux rôles autorisés (Proviseur).
     * Elle fige la note pour le calcul des bulletins officiels.
     * </p>
     *
     * @param moyenneId L'identifiant unique de la moyenne à valider.
     * @return Un CompletableFuture de type Void.
     */
    public static CompletableFuture<Void> validerMoyenneAsync(int moyenneId) {
        return CompletableFuture.runAsync(() -> {
            try {
                ApiClient.getInstance().patch("/moyennes/" + moyenneId + "/valider", "{}");
            } catch (Exception e) {
                throw new CompletionException("Erreur lors de la validation de la moyenne ID: " + moyenneId, e);
            }
        });
    }

    /**
     * Supprime une entrée de moyenne de la base de données.
     *
     * @param moyenneId L'identifiant technique de la ressource à supprimer.
     * @return Un CompletableFuture de type Void.
     */
    public static CompletableFuture<Void> deleteMoyenneAsync(int moyenneId) {
        return CompletableFuture.runAsync(() -> {
            try {
                ApiClient.getInstance().delete("/moyennes/" + moyenneId);
            } catch (Exception e) {
                throw new CompletionException("Échec de la suppression de la moyenne", e);
            }
        });
    }
}