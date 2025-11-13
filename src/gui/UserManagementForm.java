// JavaFX version of UserManagementForm
package gui;

import dao.UserDAO;
import models.User;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import java.util.List;

public class UserManagementForm extends Application {
    private final UserDAO userDAO = new UserDAO();
    private TableView<User> userTable;
    private ObservableList<User> userData;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("IMS - Users");
        BorderPane container = new BorderPane();
        container.setPadding(new Insets(10));

        VBox sidebar = NavigationPanel.createSidebar(primaryStage, "Users");
        container.setLeft(sidebar);
        // removed topBar because the navigation drawer provides the page heading

        VBox mainPanel = new VBox(10);
        mainPanel.setPadding(new Insets(20));
        mainPanel.setStyle("-fx-background-color: white;");

        HBox topActions = new HBox(10);
        topActions.setAlignment(Pos.CENTER_LEFT);
        Label usersLabel = new Label("Users");
        GuiUtils.styleHeaderLabel(usersLabel);
        topActions.getChildren().add(usersLabel);
        Button addBtn = new Button("Add User");
        addBtn.setOnAction(e -> showAddDialog());
        GuiUtils.stylePrimary(addBtn);
        // Only show Add User to admins
        if (utils.SessionManager.isAdmin()) {
            topActions.getChildren().add(addBtn);
        }

        Button deleteBtn = new Button("Delete User");
        deleteBtn.setOnAction(e -> {
            if (!utils.SessionManager.isAdmin()) {
                showAlert("Only Admin can delete users.", Alert.AlertType.WARNING);
                return;
            }
            User selected = userTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select a user to delete.", Alert.AlertType.WARNING);
                return;
            }
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
        });
        // Only show delete button to admins
        if (utils.SessionManager.isAdmin()) {
            topActions.getChildren().add(deleteBtn);
        }
        // style the top action area as a card
        GuiUtils.styleCard(topActions);
        topActions.setPadding(new Insets(12));
        mainPanel.getChildren().add(topActions);

        userTable = new TableView<>();
        userData = FXCollections.observableArrayList();
        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("userId"));
        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("username"));
        TableColumn<User, String> fullNameCol = new TableColumn<>("Full Name");
        fullNameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("fullName"));
        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("role"));
        userTable.getColumns().addAll(idCol, usernameCol, fullNameCol, roleCol);
        userTable.setItems(userData);
        userTable.setPrefHeight(500);
        userTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    if (utils.SessionManager.isAdmin()) {
                        showEditDialog(row.getItem());
                    } else {
                        showAlert("Only Admin can edit users.", Alert.AlertType.WARNING);
                    }
                }
            });
            return row;
        });
        // wrap the table in a subtle card for consistent spacing
        VBox tableBox = new VBox(8, userTable);
        GuiUtils.styleCard(tableBox);
        tableBox.setPadding(new Insets(10));
        mainPanel.getChildren().add(tableBox);

        container.setCenter(mainPanel);
        loadUsers();

        Scene scene = new Scene(container, 1200, 700);
        primaryStage.setScene(scene);
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private void loadUsers() {
        userData.clear();
        userData.addAll(userDAO.getAllUsers());
    }

    private void showAddDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add User");
        dialog.setHeaderText("Fill in user details");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        TextField username = new TextField();
        username.setPromptText("Username");
        GuiUtils.styleInput(username);
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        GuiUtils.styleInput(password);
        TextField full = new TextField();
        full.setPromptText("Full Name");
        GuiUtils.styleInput(full);
        ComboBox<String> role = new ComboBox<>(FXCollections.observableArrayList("Admin", "Staff"));
        role.setValue("Staff");
        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);
        grid.add(new Label("Full Name:"), 0, 2);
        grid.add(full, 1, 2);
        grid.add(new Label("Role:"), 0, 3);
        grid.add(role, 1, 3);
        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new User(username.getText().trim(), password.getText(), full.getText().trim(), role.getValue());
            }
            return null;
        });
        dialog.showAndWait().ifPresent(user -> {
            if (userDAO.createUser(user)) {
                showAlert("User added successfully!", Alert.AlertType.INFORMATION);
                loadUsers();
            } else {
                showAlert("Failed to add user!", Alert.AlertType.ERROR);
            }
        });
    }

    private void showEditDialog(User user) {
        if (user == null) return;
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit user details");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        TextField username = new TextField(user.getUsername());
        GuiUtils.styleInput(username);
        PasswordField password = new PasswordField();
        password.setPromptText("Password (leave blank to keep)");
        GuiUtils.styleInput(password);
        TextField full = new TextField(user.getFullName());
        GuiUtils.styleInput(full);
        ComboBox<String> role = new ComboBox<>(FXCollections.observableArrayList("Admin", "Staff"));
        role.setValue(user.getRole());
        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);
        grid.add(new Label("Full Name:"), 0, 2);
        grid.add(full, 1, 2);
        grid.add(new Label("Role:"), 0, 3);
        grid.add(role, 1, 3);
        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                user.setUsername(username.getText().trim());
                String pw = password.getText();
                if (!pw.isEmpty()) user.setPassword(pw);
                user.setFullName(full.getText().trim());
                user.setRole(role.getValue());
                return user;
            }
            return null;
        });
        dialog.showAndWait().ifPresent(u -> {
            if (userDAO.updateUser(u)) {
                showAlert("User updated successfully!", Alert.AlertType.INFORMATION);
                loadUsers();
            } else {
                showAlert("Failed to update user!", Alert.AlertType.ERROR);
            }
        });
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
