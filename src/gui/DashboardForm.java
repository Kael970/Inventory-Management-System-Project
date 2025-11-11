// JavaFX version of DashboardForm
package gui;

import dao.*;
import models.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

import java.util.List;
import java.util.Map;

public class DashboardForm extends Application {
    private ProductDAO productDAO;
    private SaleDAO saleDAO;
    private RequestDAO requestDAO;
    private VBox mainPanel;

    @Override
    public void start(Stage primaryStage) {
        productDAO = new ProductDAO();
        saleDAO = new SaleDAO();
        requestDAO = new RequestDAO();

        primaryStage.setTitle("IMS - Dashboard");
        BorderPane container = new BorderPane();
        container.setPadding(new Insets(10));

        // Sidebar (removed top header because the navigation drawer provides the title)
        VBox sidebar = NavigationPanel.createSidebar(primaryStage, "Dashboard");
        container.setLeft(sidebar);

        // Main Content Area
        mainPanel = new VBox(20);
        mainPanel.setPadding(new Insets(20));
        mainPanel.setStyle("-fx-background-color: white;");
        ScrollPane scrollPane = new ScrollPane(mainPanel);
        scrollPane.setFitToWidth(true);
        container.setCenter(scrollPane);

        loadDashboardData();
        showLowStockAlert();

        Scene scene = new Scene(container, 1200, 700);
        primaryStage.setScene(scene);
        // make window occupy the primary screen and be maximized for consistent sizing
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private void showLowStockAlert() {
        List<Product> low = productDAO.getLowStockProducts();
        if (!low.isEmpty()) {
            StringBuilder sb = new StringBuilder("Low stock items (" + low.size() + "):\n");
            low.stream().limit(5).forEach(p -> sb.append("- ").append(p.getProductName()).append(" (").append(p.getStockQuantity()).append(")\n"));
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Low Stock Alert");
            alert.setHeaderText(null);
            alert.setContentText(sb.toString());
            alert.showAndWait();
        }
    }

    private void loadDashboardData() {
        mainPanel.getChildren().clear();
        mainPanel.getChildren().add(createBestSellingPanel());
        mainPanel.getChildren().add(createStatsPanel());
        mainPanel.getChildren().add(createInventoryPanel());
    }

    private VBox createBestSellingPanel() {
        VBox panel = new VBox(10);
        panel.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-width: 1; -fx-border-radius: 5;");
        Label titleLabel = new Label("Best selling items");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        panel.getChildren().add(titleLabel);

        TableView<Product> table = new TableView<>();
        ObservableList<Product> products = FXCollections.observableArrayList(productDAO.getAllProducts());
        TableColumn<Product, String> nameCol = new TableColumn<>("Product");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        TableColumn<Product, Integer> idCol = new TableColumn<>("Product ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        TableColumn<Product, Integer> qtyCol = new TableColumn<>("Remaining Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        TableColumn<Product, String> availCol = new TableColumn<>("Availability");
        availCol.setCellValueFactory(new PropertyValueFactory<>("availabilityStatus"));
        table.getColumns().addAll(List.of(nameCol, idCol, qtyCol, priceCol, availCol));
        table.setItems(products);
        table.setPrefHeight(180);
        panel.getChildren().add(table);
        return panel;
    }

    private HBox createStatsPanel() {
        HBox panel = new HBox(20);
        panel.setStyle("-fx-background-color: white;");
        panel.setPrefHeight(150);
        int salesCount = saleDAO.getLast7DaysSalesCount();
        int outOfStock = productDAO.getOutOfStockCount();
        int requestedItems = requestDAO.getRequestsCount();
        panel.getChildren().add(createStatCard("Sales", String.valueOf(salesCount), Color.web("#64b5f6"), true));
        panel.getChildren().add(createStatCard("Out of stock", String.valueOf(outOfStock), Color.WHITE, false));
        panel.getChildren().add(createStatCard("Requested items", String.valueOf(requestedItems), Color.WHITE, false));
        return panel;
    }

    private VBox createStatCard(String title, String value, Color bgColor, boolean isPrimary) {
        VBox card = new VBox(5);
        card.setPrefSize(280, 120);
        // use the provided bgColor (converted to hex) for background
        card.setStyle("-fx-background-color: " + toHex(bgColor) + "; -fx-border-color: #e6e6e6; -fx-border-width: 1;");
        Label titleLabel = new Label(title);
        // choose text color based on isPrimary flag to keep contrast
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + (isPrimary ? "white" : "#333"));
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + (isPrimary ? "white" : "#333"));
        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private VBox createInventoryPanel() {
        VBox panel = new VBox(10);
        panel.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-width: 1; -fx-border-radius: 5;");
        Label titleLabel = new Label("Overall Inventory");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        panel.getChildren().add(titleLabel);

        HBox statsPanel = new HBox(80);
        statsPanel.setStyle("-fx-background-color: white;");
        List<Product> products = productDAO.getAllProducts();
        int totalProducts = products.size();
        int lowStocks = 0;
        for (Product p : products) {
            if (p.isLowStock()) lowStocks++;
        }
        Map<String, Integer> top = saleDAO.getTopSellingProducts(1, 30);
        int topSelling = top.values().stream().findFirst().orElse(0);
        statsPanel.getChildren().add(createInventoryStat("Sales (7d)", String.valueOf(saleDAO.getLast7DaysSalesCount()), "Last 7 days", Color.web("#007bff")));
        statsPanel.getChildren().add(createInventoryStat("Total Products", String.valueOf(totalProducts), "All items", Color.web("#ff9800")));
        statsPanel.getChildren().add(createInventoryStat("Top Selling (30d)", String.valueOf(topSelling), "Qty sold", Color.web("#4caf50")));
        statsPanel.getChildren().add(createInventoryStat("Low Stocks", String.valueOf(lowStocks), "At/below threshold", Color.web("#f44336")));
        panel.getChildren().add(statsPanel);
        return panel;
    }

    private VBox createInventoryStat(String label, String value, String subtitle, Color color) {
        VBox panel = new VBox(5);
        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + toHex(color) + ";");
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");
        panel.getChildren().addAll(labelLabel, valueLabel, subtitleLabel);
        return panel;
    }

    private String toHex(Color color) {
        return String.format("#%02x%02x%02x", (int)(color.getRed()*255), (int)(color.getGreen()*255), (int)(color.getBlue()*255));
    }

    static void main(String[] args) {
        launch(args);
    }
}
