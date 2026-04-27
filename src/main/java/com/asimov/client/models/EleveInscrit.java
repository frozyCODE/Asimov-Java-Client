package com.asimov.client.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Modèle représentant un élève inscrit dans une classe spécifique.
 * Utilisé pour le trombinoscope et la gestion des effectifs.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EleveInscrit {
    private final SimpleIntegerProperty id = new SimpleIntegerProperty(); // ID de l'inscription
    private final SimpleIntegerProperty eleve_id = new SimpleIntegerProperty();
    private final SimpleStringProperty nom = new SimpleStringProperty();
    private final SimpleStringProperty prenom = new SimpleStringProperty();
    private final SimpleStringProperty email = new SimpleStringProperty();

    public EleveInscrit() {}

    public int getId() { return id.get(); }
    public void setId(int v) { id.set(v); }
    public SimpleIntegerProperty idProperty() { return id; }

    public int getEleve_id() { return eleve_id.get(); }
    public void setEleve_id(int v) { eleve_id.set(v); }
    public SimpleIntegerProperty eleve_idProperty() { return eleve_id; }

    public String getNom() { return nom.get(); }
    public void setNom(String v) { nom.set(v); }
    public SimpleStringProperty nomProperty() { return nom; }

    public String getPrenom() { return prenom.get(); }
    public void setPrenom(String v) { prenom.set(v); }
    public SimpleStringProperty prenomProperty() { return prenom; }

    public String getEmail() { return email.get(); }
    public void setEmail(String v) { email.set(v); }
    public SimpleStringProperty emailProperty() { return email; }
}