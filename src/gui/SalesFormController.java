package gui;

import dao.ProductDAO;
import dao.SaleDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import models.Product;
import models.Sale;
import utils.Logger;
import utils.SessionManager;
import javafx.stage.FileChooser;

import java.util.List;

public class SalesFormController {
    @SuppressWarnings("unused") @FXML private VBox mainPanel;
    @SuppressWarnings("unused") @FXML private VBox sidebarPlaceholder;

    @FXML private ComboBox<String> productCombo;
    @FXML private Spinner<Integer> qtySpinner;
    @FXML private Label unitPriceLabel;
    @FXML private Label totalPriceLabel;
    @FXML private Button recordBtn;
    @FXML private Button refreshBtn;
    @FXML private Button exportExcelBtn;

    @FXML private TableView<Sale> historyTable;
    @FXML private TableColumn<Sale,Integer> colId;
    @FXML private TableColumn<Sale,String> colProduct;
    @FXML private TableColumn<Sale,Integer> colQty;
    @FXML private TableColumn<Sale,Double> colUnit;
    @FXML private TableColumn<Sale,Double> colTotal;
    @FXML private TableColumn<Sale,String> colDate;
    @FXML private TableColumn<Sale, Void> colPrint;
    @FXML private TableColumn<Sale, Void> colEdit;
    @FXML private TableColumn<Sale, Void> colDelete;

    @SuppressWarnings("unused") private final ProductDAO productDAO = new ProductDAO();
    @SuppressWarnings("unused") private final SaleDAO saleDAO = new SaleDAO();

    @FXML
    public void initialize() {
        sidebarPlaceholder.getChildren().clear();
        sidebarPlaceholder.getChildren().add(NavigationPanel.createSidebar(AppNavigator.getPrimaryStage(), "Sales"));

        // setup spinner
        qtySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100000, 1));

        // populate product combo
        try {
            List<Product> products = productDAO.getAllProducts();
            for (Product p : products) {
                productCombo.getItems().add(p.getProductId() + " - " + p.getProductName());
            }
            if (!productCombo.getItems().isEmpty()) productCombo.getSelectionModel().select(0);
        } catch (Exception ex) {
            Logger.error("Failed to load products for sales combo", ex);
        }

        productCombo.setOnAction(e -> updatePrice());
        qtySpinner.valueProperty().addListener((obs, oldV, newV) -> updatePrice());

        recordBtn.setOnAction(e -> recordSale());
        refreshBtn.setOnAction(e -> updatePrice());
        exportExcelBtn.setOnAction(e -> exportHistoryExcel());

        // setup history table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("saleId"));
        colProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("saleDate"));

        loadHistory();
    }

    private void updatePrice() {
        try {
            String val = productCombo.getValue();
            if (val == null) {
                unitPriceLabel.setText("0.00");
                totalPriceLabel.setText("0.00");
                return;
            }
            int productId = Integer.parseInt(val.split(" - ")[0]);
            Product p = productDAO.getProductById(productId);
            if (p == null) return;
            double price = p.getSellingPrice();
            int qty = qtySpinner.getValue();
            unitPriceLabel.setText(String.format("%.2f", price));
            totalPriceLabel.setText(String.format("%.2f", price * qty));
        } catch (Exception ex) {
            Logger.error("Failed to update price", ex);
        }
    }

    private void recordSale() {
        try {
            String val = productCombo.getValue();
            if (val == null) { showAlert("Select a product", Alert.AlertType.WARNING); return; }
            int productId = Integer.parseInt(val.split(" - ")[0]);
            Product p = productDAO.getProductById(productId);
            int qty = qtySpinner.getValue();
            if (p == null) { showAlert("Invalid product", Alert.AlertType.ERROR); return; }

            Sale sale = new Sale(productId, p.getProductName(), qty, p.getSellingPrice(),
                    SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getUserId() : 0);
            int id = saleDAO.createSaleWithStockCheck(sale);
            if (id == -1) {
                showAlert("Insufficient stock or invalid input.", Alert.AlertType.ERROR);
                return;
            }
            showAlert("Sale recorded (ID: " + id + ")", Alert.AlertType.INFORMATION);
            loadHistory();
        } catch (Exception ex) {
            Logger.error("Failed to record sale", ex);
            showAlert(ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadHistory() {
        try {
            historyTable.getItems().clear();
            historyTable.getItems().addAll(saleDAO.getAllSales());
        } catch (Exception ex) {
            Logger.error("Failed to load sales history", ex);
        }
    }

    private void exportHistoryExcel() {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName("sales_history.xls");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xls", "*.xlsx"));
            java.io.File file = chooser.showSaveDialog(historyTable.getScene().getWindow());
            if (file != null) {
                utils.ExcelExportUtils.exportSalesToExcel(saleDAO.getAllSales(), file);
                showAlert("Exported successfully.", Alert.AlertType.INFORMATION);
            }
        } catch (Exception ex) {
            Logger.error("Failed to export history", ex);
            showAlert("Export failed: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Info");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
