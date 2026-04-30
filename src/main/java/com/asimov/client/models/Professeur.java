package com.asimov.client.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modèle de données représentant un Professeur dans le système Asimov.
 * <p>
 * Cette classe est conçue pour être utilisée de manière bidirectionnelle :
 * <ul>
 *   <li><b>Côté API (Jackson) :</b> Désérialisation transparente du JSON grâce aux annotations {@code @JsonProperty}.</li>
 *   <li><b>Côté UI (JavaFX) :</b> Utilisation des {@link StringProperty} et {@link IntegerProperty} pour le rafraîchissement
 *       automatique de l'interface graphique (Data-Binding dans les TableView).</li>
 * </ul>
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Professeur {

    private final IntegerProperty id = new SimpleIntegerProperty(0);
    private final StringProperty nom = new SimpleStringProperty("");
    private final StringProperty prenom = new SimpleStringProperty("");
    private final StringProperty email = new SimpleStringProperty("");

    /**
     * Mot de passe temporaire. Il n'est utilisé que lors de la phase de création
     * du compte et n'est jamais renvoyé par l'API par la suite pour des raisons de sécurité.
     */
    private final StringProperty password = new SimpleStringProperty("");

    /**
     * Constructeur par défaut.
     * Requis par Jackson pour l'instanciation lors de la désérialisation du JSON.
     */
    public Professeur() {}

    /**
     * Récupère l'identifiant technique (clé primaire) du professeur.
     * @return L'identifiant du professeur.
     */
    @JsonProperty("id")
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    /**
     * Récupère le nom de famille du professeur.
     * @return Le nom.
     */
    @JsonProperty("nom")
    public String getNom() { return nom.get(); }
    public void setNom(String nom) { this.nom.set(nom); }
    public StringProperty nomProperty() { return nom; }

    /**
     * Récupère le prénom du professeur.
     * @return Le prénom.
     */
    @JsonProperty("prenom")
    public String getPrenom() { return prenom.get(); }
    public void setPrenom(String prenom) { this.prenom.set(prenom); }
    public StringProperty prenomProperty() { return prenom; }

    /**
     * Récupère l'adresse email de contact du professeur.
     * @return L'adresse email professionnelle.
     */
    @JsonProperty("email")
    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }
    public StringProperty emailProperty() { return email; }

    /**
     * Récupère le mot de passe défini lors de la création du compte.
     * @return Le mot de passe en clair.
     */
    @JsonProperty("password")
    public String getPassword() { return password.get(); }
    public void setPassword(String password) { this.password.set(password); }
    public StringProperty passwordProperty() { return password; }

    /**
     * Représentation textuelle simplifiée du professeur.
     * Utile si l'objet est injecté directement dans une liste déroulante (ComboBox).
     * @return Une chaîne formatée de type "Nom Prénom".
     */
    @Override
    public String toString() {
        return getNom() + " " + getPrenom();
    }
}