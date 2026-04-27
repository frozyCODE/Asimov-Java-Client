package com.asimov.client.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Modèle représentant l'inscription d'un élève dans une classe pour une année scolaire donnée.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Inscription {
    private int id;
    private int eleve_id;
    private String annee_scolaire;
    private int niveau;
    private String lettre_classe;

    public Inscription() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEleve_id() { return eleve_id; }
    public void setEleve_id(int eleve_id) { this.eleve_id = eleve_id; }

    public String getAnnee_scolaire() { return annee_scolaire; }
    public void setAnnee_scolaire(String annee_scolaire) { this.annee_scolaire = annee_scolaire; }

    public int getNiveau() { return niveau; }
    public void setNiveau(int niveau) { this.niveau = niveau; }

    public String getLettre_classe() { return lettre_classe; }
    public void setLettre_classe(String lettre_classe) { this.lettre_classe = lettre_classe; }

    /**
     * Retourne un affichage propre pour les listes déroulantes JavaFX.
     * Exemple : "2025-2026 - 3ème A"
     */
    @Override
    public String toString() {
        return annee_scolaire + " - " + niveau + "ème " + lettre_classe;
    }
}