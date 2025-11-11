// JavaFX version of LoginForm
package gui;

import services.AuthService;
import models.User;
import utils.SessionManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

public class LoginForm extends Application {
    private AuthService authService;
    private TextField emailField;
    private PasswordField passwordField;

    @Override
    public void start(Stage primaryStage) {
        authService = new AuthService();
        primaryStage.setTitle("IMS - Inventory Management System");
        BorderPane root = new BorderPane();
        root.setPrefSize(900, 600);

        HBox mainPanel = new HBox();
        mainPanel.setPrefSize(900, 600);

        // Left panel with logo
        VBox leftPanel = new VBox();
        leftPanel.setPrefWidth(450);
        leftPanel.setAlignment(Pos.CENTER);
        leftPanel.setStyle("-fx-background-color: #769cbf;");
        Label logoLabel = new Label("IMS");
        logoLabel.setStyle("-fx-font-size: 120px; -fx-font-weight: bold; -fx-text-fill: #1a365d;");
        leftPanel.getChildren().add(logoLabel);
        mainPanel.getChildren().add(leftPanel);

        // Right panel with login form
        VBox rightPanel = new VBox(20);
        rightPanel.setPrefWidth(450);
        rightPanel.setAlignment(Pos.TOP_LEFT);
        rightPanel.setPadding(new Insets(60, 40, 40, 40));
        rightPanel.setStyle("-fx-background-color: white;");
        Label imsIconLabel = new Label("IMS");
        imsIconLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a365d;");
        Label titleLabel = new Label("Inventory management system");
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
        rightPanel.getChildren().addAll(imsIconLabel, titleLabel);

        Label emailLabel = new Label("Username");
        emailField = new TextField("admin");
        emailField.setPromptText("Username");
        Label passwordLabel = new Label("Password");
        passwordField = new PasswordField();
        passwordField.setText("password");
        passwordField.setPromptText("Password");
        Button loginButton = new Button("Login");
        GuiUtils.stylePrimary(loginButton);
        loginButton.setPrefWidth(290);
        loginButton.setOnAction(e -> handleLogin(primaryStage));
        rightPanel.getChildren().addAll(emailLabel, emailField, passwordLabel, passwordField, loginButton);

        mainPanel.getChildren().add(rightPanel);
        root.setCenter(mainPanel);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        // center the login window on primary screen with a comfortable size
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double w = Math.min(900, bounds.getWidth() * 0.8);
        double h = Math.min(600, bounds.getHeight() * 0.8);
        primaryStage.setWidth(w);
        primaryStage.setHeight(h);
        primaryStage.setX(bounds.getMinX() + (bounds.getWidth() - w) / 2);
        primaryStage.setY(bounds.getMinY() + (bounds.getHeight() - h) / 2);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    private void handleLogin(Stage stage) {
        String username = emailField.getText().trim();
        String password = passwordField.getText();
        User user = authService.login(username, password);
        if (user != null) {
            SessionManager.setCurrentUser(user);
            showAlert("Login successful!", Alert.AlertType.INFORMATION);
            // Close login window
            stage.close();
            // Open DashboardForm (JavaFX)
            try {
                new DashboardForm().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            showAlert("Invalid username or password.", Alert.AlertType.ERROR);
        }
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
