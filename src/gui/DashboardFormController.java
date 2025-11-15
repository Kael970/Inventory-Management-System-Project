package gui;

import dao.ProductDAO;
import dao.SaleDAO;
import dao.RequestDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import models.Product;
import java.util.List;
import java.util.Map;
import utils.Logger;

public class DashboardFormController {
    @SuppressWarnings("unused") @FXML private VBox mainPanel;
    @SuppressWarnings("unused") @FXML private VBox sidebarPlaceholder;
    @SuppressWarnings("unused") @FXML private ScrollPane centerScroll;

    @FXML private TableView<Product> bestSellingTable;
    @FXML private TableColumn<Product, String> nameCol;
    @FXML private TableColumn<Product, Integer> idCol;
    @FXML private TableColumn<Product, Integer> qtyCol;
    @FXML private TableColumn<Product, Double> priceCol;
    @FXML private TableColumn<Product, String> availCol;

    @FXML private Label salesCountLabel;
    @FXML private Label outOfStockLabel;
    @FXML private Label requestedItemsLabel;
    @FXML private Label sales7dLabel;
    @FXML private Label totalProductsLabel;
    @FXML private Label topSellingLabel;
    @FXML private Label lowStocksLabel;

    private final ProductDAO productDAO = new ProductDAO();
    private final SaleDAO saleDAO = new SaleDAO();
    private final RequestDAO requestDAO = new RequestDAO();

    @FXML
    public void initialize() {
        // create sidebar and add it to the placeholder
        try {
            sidebarPlaceholder.getChildren().clear();
            sidebarPlaceholder.getChildren().add(NavigationPanel.createSidebar(AppNavigator.getPrimaryStage(), "Dashboard"));
        } catch (Exception ex) {
            Logger.error("Failed to initialize sidebar in DashboardFormController", ex);
        }

        // configure table columns
        nameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        idCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        availCol.setCellValueFactory(new PropertyValueFactory<>("availabilityStatus"));

        loadDashboardData();
    }

    private void loadDashboardData() {
        // Best selling table
        List<Product> products = productDAO.getAllProducts();
        bestSellingTable.getItems().clear();
        bestSellingTable.getItems().addAll(products);

        // Stats
        int salesCount = saleDAO.getLast7DaysSalesCount();
        int outOfStock = productDAO.getOutOfStockCount();
        int requestedItems = requestDAO.getRequestsCount();
        salesCountLabel.setText(String.valueOf(salesCount));
        outOfStockLabel.setText(String.valueOf(outOfStock));
        requestedItemsLabel.setText(String.valueOf(requestedItems));

        // Inventory summary
        int totalProducts = products.size();
        int lowStocks = 0;
        for (Product p : products) if (p.isLowStock()) lowStocks++;
        Map<String, Integer> top = saleDAO.getTopSellingProducts(1, 30);
        String topSellingTxt = "-";
        if (top != null && !top.isEmpty()) {
            Map.Entry<String,Integer> e = top.entrySet().iterator().next();
            topSellingTxt = e.getKey() + " (" + e.getValue() + ")";
        }
        sales7dLabel.setText(String.valueOf(saleDAO.getLast7DaysSalesCount()));
        totalProductsLabel.setText(String.valueOf(totalProducts));
        topSellingLabel.setText(topSellingTxt);
        lowStocksLabel.setText(String.valueOf(lowStocks));
    }

    // keep other helper methods for consistency (unused by FXML path)
    private VBox createStatCard(String title, String value, Color bgColor, boolean isPrimary) { return new VBox(); }
    private VBox createInventoryStat(String label, String value, String subtitle, Color color) { return new VBox(); }
    private String toHex(Color color) { return "#000"; }
}
