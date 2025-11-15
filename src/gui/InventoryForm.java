// JavaFX version of InventoryForm
package gui;

import dao.ProductDAO;
import models.Product;
import utils.SessionManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.fxml.FXMLLoader;
import utils.Logger;

import java.io.File;
import java.util.List;

public class InventoryForm extends Application {
    private ProductDAO productDAO;
    private TableView<Product> productTable;
    private ObservableList<Product> tableData;
    private TextField searchField;

    @Override
    public void start(Stage primaryStage) {
        productDAO = new ProductDAO();
        AppNavigator.init(primaryStage);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/InventoryForm.fxml"));
            BorderPane root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();
            return;
        } catch (Exception ex) {
            Logger.error("Failed to load InventoryForm.fxml, falling back to programmatic UI.", ex);
        }

        // fallback to programmatic UI (existing code)...
        primaryStage.setTitle("IMS - Inventory");
        BorderPane container = new BorderPane();
        container.setPadding(new Insets(10));

        VBox sidebar = NavigationPanel.createSidebar(primaryStage, "Items");
        container.setLeft(sidebar);

        VBox mainPanel = new VBox(10);
        mainPanel.setPadding(new Insets(20));
        mainPanel.setStyle("-fx-background-color: white;");

        HBox headerPanel = new HBox(10);
        headerPanel.setAlignment(Pos.CENTER_LEFT);
        Label productsLabel = new Label("Products");
        GuiUtils.styleHeaderLabel(productsLabel);
        headerPanel.getChildren().add(productsLabel);

        // Search field on the left
        searchField = new TextField();
        searchField.setPromptText("Search by product name");
        searchField.setPrefWidth(300);
        GuiUtils.styleInput(searchField);
        headerPanel.getChildren().add(searchField);

        // spacer to push buttons to the right
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        headerPanel.getChildren().add(headerSpacer);

        // Right-side action buttons
        Button searchBtn = new Button("Search");
        searchBtn.setOnAction(this::searchAction);
        GuiUtils.styleSecondary(searchBtn);
        Button addButton = new Button("+ Add Product");
        addButton.setOnAction(this::addProductAction);
        GuiUtils.stylePrimary(addButton);
        Button downloadButton = new Button("Download all");
        downloadButton.setOnAction(this::downloadAction);
        GuiUtils.styleSecondary(downloadButton);
        // Add in order: Search, Add (admin only), Download (admin only)
        headerPanel.getChildren().add(searchBtn);
        if (SessionManager.isAdmin()) headerPanel.getChildren().add(addButton);
        if (SessionManager.isAdmin()) headerPanel.getChildren().add(downloadButton);

        // style header panel as a subtle card for visual grouping and add once
        GuiUtils.styleCard(headerPanel);
        headerPanel.setPadding(new Insets(12));
        mainPanel.getChildren().add(headerPanel);

