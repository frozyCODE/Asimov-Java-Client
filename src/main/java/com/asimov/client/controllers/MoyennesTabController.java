package com.asimov.client.controllers;

import com.asimov.client.models.Eleve;
import com.asimov.client.models.Inscription;
import com.asimov.client.models.Moyenne;
import com.asimov.client.services.EleveService;
import com.asimov.client.services.InscriptionService;
import com.asimov.client.services.MoyenneService;
import com.asimov.client.utils.UserSession;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

/**
 * Contrôleur de gestion des moyennes.
 * Gère la saisie et la validation des notes avec mise à jour réactive de l'interface.
 */
public class MoyennesTabController {

    @FXML private ComboBox<Eleve> eleveMoyenneCombo;
    @FXML private ComboBox<Inscription> inscriptionMoyenneCombo;
    @FXML private TextField newMoyenneField;
    @FXML private ComboBox<Integer> semestreAddCombo;
    @FXML private Button saisirNoteButton, validerMoyenneButton, deleteMoyenneButton;
    @FXML private TableView<Moyenne> moyennesTable;
    @FXML private TableColumn<Moyenne, Integer> semestreColumn;
    @FXML private TableColumn<Moyenne, Double> moyenneColumn;
    @FXML private TableColumn<Moyenne, String> statutColumn;

    private final ObservableList<Eleve> masterEleveData = FXCollections.observableArrayList();
    private final ObservableList<Moyenne> masterMoyenneData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        semestreColumn.setCellValueFactory(d -> d.getValue().semestreProperty().asObject());
        moyenneColumn.setCellValueFactory(d -> d.getValue().moyenne_generaleProperty().asObject());

        /**
         * Liaison dynamique pour le statut : se met à jour dès que l'objet change.
         */
        statutColumn.setCellValueFactory(d -> Bindings.createStringBinding(
                () -> d.getValue().isValidee_par_proviseur() ? "✅ Validée" : "⏳ En attente",
                d.getValue().validee_par_proviseurProperty()
        ));

        moyennesTable.setItems(masterMoyenneData);
        semestreAddCombo.setItems(FXCollections.observableArrayList(1, 2));

        eleveMoyenneCombo.setConverter(new StringConverter<Eleve>() {
            @Override public String toString(Eleve e) { return e == null ? "" : e.getPrenom() + " " + e.getNom(); }
            @Override public Eleve fromString(String s) { return null; }
        });

        setupListeners();
    }

    private void setupListeners() {
        eleveMoyenneCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            if (newSel != null) {
                inscriptionMoyenneCombo.setDisable(false);
                InscriptionService.getInscriptionsByEleveAsync(newSel.getId())
                        .thenAcceptAsync(list -> Platform.runLater(() ->
                                inscriptionMoyenneCombo.setItems(FXCollections.observableArrayList(list))
                        ));
            }
        });

        inscriptionMoyenneCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            boolean hasSelection = newSel != null;
            newMoyenneField.setDisable(!hasSelection);
            semestreAddCombo.setDisable(!hasSelection);
            saisirNoteButton.setDisable(!hasSelection);
            if (hasSelection) loadMoyennesForInscription(newSel.getId());
        });

        moyennesTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            boolean isProviseur = UserSession.getInstance().hasRole("Proviseur");
            validerMoyenneButton.setDisable(newSel == null || !isProviseur || newSel.isValidee_par_proviseur());
            deleteMoyenneButton.setDisable(newSel == null || !isProviseur);
        });
    }

    public void loadData() {
        EleveService.recupererToutPourSelectionAsync().thenAcceptAsync(list -> Platform.runLater(() -> {
            masterEleveData.setAll(list);
            eleveMoyenneCombo.setItems(masterEleveData);
        }));
    }

    private void loadMoyennesForInscription(int id) {
        MoyenneService.getMoyennesByInscriptionAsync(id).thenAcceptAsync(list ->
                Platform.runLater(() -> masterMoyenneData.setAll(list))
        );
    }

    @FXML
    private void handleAddMoyenne() {
        Inscription ins = inscriptionMoyenneCombo.getValue();
        Integer sem = semestreAddCombo.getValue();
        String val = newMoyenneField.getText();

        if (ins != null && sem != null && val != null) {
            Moyenne m = new Moyenne();
            m.setInscription_id(ins.getId());
            m.setSemestre(sem);
            m.setMoyenne_generale(Double.parseDouble(val.replace(",", ".")));

            MoyenneService.createMoyenneAsync(m).thenRunAsync(() -> Platform.runLater(() -> {
                loadMoyennesForInscription(ins.getId());
                newMoyenneField.clear();
            }));
        }
    }

    /**
     * Valide la moyenne et force la mise à jour visuelle immédiate.
     */
    @FXML
    private void handleValiderMoyenne() {
        Moyenne selection = moyennesTable.getSelectionModel().getSelectedItem();
        if (selection != null) {
            MoyenneService.validerMoyenneAsync(selection.getId())
                    .thenRunAsync(() -> Platform.runLater(() -> {
                        selection.setValidee_par_proviseur(true);
                        moyennesTable.refresh();

                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "La moyenne a été validée avec succès !");
                        alert.show();
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Erreur serveur : " + ex.getMessage()).show());
                        return null;
                    });
        }
    }

    @FXML
    private void handleDeleteMoyenne() {
        Moyenne selection = moyennesTable.getSelectionModel().getSelectedItem();
        if (selection != null) {
            MoyenneService.deleteMoyenneAsync(selection.getId())
                    .thenRunAsync(() -> Platform.runLater(() -> loadMoyennesForInscription(selection.getInscription_id())));
        }
    }
}