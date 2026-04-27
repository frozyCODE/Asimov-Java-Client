package com.asimov.client.controllers;

import com.asimov.client.models.Classe;
import com.asimov.client.models.Eleve;
import com.asimov.client.models.EleveInscrit;
import com.asimov.client.services.ClasseService;
import com.asimov.client.services.EleveService;
import com.asimov.client.services.InscriptionService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

/**
 * Contrôleur de l'onglet des classes et du trombinoscope.
 * Gère le référentiel des classes et l'inscription des élèves.
 */
public class ClassesTabController {

    @FXML private TableView<Classe> classesTable;
    @FXML private TableColumn<Classe, Integer> classeIdColumn, classeNiveauColumn;
    @FXML private TableColumn<Classe, String> classeAnneeColumn, classeLettreColumn;

    @FXML private ComboBox<String> newClasseAnneeCombo;
    @FXML private ComboBox<Integer> newClasseNiveauCombo;
    @FXML private TextField newClasseLettreField;
    @FXML private Button deleteClasseButton, inscrireEleveButton, retirerEleveButton;

    @FXML private Label titreTrombinoscopeLabel;
    @FXML private ComboBox<Eleve> eleveToEnrollCombo;
    @FXML private TableView<EleveInscrit> elevesInscritsTable;
    @FXML private TableColumn<EleveInscrit, String> inscNomColumn, inscPrenomColumn, inscEmailColumn;

    private final ObservableList<Classe> masterClasseData = FXCollections.observableArrayList();
    private final ObservableList<EleveInscrit> masterElevesInscritsData = FXCollections.observableArrayList();
    private final ObservableList<Eleve> masterEleveData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupClassesTable();
        setupElevesInscritsTable();

        newClasseAnneeCombo.setItems(FXCollections.observableArrayList("2024-2025", "2025-2026", "2026-2027"));
        newClasseNiveauCombo.setItems(FXCollections.observableArrayList(6, 5, 4, 3));

        eleveToEnrollCombo.setConverter(new StringConverter<Eleve>() {
            @Override public String toString(Eleve e) { return e == null ? "" : e.getPrenom() + " " + e.getNom(); }
            @Override public Eleve fromString(String s) { return null; }
        });
    }

    private void setupClassesTable() {
        classeIdColumn.setCellValueFactory(d -> d.getValue().idProperty().asObject());
        classeAnneeColumn.setCellValueFactory(d -> d.getValue().annee_scolaireProperty());
        classeNiveauColumn.setCellValueFactory(d -> d.getValue().niveauProperty().asObject());
        classeLettreColumn.setCellValueFactory(d -> d.getValue().lettreProperty());
        classesTable.setItems(masterClasseData);

        classesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean isSelected = (newSel != null);
            deleteClasseButton.setDisable(!isSelected);
            elevesInscritsTable.setDisable(!isSelected);
            inscrireEleveButton.setDisable(!isSelected);
            eleveToEnrollCombo.setDisable(!isSelected);

            if (isSelected) {
                titreTrombinoscopeLabel.setText("Effectif : " + newSel.getNiveau() + "ème " + newSel.getLettre());
                loadElevesInscritsData(newSel.getId());
            } else {
                titreTrombinoscopeLabel.setText("Élèves inscrits");
                masterElevesInscritsData.clear();
            }
        });
    }

    private void setupElevesInscritsTable() {
        inscNomColumn.setCellValueFactory(d -> d.getValue().nomProperty());
        inscPrenomColumn.setCellValueFactory(d -> d.getValue().prenomProperty());
        inscEmailColumn.setCellValueFactory(d -> d.getValue().emailProperty());
        elevesInscritsTable.setItems(masterElevesInscritsData);

        elevesInscritsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) ->
                retirerEleveButton.setDisable(newSel == null)
        );
    }

    /**
     * Charge les données initiales : le catalogue des classes et la liste des élèves disponibles.
     */
    public void loadData() {
        masterClasseData.clear();
        ClasseService.getAllClassesAsync().thenAcceptAsync(list -> Platform.runLater(() -> masterClasseData.addAll(list)));

        // Correction : Utilisation de la méthode dédiée à la sélection
        EleveService.recupererToutPourSelectionAsync().thenAcceptAsync(list -> Platform.runLater(() -> {
            masterEleveData.setAll(list);
            eleveToEnrollCombo.setItems(masterEleveData);
        }));
    }

    private void loadElevesInscritsData(int classeId) {
        masterElevesInscritsData.clear();
        InscriptionService.getElevesByClasseAsync(classeId).thenAcceptAsync(list ->
                Platform.runLater(() -> masterElevesInscritsData.addAll(list))
        );
    }

    @FXML private void handleAddClasse() {
        Classe c = new Classe();
        c.setAnnee_scolaire(newClasseAnneeCombo.getValue());
        if(newClasseNiveauCombo.getValue() != null) c.setNiveau(newClasseNiveauCombo.getValue());
        c.setLettre(newClasseLettreField.getText().toUpperCase());

        if(c.getAnnee_scolaire() != null && !c.getLettre().isEmpty()) {
            ClasseService.createClasseAsync(c).thenRunAsync(() -> Platform.runLater(this::loadData));
        }
    }

    @FXML private void handleDeleteClasse() {
        Classe s = classesTable.getSelectionModel().getSelectedItem();
        if (s != null) ClasseService.deleteClasseAsync(s.getId()).thenRunAsync(() -> Platform.runLater(this::loadData));
    }

    @FXML private void handleInscrireEleve() {
        Classe c = classesTable.getSelectionModel().getSelectedItem();
        Eleve e = eleveToEnrollCombo.getValue();
        if(c != null && e != null) {
            InscriptionService.inscrireEleveAsync(e.getId(), c.getId())
                    .thenRunAsync(() -> Platform.runLater(() -> {
                        loadElevesInscritsData(c.getId());
                        eleveToEnrollCombo.getSelectionModel().clearSelection();
                    }));
        }
    }

    @FXML private void handleRetirerEleve() {
        EleveInscrit e = elevesInscritsTable.getSelectionModel().getSelectedItem();
        if(e != null) {
            InscriptionService.desinscrireEleveAsync(e.getId())
                    .thenRunAsync(() -> Platform.runLater(() ->
                            loadElevesInscritsData(classesTable.getSelectionModel().getSelectedItem().getId())
                    ));
        }
    }
}