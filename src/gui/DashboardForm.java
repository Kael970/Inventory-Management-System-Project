package gui;

import dao.*;
import models.*;
import utils.SessionManager;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Dashboard Form
 * Main dashboard showing inventory overview and sales statistics
 */
public class DashboardForm extends JFrame {
    private ProductDAO productDAO;
    private SaleDAO saleDAO;
    private RequestDAO requestDAO;
    private JPanel mainPanel;
    private JLabel userNameLabel;
    
    public DashboardForm() {
        productDAO = new ProductDAO();
        saleDAO = new SaleDAO();
        requestDAO = new RequestDAO();
        initComponents();
        loadDashboardData();
    }
    
    private void initComponents() {
        setTitle("IMS - Dashboard");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Main container
        JPanel container = new JPanel(new BorderLayout());
        
        // Left Sidebar
        JPanel sidebar = createSidebar();
        container.add(sidebar, BorderLayout.WEST);
        
        // Top Navigation Bar
        JPanel topBar = createTopBar();
        container.add(topBar, BorderLayout.NORTH);
        
        // Main Content Area
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        container.add(scrollPane, BorderLayout.CENTER);
        
        add(container);
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(200, 700));
        sidebar.setBackground(new Color(240, 248, 255));
        sidebar.setLayout(null);
        
        // Logo
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
        
        // Menu Items
        JButton dashboardBtn = createMenuButton("Dashboard", 80, true);
        JButton itemsBtn = createMenuButton("Items", 130, false);
        JButton requestBtn = createMenuButton("Request", 180, false);
        
        sidebar.add(dashboardBtn);
        sidebar.add(itemsBtn);
        sidebar.add(requestBtn);
        
        // Action listeners
        dashboardBtn.addActionListener(e -> loadDashboardData());
        itemsBtn.addActionListener(e -> {
            this.dispose();
            new InventoryForm().setVisible(true);
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
        
        JLabel titleLabel = new JLabel("Welcome to dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBounds(20, 15, 300, 30);
        topBar.add(titleLabel);
        
        // Search field
        JTextField searchField = new JTextField("Search");
        searchField.setBounds(650, 15, 200, 30);
        searchField.setForeground(Color.GRAY);
        topBar.add(searchField);
        
        // User info
        User currentUser = SessionManager.getCurrentUser();
        userNameLabel = new JLabel(currentUser != null ? currentUser.getFullName() : "User");
        userNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userNameLabel.setBounds(900, 15, 150, 30);
        topBar.add(userNameLabel);
        
        // Logout button
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBounds(1050, 15, 80, 30);
        logoutBtn.setBackground(new Color(220, 53, 69));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            SessionManager.clearSession();
            this.dispose();
            new LoginForm().setVisible(true);
        });
        topBar.add(logoutBtn);
        
        return topBar;
    }
    
    private void loadDashboardData() {
        mainPanel.removeAll();
        
        // Best Selling Items Section
        JPanel bestSellingPanel = createBestSellingPanel();
        mainPanel.add(bestSellingPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Statistics Cards
        JPanel statsPanel = createStatsPanel();
        mainPanel.add(statsPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Overall Inventory Section
        JPanel inventoryPanel = createInventoryPanel();
        mainPanel.add(inventoryPanel);
        
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    private JPanel createBestSellingPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        
        JLabel titleLabel = new JLabel("Best selling items");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"Product", "Product ID", "Remaining Quantity", "Price", "Availability"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Load products
        List<Product> products = productDAO.getAllProducts();
        int count = 0;
        for (Product p : products) {
            if (count >= 4) break;
            model.addRow(new Object[]{
                p.getProductName(),
                p.getProductId(),
                p.getStockQuantity(),
                "₱" + String.format("%.2f", p.getSellingPrice()),
                p.getAvailabilityStatus()
            });
            count++;
        }
        
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(950, 180));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 0));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        // Sales Card
        int salesCount = saleDAO.getLast7DaysSalesCount();
        JPanel salesCard = createStatCard("Sales", String.valueOf(salesCount), 
            "↑ 12% vs last month", new Color(100, 181, 246), true);
        panel.add(salesCard);
        
        // Out of Stock Card
        int outOfStock = productDAO.getOutOfStockCount();
        JPanel outOfStockCard = createStatCard("Out of stock", String.valueOf(outOfStock), 
            "↑ 12% vs last month", Color.WHITE, false);
        panel.add(outOfStockCard);
        
        // Requested Items Card
        int requestedItems = requestDAO.getRequestsCount();
        JPanel requestedCard = createStatCard("Requested items", String.valueOf(requestedItems), 
            "↑ 12% vs last month", Color.WHITE, false);
        panel.add(requestedCard);
        
        return panel;
    }
    
    private JPanel createStatCard(String title, String value, String subtitle, Color bgColor, boolean isPrimary) {
        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(280, 120));
        card.setBackground(bgColor);
        card.setLayout(null);
        card.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        
        Color textColor = isPrimary ? Color.WHITE : Color.DARK_GRAY;
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        titleLabel.setForeground(textColor);
        titleLabel.setBounds(20, 20, 200, 20);
        card.add(titleLabel);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 32));
        valueLabel.setForeground(textColor);
        valueLabel.setBounds(20, 40, 200, 40);
        card.add(valueLabel);
        
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        subtitleLabel.setForeground(textColor);
        subtitleLabel.setBounds(20, 85, 200, 20);
        card.add(subtitleLabel);
        
        return card;
    }
    
    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        
        JLabel titleLabel = new JLabel("Overall Inventory");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Inventory stats
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 80, 20));
        statsPanel.setBackground(Color.WHITE);
        
        List<Product> products = productDAO.getAllProducts();
        int totalProducts = products.size();
        double totalRevenue = 0;
        int topSelling = 0;
        int lowStocks = 0;
        
        for (Product p : products) {
            totalRevenue += p.getSellingPrice() * p.getStockQuantity();
            if (p.isLowStock()) lowStocks++;
        }
        
        statsPanel.add(createInventoryStat("Sales", "100", "Last 7 days", new Color(0, 123, 255)));
        statsPanel.add(createInventoryStat("Total Products", String.valueOf(totalProducts), "Last 7 days", new Color(255, 152, 0)));
        statsPanel.add(createInventoryStat("Top Selling", String.valueOf(topSelling), "Last 7 days", new Color(76, 175, 80)));
        statsPanel.add(createInventoryStat("Low Stocks", String.valueOf(lowStocks), "Not in stock", new Color(244, 67, 54)));
        
        panel.add(statsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createInventoryStat(String label, String value, String subtitle, Color color) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        
        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        labelLabel.setForeground(color);
        labelLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(labelLabel);
        panel.add(valueLabel);
        panel.add(subtitleLabel);
        
        return panel;
    }
}

