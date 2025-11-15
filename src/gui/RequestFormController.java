package gui;

import dao.RequestDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import models.Request;
import utils.Logger;

import java.util.List;
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;

public class RequestFormController {
    @SuppressWarnings("unused") @FXML private VBox mainPanel;
    @SuppressWarnings("unused") @FXML private VBox sidebarPlaceholder;

    @SuppressWarnings("unused") @FXML private Button requestedTab;
    @SuppressWarnings("unused") @FXML private Button addRequestButton;
    @SuppressWarnings("unused") @FXML private Button exportExcelBtn;
    @SuppressWarnings("unused") @FXML private Button exportPdfBtn;
    @SuppressWarnings("unused") @FXML private Button exportCsvBtn;
    @SuppressWarnings("unused") @FXML private ComboBox<String> statusFilter;
    @SuppressWarnings("unused") @FXML private TableView<Request> requestTable;
    @SuppressWarnings("unused") @FXML private TableColumn<Request,Integer> prodIdCol;
    @SuppressWarnings("unused") @FXML private TableColumn<Request,String> prodNameCol;
    @SuppressWarnings("unused") @FXML private TableColumn<Request,Integer> qtyCol;
    @SuppressWarnings("unused") @FXML private TableColumn<Request,String> requestedByCol;
    @SuppressWarnings("unused") @FXML private TableColumn<Request,String> statusCol;
    @SuppressWarnings("unused") @FXML private TableColumn<Request,java.sql.Timestamp> dateCol;
    @SuppressWarnings("unused") @FXML private TableColumn<Request, Void> actionCol;

    @SuppressWarnings("unused") private final RequestDAO requestDAO = new RequestDAO();

    @FXML
    public void initialize() {
        sidebarPlaceholder.getChildren().clear();
        sidebarPlaceholder.getChildren().add(NavigationPanel.createSidebar(AppNavigator.getPrimaryStage(), "Request"));

        prodIdCol.setCellValueFactory(new PropertyValueFactory<>("productId"));
        prodNameCol.setCellValueFactory(new PropertyValueFactory<>("productName"));
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("requestedQuantity"));
        requestedByCol.setCellValueFactory(new PropertyValueFactory<>("requestedByName"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("requestDate"));

        statusFilter.getItems().addAll("All", "Pending", "Approved", "Rejected");
        statusFilter.setValue("All");
        statusFilter.setOnAction(this::onStatusFilter);

        addRequestButton.setOnAction(this::onAddRequest);
        exportExcelBtn.setOnAction(this::onExportExcel);
        exportPdfBtn.setOnAction(this::onExportPdf);
        exportCsvBtn.setOnAction(this::onExportCsv);

        loadRequests();
    }

    public void loadRequests() {
        try {
            requestTable.getItems().clear();
            String filter = statusFilter.getValue();
            boolean showAll = utils.SessionManager.isAdmin();
            List<Request> requests;
            if (showAll) {
                if ("All".equals(filter)) requests = requestDAO.getAllRequests();
                else if ("Pending".equals(filter)) requests = requestDAO.getPendingRequests();
                else {
                    requests = requestDAO.getAllRequests();
                    requests.removeIf(r -> !r.getStatus().equals(filter));
                }
            } else {
                Integer currentUserId = utils.SessionManager.getCurrentUser() == null ? null : utils.SessionManager.getCurrentUser().getUserId();
                if (currentUserId == null) {
                    requests = java.util.Collections.emptyList();
                } else {
                    requests = requestDAO.getRequestsByUser(currentUserId);
                    if (!"All".equals(filter)) requests.removeIf(r -> !r.getStatus().equals(filter));
                }
            }
            requestTable.getItems().addAll(requests);
        } catch (Exception ex) {
            Logger.error("Failed to load requests", ex);
        }
    }

    // handlers wired from initialize
    private void onStatusFilter(ActionEvent e) { loadRequests(); }
    private void onAddRequest(ActionEvent e) {
        try { new RequestForm().showAddRequestDialog(); } catch (Exception ex) { Logger.error("Failed to open Add Request dialog", ex); }
    }
    private void onExportExcel(ActionEvent e) { exportRequestsExcel(); }
    private void onExportPdf(ActionEvent e) { exportRequestsPDF(); }
    private void onExportCsv(ActionEvent e) { exportRequestsCSV(); }

    private void exportRequestsExcel() { /* reuse existing programmatic utils */
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName("requests.xls");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xls", "*.xlsx"));
        java.io.File f = chooser.showSaveDialog(requestTable.getScene().getWindow());
        if (f != null) {
            try {
                utils.ExcelExportUtils.exportRequestsToExcel(requestDAO.getAllRequests(), f);
                showAlert("Exported successfully.", Alert.AlertType.INFORMATION);
            } catch (Exception ex) {
                showAlert("Export failed: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void exportRequestsPDF() {
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName("requests.pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        java.io.File f = chooser.showSaveDialog(requestTable.getScene().getWindow());
        if (f != null) {
            try {
                utils.PdfExportUtils.exportRequestsToPDF(requestDAO.getAllRequests(), f);
                showAlert("Exported to PDF successfully.", Alert.AlertType.INFORMATION);
            } catch (Exception ex) {
                showAlert("Export to PDF failed: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void exportRequestsCSV() {
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName("requests.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        java.io.File f = chooser.showSaveDialog(requestTable.getScene().getWindow());
        if (f != null) {
            try {
                utils.ExportUtils.exportRequestsToCSV(requestDAO.getAllRequests(), f);
                showAlert("Exported to CSV successfully.", Alert.AlertType.INFORMATION);
            } catch (Exception ex) {
                showAlert("Export to CSV failed: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
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
