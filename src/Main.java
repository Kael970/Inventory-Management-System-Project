import database.DatabaseMigration;
import gui.LoginForm;

/**
 * Main Class
 * Entry point for the Inventory Management System
 *
 * This application demonstrates Object-Oriented Programming principles:
 * - Encapsulation: Private fields with getters/setters in model classes
 * - Inheritance: Can be extended for specific user types
 * - Polymorphism: DAO interfaces can have multiple implementations
 * - Abstraction: Service layer abstracts business logic from GUI
 *
 * Project Structure:
 * - config: Database configuration
 * - database: Database connection and migration
 * - models: Entity classes (User, Product, Sale, Request)
 * - dao: Data Access Objects for database operations
 * - services: Business logic layer
 * - utils: Utility classes
 * - gui: User interface components
 */
public class Main {
    public static void main(String[] args) {
        // First, run database migration to set up tables
        System.out.println("=== Inventory Management System ===");
        System.out.println("Initializing database...");
        DatabaseMigration.runMigrations();

        // Launch the JavaFX login form
        LoginForm.main(args);
    }
}