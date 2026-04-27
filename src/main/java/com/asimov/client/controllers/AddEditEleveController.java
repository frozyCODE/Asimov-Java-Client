package com.asimov.client.controllers;

import com.asimov.client.models.Classe;
import com.asimov.client.models.Eleve;
import com.asimov.client.models.Option;
import com.asimov.client.services.ClasseService;
import com.asimov.client.services.EleveService;
import com.asimov.client.services.InscriptionService;
import com.asimov.client.services.OptionService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la vue de création et d'édition d'un élève.
 * <p>
 * Gère la validation des entrées, la création/modification de l'élève,
 * l'inscription dans une classe, et la synchronisation des options (ajout/retrait).
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

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

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
                classeCombo.setDisable(true); // En édition, on ne change pas la classe ici
            }
        } else {
            titleLabel.setText("Nouvel Élève");
        }
        chargerReferentiels();
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Séquence d'enregistrement complète :
     * 1. Sauvegarde de l'élève (Création ou Mise à jour).
     * 2. Si création, inscription dans la classe.
     * 3. Synchronisation des options (retrait des anciennes, ajout des nouvelles).
     */
    @FXML
    private void handleSave() {
        if (isInputValid()) {
            eleve.setNom(nomField.getText().trim());
            eleve.setPrenom(prenomField.getText().trim());
            eleve.setEmail(emailField.getText().trim());

            String pass = passwordField.getText();
            if (pass != null && !pass.trim().isEmpty()) {
                eleve.setPassword(pass.trim());
            }

            // Désactivation du bouton pendant le traitement pour éviter les clics multiples
            errorLabel.setText("Sauvegarde en cours...");
            errorLabel.setStyle("-fx-text-fill: #3B82F6;");
            errorLabel.setVisible(true);

            CompletableFuture<Void> operation;

            if (eleve.getId() == 0) {
                // --- MODE CRÉATION ---
                eleve.setIdentifiant_csv("CSV-" + System.currentTimeMillis());
                operation = EleveService.createEleveAsync(eleve)
                        .thenCompose(eleveCree -> {
                            // On inscrit l'élève dans la classe sélectionnée
                            if (classeCombo.getValue() != null) {
                                return InscriptionService.inscrireEleveAsync(eleveCree.getId(), classeCombo.getValue().getId());
                            }
                            return CompletableFuture.completedFuture(null);
                        });
            } else {
                // --- MODE MODIFICATION ---
                operation = EleveService.updateEleveAsync(eleve);
            }

            // Une fois l'élève sauvegardé, on gère les options
            operation.thenCompose(v -> synchroniserOptionsAsync())
                    .thenRunAsync(() -> Platform.runLater(() -> {
                        okClicked = true;
                        dialogStage.close();
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            errorLabel.setStyle("-fx-text-fill: #EF5350;");
                            errorLabel.setText("Erreur lors de la sauvegarde : " + ex.getMessage());
                        });
                        ex.printStackTrace();
                        return null;
                    });
        }
    }

    /**
     * Compare les options initiales avec la nouvelle sélection et lance les requêtes
     * pour ajouter les nouvelles options et retirer celles qui ont été décochées.
     */
    private CompletableFuture<Void> synchroniserOptionsAsync() {
        if (eleve.getId() == 0) return CompletableFuture.completedFuture(null); // Sécurité

        List<Integer> anciennesIds = optionsInitiales.stream().map(Option::getId).collect(Collectors.toList());
        List<Integer> nouvellesIds = getSelectedOptionIds();

        // Trouver les options à ajouter (présentes dans la nouvelle liste, pas dans l'ancienne)
        List<Integer> aAjouter = nouvellesIds.stream()
                .filter(id -> !anciennesIds.contains(id))
                .collect(Collectors.toList());

        // Trouver les options à retirer (présentes dans l'ancienne, plus dans la nouvelle)
        List<Integer> aRetirer = anciennesIds.stream()
                .filter(id -> !nouvellesIds.contains(id))
                .collect(Collectors.toList());

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Integer id : aAjouter) {
            futures.add(OptionService.assignerOptionAsync(eleve.getId(), id));
        }
        for (Integer id : aRetirer) {
            futures.add(OptionService.retirerOptionAsync(eleve.getId(), id));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        StringBuilder sb = new StringBuilder();

        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) sb.append("- Nom manquant\n");
        if (prenomField.getText() == null || prenomField.getText().trim().isEmpty()) sb.append("- Prénom manquant\n");
        if (emailField.getText() == null || !emailField.getText().contains("@")) sb.append("- Email invalide\n");

        if (eleve.getId() == 0 && (passwordField.getText() == null || passwordField.getText().trim().length() < 6)) {
            sb.append("- Mot de passe requis (min. 6 car.)\n");
        }
        if (eleve.getId() == 0 && (classeCombo == null || classeCombo.getValue() == null)) {
            sb.append("- Sélection de classe requise\n");
        }

        if (sb.length() == 0) {
            return true;
        } else {
            errorLabel.setStyle("-fx-text-fill: #EF5350;");
            errorLabel.setText(sb.toString());
            errorLabel.setVisible(true);
            return false;
        }
    }

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
                        optionsInitiales = choix; // On mémorise pour la synchronisation
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
}