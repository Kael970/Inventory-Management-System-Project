package gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.Logger;

public class AppNavigator {
    private static Stage primaryStage;

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void switchTo(Parent root) {
        if (primaryStage == null) return;
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void switchToFXML(String resourcePath) {
        try {
            FXMLLoader loader = new FXMLLoader(AppNavigator.class.getResource(resourcePath));
            Parent root = loader.load();
            switchTo(root);
        } catch (Exception ex) {
            Logger.error("Failed to load FXML: " + resourcePath, ex);
        }
    }

    public static void showLogin() {
        switchToFXML("/gui/LoginForm.fxml");
    }

    public static void showDashboard() {
        if (primaryStage == null) return;
        try {
            new DashboardForm().start(primaryStage);
        } catch (Exception ex) {
            Logger.error("Failed to show Dashboard", ex);
        }
    }

    public static void showInventory() {
        if (primaryStage == null) return;
        try {
            new InventoryForm().start(primaryStage);
        } catch (Exception ex) {
            Logger.error("Failed to show Inventory", ex);
        }
    }

    public static void showRequest() {
        if (primaryStage == null) return;
        try {
            new RequestForm().start(primaryStage);
        } catch (Exception ex) {
            Logger.error("Failed to show Request", ex);
        }
    }

    public static void showSales() {
        if (primaryStage == null) return;
        try {
            new SalesForm().start(primaryStage);
        } catch (Exception ex) {
            Logger.error("Failed to show Sales", ex);
        }
    }

    public static void showUsers() {
        if (primaryStage == null) return;
        try {
            new UserManagementForm().start(primaryStage);
        } catch (Exception ex) {
            Logger.error("Failed to show Users", ex);
        }
    }

    public static void showReports() {
        if (primaryStage == null) return;
        try {
            new ReportsForm().start(primaryStage);
        } catch (Exception ex) {
            Logger.error("Failed to show Reports", ex);
        }
    }
}
