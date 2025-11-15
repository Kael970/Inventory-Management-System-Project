// JavaFX version of LoginForm
package gui;

import services.AuthService;
import models.User;
import utils.SessionManager;
import utils.Logger;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.fxml.FXMLLoader;

public class LoginForm extends Application {
    private AuthService authService;
    private TextField emailField;
    private PasswordField passwordField;

    @Override
    public void start(Stage primaryStage) {
        authService = new AuthService();
        primaryStage.setTitle("IMS - Inventory Management System");
        AppNavigator.init(primaryStage);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/LoginForm.fxml"));
            BorderPane root = loader.load();
            Scene scene = new Scene(root);
            try {
                java.net.URL css = getClass().getResource("/login.css");
                if (css != null) scene.getStylesheets().add(css.toExternalForm());
            } catch (Exception ignored) {
                // ignore
            }
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.setResizable(true);
            primaryStage.show();
        } catch (Exception ex) {
            Logger.error("Failed to load LoginForm.fxml, falling back to programmatic UI.", ex);
            // Fallback to programmatic UI if loading FXML fails
            BorderPane root = new BorderPane();
            // make root occupy available space; we'll maximize the stage to give a full-screen look
            root.setPrefSize(1200, 760);

            HBox mainPanel = new HBox(40); // bring columns closer together
            mainPanel.getStyleClass().add("login-main");
            mainPanel.setPrefSize(1200, 760);
            mainPanel.setAlignment(Pos.CENTER);

            // Left panel with large logo square (centered)
            StackPane leftPanel = new StackPane();
            leftPanel.setPrefWidth(600);
            leftPanel.setAlignment(Pos.CENTER);
            leftPanel.setStyle("-fx-background-color: white;");

            // blue square background for the large logo
            Rectangle square = new Rectangle(520, 520);
            square.setArcWidth(0);
            square.setArcHeight(0);
            square.setFill(Color.web("#b3c6db"));

            ImageView largeLogoView = new ImageView();
            largeLogoView.setFitWidth(420);
            largeLogoView.setFitHeight(420);
            largeLogoView.setPreserveRatio(true);
            largeLogoView.getStyleClass().add("login-logo-large");

            // try to load the project's logo from assets
            Label fallbackLarge = new Label("IMS");
            fallbackLarge.setStyle("-fx-font-size: 140px; -fx-font-weight: bold; -fx-text-fill: #153859;");

            try {
                java.io.InputStream is = getClass().getResourceAsStream("/assets/IMS-Logo.jpg");
                if (is == null) {
                    // try png variant
                    is = getClass().getResourceAsStream("/assets/IMS-Logo.png");
                }
                if (is != null) {
                    Image img = new Image(is);
                    largeLogoView.setImage(img);
                    leftPanel.getChildren().addAll(square, largeLogoView);
                } else {
                    leftPanel.getChildren().addAll(square, fallbackLarge);
                }
            } catch (Exception ignored) {
                leftPanel.getChildren().addAll(square, fallbackLarge);
            }

            mainPanel.getChildren().add(leftPanel);

            // Right panel with login form
            VBox rightPanel = new VBox();
            rightPanel.setPrefWidth(420);
            // we'll center the combined title+form vertically using spacers below
            rightPanel.setAlignment(Pos.CENTER);
            rightPanel.setPadding(new Insets(40, 40, 40, 40));
            // style the right panel as a subtle card
            GuiUtils.styleCard(rightPanel);
            rightPanel.setStyle(rightPanel.getStyle() + " -fx-background-color: white;");

            // small logo + bold larger title (will sit above the input fields)
            HBox titleRow = new HBox(10);
            titleRow.getStyleClass().add("title-row");
            titleRow.setAlignment(Pos.CENTER_LEFT);
            ImageView smallLogoView = new ImageView();
            smallLogoView.setFitWidth(36);
            smallLogoView.setFitHeight(36);
            smallLogoView.setPreserveRatio(true);
            smallLogoView.getStyleClass().add("login-logo-small");
            Label titleLabel = new Label("Inventory Management System");
            // larger bold title to match design
            titleLabel.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#153859;");
            try {
                java.io.InputStream is2 = getClass().getResourceAsStream("/assets/IMS-Logo.jpg");
                if (is2 == null) is2 = getClass().getResourceAsStream("/assets/IMS-Logo.png");
                if (is2 != null) {
                    Image small = new Image(is2);
                    smallLogoView.setImage(small);
                }
            } catch (Exception ignored) {
                // optional
            }
            titleRow.getChildren().addAll(smallLogoView, titleLabel);

            // form container: fixed width to match inputs and center elements neatly
            VBox formBox = new VBox(12);
            formBox.getStyleClass().add("form-box");
            formBox.setAlignment(Pos.CENTER_LEFT);
            formBox.setPrefWidth(360);

            emailField = new TextField();
            emailField.setPromptText("Email Address");
            emailField.setPrefWidth(360);
            GuiUtils.styleInput(emailField);

            passwordField = new PasswordField();
            passwordField.setPromptText("Password");
            passwordField.setPrefWidth(360);
            GuiUtils.styleInput(passwordField);

            HBox optionsRow = new HBox(10);
            optionsRow.setAlignment(Pos.CENTER_LEFT);
            CheckBox rememberCheck = new CheckBox("Remember Me");
            Hyperlink forgot = new Hyperlink("Forgot Password?");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            optionsRow.getChildren().addAll(rememberCheck, spacer, forgot);

            Button loginButton = new Button("Login");
            GuiUtils.stylePrimary(loginButton);
            loginButton.setPrefWidth(360);
            // keep default height from GuiUtils styling

            loginButton.setOnAction(e -> { e.consume(); handleLogin(primaryStage); });

            VBox helperLinks = new VBox(6);
            helperLinks.getStyleClass().add("helper-links");
            helperLinks.setAlignment(Pos.CENTER);
            HBox loginAsRow = new HBox(6);
            loginAsRow.setAlignment(Pos.CENTER);
            Label loginAsLabel = new Label("Login as");
            loginAsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333;");
            Hyperlink adminLink = new Hyperlink("Admin");
            loginAsRow.getChildren().addAll(loginAsLabel, adminLink);
            HBox signupRow = new HBox(6);
            signupRow.setAlignment(Pos.CENTER);
            Label dontLabel = new Label("Don't have an account?");
            Hyperlink signLink = new Hyperlink("Sign in");
            signupRow.getChildren().addAll(dontLabel, signLink);
            helperLinks.getChildren().addAll(loginAsRow, signupRow);

            formBox.getChildren().addAll(emailField, passwordField, optionsRow, loginButton, helperLinks);

            VBox combined = new VBox(16);
            combined.setAlignment(Pos.TOP_CENTER);
            combined.getChildren().addAll(titleRow, formBox);

            // center vertically: add top and bottom spacers so combined sits centered
            Region topSpacer = new Region();
            Region bottomSpacer = new Region();
            VBox.setVgrow(topSpacer, Priority.ALWAYS);
            VBox.setVgrow(bottomSpacer, Priority.ALWAYS);
            rightPanel.getChildren().addAll(topSpacer, combined, bottomSpacer);

            mainPanel.getChildren().add(rightPanel);
            root.setCenter(mainPanel);

            Scene scene = new Scene(root);
            // load CSS if available for polished styling
            try {
                java.net.URL css = getClass().getResource("/login.css");
                if (css != null) scene.getStylesheets().add(css.toExternalForm());
            } catch (Exception ignored) {
                // ignore if CSS missing
            }
            primaryStage.setScene(scene);

            // maximize the window to closely match the full-screen screenshot
            primaryStage.setMaximized(true);
            primaryStage.setResizable(true);
            primaryStage.show();
        }
    }

    private void handleLogin(Stage stage) {
        String username = emailField.getText().trim();
        String password = passwordField.getText();
        User user = authService.login(username, password);
        if (user != null) {
            SessionManager.setCurrentUser(user);
            showAlert("Login successful!", Alert.AlertType.INFORMATION);
            // Open DashboardForm using the same stage (replace scene) to avoid flicker
            try {
                new DashboardForm().start(stage);
            } catch (Exception ex) {
                Logger.error("Failed to open DashboardForm", ex);
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
