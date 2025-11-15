package gui;

import dao.SaleDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import models.Sale;

import java.time.LocalDate;
import java.sql.Date;
import java.util.List;
import java.util.Map;

public class ReportsFormController {
    @SuppressWarnings("unused") @FXML private VBox mainPanel;
    @SuppressWarnings("unused") @FXML private VBox sidebarPlaceholder;

    @FXML private ComboBox<String> rangeCombo;
    @FXML private Button exportCsvBtn;
    @FXML private Button exportPdfBtn;
    @FXML private Button exportExcelBtn;
    @FXML private Label totalCardValueLabel;
    @FXML private Label itemsCardValueLabel;
    @FXML private Label topCardValueLabel;
    @FXML private Label statusLabel;
    @FXML private TableView<Sale> reportTable;
    @FXML private TableColumn<Sale,Integer> idCol;
    @FXML private TableColumn<Sale,String> prodCol;
    @FXML private TableColumn<Sale,Integer> qtyCol;
    @FXML private TableColumn<Sale,Double> unitCol;
    @FXML private TableColumn<Sale,Double> totalCol;
    @FXML private TableColumn<Sale,String> dateCol;

    private final SaleDAO saleDAO = new SaleDAO();
    @SuppressWarnings("unused")
    private final SaleDAO unusedSaleDAO = saleDAO;

    @FXML
    public void initialize() {
        sidebarPlaceholder.getChildren().clear();
        sidebarPlaceholder.getChildren().add(NavigationPanel.createSidebar(AppNavigator.getPrimaryStage(), "Reports"));

        rangeCombo.getItems().addAll("Daily", "Weekly", "Monthly");
        rangeCombo.setValue("Weekly");
        rangeCombo.setOnAction(e -> loadRange(rangeCombo.getValue()));

        exportCsvBtn.setOnAction(e -> exportCSV());
        exportPdfBtn.setOnAction(e -> exportPDF());
        exportExcelBtn.setOnAction(e -> exportExcel(rangeCombo.getValue()));

        idCol.setCellValueFactory(new PropertyValueFactory<>("saleId"));
        prodCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("saleDate"));

        loadRange("Weekly");
    }

    private void loadRange(String type) {
        reportTable.getItems().clear();
        LocalDate now = LocalDate.now();
        LocalDate start;
        start = switch (type) {
            case "Weekly" -> now.minusDays(6);
            case "Monthly" -> now.minusDays(29);
            default -> now;
        };
        List<Sale> sales = saleDAO.getSalesByDateRange(Date.valueOf(start), Date.valueOf(now));
        double total = 0; int items = 0;
        if (sales != null) {
            for (Sale s : sales) {
                reportTable.getItems().add(s);
                total += s.getTotalPrice();
                items += s.getQuantity();
            }
        }
        totalCardValueLabel.setText(String.format("%.2f", total));
        itemsCardValueLabel.setText(String.valueOf(items));
        Map<String,Integer> top = saleDAO.getTopSellingProducts(1, type.equals("Daily")?1:type.equals("Weekly")?7:30);
        String txt = "-";
        if (top != null && !top.isEmpty()) { var e = top.entrySet().iterator().next(); txt = e.getKey() + " (" + e.getValue() + ")"; }
        topCardValueLabel.setText(txt);
        statusLabel.setVisible(reportTable.getItems().isEmpty());
    }

    private void exportCSV() {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName("report.csv");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            java.io.File f = chooser.showSaveDialog(reportTable.getScene().getWindow());
            if (f != null) {
                utils.ExportUtils.exportSalesToCSV(saleDAO.getAllSales(), f);
                showAlert("Exported successfully.", Alert.AlertType.INFORMATION);
            }
        } catch (Exception ex) {
            showAlert("Export failed: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void exportPDF() {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName("report.pdf");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            java.io.File f = chooser.showSaveDialog(reportTable.getScene().getWindow());
            if (f != null) {
                utils.PdfBoxExportUtils.exportTableToPDF(reportTable, f, "Sales Report");
                showAlert("Exported PDF successfully.", Alert.AlertType.INFORMATION);
            }
        } catch (Exception ex) {
            showAlert("PDF export failed: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void exportExcel(String rangeType) {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setInitialFileName("report.xls");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xls", "*.xlsx"));
            java.io.File f = chooser.showSaveDialog(reportTable.getScene().getWindow());
            if (f != null) {
                List<Sale> sales = saleDAO.getSalesByDateRange(java.sql.Date.valueOf(LocalDate.now().minusDays(rangeType.equals("Weekly")?6:rangeType.equals("Monthly")?29:0)), java.sql.Date.valueOf(LocalDate.now()));
                utils.ExcelExportUtils.exportSalesToExcel(sales, f);
                showAlert("Exported successfully.", Alert.AlertType.INFORMATION);
            }
        } catch (Exception ex) {
            showAlert("Export failed: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Error" : "Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
