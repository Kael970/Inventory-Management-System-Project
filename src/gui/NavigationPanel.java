// JavaFX version of NavigationPanel
package gui;

import utils.SessionManager;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.InputStream;

public class NavigationPanel {
    public static VBox createSidebar(Stage owner, String active) {
        VBox sidebar = new VBox(20);
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: #f0f8ff;");
        sidebar.setPadding(new Insets(20));
        sidebar.setAlignment(Pos.TOP_LEFT);

        // Try to load the project's logo from assets and show it at the top of the sidebar
        ImageView logoView = new ImageView();
        // use GuiUtils to set consistent logo sizing
        gui.GuiUtils.styleSidebarLogo(logoView);
        try {
            InputStream is = NavigationPanel.class.getResourceAsStream("/assets/IMS-Logo.jpg");
            if (is == null) is = NavigationPanel.class.getResourceAsStream("/assets/IMS-Logo.png");
            if (is != null) {
                Image img = new Image(is);
                logoView.setImage(img);
            }
        } catch (Exception ignored) {}

        Label logoText = new Label("");
        gui.GuiUtils.styleSidebarSubtitle(logoText);
        // make subtitle slightly bolder, larger and allow wrapping so it doesn't truncate
        logoText.setStyle(logoText.getStyle() + " -fx-font-weight: bold; -fx-font-size: 13px;");
        logoText.setWrapText(true);
        logoText.setMaxWidth(160);

        if (logoView.getImage() != null) {
            VBox logoBox = new VBox(6, logoView, logoText);
            logoBox.setAlignment(Pos.CENTER_LEFT);
            logoBox.setPadding(new Insets(0, 0, 12, 0));
            // Make the logo clickable to go to Dashboard for quicker navigation
            logoBox.setOnMouseClicked(e -> { e.consume(); openForm(owner, "Dashboard"); });
            sidebar.getChildren().add(logoBox);
        } else {
            Label logoLabel = new Label("IMS");
            logoLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a365d;");
            sidebar.getChildren().addAll(logoLabel, logoText);
        }

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

        dashboardBtn.setOnAction(e -> { e.consume(); openForm(owner, "Dashboard"); });
        itemsBtn.setOnAction(e -> { e.consume(); openForm(owner, "Items"); });
        requestBtn.setOnAction(e -> { e.consume(); openForm(owner, "Request"); });
        salesBtn.setOnAction(e -> { e.consume(); openForm(owner, "Sales"); });
        usersBtn.setOnAction(e -> { e.consume(); openForm(owner, "Users"); });
        reportsBtn.setOnAction(e -> { e.consume(); openForm(owner, "Reports"); });
        logoutBtn.setOnAction(e -> {
            e.consume();
            SessionManager.clearSession();
            owner.close();
            new gui.LoginForm().start(new Stage());
        });

        return sidebar;
    }

    private static Button makeMenuButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setPrefWidth(180);
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
