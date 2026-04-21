package com.asimov.client.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Modèle représentant une option d'enseignement dans le système.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Option {
    private final SimpleIntegerProperty id = new SimpleIntegerProperty();
    private final SimpleStringProperty nom = new SimpleStringProperty();

    /**
     * Constructeur par défaut nécessaire pour la désérialisation Jackson.
     */
    public Option() {}

    /**
     * Constructeur avec paramètres.
     *
     * @param id  L'identifiant unique de l'option.
     * @param nom Le nom de l'option.
     */
    public Option(int id, String nom) {
        setId(id);
        setNom(nom);
    }

    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public SimpleIntegerProperty idProperty() { return id; }

    public String getNom() { return nom.get(); }
    public void setNom(String nom) { this.nom.set(nom); }
    public SimpleStringProperty nomProperty() { return nom; }

    /**
     * Retourne le nom de l'option pour un affichage correct dans les listes déroulantes JavaFX.
     *
     * @return Le nom de l'option.
     */
    @Override
    public String toString() {
        return getNom();
    }
}