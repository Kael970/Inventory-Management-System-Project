package dao;

import models.Request;
import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import utils.Logger;

/**
 * Request Data Access Object
 * Handles all database operations for Request entity
 */
public class RequestDAO {
    private Connection connection;

    public RequestDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Create a new request
     */
    public boolean createRequest(Request request) {
        String sql = "INSERT INTO requests (product_id, product_name, requested_quantity, requested_by_user_id, requested_by_name, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, request.getProductId());
            pstmt.setString(2, request.getProductName());
            pstmt.setInt(3, request.getRequestedQuantity());
            if (request.getRequestedByUserId() != null) pstmt.setInt(4, request.getRequestedByUserId()); else pstmt.setNull(4, java.sql.Types.INTEGER);
            pstmt.setString(5, request.getRequestedByName());
            pstmt.setString(6, request.getStatus());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            Logger.error("Failed to create request for product id: " + request.getProductId(), e);
        }
        return false;
    }

    /**
     * Get all requests
     */
    public List<Request> getAllRequests() {
        List<Request> requests = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name as requested_by_name_from_user FROM requests r LEFT JOIN users u ON r.requested_by_user_id = u.user_id ORDER BY r.request_date DESC";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                requests.add(extractRequestFromResultSet(rs));
            }
        } catch (SQLException e) {
            Logger.error("Failed to get all requests", e);
        }
        return requests;
    }

    /**
     * Get pending requests
     */
    public List<Request> getPendingRequests() {
        List<Request> requests = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name as requested_by_name_from_user FROM requests r LEFT JOIN users u ON r.requested_by_user_id = u.user_id WHERE r.status = 'Pending' ORDER BY r.request_date DESC";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                requests.add(extractRequestFromResultSet(rs));
            }
        } catch (SQLException e) {
            Logger.error("Failed to get pending requests", e);
        }
        return requests;
    }

    /**
     * Get requests for a specific user (requested_by_user_id)
     */
    public List<Request> getRequestsByUser(int requestedByUserId) {
        List<Request> requests = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name as requested_by_name_from_user FROM requests r LEFT JOIN users u ON r.requested_by_user_id = u.user_id WHERE r.requested_by_user_id = ? ORDER BY r.request_date DESC";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, requestedByUserId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                requests.add(extractRequestFromResultSet(rs));
            }
        } catch (SQLException e) {
            Logger.error("Failed to get requests by user id: " + requestedByUserId, e);
        }
        return requests;
    }

    /**
     * Legacy: get requests by matching requested_by_name (string). Kept for compatibility.
     */
    public List<Request> getRequestsByUser(String requestedByName) {
        List<Request> requests = new ArrayList<>();
        String sql = "SELECT * FROM requests WHERE requested_by_name = ? ORDER BY request_date DESC";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, requestedByName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                requests.add(extractRequestFromResultSet(rs));
            }
        } catch (SQLException e) {
            Logger.error("Failed to get requests by user name: " + requestedByName, e);
        }
        return requests;
    }

    /**
     * Update request status
     */
    public boolean updateRequestStatus(int requestId, String status) {
        String sql = "UPDATE requests SET status = ? WHERE request_id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, requestId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            Logger.error("Failed to update request status id: " + requestId, e);
        }
        return false;
    }

    /**
     * Delete request
     */
    public boolean deleteRequest(int requestId) {
        String sql = "DELETE FROM requests WHERE request_id = ?";
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, requestId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            Logger.error("Failed to delete request id: " + requestId, e);
        }
        return false;
    }

    /**
     * Get requests count
     */
    public int getRequestsCount() {
        String sql = "SELECT COUNT(*) FROM requests WHERE status = 'Pending'";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            Logger.error("Failed to count pending requests", e);
        }
        return 0;
    }

    /**
     * Extract Request object from ResultSet
     */
    private Request extractRequestFromResultSet(ResultSet rs) throws SQLException {
        Request request = new Request();
        request.setRequestId(rs.getInt("request_id"));
        request.setProductId(rs.getInt("product_id"));
        request.setProductName(rs.getString("product_name"));
        request.setRequestedQuantity(rs.getInt("requested_quantity"));
        // read numeric user id if available
        int uid = rs.getInt("requested_by_user_id");
        if (!rs.wasNull()) request.setRequestedByUserId(uid);
        // prefer joined full_name if available
        String joinedName = null;
        try { joinedName = rs.getString("requested_by_name_from_user"); } catch (SQLException ignored) {}
        if (joinedName == null || joinedName.isEmpty()) {
            // fallback to stored requested_by_name column
            try { joinedName = rs.getString("requested_by_name"); } catch (SQLException ignored) {}
        }
        request.setRequestedByName(joinedName);
        request.setStatus(rs.getString("status"));
        request.setRequestDate(rs.getTimestamp("request_date"));
        return request;
    }
}
