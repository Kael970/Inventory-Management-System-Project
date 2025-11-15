package gui;

import dao.ProductDAO;
import models.Product;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;
import utils.Logger;

import java.io.File;
import java.util.List;

public class InventoryFormController {
    @SuppressWarnings("unused") @FXML private VBox mainPanel;
    @SuppressWarnings("unused") @FXML private VBox sidebarPlaceholder;

    @SuppressWarnings("unused") @FXML private TextField searchField;
    @SuppressWarnings("unused") @FXML private Button searchBtn;
    @SuppressWarnings("unused") @FXML private Button addButton;
    @SuppressWarnings("unused") @FXML private Button downloadButton;
    @SuppressWarnings("unused") @FXML private TableView<Product> productTable;
    @SuppressWarnings("unused") @FXML private TableColumn<Product,Integer> colId;
    @SuppressWarnings("unused") @FXML private TableColumn<Product,String> colName;
    @SuppressWarnings("unused") @FXML private TableColumn<Product,Double> colBuying;
    @SuppressWarnings("unused") @FXML private TableColumn<Product,Double> colSelling;
    @SuppressWarnings("unused") @FXML private TableColumn<Product,Integer> colStock;
    @SuppressWarnings("unused") @FXML private TableColumn<Product,Integer> colThreshold;
    @SuppressWarnings("unused") @FXML private Button editButton;
    @SuppressWarnings("unused") @FXML private Button deleteButton;

    private final ProductDAO productDAO = new ProductDAO();

    @FXML
    public void initialize() {
        sidebarPlaceholder.getChildren().clear();
        sidebarPlaceholder.getChildren().add(NavigationPanel.createSidebar(AppNavigator.getPrimaryStage(), "Items"));

        // wire table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colBuying.setCellValueFactory(new PropertyValueFactory<>("buyingPrice"));
        colSelling.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        colThreshold.setCellValueFactory(new PropertyValueFactory<>("thresholdValue"));

        // wire buttons
        searchBtn.setOnAction(this::onSearch);
        addButton.setOnAction(this::onAdd);
        downloadButton.setOnAction(this::onDownload);
        editButton.setOnAction(this::onEdit);
        deleteButton.setOnAction(this::onDelete);

        loadProducts();
    }

    // Action handlers to avoid 'parameter not used' inspection warnings
    private void onSearch(ActionEvent e) { loadProducts(searchField.getText().trim()); }
    private void onAdd(ActionEvent e) { openAddDialog(); }
    private void onDownload(ActionEvent e) { exportAllProducts(); }
    private void onEdit(ActionEvent e) { editSelectedAction(); }
    private void onDelete(ActionEvent e) { deleteSelectedProduct(); }

