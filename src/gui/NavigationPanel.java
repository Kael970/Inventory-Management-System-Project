// JavaFX version of NavigationPanel
package gui;

import utils.SessionManager;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;

public class NavigationPanel {
    public static VBox createSidebar(Stage owner, String active) {
        VBox sidebar = new VBox(20);
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: #f0f8ff;");
        sidebar.setPadding(new Insets(20));
        sidebar.setAlignment(Pos.TOP_LEFT);

        Label logoLabel = new Label("IMS");
        logoLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a365d;");
        Label logoText = new Label("Inventory System");
        logoText.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");
        sidebar.getChildren().addAll(logoLabel, logoText);

        Button dashboardBtn = makeMenuButton("Dashboard", "Dashboard".equalsIgnoreCase(active));
        Button itemsBtn = makeMenuButton("Items", "Items".equalsIgnoreCase(active));
        Button requestBtn = makeMenuButton("Request", "Request".equalsIgnoreCase(active));
        Button salesBtn = makeMenuButton("Sales", "Sales".equalsIgnoreCase(active));
        Button usersBtn = makeMenuButton("Users", "Users".equalsIgnoreCase(active));
        Button reportsBtn = makeMenuButton("Reports", "Reports".equalsIgnoreCase(active));
        Button logoutBtn = makeMenuButton("Logout", false);
        GuiUtils.styleDanger(logoutBtn);

        sidebar.getChildren().addAll(dashboardBtn, itemsBtn, requestBtn, salesBtn);
        if (SessionManager.isAdmin()) {
            sidebar.getChildren().add(usersBtn);
            sidebar.getChildren().add(reportsBtn);
        }
        sidebar.getChildren().addAll(logoutBtn);

        dashboardBtn.setOnAction(e -> openForm(owner, "Dashboard"));
        itemsBtn.setOnAction(e -> openForm(owner, "Items"));
        requestBtn.setOnAction(e -> openForm(owner, "Request"));
        salesBtn.setOnAction(e -> openForm(owner, "Sales"));
        usersBtn.setOnAction(e -> openForm(owner, "Users"));
        reportsBtn.setOnAction(e -> openForm(owner, "Reports"));
        logoutBtn.setOnAction(e -> {
            SessionManager.clearSession();
            owner.close();
            new gui.LoginForm().start(new Stage());
        });

        return sidebar;
    }

    private static Button makeMenuButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setPrefWidth(160);
        btn.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        if (active) {
            GuiUtils.stylePrimary(btn);
        } else {
            GuiUtils.styleSecondary(btn);
            btn.setStyle(btn.getStyle() + " -fx-text-fill: #333;");
        }
        return btn;
    }

    private static void openForm(Stage owner, String form) {
        owner.close();
        switch (form) {
            case "Dashboard": new DashboardForm().start(new Stage()); break;
            case "Items": new InventoryForm().start(new Stage()); break;
            case "Request": new RequestForm().start(new Stage()); break;
            case "Sales": new SalesForm().start(new Stage()); break;
            case "Users": new UserManagementForm().start(new Stage()); break;
            case "Reports": new ReportsForm().start(new Stage()); break;
        }
    }
}
