package dao;

import models.Sale;
import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import utils.Logger;

/**
 * Sale Data Access Object
 * Handles all database operations for Sale entity
 */
public class SaleDAO {
    private Connection connection;

    public SaleDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Create a new sale and update product stock
     */
    public boolean createSale(Sale sale) {
        String sql = "INSERT INTO sales (product_id, product_name, quantity, unit_price, total_price, user_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            // Start transaction
            connection.setAutoCommit(false);

            // Insert sale
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, sale.getProductId());
            pstmt.setString(2, sale.getProductName());
            pstmt.setInt(3, sale.getQuantity());
            pstmt.setDouble(4, sale.getUnitPrice());
            pstmt.setDouble(5, sale.getTotalPrice());
            pstmt.setInt(6, sale.getUserId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // Update product stock
                String updateStock = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateStock);
                updateStmt.setInt(1, sale.getQuantity());
                updateStmt.setInt(2, sale.getProductId());
                updateStmt.executeUpdate();

                // Commit transaction
                connection.commit();
                connection.setAutoCommit(true);
                return true;
            }

            connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            try { connection.rollback(); connection.setAutoCommit(true);} catch (SQLException ex) { Logger.error("Rollback failed in createSale", ex); }
            Logger.error("Failed to create sale", e);
        }
        return false;
    }

    /**
     * Create a new sale and update product stock with stock validation
     * Returns generated sale ID or -1 on failure
     */
    public int createSaleWithStockCheck(Sale sale) {
        String stockQuery = "SELECT stock_quantity, selling_price, product_name FROM products WHERE product_id = ? FOR UPDATE";
        String insertSql = "INSERT INTO sales (product_id, product_name, quantity, unit_price, total_price, user_id) VALUES (?, ?, ?, ?, ?, ?)";
        String updateStock = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ?";
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement stockStmt = connection.prepareStatement(stockQuery)) {
                stockStmt.setInt(1, sale.getProductId());
                ResultSet rs = stockStmt.executeQuery();
                if (!rs.next()) { connection.rollback(); connection.setAutoCommit(true); return -1; }
                int stock = rs.getInt(1);
                double currentPrice = rs.getDouble(2);
                String productName = rs.getString(3);
                if (sale.getUnitPrice() <= 0) sale.setUnitPrice(currentPrice);
                if (sale.getProductName() == null || sale.getProductName().isEmpty()) sale.setProductName(productName);
                if (sale.getQuantity() <= 0 || sale.getQuantity() > stock) { connection.rollback(); connection.setAutoCommit(true); return -1; }
            }
            int generatedId = -1;
            try (PreparedStatement insert = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insert.setInt(1, sale.getProductId());
                insert.setString(2, sale.getProductName());
                insert.setInt(3, sale.getQuantity());
                insert.setDouble(4, sale.getUnitPrice());
                insert.setDouble(5, sale.getTotalPrice());
                insert.setInt(6, sale.getUserId());
                int ra = insert.executeUpdate();
                if (ra == 0) { connection.rollback(); connection.setAutoCommit(true); return -1; }
                ResultSet keys = insert.getGeneratedKeys();
                if (keys.next()) generatedId = keys.getInt(1);
            }
            try (PreparedStatement upd = connection.prepareStatement(updateStock)) {
                upd.setInt(1, sale.getQuantity());
                upd.setInt(2, sale.getProductId());
                upd.executeUpdate();
            }
            connection.commit();
            connection.setAutoCommit(true);
            return generatedId;
        } catch (SQLException e) {
            try { connection.rollback(); connection.setAutoCommit(true);} catch (SQLException ignored) {}
            Logger.error("Failed to create sale with stock check", e);
            return -1;
        }
    }

    /**
     * Get all sales
     */
    public List<Sale> getAllSales() {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales ORDER BY sale_date DESC";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                sales.add(extractSaleFromResultSet(rs));
            }
        } catch (SQLException e) {
            Logger.error("Failed to fetch all sales", e);
        }
        return sales;
    }

    /**
     * Get sales by date range
     */
    public List<Sale> getSalesByDateRange(Date startDate, Date endDate) {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT * FROM sales WHERE DATE(sale_date) BETWEEN ? AND ? ORDER BY sale_date DESC";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setDate(1, startDate);
            pstmt.setDate(2, endDate);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                sales.add(extractSaleFromResultSet(rs));
            }
        } catch (SQLException e) {
            Logger.error("Failed to fetch sales by date range", e);
        }
        return sales;
    }

    /**
     * Get total sales for today
     */
    public int getTodaySalesCount() {
        String sql = "SELECT COUNT(*) FROM sales WHERE DATE(sale_date) = CURDATE()";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            Logger.error("Failed to get today's sales count", e);
        }
        return 0;
    }

    /**
     * Get total sales revenue for a date range
     */
    public double getTotalRevenue(Date startDate, Date endDate) {
        String sql = "SELECT SUM(total_price) FROM sales WHERE DATE(sale_date) BETWEEN ? AND ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setDate(1, startDate);
            pstmt.setDate(2, endDate);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            Logger.error("Failed to get total revenue", e);
        }
        return 0.0;
    }

    /**
     * Get sales count for last 7 days
     */
    public int getLast7DaysSalesCount() {
        String sql = "SELECT COUNT(*) FROM sales WHERE sale_date >= DATE_SUB(NOW(), INTERVAL 7 DAY)";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            Logger.error("Failed to get last 7 days sales count", e);
        }
        return 0;
    }

    /**
     * Get top selling products (by total quantity) within optional last N days
     */
    public Map<String, Integer> getTopSellingProducts(int limit, int lastDays) {
        Map<String, Integer> result = new LinkedHashMap<>();
        String base = "SELECT product_name, SUM(quantity) qty FROM sales";
        String where = lastDays > 0 ? " WHERE sale_date >= DATE_SUB(NOW(), INTERVAL ? DAY)" : "";
        String group = " GROUP BY product_name ORDER BY qty DESC LIMIT ?";
        String sql = base + where + group;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int idx = 1;
            if (lastDays > 0) ps.setInt(idx++, lastDays);
            ps.setInt(idx, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.put(rs.getString(1), rs.getInt(2));
        } catch (SQLException e) {
            Logger.error("Failed to get top selling products", e);
        }
        return result;
    }

    /**
     * Delete a sale by ID
     */
    public boolean deleteSale(int saleId) {
        String sql = "DELETE FROM sales WHERE sale_id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, saleId);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            Logger.error("Failed to delete sale: " + saleId, e);
            return false;
        }
    }

    /**
     * Update a sale record (admin only)
     */
    public boolean updateSale(Sale sale) {
        String sql = "UPDATE sales SET quantity = ?, unit_price = ?, total_price = ? WHERE sale_id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, sale.getQuantity());
            pstmt.setDouble(2, sale.getUnitPrice());
            pstmt.setDouble(3, sale.getTotalPrice());
            pstmt.setInt(4, sale.getSaleId());
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            Logger.error("Failed to update sale: " + sale.getSaleId(), e);
            return false;
        }
    }

    /**
     * Extract Sale object from ResultSet
     */
    private Sale extractSaleFromResultSet(ResultSet rs) throws SQLException {
        Sale sale = new Sale();
        sale.setSaleId(rs.getInt("sale_id"));
        sale.setProductId(rs.getInt("product_id"));
        sale.setProductName(rs.getString("product_name"));
        sale.setQuantity(rs.getInt("quantity"));
        sale.setUnitPrice(rs.getDouble("unit_price"));
        sale.setTotalPrice(rs.getDouble("total_price"));
        sale.setSaleDate(rs.getTimestamp("sale_date"));
        sale.setUserId(rs.getInt("user_id"));
        return sale;
    }
}
