package database;

import config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import utils.Logger;

/**
 * Database Connection Class
 * Handles MySQL database connection using Singleton pattern
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    // Private constructor for Singleton pattern
    private DatabaseConnection() {
        try {
            Class.forName(DatabaseConfig.JDBC_DRIVER);
            this.connection = DriverManager.getConnection(
                DatabaseConfig.DB_URL_WITH_DB,
                DatabaseConfig.DB_USER,
                DatabaseConfig.DB_PASSWORD
            );
            System.out.println("Database connected successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            Logger.error("JDBC driver not found", e);
        } catch (SQLException e) {
            System.err.println("Connection failed!");
            Logger.error("Database connection failed", e);
        }
    }

    /**
     * Get singleton instance of DatabaseConnection
     * @return DatabaseConnection instance
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Get the database connection
     * @return Connection object
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(
                    DatabaseConfig.DB_URL_WITH_DB,
                    DatabaseConfig.DB_USER,
                    DatabaseConfig.DB_PASSWORD
                );
            }
        } catch (SQLException e) {
            Logger.error("Failed to re-establish database connection", e);
        }
        return connection;
    }

    /**
     * Close the database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            Logger.error("Error closing database connection", e);
        }
    }
}
