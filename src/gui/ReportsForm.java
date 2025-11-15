// JavaFX version of ReportsForm
package gui;

import dao.SaleDAO;
import models.Sale;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.time.LocalDate;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.io.File;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.fxml.FXMLLoader;
import utils.Logger;

public class ReportsForm extends Application {
    private final SaleDAO saleDAO = new SaleDAO();
    private TableView<Sale> reportTable;
    private ObservableList<Sale> reportData;
    private Label totalLabel;
    private Label totalItemsLabel; // NEW: show total items sold
    private Label topSellingLabel;
    private Label statusLabel; // shows when no data for selected range
    // make rangeCombo a field so action handlers can access its current value
    private ComboBox<String> rangeCombo;

    // Promote these summary card value labels to fields so other methods can update them
    private Label totalCardValueLabel;
    private Label itemsCardValueLabel;
    private Label topCardValueLabel;

    @SuppressWarnings("deprecation")
    @Override
    public void start(Stage primaryStage) {
        AppNavigator.init(primaryStage);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/ReportsForm.fxml"));
            BorderPane root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();
            return;
        } catch (Exception ex) {
            Logger.error("Failed to load ReportsForm.fxml, falling back to programmatic UI.", ex);
        }

        primaryStage.setTitle("IMS - Reports");
        BorderPane container = new BorderPane();
        container.setPadding(new Insets(10));

        VBox sidebar = NavigationPanel.createSidebar(primaryStage, "Reports");
        container.setLeft(sidebar);

        VBox mainPanel = new VBox(10);
        mainPanel.setPadding(new Insets(20));
        mainPanel.setStyle("-fx-background-color: white;");

