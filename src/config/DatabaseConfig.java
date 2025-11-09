package config;

/**
 * Database Configuration Class
 * Contains database connection parameters
 */
public class DatabaseConfig {
    // Database connection parameters
    public static final String DB_HOST = "localhost";
    public static final String DB_PORT = "3306";
    public static final String DB_NAME = "inventory_system";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = ""; // Change this to your MySQL password

    // JDBC URL
    public static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/";
    public static final String DB_URL_WITH_DB = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;

    // JDBC Driver
    public static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
}

