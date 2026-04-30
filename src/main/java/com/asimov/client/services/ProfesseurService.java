package com.asimov.client.services;

import com.asimov.client.api.ApiClient;
import com.asimov.client.models.Professeur;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Service gérant la logique métier et la communication réseau concernant les Professeurs.
 * <p>
 * Il sert d'intermédiaire entre l'interface utilisateur JavaFX et le backend Node.js.
 * L'intégralité des méthodes s'exécute de manière asynchrone pour préserver la fluidité de l'UI.
 * </p>
 */
public class ProfesseurService {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Interroge l'API pour récupérer la liste complète des professeurs actifs.
     * Route associée : GET /api/professeurs
     *
     * @return Un CompletableFuture contenant une liste de {@link Professeur}.
     * @throws CompletionException En cas de refus d'accès (ex: compte Eleve) ou d'erreur réseau.
     */
    public static CompletableFuture<List<Professeur>> getAllProfesseursAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = ApiClient.getInstance().get("/professeurs");
                // Désérialisation du tableau JSON vers une List Java
                return mapper.readValue(json, new TypeReference<List<Professeur>>() {});
            } catch (Exception e) {
                throw new CompletionException("Erreur lors de la récupération de la liste de l'équipe pédagogique", e);
            }
        });
    }

    /**
     * Crée un nouveau compte pour un professeur dans la base de données.
     * Route associée : POST /api/professeurs
     *
     * @param professeur L'objet {@link Professeur} contenant les informations saisies par l'administration.
     * @return Un CompletableFuture contenant l'objet Professeur mis à jour avec son nouvel identifiant généré.
     * @throws CompletionException Si la validation échoue (ex: champs manquants) ou si l'email existe déjà.
     */
    public static CompletableFuture<Professeur> createProfesseurAsync(Professeur professeur) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Transformation de l'objet en chaîne JSON
                String body = mapper.writeValueAsString(professeur);
                String response = ApiClient.getInstance().post("/professeurs", body);

                // Analyse de la réponse pour extraire l'ID nouvellement créé
                JsonNode root = mapper.readTree(response);
                if (root.has("id")) {
                    professeur.setId(root.get("id").asInt());
                }
                return professeur;
            } catch (Exception e) {
                throw new CompletionException("Impossible de procéder au recrutement (création du compte)", e);
            }
        });
    }

    /**
     * Supprime définitivement le profil d'un professeur et son compte de connexion.
     * Route associée : DELETE /api/professeurs/{id}
     *
     * @param id L'identifiant technique du professeur à révoquer.
     * @return Un CompletableFuture vide confirmant la réussite de l'opération.
     * @throws CompletionException Si la ressource est introuvable ou si le serveur rencontre un problème.
     */
    public static CompletableFuture<Void> deleteProfesseurAsync(int id) {
        return CompletableFuture.runAsync(() -> {
            try {
                ApiClient.getInstance().delete("/professeurs/" + id);
            } catch (Exception e) {
                throw new CompletionException("Échec de la révocation du compte professeur", e);
            }
        });
    }
}