        productTable = new TableView<>();
        tableData = FXCollections.observableArrayList();
        TableColumn<Product, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("productId"));
        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("productName"));
        TableColumn<Product, Double> buyCol = new TableColumn<>("Buying Price");
        buyCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("buyingPrice"));
        TableColumn<Product, Double> sellCol = new TableColumn<>("Selling Price");
        sellCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("sellingPrice"));
        TableColumn<Product, Integer> qtyCol = new TableColumn<>("Stock");
        qtyCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("stockQuantity"));
        TableColumn<Product, Integer> threshCol = new TableColumn<>("Threshold");
        threshCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("thresholdValue"));
        productTable.getColumns().addAll(java.util.List.of(idCol, nameCol, buyCol, sellCol, qtyCol, threshCol));
        productTable.setItems(tableData);
        productTable.setPrefHeight(500);
        productTable.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2 && SessionManager.isAdmin()) {
                    showEditProductDialog(row.getItem());
                }
            });
            return row;
        });
        // wrap table in a card container for consistent spacing
        VBox tableBox = new VBox(8, productTable);
        GuiUtils.styleCard(tableBox);
        tableBox.setPadding(new Insets(10));
        productTable.setPrefHeight(500);
        mainPanel.getChildren().add(tableBox);

        // Action toolbar placed directly below the table for clearer layout
        HBox actionBar = new HBox(10);
        actionBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        actionBar.setPadding(new Insets(8, 0, 0, 0));

        Button editButton = new Button("Edit Selected");
        editButton.setOnAction(this::editSelectedAction);
        GuiUtils.styleWarning(editButton);

        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(this::deleteSelectedProduct);
        GuiUtils.styleDanger(deleteButton);

        // Only show edit/delete buttons to admins and add them to the action bar
        if (SessionManager.isAdmin()) {
            actionBar.getChildren().addAll(editButton, deleteButton);
        }

        mainPanel.getChildren().add(actionBar);

        container.setCenter(mainPanel);
        loadProducts(null);

        Scene scene = new Scene(container, 1200, 700);
        primaryStage.setScene(scene);
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private void loadProducts(String search) {
        tableData.clear();
        List<Product> products = (search == null || search.isEmpty()) ? productDAO.getAllProducts() : productDAO.searchProducts(search);
        tableData.addAll(products);
    }

    public void showAddProductDialog() {
        if (!SessionManager.isAdmin()) {
            showAlert("Only Admin can add products.", Alert.AlertType.WARNING);
            return;
        }
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Add New Product");
        dialog.setHeaderText("Fill in the product details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Product Name");
        GuiUtils.styleInput(nameField);
        TextField buyingField = new TextField();
        buyingField.setPromptText("Buying Price");
        GuiUtils.styleInput(buyingField);
        TextField sellingField = new TextField();
        sellingField.setPromptText("Selling Price");
        GuiUtils.styleInput(sellingField);
        TextField qtyField = new TextField();
        qtyField.setPromptText("Quantity");
        GuiUtils.styleInput(qtyField);
        TextField thresholdField = new TextField();
        thresholdField.setPromptText("Threshold");
        GuiUtils.styleInput(thresholdField);
        TextField expiryField = new TextField();
        expiryField.setPromptText("Expiry Date (YYYY-MM-DD)");
        GuiUtils.styleInput(expiryField);

        grid.add(new Label("Product Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Buying Price:"), 0, 1);
        grid.add(buyingField, 1, 1);
        grid.add(new Label("Selling Price:"), 0, 2);
        grid.add(sellingField, 1, 2);
        grid.add(new Label("Quantity:"), 0, 3);
        grid.add(qtyField, 1, 3);
        grid.add(new Label("Threshold:"), 0, 4);
        grid.add(thresholdField, 1, 4);
        grid.add(new Label("Expiry Date:"), 0, 5);
        grid.add(expiryField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return new Product(
                    nameField.getText(),
                    Double.parseDouble(buyingField.getText()),
                    Double.parseDouble(sellingField.getText()),
                    Integer.parseInt(qtyField.getText()),
                    Integer.parseInt(thresholdField.getText()),
                    expiryField.getText().isEmpty() ? null : java.sql.Date.valueOf(expiryField.getText())
                );
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
            } catch (Exception e) {
                showAlert("Invalid input: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    public void showEditProductDialog(Product product) {
        if (!SessionManager.isAdmin()) {
            showAlert("Only Admin can edit products.", Alert.AlertType.WARNING);
            return;
        }
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Edit Product");
        dialog.setHeaderText("Edit the product details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(product.getProductName());
        nameField.setPromptText("Product Name");
        GuiUtils.styleInput(nameField);
        TextField buyingField = new TextField(String.valueOf(product.getBuyingPrice()));
        buyingField.setPromptText("Buying Price");
        GuiUtils.styleInput(buyingField);
        TextField sellingField = new TextField(String.valueOf(product.getSellingPrice()));
        sellingField.setPromptText("Selling Price");
        GuiUtils.styleInput(sellingField);
        TextField qtyField = new TextField(String.valueOf(product.getStockQuantity()));
        qtyField.setPromptText("Quantity");
        GuiUtils.styleInput(qtyField);
        TextField thresholdField = new TextField(String.valueOf(product.getThresholdValue()));
        thresholdField.setPromptText("Threshold");
        GuiUtils.styleInput(thresholdField);
        TextField expiryField = new TextField(product.getExpiryDate() != null ? product.getExpiryDate().toString() : "");
        expiryField.setPromptText("Expiry Date (YYYY-MM-DD)");
        GuiUtils.styleInput(expiryField);

        grid.add(new Label("Product Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Buying Price:"), 0, 1);
        grid.add(buyingField, 1, 1);
        grid.add(new Label("Selling Price:"), 0, 2);
        grid.add(sellingField, 1, 2);
        grid.add(new Label("Quantity:"), 0, 3);
        grid.add(qtyField, 1, 3);
        grid.add(new Label("Threshold:"), 0, 4);
        grid.add(thresholdField, 1, 4);
        grid.add(new Label("Expiry Date:"), 0, 5);
        grid.add(expiryField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                product.setProductName(nameField.getText());
                product.setBuyingPrice(Double.parseDouble(buyingField.getText()));
                product.setSellingPrice(Double.parseDouble(sellingField.getText()));
                product.setStockQuantity(Integer.parseInt(qtyField.getText()));
                product.setThresholdValue(Integer.parseInt(thresholdField.getText()));
                product.setExpiryDate(expiryField.getText().isEmpty() ? null : java.sql.Date.valueOf(expiryField.getText()));
                return product;
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
            } catch (Exception e) {
                showAlert("Invalid input: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void deleteSelectedProduct() {
        if (!SessionManager.isAdmin()) {
            showAlert("Only Admin can delete products.", Alert.AlertType.WARNING);
            return;
        }
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a product to delete.", Alert.AlertType.WARNING);
            return;
        }
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
                } catch (Exception e) {
                    showAlert("Error deleting product: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void exportAllProducts() {
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName("products.xls");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xls", "*.xlsx"));
        File file = chooser.showSaveDialog(productTable.getScene().getWindow());
        if (file != null) {
            try {
                utils.ExcelExportUtils.exportProductsToExcel(productDAO.getAllProducts(), ensureExtension(file, ".xls"));
                showAlert("Exported successfully.", Alert.AlertType.INFORMATION);
            } catch (Exception ex) {
                showAlert("Export failed: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private File ensureExtension(File file, String ext) {
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

    // Action handlers to avoid unused-parameter warnings in lambdas
    private void searchAction(javafx.event.ActionEvent e) { loadProducts(searchField.getText().trim()); }
    private void addProductAction(javafx.event.ActionEvent e) { showAddProductDialog(); }
    private void downloadAction(javafx.event.ActionEvent e) { exportAllProducts(); }
    private void editSelectedAction(javafx.event.ActionEvent e) {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null && SessionManager.isAdmin()) {
            showEditProductDialog(selected);
        } else {
            showAlert("Select a product and ensure you are Admin.", Alert.AlertType.WARNING);
        }
    }

    private void deleteSelectedProduct(javafx.event.ActionEvent e) {
        // delegate to previous implementation logic
        if (!SessionManager.isAdmin()) {
            showAlert("Only Admin can delete products.", Alert.AlertType.WARNING);
            return;
        }
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a product to delete.", Alert.AlertType.WARNING);
            return;
        }
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

    static void main(String[] args) {
        launch(args);
    }
}
