package gui;

import dao.ProductDAO;
import models.Product;
import utils.SessionManager;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Date;
import java.util.List;

/**
 * Inventory Form
 * Displays and manages product inventory
 */
public class InventoryForm extends JFrame {
    private ProductDAO productDAO;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JPanel mainPanel;

    public InventoryForm() {
        productDAO = new ProductDAO();
        initComponents();
        loadProducts();
    }

    private void initComponents() {
        setTitle("IMS - Inventory");
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

        createInventoryTable();

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
        JButton itemsBtn = createMenuButton("Items", 130, true);
        JButton requestBtn = createMenuButton("Request", 180, false);

        sidebar.add(dashboardBtn);
        sidebar.add(itemsBtn);
        sidebar.add(requestBtn);

        dashboardBtn.addActionListener(e -> {
            this.dispose();
            new DashboardForm().setVisible(true);
        });

        requestBtn.addActionListener(e -> {
            this.dispose();
            new RequestForm().setVisible(true);
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

        JLabel titleLabel = new JLabel("INVENTORY");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBounds(20, 15, 200, 30);
        topBar.add(titleLabel);

        return topBar;
    }

    private void createInventoryTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);

        // Header with buttons
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        headerPanel.setBackground(Color.WHITE);

        JLabel productsLabel = new JLabel("Products");
        productsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerPanel.add(productsLabel);

        headerPanel.add(Box.createHorizontalStrut(500));

        JButton addButton = new JButton("+ Add Product");
        addButton.setBackground(new Color(0, 123, 255));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorderPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> showAddProductDialog());
        headerPanel.add(addButton);

        JButton filterButton = new JButton("Filters");
        filterButton.setBackground(Color.WHITE);
        filterButton.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        filterButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        headerPanel.add(filterButton);

        JButton downloadButton = new JButton("Download all");
        downloadButton.setBackground(Color.WHITE);
        downloadButton.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        downloadButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        headerPanel.add(downloadButton);

        tablePanel.add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Product ID", "Products", "Buying Price", "Quantity",
                           "Threshold Value", "Expiry Date", "Availability"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        productTable = new JTable(tableModel);
        productTable.setRowHeight(35);
        productTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        productTable.setFont(new Font("Arial", Font.PLAIN, 12));

