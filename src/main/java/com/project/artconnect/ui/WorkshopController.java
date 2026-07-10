package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.stream.Collectors;

public class WorkshopController {
    @FXML private TableView<Workshop> workshopTable;
    @FXML private TableColumn<Workshop, String> titleColumn;
    @FXML private TableColumn<Workshop, String> instructorColumn;
    @FXML private TableColumn<Workshop, LocalDateTime> dateColumn;
    @FXML private TableColumn<Workshop, Integer> durationColumn;
    @FXML private TableColumn<Workshop, Integer> maxColumn;
    @FXML private TableColumn<Workshop, Double> priceColumn;
    @FXML private TableColumn<Workshop, String> levelColumn;
    @FXML private TableColumn<Workshop, String> locationColumn;

    private final WorkshopService workshopService = ServiceProvider.getWorkshopService();
    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("durationMinutes"));
        maxColumn.setCellValueFactory(new PropertyValueFactory<>("maxParticipants"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        instructorColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getInstructor() != null ? cd.getValue().getInstructor().getName() : ""));
        refreshTable();
    }

    @FXML
    private void handleAdd() {
        Dialog<Workshop> dialog = buildDialog(null);
        Optional<Workshop> result = dialog.showAndWait();
        result.ifPresent(w -> {
            try { workshopService.createWorkshop(w); refreshTable(); }
            catch (Exception e) { showError("Erreur", e.getMessage()); }
        });
    }

    @FXML
    private void handleEdit() {
        Workshop sel = workshopTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Sélection", "Sélectionnez un atelier."); return; }
        Dialog<Workshop> dialog = buildDialog(sel);
        Optional<Workshop> result = dialog.showAndWait();
        result.ifPresent(w -> {
            try { workshopService.updateWorkshop(w); refreshTable(); }
            catch (Exception e) { showError("Erreur", e.getMessage()); }
        });
    }

    @FXML
    private void handleDelete() {
        Workshop sel = workshopTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Sélection", "Sélectionnez un atelier."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer '" + sel.getTitle() + "' ?", ButtonType.YES, ButtonType.NO);
        c.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                try { workshopService.deleteWorkshop(sel.getTitle()); refreshTable(); }
                catch (Exception e) { showError("Erreur", e.getMessage()); }
            }
        });
    }

    private Dialog<Workshop> buildDialog(Workshop ex) {
        Dialog<Workshop> dialog = new Dialog<>();
        dialog.setTitle(ex == null ? "Nouvel Atelier" : "Modifier Atelier");
        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(10);
        g.setPadding(new Insets(20, 150, 10, 10));

        TextField titleF = new TextField(ex != null ? ex.getTitle() : "");
        titleF.setDisable(ex != null);
        ComboBox<String> instrCB = new ComboBox<>(FXCollections.observableArrayList(
                artistService.getAllArtists().stream().map(Artist::getName).collect(Collectors.toList())));
        if (ex != null && ex.getInstructor() != null) instrCB.setValue(ex.getInstructor().getName());
        instrCB.setDisable(ex != null);
        DatePicker dateDP = new DatePicker(ex != null && ex.getDate() != null
                ? ex.getDate().toLocalDate() : LocalDate.now());
        TextField timeF = new TextField(ex != null && ex.getDate() != null
                ? ex.getDate().toLocalTime().toString() : "10:00");
        timeF.setPromptText("HH:MM");
        TextField durF = new TextField(ex != null ? String.valueOf(ex.getDurationMinutes()) : "60");
        TextField maxF = new TextField(ex != null ? String.valueOf(ex.getMaxParticipants()) : "10");
        TextField priceF = new TextField(ex != null ? String.valueOf(ex.getPrice()) : "0");
        ComboBox<String> levelCB = new ComboBox<>(FXCollections.observableArrayList(
                "Beginner", "Intermediate", "Advanced"));
        levelCB.setValue(ex != null && ex.getLevel() != null ? ex.getLevel() : "Beginner");
        TextField locF = new TextField(ex != null ? ex.getLocation() : "");
        TextArea descF = new TextArea(ex != null ? ex.getDescription() : "");
        descF.setPrefRowCount(2);

        g.add(new Label("Titre:"), 0, 0); g.add(titleF, 1, 0);
        g.add(new Label("Instructeur:"), 0, 1); g.add(instrCB, 1, 1);
        g.add(new Label("Date:"), 0, 2); g.add(dateDP, 1, 2);
        g.add(new Label("Heure (HH:MM):"), 0, 3); g.add(timeF, 1, 3);
        g.add(new Label("Durée (min):"), 0, 4); g.add(durF, 1, 4);
        g.add(new Label("Max participants:"), 0, 5); g.add(maxF, 1, 5);
        g.add(new Label("Prix:"), 0, 6); g.add(priceF, 1, 6);
        g.add(new Label("Niveau:"), 0, 7); g.add(levelCB, 1, 7);
        g.add(new Label("Lieu:"), 0, 8); g.add(locF, 1, 8);
        g.add(new Label("Description:"), 0, 9); g.add(descF, 1, 9);
        dialog.getDialogPane().setContent(g);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                String title = titleF.getText().trim();
                if (title.isEmpty()) { showError("Validation", "Le titre est obligatoire."); return null; }
                Workshop w = new Workshop();
                w.setTitle(title);
                try {
                    LocalDate d = dateDP.getValue();
                    LocalTime t = LocalTime.parse(timeF.getText().trim());
                    w.setDate(LocalDateTime.of(d, t));
                } catch (Exception e) { showError("Validation", "Date/heure invalide (format HH:MM)."); return null; }
                try { w.setDurationMinutes(Integer.parseInt(durF.getText().trim())); }
                catch (NumberFormatException e) { showError("Validation", "Durée invalide."); return null; }
                if (w.getDurationMinutes() <= 0) { showError("Validation", "La durée doit être > 0."); return null; }
                try { w.setMaxParticipants(Integer.parseInt(maxF.getText().trim())); }
                catch (NumberFormatException e) { showError("Validation", "Max participants invalide."); return null; }
                if (w.getMaxParticipants() <= 0) { showError("Validation", "Max participants doit être > 0."); return null; }
                try { w.setPrice(Double.parseDouble(priceF.getText().trim())); }
                catch (NumberFormatException e) { showError("Validation", "Prix invalide."); return null; }
                if (w.getPrice() < 0) { showError("Validation", "Le prix doit être >= 0."); return null; }
                w.setLevel(levelCB.getValue());
                w.setLocation(locF.getText().trim().isEmpty() ? null : locF.getText().trim());
                w.setDescription(descF.getText().trim().isEmpty() ? null : descF.getText().trim());
                String instrName = instrCB.getValue();
                if (instrName == null || instrName.isEmpty()) {
                    showError("Validation", "Sélectionnez un instructeur."); return null;
                }
                Artist instr = new Artist(); instr.setName(instrName);
                w.setInstructor(instr);
                return w;
            }
            return null;
        });
        return dialog;
    }

    private void refreshTable() {
        workshopTable.setItems(FXCollections.observableArrayList(workshopService.getAllWorkshops()));
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(title);
        a.setContentText(msg); a.showAndWait();
    }
}
