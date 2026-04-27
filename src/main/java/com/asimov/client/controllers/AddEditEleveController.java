package com.asimov.client.controllers;

import com.asimov.client.models.Classe;
import com.asimov.client.models.Eleve;
import com.asimov.client.models.Option;
import com.asimov.client.services.ClasseService;
import com.asimov.client.services.OptionService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la vue de création et d'édition d'un élève.
 * <p>
 * Gère la validation des entrées, la sélection des classes et des options,
 * ainsi que la gestion sécurisée du mot de passe utilisateur.
 * </p>
 */
public class AddEditEleveController {

    @FXML private Label titleLabel;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField passwordField;

    @FXML private ComboBox<Classe> classeCombo;
    @FXML private ComboBox<Option> option1Combo;
    @FXML private ComboBox<Option> option2Combo;
    @FXML private Label errorLabel;

    private Stage dialogStage;
    private Eleve eleve;
    private boolean okClicked = false;
    private List<Option> optionsInitiales = new ArrayList<>();
    private final Option AUCUNE_OPTION = new Option(0, "--- Aucune ---");

    /**
     * Initialise le contrôleur. Cette méthode est appelée automatiquement par le FXMLLoader.
     */
    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
    }

    /**
     * Définit le stage (fenêtre) de cette boîte de dialogue.
     * @param dialogStage Le stage de la fenêtre.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Configure l'élève à modifier ou à créer.
     * @param eleve L'instance de l'élève.
     */
    public void setEleve(Eleve eleve) {
        this.eleve = eleve;

        if (eleve.getId() != 0) {
            titleLabel.setText("Modifier un Élève");
            nomField.setText(eleve.getNom());
            prenomField.setText(eleve.getPrenom());
            emailField.setText(eleve.getEmail());

            if (passwordField != null) {
                passwordField.setPromptText("Laisser vide pour conserver l'actuel");
            }
            if (classeCombo != null) {
                classeCombo.setDisable(true);
            }
        } else {
            titleLabel.setText("Nouvel Élève");
        }
        chargerReferentiels();
    }

    /**
     * @return true si l'utilisateur a cliqué sur Enregistrer, false sinon.
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Valide la saisie et ferme la fenêtre en cas de succès.
     */
    @FXML
    private void handleSave() {
        if (isInputValid()) {
            // Nettoyage rigoureux des données (Trim)
            eleve.setNom(nomField.getText().trim());
            eleve.setPrenom(prenomField.getText().trim());
            eleve.setEmail(emailField.getText().trim());

            String pass = passwordField.getText();
            if (pass != null && !pass.trim().isEmpty()) {
                eleve.setPassword(pass.trim());
            }

            if (eleve.getId() == 0) {
                eleve.setIdentifiant_csv("CSV-" + System.currentTimeMillis());
            }

            okClicked = true;
            dialogStage.close();
        }
    }

    /**
     * Ferme la fenêtre sans enregistrer les modifications.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Valide les champs du formulaire.
     * @return true si les données sont valides.
     */
    private boolean isInputValid() {
        StringBuilder sb = new StringBuilder();

        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            sb.append("- Nom manquant\n");
        }
        if (prenomField.getText() == null || prenomField.getText().trim().isEmpty()) {
            sb.append("- Prénom manquant\n");
        }
        if (emailField.getText() == null || !emailField.getText().contains("@")) {
            sb.append("- Email invalide\n");
        }
        if (eleve.getId() == 0 && (passwordField.getText() == null || passwordField.getText().trim().length() < 6)) {
            sb.append("- Mot de passe requis (min. 6 car.)\n");
        }
        if (eleve.getId() == 0 && (classeCombo == null || classeCombo.getValue() == null)) {
            sb.append("- Sélection de classe requise\n");
        }

        if (sb.length() == 0) {
            return true;
        } else {
            errorLabel.setText(sb.toString());
            errorLabel.setVisible(true);
            return false;
        }
    }

    /**
     * Charge les listes de classes et d'options depuis les services.
     */
    private void chargerReferentiels() {
        ClasseService.getAllClassesAsync().thenAcceptAsync(classes -> {
            Platform.runLater(() -> {
                if (classeCombo != null) {
                    classeCombo.setItems(FXCollections.observableArrayList(classes));
                    if (!classes.isEmpty() && eleve.getId() == 0) classeCombo.getSelectionModel().selectFirst();
                }
            });
        });

        OptionService.getAllOptionsAsync().thenAcceptAsync(catalogue -> {
            ObservableList<Option> options = FXCollections.observableArrayList(AUCUNE_OPTION);
            options.addAll(catalogue);
            Platform.runLater(() -> {
                option1Combo.setItems(options);
                option2Combo.setItems(options);
                option1Combo.setValue(AUCUNE_OPTION);
                option2Combo.setValue(AUCUNE_OPTION);

                if (eleve.getId() != 0) {
                    OptionService.getOptionsByEleveAsync(eleve.getId()).thenAcceptAsync(choix -> {
                        optionsInitiales = choix;
                        Platform.runLater(() -> {
                            if (choix.size() >= 1) selectionnerOption(option1Combo, choix.get(0).getId());
                            if (choix.size() >= 2) selectionnerOption(option2Combo, choix.get(1).getId());
                        });
                    });
                }
            });
        });
    }

    private void selectionnerOption(ComboBox<Option> combo, int id) {
        combo.getItems().stream().filter(o -> o.getId() == id).findFirst().ifPresent(combo::setValue);
    }

    public List<Integer> getSelectedOptionIds() {
        List<Integer> ids = new ArrayList<>();
        if (option1Combo.getValue() != null && option1Combo.getValue().getId() != 0) ids.add(option1Combo.getValue().getId());
        if (option2Combo.getValue() != null && option2Combo.getValue().getId() != 0) ids.add(option2Combo.getValue().getId());
        return ids.stream().distinct().collect(Collectors.toList());
    }

    public Classe getSelectedClasse() { return (classeCombo != null) ? classeCombo.getValue() : null; }
    public List<Option> getOptionsInitiales() { return optionsInitiales; }
}