package gui;

import dao.ProductDAO;
import dao.RequestDAO;
import models.Product;
import models.Request;
import utils.SessionManager;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Request Form
 * Displays and manages product restock requests
 */
public class RequestForm extends JFrame {
    private RequestDAO requestDAO;
    private ProductDAO productDAO;
    private JTable requestTable;
    private DefaultTableModel tableModel;
    private JPanel mainPanel;

    public RequestForm() {
        requestDAO = new RequestDAO();
        productDAO = new ProductDAO();
        initComponents();
        loadRequests();
    }

    private void initComponents() {
        setTitle("IMS - Requests");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel container = new JPanel(new BorderLayout());

        // Sidebar
        JPanel sidebar = createSidebar();
        container.add(sidebar, BorderLayout.WEST);

        // Top Bar
        JPanel topBar = createTopBar();
        container.add(topBar, BorderLayout.NORTH);

        // Main Content
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        createRequestTable();

        container.add(mainPanel, BorderLayout.CENTER);
        add(container);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(200, 700));
        sidebar.setBackground(new Color(240, 248, 255));
        sidebar.setLayout(null);

        JLabel logoLabel = new JLabel("IMS");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 20));
        logoLabel.setForeground(new Color(26, 54, 93));
        logoLabel.setBounds(20, 20, 50, 30);
        sidebar.add(logoLabel);

        JLabel logoText = new JLabel("Inventory System");
        logoText.setFont(new Font("Arial", Font.PLAIN, 11));
        logoText.setForeground(Color.GRAY);
        logoText.setBounds(70, 23, 120, 25);
        sidebar.add(logoText);

        JButton dashboardBtn = createMenuButton("Dashboard", 80, false);
        JButton itemsBtn = createMenuButton("Items", 130, false);
        JButton requestBtn = createMenuButton("Request", 180, true);

        sidebar.add(dashboardBtn);
        sidebar.add(itemsBtn);
        sidebar.add(requestBtn);

        dashboardBtn.addActionListener(e -> {
            this.dispose();
            new DashboardForm().setVisible(true);
        });

        itemsBtn.addActionListener(e -> {
            this.dispose();
            new InventoryForm().setVisible(true);
        });

        return sidebar;
    }

    private JButton createMenuButton(String text, int y, boolean selected) {
        JButton btn = new JButton(text);
        btn.setBounds(10, y, 180, 40);
        btn.setFont(new Font("Arial", Font.PLAIN, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (selected) {
            btn.setBackground(new Color(0, 123, 255));
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(new Color(240, 248, 255));
            btn.setForeground(Color.DARK_GRAY);
        }

        return btn;
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel();
        topBar.setPreferredSize(new Dimension(1000, 60));
        topBar.setBackground(Color.WHITE);
        topBar.setLayout(null);
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));

        JLabel titleLabel = new JLabel("Request");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBounds(20, 15, 200, 30);
        topBar.add(titleLabel);

        return topBar;
    }

    private void createRequestTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);

        // Header with tab
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        headerPanel.setBackground(Color.WHITE);

        JButton requestedTab = new JButton("Requested");
        requestedTab.setBackground(new Color(0, 123, 255));
        requestedTab.setForeground(Color.WHITE);
        requestedTab.setFocusPainted(false);
        requestedTab.setBorderPainted(false);
        requestedTab.setCursor(new Cursor(Cursor.HAND_CURSOR));
        headerPanel.add(requestedTab);

        headerPanel.add(Box.createHorizontalStrut(500));

        JButton addRequestButton = new JButton("+ Add Request");
        addRequestButton.setBackground(new Color(0, 123, 255));
        addRequestButton.setForeground(Color.WHITE);
        addRequestButton.setFocusPainted(false);
        addRequestButton.setBorderPainted(false);
        addRequestButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addRequestButton.addActionListener(e -> showAddRequestDialog());
        headerPanel.add(addRequestButton);

        tablePanel.add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Product ID", "Product Name", "Image", "Price",
                           "Requested Quantity", "Requested By"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        requestTable = new JTable(tableModel);
        requestTable.setRowHeight(40);
        requestTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        requestTable.setFont(new Font("Arial", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(requestTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Footer with pagination
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(Color.WHITE);

        JLabel pageLabel = new JLabel("Showing 1 to 10 out of 10 records");
        footerPanel.add(pageLabel);

        JButton prevButton = new JButton("<");
        prevButton.setEnabled(false);
        footerPanel.add(prevButton);

        JButton pageButton = new JButton("1");
        pageButton.setBackground(new Color(0, 123, 255));
        pageButton.setForeground(Color.WHITE);
        footerPanel.add(pageButton);

        JButton nextButton = new JButton(">");
        nextButton.setEnabled(false);
        footerPanel.add(nextButton);

        tablePanel.add(footerPanel, BorderLayout.SOUTH);

        mainPanel.add(tablePanel);
    }

    private void loadRequests() {
        tableModel.setRowCount(0);
        List<Request> requests = requestDAO.getPendingRequests();

        for (Request r : requests) {
            Product product = productDAO.getProductById(r.getProductId());
            Object[] row = {
                String.format("%02d", r.getProductId()),
                r.getProductName(),
                "[Image]", // Placeholder for image
                product != null ? "₱" + String.format("%.2f", product.getSellingPrice()) : "N/A",
                r.getRequestedQuantity() + " pcs",
                r.getRequestedBy()
            };
            tableModel.addRow(row);
        }
    }

    private void showAddRequestDialog() {
        JDialog dialog = new JDialog(this, "Add New Request", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(null);

        JLabel titleLabel = new JLabel("Add New Request");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBounds(20, 20, 200, 25);
        dialog.add(titleLabel);

        // Product selection
        JLabel productLabel = new JLabel("Select Product:");
        productLabel.setBounds(20, 60, 150, 25);
        dialog.add(productLabel);

        List<Product> products = productDAO.getAllProducts();
        JComboBox<String> productCombo = new JComboBox<>();
        for (Product p : products) {
            productCombo.addItem(p.getProductId() + " - " + p.getProductName());
        }
        productCombo.setBounds(20, 85, 350, 30);
        dialog.add(productCombo);

        // Quantity
        JLabel qtyLabel = new JLabel("Requested Quantity:");
        qtyLabel.setBounds(20, 125, 150, 25);
        dialog.add(qtyLabel);

        JTextField qtyField = new JTextField();
        qtyField.setBounds(20, 150, 350, 30);
        dialog.add(qtyField);

        // Requested By
        JLabel requestedByLabel = new JLabel("Requested By:");
        requestedByLabel.setBounds(20, 190, 150, 25);
        dialog.add(requestedByLabel);

        JTextField requestedByField = new JTextField(
            SessionManager.getCurrentUser() != null ?
            SessionManager.getCurrentUser().getFullName() : "User");
        requestedByField.setBounds(20, 215, 350, 30);
        dialog.add(requestedByField);

        // Buttons
        JButton saveButton = new JButton("Submit Request");
        saveButton.setBounds(20, 280, 165, 35);
        saveButton.setBackground(new Color(0, 123, 255));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.addActionListener(e -> {
            try {
                String selected = (String) productCombo.getSelectedItem();
                int productId = Integer.parseInt(selected.split(" - ")[0]);
                Product product = productDAO.getProductById(productId);

                Request request = new Request();
                request.setProductId(productId);
                request.setProductName(product.getProductName());
                request.setRequestedQuantity(Integer.parseInt(qtyField.getText()));
                request.setRequestedBy(requestedByField.getText());
                request.setStatus("Pending");

                if (requestDAO.createRequest(request)) {
                    JOptionPane.showMessageDialog(dialog, "Request submitted successfully!");
                    loadRequests();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to submit request!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.add(saveButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(205, 280, 165, 35);
        cancelButton.setBackground(Color.LIGHT_GRAY);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.add(cancelButton);

        dialog.setVisible(true);
    }
}

