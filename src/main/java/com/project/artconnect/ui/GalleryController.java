package com.project.artconnect.ui;

import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.util.Optional;

public class GalleryController {
    @FXML private TableView<Gallery> galleryTable;
    @FXML private TableColumn<Gallery, String> nameColumn;
    @FXML private TableColumn<Gallery, String> addressColumn;
    @FXML private TableColumn<Gallery, String> ownerColumn;
    @FXML private TableColumn<Gallery, String> hoursColumn;
    @FXML private TableColumn<Gallery, String> phoneColumn;
    @FXML private TableColumn<Gallery, Double> ratingColumn;
    @FXML private TableColumn<Gallery, String> websiteColumn;

    private final GalleryService galleryService = ServiceProvider.getGalleryService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        ownerColumn.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        hoursColumn.setCellValueFactory(new PropertyValueFactory<>("openingHours"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("contactPhone"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        websiteColumn.setCellValueFactory(new PropertyValueFactory<>("website"));
        refreshTable();
    }

    @FXML
    private void handleAdd() {
        Dialog<Gallery> dialog = buildDialog(null);
        Optional<Gallery> result = dialog.showAndWait();
        result.ifPresent(g -> {
            try { galleryService.createGallery(g); refreshTable(); }
            catch (Exception e) { showError("Erreur", e.getMessage()); }
        });
    }

    @FXML
    private void handleEdit() {
        Gallery sel = galleryTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Sélection", "Sélectionnez une galerie."); return; }
        Dialog<Gallery> dialog = buildDialog(sel);
        Optional<Gallery> result = dialog.showAndWait();
        result.ifPresent(g -> {
            try { galleryService.updateGallery(g); refreshTable(); }
            catch (Exception e) { showError("Erreur", e.getMessage()); }
        });
    }

    @FXML
    private void handleDelete() {
        Gallery sel = galleryTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Sélection", "Sélectionnez une galerie."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer '" + sel.getName() + "' et ses expositions ?", ButtonType.YES, ButtonType.NO);
        c.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                try { galleryService.deleteGallery(sel.getName()); refreshTable(); }
                catch (Exception e) { showError("Erreur", e.getMessage()); }
            }
        });
    }

    private Dialog<Gallery> buildDialog(Gallery ex) {
        Dialog<Gallery> dialog = new Dialog<>();
        dialog.setTitle(ex == null ? "Nouvelle Galerie" : "Modifier Galerie");
        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(10);
        g.setPadding(new Insets(20, 150, 10, 10));

        TextField nameF = new TextField(ex != null ? ex.getName() : "");
        nameF.setDisable(ex != null);
        TextField addrF = new TextField(ex != null ? ex.getAddress() : "");
        TextField ownerF = new TextField(ex != null ? ex.getOwnerName() : "");
        TextField hoursF = new TextField(ex != null ? ex.getOpeningHours() : "");
        TextField phoneF = new TextField(ex != null ? ex.getContactPhone() : "");
        TextField ratingF = new TextField(ex != null ? String.valueOf(ex.getRating()) : "0.0");
        TextField webF = new TextField(ex != null ? ex.getWebsite() : "");

        g.add(new Label("Nom:"), 0, 0); g.add(nameF, 1, 0);
        g.add(new Label("Adresse:"), 0, 1); g.add(addrF, 1, 1);
        g.add(new Label("Propriétaire:"), 0, 2); g.add(ownerF, 1, 2);
        g.add(new Label("Horaires:"), 0, 3); g.add(hoursF, 1, 3);
        g.add(new Label("Téléphone:"), 0, 4); g.add(phoneF, 1, 4);
        g.add(new Label("Note (0-5):"), 0, 5); g.add(ratingF, 1, 5);
        g.add(new Label("Site web:"), 0, 6); g.add(webF, 1, 6);
        dialog.getDialogPane().setContent(g);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                String name = nameF.getText().trim();
                if (name.isEmpty()) { showError("Validation", "Le nom est obligatoire."); return null; }
                Gallery gallery = new Gallery();
                gallery.setName(name);
                gallery.setAddress(addrF.getText().trim().isEmpty() ? null : addrF.getText().trim());
                gallery.setOwnerName(ownerF.getText().trim().isEmpty() ? null : ownerF.getText().trim());
                gallery.setOpeningHours(hoursF.getText().trim().isEmpty() ? null : hoursF.getText().trim());
                gallery.setContactPhone(phoneF.getText().trim().isEmpty() ? null : phoneF.getText().trim());
                gallery.setWebsite(webF.getText().trim().isEmpty() ? null : webF.getText().trim());
                try {
                    double r = Double.parseDouble(ratingF.getText().trim());
                    if (r < 0 || r > 5) { showError("Validation", "La note doit être entre 0 et 5."); return null; }
                    gallery.setRating(r);
                } catch (NumberFormatException e) { showError("Validation", "Note invalide."); return null; }
                return gallery;
            }
            return null;
        });
        return dialog;
    }

    private void refreshTable() {
        galleryTable.setItems(FXCollections.observableArrayList(galleryService.getAllGalleries()));
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(title);
        a.setContentText(msg); a.showAndWait();
    }
}
