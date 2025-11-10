package models;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Product Model Class
 * Represents a product in the inventory
 * Demonstrates Encapsulation
 */
public class Product {
    private int productId;
    private String productName;
    private double buyingPrice;
    private double sellingPrice;
    private int stockQuantity;
    private int thresholdValue;
    private Date expiryDate;
    private String imagePath;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Constructors
    public Product() {
    }

    public Product(String productName, double buyingPrice, double sellingPrice,
                   int stockQuantity, int thresholdValue, Date expiryDate) {
        this.productName = productName;
        this.buyingPrice = buyingPrice;
        this.sellingPrice = sellingPrice;
        this.stockQuantity = stockQuantity;
        this.thresholdValue = thresholdValue;
        this.expiryDate = expiryDate;
    }

    // Getters and Setters
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

    public double getBuyingPrice() {
        return buyingPrice;
    }

    public void setBuyingPrice(double buyingPrice) {
        this.buyingPrice = buyingPrice;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public int getThresholdValue() {
        return thresholdValue;
    }

    public void setThresholdValue(int thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Business logic methods
    public boolean isLowStock() {
        return stockQuantity <= thresholdValue;
    }

    public boolean isOutOfStock() {
        return stockQuantity <= 0;
    }

    public String getAvailabilityStatus() {
        if (isOutOfStock()) {
            return "Out of stock";
        } else if (isLowStock()) {
            return "Low stock";
        } else {
            return "In stock";
        }
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", sellingPrice=" + sellingPrice +
                ", stockQuantity=" + stockQuantity +
                '}';
    }
}

