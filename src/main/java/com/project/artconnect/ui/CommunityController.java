package com.project.artconnect.ui;

import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.service.CommunityService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.util.Optional;

public class CommunityController {
    @FXML private TableView<CommunityMember> memberTable;
    @FXML private TableColumn<CommunityMember, String> nameColumn;
    @FXML private TableColumn<CommunityMember, String> emailColumn;
    @FXML private TableColumn<CommunityMember, String> cityColumn;
    @FXML private TableColumn<CommunityMember, String> phoneColumn;
    @FXML private TableColumn<CommunityMember, Integer> yearColumn;
    @FXML private TableColumn<CommunityMember, String> membershipColumn;

    private final CommunityService communityService = ServiceProvider.getCommunityService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("birthYear"));
        membershipColumn.setCellValueFactory(new PropertyValueFactory<>("membershipType"));
        refreshTable();
    }

    @FXML
    private void handleAdd() {
        Dialog<CommunityMember> dialog = buildDialog(null);
        Optional<CommunityMember> result = dialog.showAndWait();
        result.ifPresent(m -> {
            try { communityService.createMember(m); refreshTable(); }
            catch (Exception e) { showError("Erreur", e.getMessage()); }
        });
    }

    @FXML
    private void handleEdit() {
        CommunityMember sel = memberTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Sélection", "Sélectionnez un membre."); return; }
        Dialog<CommunityMember> dialog = buildDialog(sel);
        Optional<CommunityMember> result = dialog.showAndWait();
        result.ifPresent(m -> {
            try { communityService.updateMember(m); refreshTable(); }
            catch (Exception e) { showError("Erreur", e.getMessage()); }
        });
    }

    @FXML
    private void handleDelete() {
        CommunityMember sel = memberTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Sélection", "Sélectionnez un membre."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le membre '" + sel.getName() + "' ?", ButtonType.YES, ButtonType.NO);
        c.showAndWait().ifPresent(b -> {
            if (b == ButtonType.YES) {
                try { communityService.deleteMember(sel.getName()); refreshTable(); }
                catch (Exception e) { showError("Erreur", e.getMessage()); }
            }
        });
    }

    private Dialog<CommunityMember> buildDialog(CommunityMember ex) {
        Dialog<CommunityMember> dialog = new Dialog<>();
        dialog.setTitle(ex == null ? "Nouveau Membre" : "Modifier Membre");
        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(10); g.setVgap(10);
        g.setPadding(new Insets(20, 150, 10, 10));

        TextField nameF = new TextField(ex != null ? ex.getName() : "");
        nameF.setDisable(ex != null);
        TextField emailF = new TextField(ex != null ? ex.getEmail() : "");
        TextField yearF = new TextField(ex != null && ex.getBirthYear() != null
                ? String.valueOf(ex.getBirthYear()) : "");
        TextField phoneF = new TextField(ex != null ? ex.getPhone() : "");
        TextField cityF = new TextField(ex != null ? ex.getCity() : "");
        ComboBox<String> membershipCB = new ComboBox<>(FXCollections.observableArrayList("free", "premium"));
        membershipCB.setValue(ex != null && ex.getMembershipType() != null ? ex.getMembershipType() : "free");

        g.add(new Label("Nom:"), 0, 0); g.add(nameF, 1, 0);
        g.add(new Label("Email:"), 0, 1); g.add(emailF, 1, 1);
        g.add(new Label("Année naissance:"), 0, 2); g.add(yearF, 1, 2);
        g.add(new Label("Téléphone:"), 0, 3); g.add(phoneF, 1, 3);
        g.add(new Label("Ville:"), 0, 4); g.add(cityF, 1, 4);
        g.add(new Label("Abonnement:"), 0, 5); g.add(membershipCB, 1, 5);
        dialog.getDialogPane().setContent(g);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                String name = nameF.getText().trim();
                String email = emailF.getText().trim();
                if (name.isEmpty()) { showError("Validation", "Le nom est obligatoire."); return null; }
                if (email.isEmpty()) { showError("Validation", "L'email est obligatoire."); return null; }
                CommunityMember m = new CommunityMember();
                m.setName(name);
                m.setEmail(email);
                try {
                    String y = yearF.getText().trim();
                    m.setBirthYear(y.isEmpty() ? null : Integer.parseInt(y));
                } catch (NumberFormatException e) {
                    showError("Validation", "Année invalide."); return null;
                }
                m.setPhone(phoneF.getText().trim().isEmpty() ? null : phoneF.getText().trim());
                m.setCity(cityF.getText().trim().isEmpty() ? null : cityF.getText().trim());
                m.setMembershipType(membershipCB.getValue());
                return m;
            }
            return null;
        });
        return dialog;
    }

    private void refreshTable() {
        memberTable.setItems(FXCollections.observableArrayList(communityService.getAllMembers()));
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(title);
        a.setContentText(msg); a.showAndWait();
    }
}
