package dao;

import models.User;
import database.DatabaseConnection;
import utils.PasswordUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import utils.Logger;

/**
 * User Data Access Object
 * Handles all database operations for User entity
 * Demonstrates Data Access Layer pattern
 */
public class UserDAO {
    private Connection connection;

    public UserDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Authenticate user login using hashed password
     */
    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String stored = rs.getString("password");
                if (PasswordUtils.verifyPassword(password, stored) || password.equals(stored)) { // fallback if legacy plain text exists
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            Logger.error("Authentication failed for user: " + username, e);
        }
        return null;
    }

    /**
     * Create a new user (hash password)
     */
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            String hashed = PasswordUtils.hashPassword(user.getPassword());
            pstmt.setString(2, hashed);
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getRole());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            Logger.error("Failed to create user: " + user.getUsername(), e);
        }
        return false;
    }

    /**
     * Get user by ID
     */
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            Logger.error("Failed to get user by id: " + userId, e);
        }
        return null;
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            Logger.error("Failed to get all users", e);
        }
        return users;
    }

    /**
     * Update user (rehash if password provided)
     */
    public boolean updateUser(User user) {
        boolean hasPassword = user.getPassword() != null && !user.getPassword().isEmpty();
        String sql = hasPassword ?
            "UPDATE users SET username = ?, password = ?, full_name = ?, role = ? WHERE user_id = ?" :
            "UPDATE users SET username = ?, full_name = ?, role = ? WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            int idx = 1;
            pstmt.setString(idx++, user.getUsername());
            if (hasPassword) {
                pstmt.setString(idx++, PasswordUtils.hashPassword(user.getPassword()));
            }
            pstmt.setString(idx++, user.getFullName());
            pstmt.setString(idx++, user.getRole());
            pstmt.setInt(idx, user.getUserId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            Logger.error("Failed to update user: " + user.getUserId(), e);
        }
        return false;
    }

    /**
     * Delete user
     */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            Logger.error("Failed to delete user: " + userId, e);
        }
        return false;
    }

    // Helper to map result set
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(rs.getString("role"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }
}
