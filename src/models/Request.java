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
    private String requestedBy;
    private String status; // "Pending", "Approved", "Rejected"
    private Timestamp requestDate;

    // Constructors
    public Request() {
    }

    public Request(int productId, String productName, int requestedQuantity, String requestedBy) {
        this.productId = productId;
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.requestedBy = requestedBy;
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

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
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

