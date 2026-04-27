package com.asimov.client.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.beans.property.*;

/**
 * Représente la moyenne semestrielle d'un élève.
 * Ce modèle intègre des propriétés JavaFX pour une liaison bidirectionnelle avec l'interface
 * et des annotations Jackson pour la désérialisation depuis l'API Node.js.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Moyenne {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty inscription_id = new SimpleIntegerProperty();
    private final IntegerProperty semestre = new SimpleIntegerProperty();
    private final DoubleProperty moyenne_generale = new SimpleDoubleProperty();
    private final BooleanProperty validee_par_proviseur = new SimpleBooleanProperty();

    public Moyenne() {}

    /**
     * @return L'identifiant unique de la moyenne.
     */
    @JsonProperty("id")
    public int getId() { return id.get(); }

    @JsonProperty("id")
    public void setId(int value) { this.id.set(value); }

    public IntegerProperty idProperty() { return id; }

    /**
     * @return L'identifiant de l'inscription associée.
     */
    @JsonProperty("inscription_id")
    public int getInscription_id() { return inscription_id.get(); }

    @JsonProperty("inscription_id")
    public void setInscription_id(int value) { this.inscription_id.set(value); }

    public IntegerProperty inscription_idProperty() { return inscription_id; }

    /**
     * @return Le numéro du semestre (1 ou 2).
     */
    @JsonProperty("semestre")
    public int getSemestre() { return semestre.get(); }

    @JsonProperty("semestre")
    public void setSemestre(int value) { this.semestre.set(value); }

    public IntegerProperty semestreProperty() { return semestre; }

    /**
     * @return La valeur numérique de la moyenne.
     */
    @JsonProperty("moyenne_generale")
    public double getMoyenne_generale() { return moyenne_generale.get(); }

    @JsonProperty("moyenne_generale")
    public void setMoyenne_generale(double value) { this.moyenne_generale.set(value); }

    public DoubleProperty moyenne_generaleProperty() { return moyenne_generale; }

    /**
     * @return true si la moyenne a été validée par le proviseur.
     */
    @JsonProperty("validee_par_proviseur")
    public boolean isValidee_par_proviseur() { return validee_par_proviseur.get(); }

    /**
     * Définit l'état de validation.
     * Gère dynamiquement les types Boolean (JSON) et Number (MySQL/TINYINT).
     *
     * @param value La valeur de validation reçue de l'API.
     */
    @JsonProperty("validee_par_proviseur")
    public void setValidee_par_proviseur(Object value) {
        if (value instanceof Boolean) {
            this.validee_par_proviseur.set((Boolean) value);
        } else if (value instanceof Number) {
            this.validee_par_proviseur.set(((Number) value).intValue() == 1);
        }
    }

    public BooleanProperty validee_par_proviseurProperty() { return validee_par_proviseur; }
}