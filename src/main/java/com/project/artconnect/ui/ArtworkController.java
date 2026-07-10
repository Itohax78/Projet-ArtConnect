package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.ArtworkTag;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.service.ArtworkService;
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

public class ArtworkController {
    @FXML private TableView<Artwork> artworkTable;
    @FXML private TableColumn<Artwork, String> titleColumn;
    @FXML private TableColumn<Artwork, String> artistColumn;
    @FXML private TableColumn<Artwork, String> typeColumn;
    @FXML private TableColumn<Artwork, String> mediumColumn;
    @FXML private TableColumn<Artwork, Integer> yearColumn;
    @FXML private TableColumn<Artwork, Double> priceColumn;
    @FXML private TableColumn<Artwork, String> statusColumn;
    @FXML private TableColumn<Artwork, String> tagsColumn;

    private final ArtworkService artworkService = ServiceProvider.getArtworkService();
    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        mediumColumn.setCellValueFactory(new PropertyValueFactory<>("medium"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("creationYear"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getStatus() != null ? cd.getValue().getStatus().name() : ""));
        artistColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getArtist() != null ? cd.getValue().getArtist().getName() : ""));
        tagsColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getTags() != null
                        ? cd.getValue().getTags().stream().map(ArtworkTag::getName)
                            .collect(Collectors.joining(", ")) : ""));
        refreshTable();
    }

    @FXML
    private void handleAdd() {
        Dialog<Artwork> dialog = buildDialog(null);
        Optional<Artwork> result = dialog.showAndWait();
        result.ifPresent(aw -> {
            try { artworkService.createArtwork(aw); refreshTable(); }
            catch (Exception e) { showError("Erreur", e.getMessage()); }
        });
    }

    @FXML
    private void handleEdit() {
        Artwork sel = artworkTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Sélection", "Sélectionnez une œuvre."); return; }
        Dialog<Artwork> dialog = buildDialog(sel);
        Optional<Artwork> result = dialog.showAndWait();
        result.ifPresent(aw -> {
            try { artworkService.updateArtwork(aw); refreshTable(); }
            catch (Exception e) { showError("Erreur", e.getMessage()); }
        });
    }

    @FXML
    private void handleDelete() {
        Artwork sel = artworkTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Sélection", "Sélectionnez une œuvre."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer '" + sel.getTitle() + "' ?", ButtonType.YES, ButtonType.NO);
        c.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                try { artworkService.deleteArtwork(sel.getTitle()); refreshTable(); }
                catch (Exception e) { showError("Erreur", e.getMessage()); }
            }
        });
    }

    private Dialog<Artwork> buildDialog(Artwork ex) {
        Dialog<Artwork> dialog = new Dialog<>();
        dialog.setTitle(ex == null ? "Nouvelle Œuvre" : "Modifier Œuvre");
        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(10);
        g.setPadding(new Insets(20, 150, 10, 10));

        TextField titleF = new TextField(ex != null ? ex.getTitle() : "");
        titleF.setDisable(ex != null);
        ComboBox<String> artistCB = new ComboBox<>(FXCollections.observableArrayList(
                artistService.getAllArtists().stream().map(Artist::getName).collect(Collectors.toList())));
        if (ex != null && ex.getArtist() != null) artistCB.setValue(ex.getArtist().getName());
        artistCB.setDisable(ex != null);
        TextField typeF = new TextField(ex != null ? ex.getType() : "");
        TextField mediumF = new TextField(ex != null ? ex.getMedium() : "");
        TextField yearF = new TextField(ex != null && ex.getCreationYear() != null
                ? String.valueOf(ex.getCreationYear()) : "");
        TextField dimF = new TextField(ex != null ? ex.getDimensions() : "");
        TextField priceF = new TextField(ex != null ? String.valueOf(ex.getPrice()) : "0");
        ComboBox<String> statusCB = new ComboBox<>(FXCollections.observableArrayList("FOR_SALE","SOLD","EXHIBITED"));
        statusCB.setValue(ex != null && ex.getStatus() != null ? ex.getStatus().name() : "FOR_SALE");
        TextField tagsF = new TextField(ex != null && ex.getTags() != null
                ? ex.getTags().stream().map(ArtworkTag::getName).collect(Collectors.joining(", ")) : "");
        TextArea descF = new TextArea(ex != null ? ex.getDescription() : "");
        descF.setPrefRowCount(2);

        g.add(new Label("Titre:"), 0, 0); g.add(titleF, 1, 0);
        g.add(new Label("Artiste:"), 0, 1); g.add(artistCB, 1, 1);
        g.add(new Label("Type:"), 0, 2); g.add(typeF, 1, 2);
        g.add(new Label("Médium:"), 0, 3); g.add(mediumF, 1, 3);
        g.add(new Label("Année:"), 0, 4); g.add(yearF, 1, 4);
        g.add(new Label("Dimensions:"), 0, 5); g.add(dimF, 1, 5);
        g.add(new Label("Prix:"), 0, 6); g.add(priceF, 1, 6);
        g.add(new Label("Statut:"), 0, 7); g.add(statusCB, 1, 7);
        g.add(new Label("Tags (virgule):"), 0, 8); g.add(tagsF, 1, 8);
        g.add(new Label("Description:"), 0, 9); g.add(descF, 1, 9);
        dialog.getDialogPane().setContent(g);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                String title = titleF.getText().trim();
                if (title.isEmpty()) { showError("Validation", "Le titre est obligatoire."); return null; }
                Artwork a = new Artwork();
                a.setTitle(title);
                a.setType(typeF.getText().trim().isEmpty() ? null : typeF.getText().trim());
                a.setMedium(mediumF.getText().trim().isEmpty() ? null : mediumF.getText().trim());
                a.setDimensions(dimF.getText().trim().isEmpty() ? null : dimF.getText().trim());
                a.setDescription(descF.getText().trim().isEmpty() ? null : descF.getText().trim());
                try { a.setCreationYear(yearF.getText().trim().isEmpty() ? null : Integer.parseInt(yearF.getText().trim())); }
                catch (NumberFormatException e) { showError("Validation", "Année invalide."); return null; }
                try { a.setPrice(Double.parseDouble(priceF.getText().trim())); }
                catch (NumberFormatException e) { showError("Validation", "Prix invalide."); return null; }
                if (a.getPrice() < 0) { showError("Validation", "Le prix doit être positif."); return null; }
                a.setStatus(Artwork.Status.valueOf(statusCB.getValue()));
                String artistName = artistCB.getValue();
                if (artistName == null || artistName.isEmpty()) {
                    showError("Validation", "Sélectionnez un artiste."); return null;
                }
                Artist artist = new Artist(); artist.setName(artistName);
                a.setArtist(artist);
                String t = tagsF.getText().trim();
                if (!t.isEmpty()) {
                    a.setTags(Arrays.stream(t.split(",")).map(String::trim)
                            .filter(s -> !s.isEmpty()).map(ArtworkTag::new)
                            .collect(Collectors.toList()));
                }
                return a;
            }
            return null;
        });
        return dialog;
    }

    private void refreshTable() {
        artworkTable.setItems(FXCollections.observableArrayList(artworkService.getAllArtworks()));
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(title);
        a.setContentText(msg); a.showAndWait();
    }
}
