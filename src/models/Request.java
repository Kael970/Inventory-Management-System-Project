package models;

import java.sql.Timestamp;

/**
 * Request Model Class
 * Represents a product restock request
 */
public class Request {
    private int requestId;
    private int productId;
    private String productName;
    private int requestedQuantity;
    // store the requesting user's id (foreign key to users.user_id)
    private Integer requestedByUserId; // nullable
    // friendly display name for the requester (populated when fetching via JOIN)
    private String requestedByName;
    private String status; // "Pending", "Approved", "Rejected"
    private Timestamp requestDate;

    // Constructors
    public Request() {
    }

    // For creating a request when you have a user id
    public Request(int productId, String productName, int requestedQuantity, int requestedByUserId) {
        this.productId = productId;
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.requestedByUserId = requestedByUserId;
        this.status = "Pending";
    }

    // Legacy constructor kept for backwards compatibility (accepts name)
    public Request(int productId, String productName, int requestedQuantity, String requestedBy) {
        this.productId = productId;
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.requestedByName = requestedBy;
        this.status = "Pending";
    }

    // Getters and Setters
    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(int requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public Integer getRequestedByUserId() {
        return requestedByUserId;
    }

    public void setRequestedByUserId(Integer requestedByUserId) {
        this.requestedByUserId = requestedByUserId;
    }

    public String getRequestedByName() {
        return requestedByName;
    }

    public void setRequestedByName(String requestedByName) {
        this.requestedByName = requestedByName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Timestamp requestDate) {
        this.requestDate = requestDate;
    }

    /**
     * Backwards-compatible accessor used by existing UI/export code.
     * Returns the requester's display name if available, otherwise an empty string or the user id.
     */
    public String getRequestedBy() {
        if (requestedByName != null && !requestedByName.isEmpty()) return requestedByName;
        if (requestedByUserId != null) return String.valueOf(requestedByUserId);
        return "";
    }

    /**
     * Legacy setter left for compatibility (stores in requestedByName).
     */
    public void setRequestedBy(String requestedBy) {
        this.requestedByName = requestedBy;
    }

    @Override
    public String toString() {
        return "Request{" +
                "requestId=" + requestId +
                ", productName='" + productName + '\'' +
                ", requestedQuantity=" + requestedQuantity +
                ", status='" + status + '\'' +
                '}';
    }
}