        // Add mouse listener for row selection
        productTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = productTable.getSelectedRow();
                    if (row >= 0) {
                        showEditProductDialog(row);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(productTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(tablePanel);
    }

    private void loadProducts() {
        tableModel.setRowCount(0);
        List<Product> products = productDAO.getAllProducts();

        for (Product p : products) {
            Object[] row = {
                String.format("%02d", p.getProductId()),
                p.getProductName(),
                "₱" + String.format("%.2f", p.getBuyingPrice()),
                p.getStockQuantity() + " Packets",
                p.getThresholdValue() + " Packets",
                p.getExpiryDate() != null ? p.getExpiryDate().toString() : "N/A",
                p.getAvailabilityStatus()
            };
            tableModel.addRow(row);
        }
    }

    private void showAddProductDialog() {
        JDialog dialog = new JDialog(this, "Add New Product", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(null);

        JLabel titleLabel = new JLabel("Add New Product");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBounds(20, 20, 200, 25);
        dialog.add(titleLabel);

        // Product Name
        JLabel nameLabel = new JLabel("Product Name:");
        nameLabel.setBounds(20, 60, 150, 25);
        dialog.add(nameLabel);

        JTextField nameField = new JTextField();
        nameField.setBounds(20, 85, 350, 30);
        dialog.add(nameField);

        // Buying Price
        JLabel buyingLabel = new JLabel("Buying Price:");
        buyingLabel.setBounds(20, 125, 150, 25);
        dialog.add(buyingLabel);

        JTextField buyingField = new JTextField();
        buyingField.setBounds(20, 150, 350, 30);
        dialog.add(buyingField);

        // Selling Price
        JLabel sellingLabel = new JLabel("Selling Price:");
        sellingLabel.setBounds(20, 190, 150, 25);
        dialog.add(sellingLabel);

        JTextField sellingField = new JTextField();
        sellingField.setBounds(20, 215, 350, 30);
        dialog.add(sellingField);

        // Quantity
        JLabel qtyLabel = new JLabel("Quantity:");
        qtyLabel.setBounds(20, 255, 150, 25);
        dialog.add(qtyLabel);

        JTextField qtyField = new JTextField();
        qtyField.setBounds(20, 280, 350, 30);
        dialog.add(qtyField);

        // Threshold
        JLabel thresholdLabel = new JLabel("Threshold:");
        thresholdLabel.setBounds(20, 320, 150, 25);
        dialog.add(thresholdLabel);

        JTextField thresholdField = new JTextField();
        thresholdField.setBounds(20, 345, 350, 30);
        dialog.add(thresholdField);

        // Buttons
        JButton saveButton = new JButton("Save");
        saveButton.setBounds(20, 400, 165, 35);
        saveButton.setBackground(new Color(0, 123, 255));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.addActionListener(e -> {
            try {
                Product product = new Product();
                product.setProductName(nameField.getText());
                product.setBuyingPrice(Double.parseDouble(buyingField.getText()));
                product.setSellingPrice(Double.parseDouble(sellingField.getText()));
                product.setStockQuantity(Integer.parseInt(qtyField.getText()));
                product.setThresholdValue(Integer.parseInt(thresholdField.getText()));

                if (productDAO.createProduct(product)) {
                    JOptionPane.showMessageDialog(dialog, "Product added successfully!");
                    loadProducts();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add product!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.add(saveButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(205, 400, 165, 35);
        cancelButton.setBackground(Color.LIGHT_GRAY);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.add(cancelButton);

        dialog.setVisible(true);
    }

    private void showEditProductDialog(int row) {
        int productId = Integer.parseInt(productTable.getValueAt(row, 0).toString());
        Product product = productDAO.getProductById(productId);

        if (product == null) return;

        JDialog dialog = new JDialog(this, "Edit Product", true);
        dialog.setSize(400, 550);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(null);

        JLabel titleLabel = new JLabel("Edit Product");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBounds(20, 20, 200, 25);
        dialog.add(titleLabel);

        // Fields pre-filled with product data
        JLabel nameLabel = new JLabel("Product Name:");
        nameLabel.setBounds(20, 60, 150, 25);
        dialog.add(nameLabel);

        JTextField nameField = new JTextField(product.getProductName());
        nameField.setBounds(20, 85, 350, 30);
        dialog.add(nameField);

        JLabel buyingLabel = new JLabel("Buying Price:");
        buyingLabel.setBounds(20, 125, 150, 25);
        dialog.add(buyingLabel);

        JTextField buyingField = new JTextField(String.valueOf(product.getBuyingPrice()));
        buyingField.setBounds(20, 150, 350, 30);
        dialog.add(buyingField);

        JLabel sellingLabel = new JLabel("Selling Price:");
        sellingLabel.setBounds(20, 190, 150, 25);
        dialog.add(sellingLabel);

        JTextField sellingField = new JTextField(String.valueOf(product.getSellingPrice()));
        sellingField.setBounds(20, 215, 350, 30);
        dialog.add(sellingField);

        JLabel qtyLabel = new JLabel("Quantity:");
        qtyLabel.setBounds(20, 255, 150, 25);
        dialog.add(qtyLabel);

        JTextField qtyField = new JTextField(String.valueOf(product.getStockQuantity()));
        qtyField.setBounds(20, 280, 350, 30);
        dialog.add(qtyField);

        JLabel thresholdLabel = new JLabel("Threshold:");
        thresholdLabel.setBounds(20, 320, 150, 25);
        dialog.add(thresholdLabel);

        JTextField thresholdField = new JTextField(String.valueOf(product.getThresholdValue()));
        thresholdField.setBounds(20, 345, 350, 30);
        dialog.add(thresholdField);

        // Buttons
        JButton updateButton = new JButton("Update");
        updateButton.setBounds(20, 400, 110, 35);
        updateButton.setBackground(new Color(0, 123, 255));
        updateButton.setForeground(Color.WHITE);
        updateButton.setFocusPainted(false);
        updateButton.setBorderPainted(false);
        updateButton.addActionListener(e -> {
            try {
                product.setProductName(nameField.getText());
                product.setBuyingPrice(Double.parseDouble(buyingField.getText()));
                product.setSellingPrice(Double.parseDouble(sellingField.getText()));
                product.setStockQuantity(Integer.parseInt(qtyField.getText()));
                product.setThresholdValue(Integer.parseInt(thresholdField.getText()));

                if (productDAO.updateProduct(product)) {
                    JOptionPane.showMessageDialog(dialog, "Product updated successfully!");
                    loadProducts();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update product!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.add(updateButton);

        JButton deleteButton = new JButton("Delete");
        deleteButton.setBounds(145, 400, 110, 35);
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.setBorderPainted(false);
        deleteButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(dialog,
                "Are you sure you want to delete this product?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (productDAO.deleteProduct(productId)) {
                    JOptionPane.showMessageDialog(dialog, "Product deleted successfully!");
                    loadProducts();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to delete product!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        dialog.add(deleteButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(270, 400, 100, 35);
        cancelButton.setBackground(Color.LIGHT_GRAY);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.add(cancelButton);

        dialog.setVisible(true);
    }
}

