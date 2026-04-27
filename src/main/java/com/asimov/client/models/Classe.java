package com.asimov.client.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Modèle représentant une classe physique dans l'établissement (Compatible JavaFX).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Classe {
    private final SimpleIntegerProperty id = new SimpleIntegerProperty();
    private final SimpleStringProperty annee_scolaire = new SimpleStringProperty();
    private final SimpleIntegerProperty niveau = new SimpleIntegerProperty();
    private final SimpleStringProperty lettre = new SimpleStringProperty();

    public Classe() {}

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public SimpleIntegerProperty idProperty() { return id; }

    public String getAnnee_scolaire() { return annee_scolaire.get(); }
    public void setAnnee_scolaire(String value) { annee_scolaire.set(value); }
    public SimpleStringProperty annee_scolaireProperty() { return annee_scolaire; }

    public int getNiveau() { return niveau.get(); }
    public void setNiveau(int value) { niveau.set(value); }
    public SimpleIntegerProperty niveauProperty() { return niveau; }

    public String getLettre() { return lettre.get(); }
    public void setLettre(String value) { lettre.set(value); }
    public SimpleStringProperty lettreProperty() { return lettre; }

    /**
     * Formate l'affichage de la classe pour les listes déroulantes (ComboBox) dans l'interface utilisateur.
     * @return Une chaîne formatée, par exemple "2025-2026 - 3ème A".
     */
    @Override
    public String toString() {
        return getAnnee_scolaire() + " - " + getNiveau() + "ème " + getLettre();
    }
}