// JavaFX version of RequestForm
package gui;

import dao.ProductDAO;
import dao.RequestDAO;
import models.Product;
import models.Request;
import utils.ExcelExportUtils;
import utils.PdfExportUtils;
import utils.ExportUtils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.List;
import java.util.Objects;
import javafx.stage.FileChooser;
import java.io.File;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.fxml.FXMLLoader;
import utils.Logger;

public class RequestForm extends Application {
    private RequestDAO requestDAO;
    private ProductDAO productDAO;
    private TableView<Request> requestTable;
    private ObservableList<Request> tableData;
    private ComboBox<String> statusFilter;

    @Override
    public void start(Stage primaryStage) {
        requestDAO = new RequestDAO();
        productDAO = new ProductDAO();
        AppNavigator.init(primaryStage);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/RequestForm.fxml"));
            BorderPane root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();
            return;
        } catch (Exception ex) {
            Logger.error("Failed to load RequestForm.fxml, falling back to programmatic UI.", ex);
        }

        // fallback to legacy programmatic UI on error
        primaryStage.setTitle("IMS - Requests");
        BorderPane container = new BorderPane();
        container.setPadding(new Insets(10));

        VBox sidebar = NavigationPanel.createSidebar(primaryStage, "Request");
        container.setLeft(sidebar);
        // removed topBar because the navigation drawer provides the page heading

        VBox mainPanel = new VBox(10);
        mainPanel.setPadding(new Insets(20));
        mainPanel.setStyle("-fx-background-color: white;");

        HBox headerPanel = new HBox(10);
        headerPanel.setAlignment(Pos.CENTER_LEFT);
        Button requestedTab = new Button("Requested");
        GuiUtils.stylePrimary(requestedTab);
        headerPanel.getChildren().add(requestedTab);
        Button addRequestButton = new Button("+ Add Request");
        addRequestButton.setOnAction(this::addRequestAction);
        GuiUtils.stylePrimary(addRequestButton);
        // show Add Request to any logged-in user
        if (utils.SessionManager.isLoggedIn()) {
            headerPanel.getChildren().add(addRequestButton);
        }
        Button exportExcelBtn = new Button("Export Excel");
        exportExcelBtn.setOnAction(this::exportRequestsExcelAction);
        GuiUtils.styleSecondary(exportExcelBtn);
        Button exportPdfBtn = new Button("Export PDF");
        exportPdfBtn.setOnAction(this::exportRequestsPDFAction);
        GuiUtils.styleSecondary(exportPdfBtn);
        Button exportCsvBtn = new Button("Export CSV");
        exportCsvBtn.setOnAction(this::exportRequestsCSVAction);
        GuiUtils.styleSecondary(exportCsvBtn);
        // show export buttons only to admins
        if (utils.SessionManager.isAdmin()) {
            headerPanel.getChildren().addAll(exportExcelBtn, exportPdfBtn, exportCsvBtn);
        }
        // style header panel
        GuiUtils.styleCard(headerPanel);
        headerPanel.setPadding(new Insets(12));
        mainPanel.getChildren().add(headerPanel);

