package com.asimov.client.controllers;

import com.asimov.client.models.Eleve;
import com.asimov.client.services.EleveService;
import com.asimov.client.utils.SceneManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

/**
 * Contrôleur de l'onglet gérant l'affichage, la pagination et les actions CRUD des élèves.
 * <p>
 * Ce contrôleur est lié à la vue FXML contenant le TableView principal des élèves
 * dans l'interface de gestion administrative.
 * </p>
 */
public class ElevesTabController {

    @FXML private TableView<Eleve> elevesTable;
    @FXML private TableColumn<Eleve, Integer> idColumn;
    @FXML private TableColumn<Eleve, String> nomColumn;
    @FXML private TableColumn<Eleve, String> prenomColumn;
    @FXML private TableColumn<Eleve, String> emailColumn;

    @FXML private Button editEleveButton;
    @FXML private Button deleteEleveButton;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label paginationLabel;

    private final ObservableList<Eleve> data = FXCollections.observableArrayList();
    private int pageCourante = 1;
    private int totalPages = 1;
    private static final int TAILLE_PAGE = 15;

    /**
     * Initialise le contrôleur. Cette méthode est appelée automatiquement
     * après le chargement du fichier FXML.
     */
    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));
        nomColumn.setCellValueFactory(c -> c.getValue().nomProperty());
        prenomColumn.setCellValueFactory(c -> c.getValue().prenomProperty());
        emailColumn.setCellValueFactory(c -> c.getValue().emailProperty());

        elevesTable.setItems(data);

        elevesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isSelected = newSelection != null;
            editEleveButton.setDisable(!isSelected);
            deleteEleveButton.setDisable(!isSelected);
        });
    }

    /**
     * Demande le chargement des données paginées depuis l'API via le service.
     * Met à jour le tableau et l'état des boutons de pagination.
     */
    @SuppressWarnings("unchecked")
    public void loadData() {
        EleveService.recupererPaginesAsync(pageCourante, TAILLE_PAGE).thenAcceptAsync(res -> {
            Platform.runLater(() -> {
                if (res != null && res.containsKey("liste")) {
                    data.setAll((List<Eleve>) res.get("liste"));
                    totalPages = (int) res.get("totalPages");

                    if (paginationLabel != null) {
                        paginationLabel.setText(String.format("Page %d / %d", pageCourante, totalPages));
                    }
                    if (prevPageButton != null) prevPageButton.setDisable(pageCourante <= 1);
                    if (nextPageButton != null) nextPageButton.setDisable(pageCourante >= totalPages);
                }
            });
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    /**
     * Charge la page précédente si elle existe.
     */
    @FXML
    private void handlePrevPage() {
        if (pageCourante > 1) {
            pageCourante--;
            loadData();
        }
    }

    /**
     * Charge la page suivante si elle existe.
     */
    @FXML
    private void handleNextPage() {
        if (pageCourante < totalPages) {
            pageCourante++;
            loadData();
        }
    }

    /**
     * Ouvre la fenêtre de dialogue pour la création d'un nouvel élève.
     * Rafraîchit le tableau en cas de succès.
     */
    @FXML
    private void handleAddEleve() {
        Eleve nouvelEleve = new Eleve();
        if (SceneManager.showEleveEditDialog(nouvelEleve) != null) {
            loadData();
        }
    }

    /**
     * Ouvre la fenêtre de dialogue pour modifier l'élève actuellement sélectionné.
     * Rafraîchit le tableau en cas de succès.
     */
    @FXML
    private void handleEditEleve() {
        Eleve selection = elevesTable.getSelectionModel().getSelectedItem();
        if (selection != null && SceneManager.showEleveEditDialog(selection) != null) {
            loadData();
        }
    }

    /**
     * Supprime l'élève actuellement sélectionné après appel au service.
     * Rafraîchit la liste des données immédiatement après le succès de l'opération.
     */
    @FXML
    private void handleDeleteEleve() {
        Eleve selection = elevesTable.getSelectionModel().getSelectedItem();
        if (selection != null) {
            EleveService.deleteEleveAsync(selection.getId())
                    .thenRunAsync(() -> Platform.runLater(this::loadData))
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        return null;
                    });
        }
    }
}