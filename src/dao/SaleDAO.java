package dao;

import models.Sale;
import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }
        return false;
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return 0;
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

