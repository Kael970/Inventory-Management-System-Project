package database;

import config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * Database Migration Class
 * Creates database and tables if they don't exist
 * Run this file first to set up the database structure
 */
public class DatabaseMigration {

    /**
     * Create the database if it doesn't exist
     */
    private static void createDatabase() {
        try {
            Connection conn = DriverManager.getConnection(
                DatabaseConfig.DB_URL,
                DatabaseConfig.DB_USER,
                DatabaseConfig.DB_PASSWORD
            );
            Statement stmt = conn.createStatement();

            String sql = "CREATE DATABASE IF NOT EXISTS " + DatabaseConfig.DB_NAME;
            stmt.executeUpdate(sql);
            System.out.println("Database created successfully or already exists.");

            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println("Error creating database!");
            e.printStackTrace();
        }
    }

    /**
     * Create all necessary tables
     */
    private static void createTables() {
        try {
            Connection conn = DriverManager.getConnection(
                DatabaseConfig.DB_URL_WITH_DB,
                DatabaseConfig.DB_USER,
                DatabaseConfig.DB_PASSWORD
            );
            Statement stmt = conn.createStatement();

            // Create Users table
            String usersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "user_id INT PRIMARY KEY AUTO_INCREMENT," +
                "username VARCHAR(50) UNIQUE NOT NULL," +
                "password VARCHAR(255) NOT NULL," +
                "full_name VARCHAR(100) NOT NULL," +
                "role ENUM('Admin', 'Staff') NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
            stmt.executeUpdate(usersTable);
            System.out.println("Users table created successfully.");

            // Create Products table
            String productsTable = "CREATE TABLE IF NOT EXISTS products (" +
                "product_id INT PRIMARY KEY AUTO_INCREMENT," +
                "product_name VARCHAR(100) NOT NULL," +
                "buying_price DECIMAL(10, 2) NOT NULL," +
                "selling_price DECIMAL(10, 2) NOT NULL," +
                "stock_quantity INT NOT NULL DEFAULT 0," +
                "threshold_value INT NOT NULL DEFAULT 10," +
                "expiry_date DATE," +
                "image_path VARCHAR(255)," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")";
            stmt.executeUpdate(productsTable);
            System.out.println("Products table created successfully.");

            // Create Sales table
            String salesTable = "CREATE TABLE IF NOT EXISTS sales (" +
                "sale_id INT PRIMARY KEY AUTO_INCREMENT," +
                "product_id INT NOT NULL," +
                "product_name VARCHAR(100) NOT NULL," +
                "quantity INT NOT NULL," +
                "unit_price DECIMAL(10, 2) NOT NULL," +
                "total_price DECIMAL(10, 2) NOT NULL," +
                "sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "user_id INT," +
                "FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE," +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL" +
                ")";
            stmt.executeUpdate(salesTable);
            System.out.println("Sales table created successfully.");

            // Create Requests table
            String requestsTable = "CREATE TABLE IF NOT EXISTS requests (" +
                "request_id INT PRIMARY KEY AUTO_INCREMENT," +
                "product_id INT NOT NULL," +
                "product_name VARCHAR(100) NOT NULL," +
                "requested_quantity INT NOT NULL," +
                "requested_by VARCHAR(100) NOT NULL," +
                "status ENUM('Pending', 'Approved', 'Rejected') DEFAULT 'Pending'," +
                "request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE" +
                ")";
            stmt.executeUpdate(requestsTable);
            System.out.println("Requests table created successfully.");

            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println("Error creating tables!");
            e.printStackTrace();
        }
    }

    /**
     * Insert default admin user
     */
    private static void insertDefaultData() {
        try {
            Connection conn = DriverManager.getConnection(
                DatabaseConfig.DB_URL_WITH_DB,
                DatabaseConfig.DB_USER,
                DatabaseConfig.DB_PASSWORD
            );
            Statement stmt = conn.createStatement();

            // Insert default admin (password: admin123)
            String insertAdmin = "INSERT IGNORE INTO users (username, password, full_name, role) " +
                "VALUES ('admin', 'admin123', 'System Administrator', 'Admin')";
            stmt.executeUpdate(insertAdmin);

            // Insert sample staff user (password: staff123)
            String insertStaff = "INSERT IGNORE INTO users (username, password, full_name, role) " +
                "VALUES ('staff', 'staff123', 'Kael V.', 'Staff')";
            stmt.executeUpdate(insertStaff);

            System.out.println("Default users created successfully.");
            System.out.println("Admin - Username: admin, Password: admin123");
            System.out.println("Staff - Username: staff, Password: staff123");

            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println("Error inserting default data!");
            e.printStackTrace();
        }
    }

    /**
     * Run all migrations
     */
    public static void runMigrations() {
        System.out.println("=== Starting Database Migration ===");
        try {
            Class.forName(DatabaseConfig.JDBC_DRIVER);
            createDatabase();
            createTables();
            insertDefaultData();
            System.out.println("=== Database Migration Completed Successfully ===");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }

    /**
     * Main method to run migrations
     */
    public static void main(String[] args) {
        runMigrations();
    }
}

