package com.asimov.client.controllers;

import com.asimov.client.models.Parent;
import com.asimov.client.services.ParentService;
import com.asimov.client.utils.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Contrôleur pour l'onglet de gestion des parents (Vue Administration).
 * <p>
 * Ce module permet aux administrateurs (Proviseur, Secrétariat) de visualiser
 * la liste exhaustive des parents d'élèves enregistrés dans l'application.
 * </p>
 */
public class ParentsTabController {

    @FXML
    private TableView<Parent> parentsTable;
    @FXML
    private TableColumn<Parent, Number> colId;
    @FXML
    private TableColumn<Parent, String> colNom;
    @FXML
    private TableColumn<Parent, String> colPrenom;
    @FXML
    private TableColumn<Parent, String> colEmail;
    @FXML
    private TableColumn<Parent, String> colEnfants;
    @FXML
    private TableColumn<Parent, Void> colActions;

    /**
     * Initialise la structure du tableau JavaFX.
     * <p>
     * Relie chaque colonne du TableView aux propriétés correspondantes du modèle
     * {@link Parent}. Configure également la colonne d'actions personnalisée.
     * </p>
     */
    @FXML
    public void initialize() {
        colId.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        colNom.setCellValueFactory(cellData -> cellData.getValue().nomProperty());
        colPrenom.setCellValueFactory(cellData -> cellData.getValue().prenomProperty());
        colEmail.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        colEnfants.setCellValueFactory(cellData -> cellData.getValue().enfantsProperty());

        setupActionsColumn();
    }

    /**
     * Configure la colonne d'actions pour inclure un bouton de suppression
     * dynamique pour chaque parent affiché dans le tableau.
     */
    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Supprimer");

            {
                deleteBtn.getStyleClass().add("button-danger");
                deleteBtn.setOnAction(event -> {
                    Parent p = getTableView().getItems().get(getIndex());
                    handleDeleteParent(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });
    }

    /**
     * Charge ou rafraîchit la liste des parents depuis l'API Node.js.
     * <p>
     * Cette méthode est publique afin de permettre au contrôleur principal
     * (Dashboard) de déclencher le chargement des données uniquement lorsque
     * l'onglet est affiché.
     * </p>
     */
    public void loadParents() {
        ParentService.recupererTousLesParentsAsync().thenAccept(parents -> {
            Platform.runLater(() -> parentsTable.getItems().setAll(parents));
        }).exceptionally(ex -> {
            System.err.println("Erreur de chargement des parents : " + ex.getMessage());
            return null;
        });
    }

    /**
     * Gère la suppression d'un parent via un appel asynchrone au service dédié.
     * <p>
     * Après une suppression réussie sur le backend, la liste locale est
     * rafraîchie pour refléter les changements.
     * </p>
     * 
     * @param p Le parent sélectionné pour la suppression.
     */
    private void handleDeleteParent(Parent p) {
        ParentService.supprimerParentAsync(p.getId())
                .thenRun(() -> Platform.runLater(this::loadParents))
                .exceptionally(ex -> {
                    System.err.println("Erreur lors de la suppression : " + ex.getMessage());
                    return null;
                });
    }

    /**
     * Gère l'événement de création d'un nouveau parent.
     * <p>
     * Ouvre une boîte de dialogue de saisie et rafraîchit la liste en cas de
     * succès.
     * </p>
     */
    @FXML
    private void handleNewParent() {
        SceneManager.showParentAddDialog(this::loadParents);
    }
}
