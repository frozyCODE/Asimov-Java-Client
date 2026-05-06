package com.asimov.client.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.beans.property.*;

/**
 * Modèle de données représentant un Parent.
 * <p>
 * Encapsule les informations d'un parent en utilisant les propriétés JavaFX
 * pour permettre un couplage fort avec les composants de l'interface
 * utilisateur.
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Parent {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nom = new SimpleStringProperty();
    private final StringProperty prenom = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty enfants = new SimpleStringProperty();

    /**
     * Constructeur par défaut requis pour la désérialisation JSON par Jackson.
     */
    public Parent() {
    }

    /**
     * @return L'identifiant unique du parent.
     */
    @JsonProperty("id")
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    /**
     * @return Le nom de famille du parent.
     */
    @JsonProperty("nom")
    public String getNom() {
        return nom.get();
    }

    public void setNom(String nom) {
        this.nom.set(nom);
    }

    public StringProperty nomProperty() {
        return nom;
    }

    /**
     * @return Le prénom du parent.
     */
    @JsonProperty("prenom")
    public String getPrenom() {
        return prenom.get();
    }

    public void setPrenom(String prenom) {
        this.prenom.set(prenom);
    }

    public StringProperty prenomProperty() {
        return prenom;
    }

    /**
     * @return L'adresse email du parent.
     */
    @JsonProperty("email")
    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public StringProperty emailProperty() {
        return email;
    }

    /**
     * @return La liste des noms des enfants associés (formatée par l'API).
     */
    @JsonProperty("enfants")
    public String getEnfants() {
        return enfants.get();
    }

    public void setEnfants(String enfants) {
        this.enfants.set(enfants);
    }

    public StringProperty enfantsProperty() {
        return enfants;
    }
}
