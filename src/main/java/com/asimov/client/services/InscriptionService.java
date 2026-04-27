package com.asimov.client.services;

import com.asimov.client.api.ApiClient;
import com.asimov.client.models.EleveInscrit;
import com.asimov.client.models.Inscription;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Service de gestion des flux d'inscriptions scolaires.
 * <p>
 * Gère les affectations des élèves aux classes et la consultation des historiques
 * d'inscription. Utilise systématiquement les identifiants métiers pour la sécurité.
 * </p>
 */
public class InscriptionService {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Récupère la liste des élèves affectés à une classe spécifique.
     *
     * @param classeId L'identifiant technique de la classe.
     * @return Une liste d'objets EleveInscrit contenant les détails du profil.
     */
    public static CompletableFuture<List<EleveInscrit>> getElevesByClasseAsync(int classeId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = ApiClient.getInstance().get("/inscriptions/classe/" + classeId);
                return mapper.readValue(json, new TypeReference<List<EleveInscrit>>() {});
            } catch (Exception e) {
                throw new CompletionException("Erreur lors de la récupération de la liste de classe", e);
            }
        });
    }

    /**
     * Réalise l'inscription d'un élève dans une classe.
     *
     * @param eleveId  L'identifiant métier de l'élève (id issu du profil).
     * @param classeId L'identifiant de la classe cible.
     * @return Un CompletableFuture de type Void.
     */
    public static CompletableFuture<Void> inscrireEleveAsync(int eleveId, int classeId) {
        return CompletableFuture.runAsync(() -> {
            try {
                Map<String, Integer> payload = Map.of(
                        "eleve_id", eleveId,
                        "classe_id", classeId
                );
                String body = mapper.writeValueAsString(payload);
                ApiClient.getInstance().post("/inscriptions", body);
            } catch (Exception e) {
                throw new CompletionException("L'inscription a échoué pour l'élève ID: " + eleveId, e);
            }
        });
    }

    /**
     * Supprime une inscription active (désinscription).
     *
     * @param inscriptionId L'identifiant de la ligne d'inscription à supprimer.
     * @return Un CompletableFuture de type Void.
     */
    public static CompletableFuture<Void> desinscrireEleveAsync(int inscriptionId) {
        return CompletableFuture.runAsync(() -> {
            try {
                ApiClient.getInstance().delete("/inscriptions/" + inscriptionId);
            } catch (Exception e) {
                throw new CompletionException("Erreur lors de la désinscription", e);
            }
        });
    }

    /**
     * Récupère l'historique complet des inscriptions pour un élève donné.
     * <p>
     * Note : L'ID passé en paramètre doit impérativement correspondre à l'ID métier
     * de l'élève (eleve_id) et non à l'ID utilisateur.
     * </p>
     *
     * @param eleveId L'identifiant métier de l'élève (récupéré via le profil).
     * @return Une liste d'inscriptions classées par ordre chronologique.
     */
    public static CompletableFuture<List<Inscription>> getInscriptionsByEleveAsync(int eleveId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = ApiClient.getInstance().get("/inscriptions/eleve/" + eleveId);
                return mapper.readValue(json, new TypeReference<List<Inscription>>() {});
            } catch (Exception e) {
                throw new CompletionException("Accès refusé ou données introuvables pour l'élève : " + eleveId, e);
            }
        });
    }
}