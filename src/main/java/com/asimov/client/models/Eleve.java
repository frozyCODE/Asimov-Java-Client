package com.asimov.client.models;

import com.fasterxml.jackson.annotation.*;
import javafx.beans.property.*;

/**
 * Modèle de données représentant un Élève.
 * <p>
 * Utilise les propriétés JavaFX pour permettre le rafraîchissement automatique de l'UI
 * et les annotations Jackson pour la communication avec l'API REST.
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Eleve {

    private final IntegerProperty id = new SimpleIntegerProperty(0);
    private final StringProperty nom = new SimpleStringProperty("");
    private final StringProperty prenom = new SimpleStringProperty("");
    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty password = new SimpleStringProperty("");
    private final StringProperty identifiant_csv = new SimpleStringProperty("");

    /**
     * Constructeur par défaut requis pour la désérialisation Jackson.
     */
    public Eleve() {}

    @JsonProperty("id")
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    @JsonProperty("nom")
    public String getNom() { return nom.get(); }
    public void setNom(String nom) { this.nom.set(nom); }
    public StringProperty nomProperty() { return nom; }

    @JsonProperty("prenom")
    public String getPrenom() { return prenom.get(); }
    public void setPrenom(String prenom) { this.prenom.set(prenom); }
    public StringProperty prenomProperty() { return prenom; }

    @JsonProperty("email")
    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }
    public StringProperty emailProperty() { return email; }

    /**
     * Le mot de passe est envoyé à l'API lors de la création ou modification.
     * Il n'est généralement pas récupéré depuis l'API pour des raisons de sécurité.
     */
    @JsonProperty("password")
    public String getPassword() { return password.get(); }
    public void setPassword(String password) { this.password.set(password); }
    public StringProperty passwordProperty() { return password; }

    @JsonProperty("identifiant_csv")
    public String getIdentifiant_csv() { return identifiant_csv.get(); }
    public void setIdentifiant_csv(String identifiant_csv) { this.identifiant_csv.set(identifiant_csv); }
    public StringProperty identifiant_csvProperty() { return identifiant_csv; }
}