        requestTable = new TableView<>();
        tableData = FXCollections.observableArrayList();
        TableColumn<Request, Integer> prodIdCol = new TableColumn<>("Product ID");
        prodIdCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("productId"));
        TableColumn<Request, String> prodNameCol = new TableColumn<>("Product Name");
        prodNameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("productName"));
        TableColumn<Request, Integer> qtyCol = new TableColumn<>("Requested Quantity");
        qtyCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("requestedQuantity"));
        TableColumn<Request, String> requestedByCol = new TableColumn<>("Requested By");
        requestedByCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("requestedBy"));
        TableColumn<Request, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));
        TableColumn<Request, java.sql.Timestamp> dateCol = new TableColumn<>("Request Date");
        dateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("requestDate"));
        requestTable.getColumns().addAll(java.util.List.of(prodIdCol, prodNameCol, qtyCol, requestedByCol, statusCol, dateCol));
        TableColumn<Request, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> {
            Objects.requireNonNull(col);
            return new TableCell<>() {
                private final Button approveBtn = new Button("Approve");
                private final Button rejectBtn = new Button("Reject");
                private final Button deleteBtn = new Button("Delete");
                {
                    approveBtn.setOnAction(e -> { Objects.requireNonNull(e); Request req = getTableView().getItems().get(getIndex()); updateRequestStatus(req, "Approved"); });
                    rejectBtn.setOnAction(e -> { Objects.requireNonNull(e); Request req = getTableView().getItems().get(getIndex()); updateRequestStatus(req, "Rejected"); });
                    deleteBtn.setOnAction(e -> { Objects.requireNonNull(e); Request req = getTableView().getItems().get(getIndex()); deleteRequest(req); });
                    GuiUtils.stylePrimary(approveBtn);
                    GuiUtils.styleWarning(rejectBtn);
                    GuiUtils.styleDanger(deleteBtn);
                }
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        if (utils.SessionManager.isAdmin()) {
                            HBox box = new HBox(5, approveBtn, rejectBtn, deleteBtn);
                            setGraphic(box);
                        } else {
                            // non-admin users should not see action buttons
                            setGraphic(null);
                        }
                    }
                }
            };
        });
        requestTable.getColumns().add(actionCol);
        requestTable.setItems(tableData);
        requestTable.setPrefHeight(500);
        // wrap table in a card for consistent spacing
        VBox tableBox = new VBox(8, requestTable);
        GuiUtils.styleCard(tableBox);
        tableBox.setPadding(new Insets(10));
        mainPanel.getChildren().add(tableBox);

        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "Pending", "Approved", "Rejected");
        statusFilter.setValue("All");
        statusFilter.setOnAction(this::statusFilterAction);
        // style the status filter input
        statusFilter.setPrefHeight(36);
        statusFilter.setStyle("-fx-padding:6 10 6 10; -fx-font-size:13px; -fx-border-radius:6; -fx-background-radius:6;");
        HBox filterPanel = new HBox(10, new Label("Filter by status:"), statusFilter);
        filterPanel.setAlignment(Pos.CENTER_LEFT);
        filterPanel.setPadding(new Insets(0, 0, 10, 0));
        mainPanel.getChildren().add(filterPanel);

        container.setCenter(mainPanel);
        loadRequests();

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

    private void loadRequests() {
        tableData.clear();
        List<Request> requests;
        String filter = statusFilter.getValue();
        boolean showAll = utils.SessionManager.isAdmin();
        if (showAll) {
            if (filter.equals("All")) {
                requests = requestDAO.getAllRequests();
            } else if (filter.equals("Pending")) {
                requests = requestDAO.getPendingRequests();
            } else {
                requests = requestDAO.getAllRequests();
                requests.removeIf(r -> !r.getStatus().equals(filter));
            }
        } else {
            // Non-admin: show only their own requests
            Integer currentUserId = null;
            if (utils.SessionManager.getCurrentUser() != null) {
                currentUserId = utils.SessionManager.getCurrentUser().getUserId();
            }
            if (currentUserId == null) {
                requests = java.util.Collections.emptyList();
            } else {
                requests = requestDAO.getRequestsByUser(currentUserId);
                if (!filter.equals("All")) {
                    requests.removeIf(r -> !r.getStatus().equals(filter));
                }
            }
        }
        tableData.addAll(requests);
    }

    public void showAddRequestDialog() {
        Dialog<Request> dialog = new Dialog<>();
        dialog.setTitle("Add Request");
        dialog.setHeaderText("Fill in request details");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        final ComboBox<Product> prodCombo = new ComboBox<>();
        prodCombo.setPromptText("Select Product");
        prodCombo.setItems(FXCollections.observableArrayList(productDAO.getAllProducts()));
        final TextField qty = new TextField();
        qty.setPromptText("Requested Quantity");
        // style inputs
        prodCombo.setPrefHeight(36);
        prodCombo.setStyle("-fx-padding:6 10 6 10; -fx-font-size:13px; -fx-border-radius:6; -fx-background-radius:6;");
        GuiUtils.styleInput(qty);

        // Show requester name as label (do not accept free text)
        Label requestedByLabel = new Label();
        final String currentUserDisplay;
        final Integer currentUserId;
        if (utils.SessionManager.getCurrentUser() != null) {
            String tmpName = utils.SessionManager.getCurrentUser().getFullName();
            if (tmpName == null || tmpName.trim().isEmpty()) {
                tmpName = utils.SessionManager.getCurrentUser().getUsername();
            }
            currentUserDisplay = tmpName;
            currentUserId = utils.SessionManager.getCurrentUser().getUserId();
        } else {
            currentUserDisplay = "";
            currentUserId = null;
        }
        requestedByLabel.setText(currentUserDisplay);

        grid.add(new Label("Product:"), 0, 0);
        grid.add(prodCombo, 1, 0);
        grid.add(new Label("Requested Quantity:"), 0, 1);
        grid.add(qty, 1, 1);
        grid.add(new Label("Requested By:"), 0, 2);
        grid.add(requestedByLabel, 1, 2);
        dialog.getDialogPane().setContent(grid);
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    Product selected = prodCombo.getValue();
                    if (selected == null) throw new Exception("Select a product.");
                    int quantity = Integer.parseInt(qty.getText());
                    Request req;
                    if (currentUserId != null) {
                        req = new Request(selected.getProductId(), selected.getProductName(), quantity, currentUserId);
                        req.setRequestedByName(currentUserDisplay);
                    } else {
                        // fallback to legacy string-based constructor
                        req = new Request(selected.getProductId(), selected.getProductName(), quantity, currentUserDisplay);
                    }
                    return req;
                } catch (Exception ex) {
                    showAlert("Error: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
            return null;
        });
        dialog.showAndWait().ifPresent(request -> {
            try {
                if (requestDAO.createRequest(request)) {
                    showAlert("Request added successfully!", Alert.AlertType.INFORMATION);
                    loadRequests();
                } else {
                    showAlert("Failed to add request!", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                showAlert("Error: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void updateRequestStatus(Request req, String status) {
        if (requestDAO.updateRequestStatus(req.getRequestId(), status)) {
            showAlert("Request status updated to " + status, Alert.AlertType.INFORMATION);
            loadRequests();
        } else {
            showAlert("Failed to update request status.", Alert.AlertType.ERROR);
        }
    }

    private void deleteRequest(Request req) {
        if (requestDAO.deleteRequest(req.getRequestId())) {
            showAlert("Request deleted.", Alert.AlertType.INFORMATION);
            loadRequests();
        } else {
            showAlert("Failed to delete request.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(title);
        alert.showAndWait();
    }

    private void exportRequestsExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName("requests.xls");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xls", "*.xlsx"));
        File file = chooser.showSaveDialog(requestTable.getScene().getWindow());
        if (file != null) {
            try {
                ExcelExportUtils.exportRequestsToExcel(requestDAO.getAllRequests(), ensureExtension(file, ".xls"));
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
        File file = chooser.showSaveDialog(requestTable.getScene().getWindow());
        if (file != null) {
            try {
                PdfExportUtils.exportRequestsToPDF(requestDAO.getAllRequests(), ensureExtension(file, ".pdf"));
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
        File file = chooser.showSaveDialog(requestTable.getScene().getWindow());
        if (file != null) {
            try {
                ExportUtils.exportRequestsToCSV(requestDAO.getAllRequests(), ensureExtension(file, ".csv"));
                showAlert("Exported to CSV successfully.", Alert.AlertType.INFORMATION);
            } catch (Exception ex) {
                showAlert("Export to CSV failed: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private File ensureExtension(File file, String ext) {
        if (!file.getName().toLowerCase().endsWith(ext)) {
            return new File(file.getParent(), file.getName() + ext);
        }
        return file;
    }

    // action handlers to avoid unused-lambda warnings
    private void addRequestAction(javafx.event.ActionEvent e) { showAddRequestDialog(); }
    private void exportRequestsExcelAction(javafx.event.ActionEvent e) { exportRequestsExcel(); }
    private void exportRequestsPDFAction(javafx.event.ActionEvent e) { exportRequestsPDF(); }
    private void exportRequestsCSVAction(javafx.event.ActionEvent e) { exportRequestsCSV(); }
    private void statusFilterAction(javafx.event.ActionEvent e) { loadRequests(); }

    static void main(String[] args) {
        launch(args);
    }
}
