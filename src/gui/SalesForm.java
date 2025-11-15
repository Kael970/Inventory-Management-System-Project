// JavaFX version of SalesForm
package gui;

import dao.ProductDAO;
import dao.SaleDAO;
import models.Product;
import models.Sale;
import utils.ExportUtils;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.fxml.FXMLLoader;
import utils.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class SalesForm extends Application {
    private final ProductDAO productDAO = new ProductDAO();
    private final SaleDAO saleDAO = new SaleDAO();

    private ComboBox<String> productCombo;
    private Spinner<Integer> qtySpinner;
    private Label unitPriceLabel;
    private Label totalPriceLabel;
    private TableView<Sale> historyTable;
    private ObservableList<Sale> historyData;

    @Override
    public void start(Stage primaryStage) {
        AppNavigator.init(primaryStage);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/SalesForm.fxml"));
            BorderPane root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();
            return;
        } catch (Exception ex) {
            Logger.error("Failed to load SalesForm.fxml, falling back to programmatic UI.", ex);
        }

        primaryStage.setTitle("IMS - Sales");
        BorderPane container = new BorderPane();
        container.setPadding(new Insets(10));

        VBox center = new VBox(10);
        center.setPadding(new Insets(12));
        center.setAlignment(Pos.TOP_LEFT);

        VBox sidebar = NavigationPanel.createSidebar(primaryStage, "Sales");
        container.setLeft(sidebar);

        // Record panel
        GridPane record = new GridPane();
        record.setHgap(8);
        record.setVgap(8);
        record.setPadding(new Insets(12));
        // use card styling helper
        GuiUtils.styleCard(record);

        record.add(new Label("Product:"), 0, 0);
        productCombo = new ComboBox<>();
        for (Product p : productDAO.getAllProducts()) {
            productCombo.getItems().add(p.getProductId() + " - " + p.getProductName());
        }
        if (!productCombo.getItems().isEmpty()) {
            productCombo.getSelectionModel().select(0);
        }
        GuiUtils.styleComboBox(productCombo);
        record.add(productCombo, 1, 0);

        record.add(new Label("Quantity:"), 0, 1);
        qtySpinner = new Spinner<>(1, 100000, 1);
        qtySpinner.setPrefHeight(36);
        record.add(qtySpinner, 1, 1);

        record.add(new Label("Unit Price:"), 0, 2);
        unitPriceLabel = new Label("0.00");
        unitPriceLabel.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");
        record.add(unitPriceLabel, 1, 2);

        record.add(new Label("Total:"), 0, 3);
        totalPriceLabel = new Label("0.00");
        totalPriceLabel.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");
        record.add(totalPriceLabel, 1, 3);

        Button submit = new Button("Record & Print Receipt");
        submit.setOnAction(e -> recordSale());
        GuiUtils.stylePrimary(submit);
        record.add(submit, 0, 4, 2, 1);

        Button refresh = new Button("Refresh Prices");
        refresh.setOnAction(e -> updatePrice());
        GuiUtils.styleSecondary(refresh);
        record.add(refresh, 0, 5, 2, 1);

        Button exportExcelBtn = new Button("Export Excel History");
        exportExcelBtn.setOnAction(e -> exportHistoryExcel());
        GuiUtils.styleSecondary(exportExcelBtn);
        center.getChildren().add(exportExcelBtn);
        center.getChildren().add(record);

        // History table
        historyTable = new TableView<>();
        historyData = FXCollections.observableArrayList();
        TableColumn<Sale, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("saleId"));
        TableColumn<Sale, String> prodCol = new TableColumn<>("Product");
        prodCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        TableColumn<Sale, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TableColumn<Sale, Double> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        TableColumn<Sale, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        TableColumn<Sale, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("saleDate"));
        TableColumn<Sale, Void> printCol = new TableColumn<>("Print Receipt");
        printCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Print Receipt");
            {
                btn.setOnAction(e -> {
                    Sale sale = getTableView().getItems().get(getIndex());
                    showReceiptDialog(buildReceiptText(sale.getSaleId(), sale));
                });
                GuiUtils.stylePrimary(btn);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
        // Admin-only Edit button
        TableColumn<Sale, Void> editCol = new TableColumn<>("Edit");
        editCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Edit");
            {
                GuiUtils.styleWarning(btn);
                btn.setOnAction(e -> {
                    Sale sale = getTableView().getItems().get(getIndex());
                    showEditSaleDialog(sale);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !utils.SessionManager.isAdmin()) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
        // Admin-only Delete button
        TableColumn<Sale, Void> deleteCol = new TableColumn<>("Delete");
        deleteCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Delete");
            {
                GuiUtils.styleDanger(btn);
                btn.setOnAction(e -> {
                    Sale sale = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirm Delete");
                    confirm.setHeaderText("Are you sure you want to delete this sale record?");
                    confirm.setContentText("Sale ID: " + sale.getSaleId());
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            if (saleDAO.deleteSale(sale.getSaleId())) {
                                showAlert("Sale deleted successfully!", Alert.AlertType.INFORMATION);
                                loadHistory();
                            } else {
                                showAlert("Failed to delete sale!", Alert.AlertType.ERROR);
                            }
                        }
                    });
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !utils.SessionManager.isAdmin()) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
        historyTable.getColumns().addAll(idCol, prodCol, qtyCol, unitCol, totalCol, dateCol, printCol, editCol, deleteCol);
        historyTable.setItems(historyData);
        historyTable.setPrefHeight(400);
        container.setCenter(historyTable);
        container.setRight(center);

        productCombo.setOnAction(e -> updatePrice());
        updatePrice();
        loadHistory();

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

    private void updatePrice() {
        try {
            if (productCombo.getValue() == null) {
                unitPriceLabel.setText("0.00");
                totalPriceLabel.setText("0.00");
                return;
            }
            int productId = Integer.parseInt(productCombo.getValue().split(" - ")[0]);
            Product p = productDAO.getProductById(productId);
            if (p != null) {
                double price = p.getSellingPrice();
                int qty = qtySpinner.getValue();
                unitPriceLabel.setText(String.format("%.2f", price));
                totalPriceLabel.setText(String.format("%.2f", price * qty));
            }
        } catch (Exception ignored) {}
    }

    private void recordSale() {
        try {
            int productId = Integer.parseInt(productCombo.getValue().split(" - ")[0]);
            Product p = productDAO.getProductById(productId);
            int qty = qtySpinner.getValue();
            if (p == null) return;

            Sale sale = new Sale(productId, p.getProductName(), qty, p.getSellingPrice(),
                    SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getUserId() : 0);
            int id = saleDAO.createSaleWithStockCheck(sale);
            if (id == -1) {
                showAlert("Insufficient stock or invalid input.", Alert.AlertType.ERROR);
                return;
            }
            showReceiptDialog(buildReceiptText(id, sale));
            loadHistory();
        } catch (Exception ex) {
            showAlert(ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showReceiptDialog(String receiptText) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Receipt");
        dialog.setHeaderText("Sale Receipt");
        TextArea area = new TextArea(receiptText);
        area.setEditable(false);
        area.setWrapText(true);
        dialog.getDialogPane().setContent(area);
        ButtonType savePdfBtn = new ButtonType("Save PDF", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(savePdfBtn, ButtonType.CLOSE);
        dialog.setResultConverter(btn -> {
            if (btn == savePdfBtn) {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Save PDF Receipt");
                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                File file = chooser.showSaveDialog(area.getScene().getWindow());
                if (file != null) {
                    try {
                        utils.PdfBoxExportUtils.exportTextToPDF(receiptText, file, "Sale Receipt");
                        showAlert("PDF saved successfully.", Alert.AlertType.INFORMATION);
                    } catch (Exception ex) {
                        showAlert("PDF export failed: " + ex.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    private String buildReceiptText(int id, Sale sale) {
        String user = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getFullName() : "Unknown";
        return new StringBuilder()
                .append("IMS Receipt\n")
                .append("----------------------\n")
                .append("Sale ID: ").append(id).append("\n")
                .append("Product: ").append(sale.getProductName()).append("\n")
                .append("Quantity: ").append(sale.getQuantity()).append("\n")
                .append("Unit Price: ").append(String.format("%.2f", sale.getUnitPrice())).append("\n")
                .append("Total: ").append(String.format("%.2f", sale.getTotalPrice())).append("\n")
                .append("Cashier: ").append(user).append("\n")
                .toString();
    }

    private void showAlert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Info");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void loadHistory() {
        historyData.clear();
        List<Sale> sales = saleDAO.getAllSales();
        historyData.addAll(sales);
    }

    private void exportHistoryExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName("sales_history.xls");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xls", "*.xlsx"));
        File file = chooser.showSaveDialog(historyTable.getScene().getWindow());
        if (file != null) {
            try {
                utils.ExcelExportUtils.exportSalesToExcel(new ArrayList<>(historyData), ensureExtension(file, ".xls"));
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

    // Admin-only sale edit dialog
    private void showEditSaleDialog(Sale sale) {
        if (!utils.SessionManager.isAdmin()) {
            showAlert("Only Admin can edit sales.", Alert.AlertType.WARNING);
            return;
        }
        Dialog<Sale> dialog = new Dialog<>();
        dialog.setTitle("Edit Sale");
        dialog.setHeaderText("Edit sale details");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        TextField qtyField = new TextField(String.valueOf(sale.getQuantity()));
        TextField priceField = new TextField(String.valueOf(sale.getUnitPrice()));
        grid.add(new Label("Quantity:"), 0, 0);
        grid.add(qtyField, 1, 0);
        grid.add(new Label("Unit Price:"), 0, 1);
        grid.add(priceField, 1, 1);
        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    int newQty = Integer.parseInt(qtyField.getText());
                    double newPrice = Double.parseDouble(priceField.getText());
                    sale.setQuantity(newQty);
                    sale.setUnitPrice(newPrice);
                    sale.setTotalPrice(newQty * newPrice);
                    if (saleDAO.updateSale(sale)) {
                        showAlert("Sale updated successfully!", Alert.AlertType.INFORMATION);
                        loadHistory();
                    } else {
                        showAlert("Failed to update sale!", Alert.AlertType.ERROR);
                    }
                } catch (Exception ex) {
                    showAlert("Invalid input: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
