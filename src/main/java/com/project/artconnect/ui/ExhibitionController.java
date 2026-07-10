package com.project.artconnect.ui;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.ExhibitionService;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExhibitionController {
    @FXML private TableView<Exhibition> exhibitionTable;
    @FXML private TableColumn<Exhibition, String> titleColumn;
    @FXML private TableColumn<Exhibition, String> galleryColumn;
    @FXML private TableColumn<Exhibition, LocalDate> startColumn;
    @FXML private TableColumn<Exhibition, LocalDate> endColumn;
    @FXML private TableColumn<Exhibition, String> curatorColumn;
    @FXML private TableColumn<Exhibition, String> themeColumn;

    private final ExhibitionService exhibitionService = ServiceProvider.getExhibitionService();
    private final GalleryService galleryService = ServiceProvider.getGalleryService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        startColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        curatorColumn.setCellValueFactory(new PropertyValueFactory<>("curatorName"));
        themeColumn.setCellValueFactory(new PropertyValueFactory<>("theme"));
        galleryColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getGallery() != null ? cd.getValue().getGallery().getName() : ""));
        refreshTable();
    }

    @FXML
    private void handleAdd() {
        Dialog<Exhibition> dialog = buildDialog(null);
        Optional<Exhibition> result = dialog.showAndWait();
        result.ifPresent(e -> {
            try { exhibitionService.createExhibition(e); refreshTable(); }
            catch (Exception ex) { showError("Erreur", ex.getMessage()); }
        });
    }

    @FXML
    private void handleEdit() {
        Exhibition sel = exhibitionTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Sélection", "Sélectionnez une exposition."); return; }
        Dialog<Exhibition> dialog = buildDialog(sel);
        Optional<Exhibition> result = dialog.showAndWait();
        result.ifPresent(e -> {
            try { exhibitionService.updateExhibition(e); refreshTable(); }
            catch (Exception ex) { showError("Erreur", ex.getMessage()); }
        });
    }

    @FXML
    private void handleDelete() {
        Exhibition sel = exhibitionTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Sélection", "Sélectionnez une exposition."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer '" + sel.getTitle() + "' ?", ButtonType.YES, ButtonType.NO);
        c.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                try { exhibitionService.deleteExhibition(sel.getTitle()); refreshTable(); }
                catch (Exception ex) { showError("Erreur", ex.getMessage()); }
            }
        });
    }

    private Dialog<Exhibition> buildDialog(Exhibition ex) {
        Dialog<Exhibition> dialog = new Dialog<>();
        dialog.setTitle(ex == null ? "Nouvelle Exposition" : "Modifier Exposition");
        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(10);
        g.setPadding(new Insets(20, 150, 10, 10));

        TextField titleF = new TextField(ex != null ? ex.getTitle() : "");
        titleF.setDisable(ex != null);
        ComboBox<String> galleryCB = new ComboBox<>(FXCollections.observableArrayList(
                galleryService.getAllGalleries().stream().map(Gallery::getName).collect(Collectors.toList())));
        if (ex != null && ex.getGallery() != null) galleryCB.setValue(ex.getGallery().getName());
        galleryCB.setDisable(ex != null);
        DatePicker startDP = new DatePicker(ex != null ? ex.getStartDate() : LocalDate.now());
        DatePicker endDP = new DatePicker(ex != null ? ex.getEndDate() : LocalDate.now().plusDays(30));
        TextField curatorF = new TextField(ex != null ? ex.getCuratorName() : "");
        TextField themeF = new TextField(ex != null ? ex.getTheme() : "");
        TextArea descF = new TextArea(ex != null ? ex.getDescription() : "");
        descF.setPrefRowCount(2);

        g.add(new Label("Titre:"), 0, 0); g.add(titleF, 1, 0);
        g.add(new Label("Galerie:"), 0, 1); g.add(galleryCB, 1, 1);
        g.add(new Label("Date début:"), 0, 2); g.add(startDP, 1, 2);
        g.add(new Label("Date fin:"), 0, 3); g.add(endDP, 1, 3);
        g.add(new Label("Curateur:"), 0, 4); g.add(curatorF, 1, 4);
        g.add(new Label("Thème:"), 0, 5); g.add(themeF, 1, 5);
        g.add(new Label("Description:"), 0, 6); g.add(descF, 1, 6);
        dialog.getDialogPane().setContent(g);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                String title = titleF.getText().trim();
                if (title.isEmpty()) { showError("Validation", "Le titre est obligatoire."); return null; }
                if (startDP.getValue() == null || endDP.getValue() == null) {
                    showError("Validation", "Les dates sont obligatoires."); return null;
                }
                if (endDP.getValue().isBefore(startDP.getValue())) {
                    showError("Validation", "La date de fin doit être après la date de début."); return null;
                }
                String galName = galleryCB.getValue();
                if (galName == null || galName.isEmpty()) {
                    showError("Validation", "Sélectionnez une galerie."); return null;
                }
                Exhibition exhibition = new Exhibition();
                exhibition.setTitle(title);
                exhibition.setStartDate(startDP.getValue());
                exhibition.setEndDate(endDP.getValue());
                exhibition.setCuratorName(curatorF.getText().trim().isEmpty() ? null : curatorF.getText().trim());
                exhibition.setTheme(themeF.getText().trim().isEmpty() ? null : themeF.getText().trim());
                exhibition.setDescription(descF.getText().trim().isEmpty() ? null : descF.getText().trim());
                Gallery gal = new Gallery(); gal.setName(galName);
                exhibition.setGallery(gal);
                return exhibition;
            }
            return null;
        });
        return dialog;
    }

    private void refreshTable() {
        exhibitionTable.setItems(FXCollections.observableArrayList(exhibitionService.getAllExhibitions()));
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(title);
        a.setContentText(msg); a.showAndWait();
    }
}
