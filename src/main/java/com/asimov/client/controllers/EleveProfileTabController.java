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
 * Contrôleur exclusif à l'espace personnel de l'élève.
 * Ne charge que les données strictement liées à l'utilisateur connecté.
 */
public class EleveProfileTabController {

    @FXML private Label nomCompletLabel;
    @FXML private Label emailLabel;
    @FXML private Label classeLabel;
    @FXML private ListView<String> optionsList;
    @FXML private TableView<Moyenne> moyennesTable;
    @FXML private TableColumn<Moyenne, Integer> semestreColumn;
    @FXML private TableColumn<Moyenne, Double> moyenneColumn;
    @FXML private TableColumn<Moyenne, String> statutColumn;

    private final ObservableList<Moyenne> resultats = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Paramétrage des colonnes du tableau
        semestreColumn.setCellValueFactory(d -> d.getValue().semestreProperty().asObject());
        moyenneColumn.setCellValueFactory(d -> d.getValue().moyenne_generaleProperty().asObject());

        statutColumn.setCellValueFactory(d -> {
            boolean isValide = d.getValue().isValidee_par_proviseur();
            return new javafx.beans.property.SimpleStringProperty(isValide ? "✅ Validée" : "⏳ En attente");
        });

        moyennesTable.setItems(resultats);

        // On ne charge les données que si le rôle est bien "Eleve"
        if ("Eleve".equalsIgnoreCase(UserSession.getInstance().getRole())) {
            loadData();
        }
    }

    public void loadData() {
        UserSession session = UserSession.getInstance();
        Integer eleveId = session.getEleveId();

        // 1. Affichage des infos de base
        nomCompletLabel.setText(session.getPrenom() + " " + session.getNom());
        emailLabel.setText(session.getEmail());

        if (eleveId != null) {
            // 2. Récupération de la classe et des moyennes
            InscriptionService.getInscriptionsByEleveAsync(eleveId).thenAcceptAsync(list -> {
                if (!list.isEmpty()) {
                    var current = list.get(0); // Prend l'inscription la plus récente
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

            // 3. Récupération des options
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
        }
    }
}