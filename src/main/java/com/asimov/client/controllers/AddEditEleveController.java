package com.asimov.client.controllers;

import com.asimov.client.models.Eleve;
import com.asimov.client.models.Option;
import com.asimov.client.services.OptionService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Contrôleur de la fenêtre modale permettant d'ajouter ou de modifier un élève et ses options.
 */
public class AddEditEleveController {

    @FXML private Label titleLabel;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private ComboBox<Option> option1Combo;
    @FXML private ComboBox<Option> option2Combo;
    @FXML private Label errorLabel;

    private Stage dialogStage;
    private Eleve eleve;
    private boolean okClicked = false;
    private List<Option> optionsInitiales = new ArrayList<>();
    private final Option AUCUNE_OPTION = new Option(0, "--- Aucune ---");

    /**
     * Définit la fenêtre parente de cette boîte de dialogue.
     *
     * @param dialogStage La fenêtre courante.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Initialise les champs du formulaire avec les données de l'élève fourni.
     *
     * @param eleve L'objet Eleve à afficher dans le formulaire.
     */
    public void setEleve(Eleve eleve) {
        this.eleve = eleve;
        if (eleve.getId() != 0) {
            titleLabel.setText("Modifier un Élève");
            nomField.setText(eleve.getNom());
            prenomField.setText(eleve.getPrenom());
            emailField.setText(eleve.getEmail());
        }
        chargerCatalogueEtOptions();
    }

    private void chargerCatalogueEtOptions() {
        OptionService.getAllOptionsAsync().thenAcceptAsync(catalogue -> {
            ObservableList<Option> obsList = FXCollections.observableArrayList(AUCUNE_OPTION);
            obsList.addAll(catalogue);

            Platform.runLater(() -> {
                option1Combo.setItems(obsList);
                option2Combo.setItems(obsList);
                option1Combo.setValue(AUCUNE_OPTION);
                option2Combo.setValue(AUCUNE_OPTION);

                if (eleve.getId() != 0) {
                    OptionService.getOptionsByEleveAsync(eleve.getId()).thenAcceptAsync(choix -> {
                        optionsInitiales = choix;
                        Platform.runLater(() -> {
                            if (choix.size() >= 1) selectionnerDansCombo(option1Combo, choix.get(0).getId());
                            if (choix.size() >= 2) selectionnerDansCombo(option2Combo, choix.get(1).getId());
                        });
                    });
                }
            });
        });
    }

    private void selectionnerDansCombo(ComboBox<Option> combo, int id) {
        combo.getItems().stream()
                .filter(o -> o.getId() == id)
                .findFirst()
                .ifPresent(combo::setValue);
    }

    @FXML
    private void handleSave() {
        if (isInputValid()) {
            eleve.setNom(nomField.getText());
            eleve.setPrenom(prenomField.getText());
            eleve.setEmail(emailField.getText());

            if (eleve.getId() == 0) {
                eleve.setPassword("P@ssword123");
                eleve.setIdentifiant_csv("CSV-" + System.currentTimeMillis());
            }

            okClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            errorLabel.setText("Le nom est obligatoire.");
            errorLabel.setVisible(true);
            return false;
        }
        if (prenomField.getText() == null || prenomField.getText().trim().isEmpty()) {
            errorLabel.setText("Le prénom est obligatoire.");
            errorLabel.setVisible(true);
            return false;
        }
        if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
            errorLabel.setText("L'email est obligatoire.");
            errorLabel.setVisible(true);
            return false;
        }
        errorLabel.setVisible(false);
        return true;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    public List<Integer> getSelectedOptionIds() {
        List<Integer> ids = new ArrayList<>();
        Option opt1 = option1Combo.getValue();
        Option opt2 = option2Combo.getValue();

        if (opt1 != null && opt1.getId() != 0) ids.add(opt1.getId());
        if (opt2 != null && opt2.getId() != 0 && opt2.getId() != opt1.getId()) ids.add(opt2.getId());

        return ids;
    }

    public List<Option> getOptionsInitiales() {
        return optionsInitiales;
    }
}