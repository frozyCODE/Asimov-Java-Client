package com.asimov.client.controllers;

import com.asimov.client.models.Professeur;
import com.asimov.client.services.ProfesseurService;
import com.asimov.client.utils.UserSession;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Contrôleur lié à la vue {@code ProfesseursTab.fxml}.
 * <p>
 * Ce contrôleur gère les interactions de l'utilisateur avec l'annuaire des professeurs.
 * Il assure l'affichage dynamique des données dans une {@link TableView} et gère la soumission
 * du formulaire de création d'un nouveau membre de l'équipe éducative.
 * </p>
 */
public class ProfesseursTabController {

    // --- Composants UI de la TableView ---
    @FXML private TableView<Professeur> tableProfesseurs;
    @FXML private TableColumn<Professeur, Integer> colId;
    @FXML private TableColumn<Professeur, String> colNom;
    @FXML private TableColumn<Professeur, String> colPrenom;
    @FXML private TableColumn<Professeur, String> colEmail;

    // --- Composants UI du Formulaire de création ---
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnAjouter;
    @FXML private Button btnSupprimer;

    private final ObservableList<Professeur> professeursList = FXCollections.observableArrayList();

    /**
     * Méthode de cycle de vie JavaFX.
     * Configure le data-binding des colonnes et déclenche le premier chargement des données
     * UNIQUEMENT si l'utilisateur a les droits suffisants.
     */
    @FXML
    public void initialize() {
        // Configuration des colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        tableProfesseurs.setItems(professeursList);

        tableProfesseurs.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (btnSupprimer != null) {
                btnSupprimer.setDisable(newSel == null);
            }
        });
        // ==========================================================
        // SÉCURITÉ : Ne charger l'annuaire que si autorisé !
        // ==========================================================
        String role = UserSession.getInstance().getRole();
        if ("Proviseur".equalsIgnoreCase(role) || "Secretariat".equalsIgnoreCase(role)) {
            chargerProfesseurs();
        }
    }


    @FXML
    void handleActualiser(ActionEvent event) {
        chargerProfesseurs();
    }

    private void chargerProfesseurs() {
        ProfesseurService.getAllProfesseursAsync()
                .thenAccept(profs -> {
                    Platform.runLater(() -> {
                        professeursList.clear();
                        professeursList.addAll(profs);
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Erreur de chargement",
                                "La synchronisation de l'annuaire a échoué.\nDétail technique : " + ex.getMessage());
                    });
                    return null;
                });
    }

    @FXML
    void handleAjouterProfesseur(ActionEvent event) {
        String nom = txtNom.getText().trim();
        String prenom = txtPrenom.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Formulaire incomplet", "Veuillez renseigner tous les champs obligatoires avant validation.");
            return;
        }

        btnAjouter.setDisable(true);

        Professeur nouveauProf = new Professeur();
        nouveauProf.setNom(nom);
        nouveauProf.setPrenom(prenom);
        nouveauProf.setEmail(email);
        nouveauProf.setPassword(password);

        ProfesseurService.createProfesseurAsync(nouveauProf)
                .thenAccept(profCree -> {
                    Platform.runLater(() -> {
                        txtNom.clear();
                        txtPrenom.clear();
                        txtEmail.clear();
                        txtPassword.clear();

                        chargerProfesseurs();

                        btnAjouter.setDisable(false);
                        showAlert(Alert.AlertType.INFORMATION, "Opération réussie", "Le compte professeur a bien été créé dans la base.");
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        btnAjouter.setDisable(false);
                        showAlert(Alert.AlertType.ERROR, "Erreur lors de la création",
                                "Le serveur a refusé l'enregistrement.\nMotif : " + ex.getMessage());
                    });
                    return null;
                });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Supprime le professeur sélectionné dans le tableau.
     * Demande d'abord une confirmation à l'utilisateur, puis interroge l'API.
     *
     * @param actionEvent L'événement du clic sur le bouton.
     */
    @FXML
    public void handleDeleteProfesseur(ActionEvent actionEvent) {
        // 1. Récupérer le professeur sélectionné dans le tableau
        Professeur selection = tableProfesseurs.getSelectionModel().getSelectedItem();

        if (selection != null) {
            // 2. Créer une boîte de dialogue de confirmation native JavaFX
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation de suppression");
            confirmation.setHeaderText("Révoquer le professeur : " + selection.getPrenom() + " " + selection.getNom());
            confirmation.setContentText("Cette action supprimera définitivement son compte et ses accès. Voulez-vous continuer ?");

            // 3. Attendre la réponse de l'utilisateur
            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {

                    // 4. Si c'est OK, on lance l'appel asynchrone à l'API
                    ProfesseurService.deleteProfesseurAsync(selection.getId())
                            .thenRunAsync(() -> {
                                // 5. En cas de succès, on rafraîchit le tableau (sur le Thread UI)
                                Platform.runLater(() -> {
                                    chargerProfesseurs();
                                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Le compte professeur a été supprimé.");
                                });
                            })
                            .exceptionally(ex -> {
                                // 6. En cas d'erreur de l'API (ex: 403 ou serveur down)
                                Platform.runLater(() -> {
                                    showAlert(Alert.AlertType.ERROR, "Erreur de suppression",
                                            "Impossible de supprimer ce professeur.\nMotif : " + ex.getMessage());
                                });
                                return null;
                            });
                }
            });
        }
    }
}