        // Controls toolbar: left-aligned range selector, optional export buttons, and totals aligned to the right
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);
        Label rangeLabel = new Label("Range:");
        GuiUtils.styleHeaderLabel(rangeLabel);
        rangeCombo = new ComboBox<>(FXCollections.observableArrayList("Daily", "Weekly", "Monthly"));
        rangeCombo.setValue("Weekly");
        rangeCombo.setPrefWidth(120);
        // wire event to a dedicated handler to avoid casts/warnings
        rangeCombo.setOnAction(this::rangeComboAction);

        Button exportCsvBtn = new Button("Export CSV");
        exportCsvBtn.setOnAction(this::exportCSVAction);
        Button exportPdfBtn = new Button("Export PDF");
        exportPdfBtn.setOnAction(this::exportPDFAction);
        Button exportExcelBtn = new Button("Export Excel");
        exportExcelBtn.setOnAction(this::exportExcelAction);
        // compact export buttons
        exportCsvBtn.setPrefWidth(100);
        exportPdfBtn.setPrefWidth(100);
        exportExcelBtn.setPrefWidth(100);

        totalLabel = new Label("Total: 0.00");
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        totalItemsLabel = new Label("Items: 0");
        totalItemsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        topSellingLabel = new Label("Top selling: -");
        topSellingLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // status label shown when table is empty for the selected range
        statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
        statusLabel.setVisible(false);

        // Group export buttons together so spacing is consistent
        HBox exportBox = new HBox(8);
        exportBox.setAlignment(Pos.CENTER_LEFT);
        exportBox.getChildren().addAll(exportCsvBtn, exportPdfBtn, exportExcelBtn);
        // small Refresh/Debug button to show DB counts / min-max dates when range empty
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setPrefWidth(80);
        refreshBtn.setOnAction(this::refreshAction);
        exportBox.getChildren().add(refreshBtn);

        // Spacer to push other toolbar items to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Some toolbar padding for visual balance
        controls.setPadding(new Insets(6, 6, 6, 6));

        // Assemble controls; exportBox only visible for admins
        if (utils.SessionManager.isAdmin()) {
            controls.getChildren().addAll(rangeLabel, rangeCombo, exportBox, spacer);
        } else {
            controls.getChildren().addAll(rangeLabel, rangeCombo, spacer);
        }
        mainPanel.getChildren().add(controls);

        // SUMMARY CARDS: visually prominent totals shown under the controls
        HBox summaryRow = new HBox(12);
        summaryRow.setAlignment(Pos.CENTER_LEFT);
        summaryRow.setPadding(new Insets(8, 0, 12, 0));

        String cardStyle = "-fx-background-color: linear-gradient(#fbfdff, #eef6ff); -fx-padding:12; -fx-border-radius:8; -fx-background-radius:8; -fx-border-color:#d9eaf7; -fx-border-width:1;";

        VBox totalCard = new VBox(6);
        totalCard.setPrefWidth(260);
        totalCard.setStyle(cardStyle);
        Label totalCardTitle = new Label("Total Revenue");
        totalCardTitle.setStyle("-fx-font-size:12px; -fx-text-fill:#333;");
        totalCardValueLabel = new Label("0.00");
        totalCardValueLabel.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#0b4f83;");
        totalCard.getChildren().addAll(totalCardTitle, totalCardValueLabel);

        VBox itemsCard = new VBox(6);
        itemsCard.setPrefWidth(200);
        itemsCard.setStyle(cardStyle);
        Label itemsCardTitle = new Label("Total Items Sold");
        itemsCardTitle.setStyle("-fx-font-size:12px; -fx-text-fill:#333;");
        itemsCardValueLabel = new Label("0");
        itemsCardValueLabel.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#0b4f83;");
        itemsCard.getChildren().addAll(itemsCardTitle, itemsCardValueLabel);

        VBox topCard = new VBox(6);
        topCard.setPrefWidth(360);
        topCard.setStyle(cardStyle);
        Label topCardTitle = new Label("Top Selling Product");
        topCardTitle.setStyle("-fx-font-size:12px; -fx-text-fill:#333;");
        topCardValueLabel = new Label("-");
        topCardValueLabel.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#c85d00;");
        topCard.getChildren().addAll(topCardTitle, topCardValueLabel);

        summaryRow.getChildren().addAll(totalCard, itemsCard, topCard);
        mainPanel.getChildren().add(summaryRow);

        reportTable = new TableView<>();
        reportData = FXCollections.observableArrayList();
        TableColumn<Sale, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("saleId"));
        TableColumn<Sale, String> prodCol = new TableColumn<>("Product");
        prodCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("productName"));
        TableColumn<Sale, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("quantity"));
        TableColumn<Sale, Double> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("unitPrice"));
        TableColumn<Sale, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("totalPrice"));
        TableColumn<Sale, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("saleDate"));
        reportTable.getColumns().addAll(java.util.List.of(idCol, prodCol, qtyCol, unitCol, totalCol, dateCol));
        reportTable.setItems(reportData);
        // make columns stretch and look consistent
        reportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // Make the table grow to fill available vertical space
        reportTable.setPrefHeight(400);
        VBox.setVgrow(reportTable, Priority.ALWAYS);
        // add status label and table (status appears above table when empty)
        mainPanel.getChildren().add(statusLabel);
        mainPanel.getChildren().add(reportTable);

        container.setCenter(mainPanel);
        // default to weekly so users see data immediately
        loadRange("Weekly", totalCardValueLabel, itemsCardValueLabel, topCardValueLabel);

        Scene scene = new Scene(container, 900, 650);
        primaryStage.setScene(scene);
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    // Updated loadRange to accept label references for the summary cards
    private void loadRange(String type, Label totalCardValue, Label itemsCardValue, Label topCardValue) {
        reportData.clear();
        LocalDate now = LocalDate.now();
        LocalDate start;
        start = switch (type) {
            case "Weekly" -> now.minusDays(6);
            case "Monthly" -> now.minusDays(29);
            default -> now;
        };
        List<Sale> sales = saleDAO.getSalesByDateRange(Date.valueOf(start), Date.valueOf(now));

        // Debug logging to help diagnose empty report ranges
        try {
            utils.Logger.info("Reports.loadRange: type=" + type + " start=" + start + " end=" + now + " -> fetched=" + (sales == null ? 0 : sales.size()));
        } catch (Exception ignored) {}

        double total = 0;
        int totalItems = 0;
        if (sales != null) {
            for (Sale s : sales) {
                reportData.add(s);
                total += s.getTotalPrice();
                totalItems += s.getQuantity();
            }
        }
        totalLabel.setText("Total: " + String.format("%.2f", total));
        totalCardValue.setText(String.format("%.2f", total));
        totalItemsLabel.setText("Items: " + totalItems);
        itemsCardValue.setText(String.valueOf(totalItems));

        // show status if no data; also provide DB diagnostics when empty
        List<Sale> all = saleDAO.getAllSales();
        try {
            utils.Logger.info("Reports.loadRange: DB total sales=" + (all == null ? 0 : all.size()));
        } catch (Exception ignored) {}
        if (reportData.isEmpty()) {
            if (all == null || all.isEmpty()) {
                statusLabel.setText("No sales found for the selected range — database contains zero sales.");
            } else {
                // compute min/max dates
                java.sql.Timestamp min = null, max = null;
                for (Sale s : all) {
                    java.sql.Timestamp t = s.getSaleDate();
                    if (t != null) {
                        if (min == null || t.before(min)) min = t;
                        if (max == null || t.after(max)) max = t;
                    }
                }
                String info = "No sales in selected range. DB total: " + all.size();
                info += ", earliest: " + (min == null ? "-" : min) + ", latest: " + (max == null ? "-" : max);
                statusLabel.setText(info);
            }
            statusLabel.setVisible(true);
            // if DB has data but current selection is empty, do not change the user's selection programmatically
            // We keep the diagnostic message for clarity but avoid auto-switching the range to prevent redundant UI behavior.
        } else {
            statusLabel.setVisible(false);
        }
        int days = type.equals("Daily") ? 1 : type.equals("Weekly") ? 7 : 30;
        Map<String,Integer> top = saleDAO.getTopSellingProducts(1, days);
        String txt = top == null || top.isEmpty() ? "-" : top.entrySet().iterator().next().getKey() + " (" + top.entrySet().iterator().next().getValue() + ")";
        topSellingLabel.setText("Top selling: " + txt);
        topCardValue.setText(txt);
    }

    // Overloaded wrapper used by action handlers (keeps compat)
    private void loadRange(String type) {
        // Call the richer loader using our field references so the UI cards update correctly
        loadRange(type, totalCardValueLabel, itemsCardValueLabel, topCardValueLabel);
    }

    private void exportCSV() {
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName("report.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showSaveDialog(reportTable.getScene().getWindow());
        if (file != null) {
            try {
                utils.ExportUtils.exportSalesToCSV(saleDAO.getAllSales(), ensureExtension(file, ".csv"));
                showAlert("Exported successfully.", Alert.AlertType.INFORMATION);
            } catch (Exception ex) {
                showAlert("Export failed: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void exportPDFBox() {
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName("report.pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = chooser.showSaveDialog(reportTable.getScene().getWindow());
        if (file != null) {
            try {
                utils.PdfBoxExportUtils.exportTableToPDF(reportTable, ensureExtension(file, ".pdf"), "Sales Report");
                showAlert("Exported PDF successfully.", Alert.AlertType.INFORMATION);
            } catch (Exception ex) {
                showAlert("PDF export failed: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void exportExcel(String rangeType) {
        LocalDate now = LocalDate.now();
        LocalDate start;
        start = switch (rangeType) {
            case "Weekly" -> now.minusDays(6);
            case "Monthly" -> now.minusDays(29);
            default -> now;
        };
        List<Sale> sales = saleDAO.getSalesByDateRange(java.sql.Date.valueOf(start), java.sql.Date.valueOf(now));
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName("report.xls");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xls", "*.xlsx"));
        File file = chooser.showSaveDialog(reportTable.getScene().getWindow());
        if (file != null) {
            try {
                utils.ExcelExportUtils.exportSalesToExcel(sales, ensureExtension(file, ".xls"));
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
        alert.setContentText(message);
        alert.setHeaderText(null);
        alert.setResizable(true);
        alert.show();
    }

    // ActionEvent handlers (use parameter to avoid unused-parameter warnings)
    private void exportCSVAction(javafx.event.ActionEvent e) { exportCSV(); }
    private void exportPDFAction(javafx.event.ActionEvent e) { exportPDFBox(); }
    private void exportExcelAction(javafx.event.ActionEvent e) {
        // use the current selection in the rangeCombo to export
        if (rangeCombo != null && rangeCombo.getValue() != null) {
            exportExcel(rangeCombo.getValue());
        } else {
            exportExcel("Daily");
        }
     }

    private void rangeComboAction(javafx.event.ActionEvent e) {
        if (rangeCombo != null && rangeCombo.getValue() != null) {
            loadRange(rangeCombo.getValue());
        }
    }

    // Entrypoint for testing the form independently
    static void main(String[] args) {
        launch(args);
    }

    // debug helper to show DB counts and date span in the UI status label
    private void debugSales() {
        List<Sale> all = saleDAO.getAllSales();
        if (all == null || all.isEmpty()) {
            statusLabel.setText("Database contains zero sales.");
            statusLabel.setVisible(true);
            return;
        }
        java.sql.Timestamp min = null, max = null;
        for (Sale s : all) {
            java.sql.Timestamp t = s.getSaleDate();
            if (t != null) {
                if (min == null || t.before(min)) min = t;
                if (max == null || t.after(max)) max = t;
            }
        }
        String info = "DB total sales: " + all.size() + ", earliest: " + (min == null ? "-" : min) + ", latest: " + (max == null ? "-" : max);
        statusLabel.setText(info);
        statusLabel.setVisible(true);
    }

    private void refreshAction(javafx.event.ActionEvent e) { debugSales(); }
}
