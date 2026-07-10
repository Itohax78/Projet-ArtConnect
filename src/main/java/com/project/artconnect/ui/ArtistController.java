package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class ArtistController {
    @FXML private TextField searchField;
    @FXML private ComboBox<Discipline> disciplineFilter;
    @FXML private TableView<Artist> artistTable;
    @FXML private TableColumn<Artist, String> nameColumn;
    @FXML private TableColumn<Artist, String> cityColumn;
    @FXML private TableColumn<Artist, String> emailColumn;
    @FXML private TableColumn<Artist, Integer> yearColumn;
    @FXML private TableColumn<Artist, String> phoneColumn;
    @FXML private TableColumn<Artist, String> disciplinesColumn;
    @FXML private TableColumn<Artist, String> activeColumn;

    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("birthYear"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        disciplinesColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getDisciplines() != null
                        ? cd.getValue().getDisciplines().stream()
                            .map(Discipline::getName).collect(Collectors.joining(", "))
                        : ""));

        activeColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().isActive() ? "Yes" : "No"));

        disciplineFilter.setItems(FXCollections.observableArrayList(artistService.getAllDisciplines()));
        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        Discipline d = disciplineFilter.getValue();
        String dName = (d != null) ? d.getName() : null;
        artistTable.setItems(FXCollections.observableArrayList(
                artistService.searchArtists(query, dName, null)));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        disciplineFilter.setValue(null);
        refreshTable();
    }

    @FXML
    private void handleAdd() {
        Dialog<Artist> dialog = buildArtistDialog(null);
        Optional<Artist> result = dialog.showAndWait();
        result.ifPresent(artist -> {
            try {
                artistService.createArtist(artist);
                refreshTable();
                refreshDisciplineFilter();
            } catch (Exception e) {
                showError("Erreur lors de la création", e.getMessage());
            }
        });
    }

    @FXML
    private void handleEdit() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Aucune sélection", "Veuillez sélectionner un artiste à modifier.");
            return;
        }
        Dialog<Artist> dialog = buildArtistDialog(selected);
        Optional<Artist> result = dialog.showAndWait();
        result.ifPresent(artist -> {
            try {
                artistService.updateArtist(artist);
                refreshTable();
            } catch (Exception e) {
                showError("Erreur lors de la modification", e.getMessage());
            }
        });
    }

    @FXML
    private void handleDelete() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Aucune sélection", "Veuillez sélectionner un artiste à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'artiste '" + selected.getName() + "' ?\nToutes ses œuvres et ateliers seront aussi supprimés (CASCADE).",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    artistService.deleteArtist(selected.getName());
                    refreshTable();
                } catch (Exception e) {
                    showError("Erreur lors de la suppression", e.getMessage());
                }
            }
        });
    }

    private Dialog<Artist> buildArtistDialog(Artist existing) {
        Dialog<Artist> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nouvel Artiste" : "Modifier Artiste");

        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameF = new TextField(existing != null ? existing.getName() : "");
        nameF.setPromptText("Nom"); nameF.setDisable(existing != null);
        TextField bioF = new TextField(existing != null ? existing.getBio() : "");
        TextField yearF = new TextField(existing != null && existing.getBirthYear() != null
                ? String.valueOf(existing.getBirthYear()) : "");
        TextField emailF = new TextField(existing != null ? existing.getContactEmail() : "");
        TextField phoneF = new TextField(existing != null ? existing.getPhone() : "");
        TextField cityF = new TextField(existing != null ? existing.getCity() : "");
        TextField websiteF = new TextField(existing != null ? existing.getWebsite() : "");
        TextField socialF = new TextField(existing != null ? existing.getSocialMedia() : "");
        TextField discF = new TextField(existing != null && existing.getDisciplines() != null
                ? existing.getDisciplines().stream().map(Discipline::getName).collect(Collectors.joining(", ")) : "");
        discF.setPromptText("Peinture, Sculpture, ...");
        CheckBox activeC = new CheckBox("Actif");
        activeC.setSelected(existing == null || existing.isActive());

        grid.add(new Label("Nom:"), 0, 0); grid.add(nameF, 1, 0);
        grid.add(new Label("Bio:"), 0, 1); grid.add(bioF, 1, 1);
        grid.add(new Label("Année naissance:"), 0, 2); grid.add(yearF, 1, 2);
        grid.add(new Label("Email:"), 0, 3); grid.add(emailF, 1, 3);
        grid.add(new Label("Téléphone:"), 0, 4); grid.add(phoneF, 1, 4);
        grid.add(new Label("Ville:"), 0, 5); grid.add(cityF, 1, 5);
        grid.add(new Label("Site web:"), 0, 6); grid.add(websiteF, 1, 6);
        grid.add(new Label("Réseaux sociaux:"), 0, 7); grid.add(socialF, 1, 7);
        grid.add(new Label("Disciplines:"), 0, 8); grid.add(discF, 1, 8);
        grid.add(activeC, 1, 9);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                String name = nameF.getText().trim();
                if (name.isEmpty()) { showError("Validation", "Le nom est obligatoire."); return null; }
                Artist a = new Artist();
                a.setName(name);
                a.setBio(bioF.getText().trim().isEmpty() ? null : bioF.getText().trim());
                try {
                    String y = yearF.getText().trim();
                    a.setBirthYear(y.isEmpty() ? null : Integer.parseInt(y));
                } catch (NumberFormatException e) {
                    showError("Validation", "L'année de naissance doit être un nombre.");
                    return null;
                }
                a.setContactEmail(emailF.getText().trim().isEmpty() ? null : emailF.getText().trim());
                a.setPhone(phoneF.getText().trim().isEmpty() ? null : phoneF.getText().trim());
                a.setCity(cityF.getText().trim().isEmpty() ? null : cityF.getText().trim());
                a.setWebsite(websiteF.getText().trim().isEmpty() ? null : websiteF.getText().trim());
                a.setSocialMedia(socialF.getText().trim().isEmpty() ? null : socialF.getText().trim());
                a.setActive(activeC.isSelected());

                String discText = discF.getText().trim();
                if (!discText.isEmpty()) {
                    a.setDisciplines(Arrays.stream(discText.split(","))
                            .map(String::trim).filter(s -> !s.isEmpty())
                            .map(Discipline::new).collect(Collectors.toList()));
                }
                return a;
            }
            return null;
        });
        return dialog;
    }

    private void refreshTable() {
        artistTable.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
    }

    private void refreshDisciplineFilter() {
        disciplineFilter.setItems(FXCollections.observableArrayList(artistService.getAllDisciplines()));
    }

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