    // Inline Add Product dialog (moved from InventoryForm to avoid cross-class access)
    private void openAddDialog() {
        if (!utils.SessionManager.isAdmin()) {
            showAlert("Only Admin can add products.", Alert.AlertType.WARNING);
            return;
        }
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Add New Product");
        dialog.setHeaderText("Fill in the product details");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));
        TextField nameField = new TextField(); GuiUtils.styleInput(nameField); nameField.setPromptText("Product Name");
        TextField buyingField = new TextField(); GuiUtils.styleInput(buyingField); buyingField.setPromptText("Buying Price");
        TextField sellingField = new TextField(); GuiUtils.styleInput(sellingField); sellingField.setPromptText("Selling Price");
        TextField qtyField = new TextField(); GuiUtils.styleInput(qtyField); qtyField.setPromptText("Quantity");
        TextField thresholdField = new TextField(); GuiUtils.styleInput(thresholdField); thresholdField.setPromptText("Threshold");
        TextField expiryField = new TextField(); GuiUtils.styleInput(expiryField); expiryField.setPromptText("Expiry Date (YYYY-MM-DD)");
        grid.add(new Label("Product Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Buying Price:"), 0, 1); grid.add(buyingField, 1, 1);
        grid.add(new Label("Selling Price:"), 0, 2); grid.add(sellingField, 1, 2);
        grid.add(new Label("Quantity:"), 0, 3); grid.add(qtyField, 1, 3);
        grid.add(new Label("Threshold:"), 0, 4); grid.add(thresholdField, 1, 4);
        grid.add(new Label("Expiry Date:"), 0, 5); grid.add(expiryField, 1, 5);
        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    return new Product(
                        nameField.getText(),
                        Double.parseDouble(buyingField.getText()),
                        Double.parseDouble(sellingField.getText()),
                        Integer.parseInt(qtyField.getText()),
                        Integer.parseInt(thresholdField.getText()),
                        expiryField.getText().isEmpty() ? null : java.sql.Date.valueOf(expiryField.getText())
                    );
                } catch (Exception ex) {
                    showAlert("Invalid input: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
            return null;
        });
        dialog.showAndWait().ifPresent(product -> {
            try {
                if (productDAO.createProduct(product)) {
                    showAlert("Product added successfully!", Alert.AlertType.INFORMATION);
                    loadProducts(null);
                } else {
                    showAlert("Failed to add product!", Alert.AlertType.ERROR);
                }
            } catch (Exception ex) {
                showAlert("Invalid input: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    // Inline Edit Product dialog
    private void openEditDialog(Product product) {
        if (!utils.SessionManager.isAdmin()) { showAlert("Only Admin can edit products.", Alert.AlertType.WARNING); return; }
        if (product == null) return;
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Edit Product"); dialog.setHeaderText("Edit the product details");
        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new javafx.geometry.Insets(20));
        TextField nameField = new TextField(product.getProductName()); GuiUtils.styleInput(nameField);
        TextField buyingField = new TextField(String.valueOf(product.getBuyingPrice())); GuiUtils.styleInput(buyingField);
        TextField sellingField = new TextField(String.valueOf(product.getSellingPrice())); GuiUtils.styleInput(sellingField);
        TextField qtyField = new TextField(String.valueOf(product.getStockQuantity())); GuiUtils.styleInput(qtyField);
        TextField thresholdField = new TextField(String.valueOf(product.getThresholdValue())); GuiUtils.styleInput(thresholdField);
        TextField expiryField = new TextField(product.getExpiryDate() != null ? product.getExpiryDate().toString() : ""); GuiUtils.styleInput(expiryField);
        grid.add(new Label("Product Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Buying Price:"), 0, 1); grid.add(buyingField, 1, 1);
        grid.add(new Label("Selling Price:"), 0, 2); grid.add(sellingField, 1, 2);
        grid.add(new Label("Quantity:"), 0, 3); grid.add(qtyField, 1, 3);
        grid.add(new Label("Threshold:"), 0, 4); grid.add(thresholdField, 1, 4);
        grid.add(new Label("Expiry Date:"), 0, 5); grid.add(expiryField, 1, 5);
        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    product.setProductName(nameField.getText());
                    product.setBuyingPrice(Double.parseDouble(buyingField.getText()));
                    product.setSellingPrice(Double.parseDouble(sellingField.getText()));
                    product.setStockQuantity(Integer.parseInt(qtyField.getText()));
                    product.setThresholdValue(Integer.parseInt(thresholdField.getText()));
                    product.setExpiryDate(expiryField.getText().isEmpty() ? null : java.sql.Date.valueOf(expiryField.getText()));
                    return product;
                } catch (Exception ex) {
                    showAlert("Invalid input: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
            return null;
        });
        dialog.showAndWait().ifPresent(updatedProduct -> {
            try {
                if (productDAO.updateProduct(updatedProduct)) {
                    showAlert("Product updated successfully!", Alert.AlertType.INFORMATION);
                    loadProducts(null);
                } else {
                    showAlert("Failed to update product!", Alert.AlertType.ERROR);
                }
            } catch (Exception ex) {
                showAlert("Invalid input: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void loadProducts() {
        loadProducts(null);
    }

    private void loadProducts(String search) {
        try {
            productTable.getItems().clear();
            List<Product> products = (search == null || search.isEmpty()) ? productDAO.getAllProducts() : productDAO.searchProducts(search);
            productTable.getItems().addAll(products);
        } catch (Exception ex) {
            Logger.error("Failed to load products", ex);
        }
    }

    private void exportAllProducts() {
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName("products.xls");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xls", "*.xlsx"));
        File file = chooser.showSaveDialog(productTable.getScene().getWindow());
        if (file != null) {
            try {
                utils.ExcelExportUtils.exportProductsToExcel(productDAO.getAllProducts(), ensureXlsExtension(file));
                showAlert("Exported successfully.", Alert.AlertType.INFORMATION);
            } catch (Exception ex) {
                showAlert("Export failed: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private File ensureXlsExtension(File file) {
        String ext = ".xls";
        if (!file.getName().toLowerCase().endsWith(ext)) {
            return new File(file.getParent(), file.getName() + ext);
        }
        return file;
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void editSelectedAction() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openEditDialog(selected);
        } else {
            showAlert("Select a product and ensure you are Admin.", Alert.AlertType.WARNING);
        }
    }

    private void deleteSelectedProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Select a product to delete.", Alert.AlertType.WARNING); return; }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Are you sure you want to delete this product?");
        alert.setContentText(selected.getProductName());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (productDAO.deleteProduct(selected.getProductId())) {
                        showAlert("Product deleted successfully!", Alert.AlertType.INFORMATION);
                        loadProducts(null);
                    } else {
                        showAlert("Failed to delete product!", Alert.AlertType.ERROR);
                    }
                } catch (Exception ex) {
                    showAlert("Error deleting product: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }
}
