package gui;

import dao.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import models.User;
import utils.Logger;

import java.util.List;

public class UserManagementFormController {
    @SuppressWarnings("unused") @FXML private VBox mainPanel;
    @SuppressWarnings("unused") @FXML private VBox sidebarPlaceholder;

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User,Integer> idCol;
    @FXML private TableColumn<User,String> usernameCol;
    @FXML private TableColumn<User,String> fullNameCol;
    @FXML private TableColumn<User,String> roleCol;
    @FXML private Button addBtn;
    @FXML private Button deleteBtn;

    private final UserDAO userDAO = new UserDAO();
    @SuppressWarnings("unused")
    private final UserDAO unusedUserDAO = userDAO;

    @FXML
    public void initialize() {
        sidebarPlaceholder.getChildren().clear();
        sidebarPlaceholder.getChildren().add(NavigationPanel.createSidebar(AppNavigator.getPrimaryStage(), "Users"));

        idCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        addBtn.setOnAction(e -> { try { new UserManagementForm().showAddDialog(); } catch (Exception ex) { Logger.error("Failed to open Add User dialog", ex); } });
        deleteBtn.setOnAction(e -> deleteSelectedUser());

        loadUsers();
    }

    private void loadUsers() {
        try {
            userTable.getItems().clear();
            List<User> users = userDAO.getAllUsers();
            userTable.getItems().addAll(users);
        } catch (Exception ex) {
            Logger.error("Failed to load users", ex);
        }
    }

    private void deleteSelectedUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Select a user to delete.", Alert.AlertType.WARNING); return; }
        if (!utils.SessionManager.isAdmin()) { showAlert("Only Admin can delete users.", Alert.AlertType.WARNING); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Are you sure you want to delete this user?");
        confirm.setContentText(selected.getUsername());
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (userDAO.deleteUser(selected.getUserId())) {
                    showAlert("User deleted successfully!", Alert.AlertType.INFORMATION);
                    loadUsers();
                } else {
                    showAlert("Failed to delete user!", Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showAlert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Info");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
