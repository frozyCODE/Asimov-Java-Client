package com.asimov.client.controllers;

import com.asimov.client.services.ParentService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Contrôleur pour la fenêtre de création d'un nouveau parent.
 */
public class AddEditParentController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private Runnable onSaveCallback;

    /**
     * Définit l'action à exécuter après un enregistrement réussi.
     * @param callback La fonction de rappel (souvent un rafraîchissement de liste).
     */
    public void setOnSave(Runnable callback) {
        this.onSaveCallback = callback;
    }

    /**
     * Valide et enregistre le nouveau parent via le service asynchrone.
     */
    @FXML
    private void handleSave() {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        ParentService.creerParentAsync(nom, prenom, email, password)
            .thenRun(() -> Platform.runLater(() -> {
                if (onSaveCallback != null) onSaveCallback.run();
                closeWindow();
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> showError("Erreur : " + ex.getMessage()));
                return null;
            });
    }

    /**
     * Ferme la fenêtre sans enregistrer.
     */
    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void closeWindow() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
}
