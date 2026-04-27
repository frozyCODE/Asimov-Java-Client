package com.asimov.client.controllers;

import com.asimov.client.models.Option;
import com.asimov.client.services.OptionService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

/**
 * Contrôleur dédié à la gestion du catalogue des options scolaires.
 * <p>
 * Gère l'ajout, la suppression et l'affichage des options disponibles
 * dans l'établissement via l'interface JavaFX.
 * </p>
 */
public class OptionsTabController {

    @FXML private TextField newOptionField;
    @FXML private Button deleteOptionButton;
    @FXML private TableView<Option> optionsTable;
    @FXML private TableColumn<Option, Integer> optionIdColumn;
    @FXML private TableColumn<Option, String> optionNomColumn;

    private final ObservableList<Option> masterOptionData = FXCollections.observableArrayList();

    /**
     * Initialise le contrôleur. Relie les colonnes du tableau aux propriétés du modèle
     * et écoute la sélection pour activer/désactiver le bouton de suppression.
     */
    @FXML
    public void initialize() {
        optionIdColumn.setCellValueFactory(d -> d.getValue().idProperty().asObject());
        optionNomColumn.setCellValueFactory(d -> d.getValue().nomProperty());
        optionsTable.setItems(masterOptionData);

        optionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (deleteOptionButton != null) {
                deleteOptionButton.setDisable(newSel == null);
            }
        });
    }

    /**
     * Charge la liste complète des options depuis l'API de manière asynchrone
     * et rafraîchit le tableau JavaFX.
     */
    public void loadData() {
        OptionService.getAllOptionsAsync().thenAcceptAsync(list ->
                Platform.runLater(() -> {
                    masterOptionData.clear();
                    masterOptionData.addAll(list);
                })
        ).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    /**
     * Gère le clic sur le bouton d'ajout d'une nouvelle option.
     * Envoie la requête à l'API, vide le champ texte et rafraîchit la liste.
     */
    @FXML
    private void handleAddOption() {
        String nouvelleOption = newOptionField.getText();

        // Vérification que le champ n'est pas vide ou rempli d'espaces
        if (nouvelleOption != null && !nouvelleOption.trim().isEmpty()) {
            OptionService.createOptionAsync(nouvelleOption.trim())
                    .thenRunAsync(() -> Platform.runLater(() -> {
                        newOptionField.clear();
                        loadData();
                    })).exceptionally(ex -> {
                        ex.printStackTrace();
                        return null;
                    });
        }
    }

    /**
     * Gère le clic sur le bouton de suppression d'une option.
     * Envoie la requête de suppression à l'API pour l'élément sélectionné.
     */
    @FXML
    private void handleDeleteOption() {
        Option selection = optionsTable.getSelectionModel().getSelectedItem();
        if (selection != null) {
            OptionService.deleteOptionAsync(selection.getId())
                    .thenRunAsync(() -> Platform.runLater(this::loadData))
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        return null;
                    });
        }
    }
}