package services;

import dao.UserDAO;
import models.User;

/**
 * Authentication Service
 * Handles user authentication and session management
 * Demonstrates Service Layer pattern
 */
public class AuthService {
    private UserDAO userDAO;
    private static User currentUser;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Login user
     */
    public User login(String username, String password) {
        User user = userDAO.authenticate(username, password);
        if (user != null) {
            currentUser = user;
            System.out.println("Login successful: " + user.getFullName());
        }
        return user;
    }

    /**
     * Logout current user
     */
    public void logout() {
        currentUser = null;
        System.out.println("User logged out.");
    }

    /**
     * Get current logged in user
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if user is logged in
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Check if current user is admin
     */
    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
}

