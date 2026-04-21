package com.asimov.client.models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Modèle représentant un Élève dans l'application client.
 * Utilise les propriétés JavaFX (SimpleIntegerProperty, SimpleStringProperty)
 * pour permettre le Data Binding direct avec l'interface graphique (TableView, Formulaires).
 * Les annotations Jackson permettent la sérialisation/désérialisation JSON avec l'API Node.js.
 * * @author TonNom
 * @version 1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.ALWAYS) // Force l'envoi des champs null au serveur Node.js
public class Eleve {

    private final SimpleIntegerProperty id = new SimpleIntegerProperty();
    private final SimpleStringProperty nom = new SimpleStringProperty();
    private final SimpleStringProperty prenom = new SimpleStringProperty();
    private final SimpleStringProperty email = new SimpleStringProperty();
    private final SimpleStringProperty referant = new SimpleStringProperty();

    // Champs nécessaires pour la création côté Node.js
    private final SimpleStringProperty password = new SimpleStringProperty();
    private final SimpleStringProperty identifiant_csv = new SimpleStringProperty();

    /**
     * Constructeur par défaut requis par Jackson pour la désérialisation JSON.
     */
    public Eleve() {}

    // --- ID ---
    @JsonProperty("id")
    public void setId(int id) { this.id.set(id); }
    public int getId() { return id.get(); }
    public SimpleIntegerProperty idProperty() { return id; }

    // --- Nom ---
    @JsonProperty("nom")
    public void setNom(String nom) { this.nom.set(nom); }
    public String getNom() { return nom.get(); }
    public SimpleStringProperty nomProperty() { return nom; }

    // --- Prénom ---
    @JsonProperty("prenom")
    public void setPrenom(String prenom) { this.prenom.set(prenom); }
    public String getPrenom() { return prenom.get(); }
    public SimpleStringProperty prenomProperty() { return prenom; }

    // --- Email ---
    @JsonProperty("email")
    public void setEmail(String email) { this.email.set(email); }
    public String getEmail() { return email.get(); }
    public SimpleStringProperty emailProperty() { return email; }

    // --- Référant ---
    @JsonProperty("referant")
    public void setReferant(String referant) { this.referant.set(referant); }
    public String getReferant() { return referant.get(); }
    public SimpleStringProperty referantProperty() { return referant; }

    // --- Mot de passe (Uniquement utilisé à la création) ---
    @JsonProperty("password")
    public void setPassword(String password) { this.password.set(password); }
    public String getPassword() { return password.get(); }
    public SimpleStringProperty passwordProperty() { return password; }

    // --- Identifiant CSV (Uniquement utilisé à la création) ---
    @JsonProperty("identifiant_csv")
    public void setIdentifiant_csv(String identifiant_csv) { this.identifiant_csv.set(identifiant_csv); }
    public String getIdentifiant_csv() { return identifiant_csv.get(); }
    public SimpleStringProperty identifiant_csvProperty() { return identifiant_csv; }
}