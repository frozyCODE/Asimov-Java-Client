package com.asimov.client.controllers;

import com.asimov.client.models.Moyenne;
import com.asimov.client.models.Option;
import com.asimov.client.services.InscriptionService;
import com.asimov.client.services.MoyenneService;
import com.asimov.client.services.OptionService;
import com.asimov.client.utils.UserSession;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.stream.Collectors;

/**
 * Contrôleur pour la vue personnelle de l'élève.
 * Affiche les informations de scolarité et les résultats de l'utilisateur connecté.
 */
public class EleveProfileTabController {

    @FXML private Label nomCompletLabel, emailLabel, classeLabel;
    @FXML private ListView<String> optionsList;
    @FXML private TableView<Moyenne> moyennesTable;
    @FXML private TableColumn<Moyenne, Integer> semestreColumn;
    @FXML private TableColumn<Moyenne, Double> moyenneColumn;
    @FXML private TableColumn<Moyenne, String> statutColumn;

    private final ObservableList<Moyenne> resultats = FXCollections.observableArrayList();

    /**
     * Initialise le tableau des résultats.
     */
    @FXML
    public void initialize() {
        semestreColumn.setCellValueFactory(d -> d.getValue().semestreProperty().asObject());
        moyenneColumn.setCellValueFactory(d -> d.getValue().moyenne_generaleProperty().asObject());

        // Affichage ultra-robuste en lecture seule
        statutColumn.setCellValueFactory(d -> {
            boolean isValide = d.getValue().isValidee_par_proviseur();
            return new javafx.beans.property.SimpleStringProperty(isValide ? "✅ Validée" : "⏳ En attente");
        });

        moyennesTable.setItems(resultats);
    }

    /**
     * Charge les données de l'élève à partir de l'ID de session et peuple l'interface.
     */
    public void loadData() {
        UserSession session = UserSession.getInstance();
        Integer eleveId = session.getEleveId();

        nomCompletLabel.setText(session.getPrenom() + " " + session.getNom());
        emailLabel.setText(session.getEmail());

        if (eleveId != null) {
            // Récupération de l'inscription et des notes
            InscriptionService.getInscriptionsByEleveAsync(eleveId).thenAcceptAsync(list -> {
                if (!list.isEmpty()) {
                    var current = list.get(list.size() - 1);
                    Platform.runLater(() -> classeLabel.setText(current.toString()));

                    MoyenneService.getMoyennesByInscriptionAsync(current.getId()).thenAcceptAsync(notes ->
                            Platform.runLater(() -> {
                                resultats.setAll(notes);
                                moyennesTable.refresh();
                            })
                    );
                } else {
                    Platform.runLater(() -> classeLabel.setText("Aucune classe assignée"));
                }
            });

            // Récupération des options
            OptionService.getOptionsByEleveAsync(eleveId).thenAcceptAsync(options -> {
                var noms = options.stream().map(Option::getNom).collect(Collectors.joining(", "));
                Platform.runLater(() -> {
                    optionsList.getItems().clear();
                    if (!noms.isEmpty()) {
                        optionsList.getItems().addAll(noms.split(", "));
                    } else {
                        optionsList.getItems().add("Aucune option choisie");
                    }
                });
            });
        } else {
            Platform.runLater(() -> {
                classeLabel.setText("Profil élève introuvable");
                optionsList.getItems().clear();
                optionsList.getItems().add("Profil élève introuvable");
            });
        }
    }
}