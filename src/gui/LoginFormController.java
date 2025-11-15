package gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import models.User;
import services.AuthService;
import utils.SessionManager;
import utils.Logger;

public class LoginFormController {
    @SuppressWarnings("unused") @FXML private TextField emailField;
    @SuppressWarnings("unused") @FXML private PasswordField passwordField;
    @SuppressWarnings("unused") @FXML private ImageView largeLogoView;
    @SuppressWarnings("unused") @FXML private ImageView smallLogoView;
    @SuppressWarnings("unused") @FXML private Button loginButton;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/assets/IMS-Logo.jpg");
            if (is == null) is = getClass().getResourceAsStream("/assets/IMS-Logo.png");
            if (is != null) {
                Image img = new Image(is);
                if (largeLogoView != null) largeLogoView.setImage(img);
                if (smallLogoView != null) smallLogoView.setImage(img);
            }
        } catch (Exception ignored) {}
    }

    @FXML
    private void onLogin() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        String username = emailField.getText().trim();
        String password = passwordField.getText();
        User user = authService.login(username, password);
        if (user != null) {
            SessionManager.setCurrentUser(user);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Login successful!", ButtonType.OK);
            alert.showAndWait();
            // Use centralized navigator to show dashboard (reuses primary stage)
            AppNavigator.showDashboard();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid username or password.", ButtonType.OK);
            alert.showAndWait();
        }
    }
}
