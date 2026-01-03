package com.salon.model;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StockMovement {
    private int movementId;
    private int productId;
    private String productName; // For joined queries
    private String productBrand; // ADDED: Brand field
    private Date movementDate;
    private double quantityChanged; // Negative for usage/sale, positive for restock
    private String movementType; // SALE, USAGE, RESTOCK, ADJUSTMENT, DISCONTINUE
    private Integer referenceId; // Reference to invoice_id or other table
    private Integer userId;
    private String userName; // For joined queries
    private String remarks;
    
    
    private double sellingPricePerUnit;
    private double actualPricePerUnit;
    private String customerName;
    
    // Constructors
    public StockMovement() {
    }
    
    // Getters and Setters
    public int getMovementId() {
        return movementId;
    }
    public void setMovementId(int movementId) {
        this.movementId = movementId;
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
    
    // ADDED: Brand getter/setter
    public String getProductBrand() {
        return productBrand;
    }
    public void setProductBrand(String productBrand) {
        this.productBrand = productBrand;
    }
    
    public Date getMovementDate() {
        return movementDate;
    }
    public void setMovementDate(Date movementDate) {
        this.movementDate = movementDate;
    }
    
    public double getQuantityChanged() {
        return quantityChanged;
    }
    public void setQuantityChanged(double quantityChanged) {
        this.quantityChanged = quantityChanged;
    }
    
    public String getMovementType() {
        return movementType;
    }
    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }
    
    public Integer getReferenceId() {
        return referenceId;
    }
    public void setReferenceId(Integer referenceId) {
        this.referenceId = referenceId;
    }
    
    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getRemarks() {
        return remarks;
    }
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    public double getSellingPricePerUnit() {
        return sellingPricePerUnit;
    }

    public void setSellingPricePerUnit(double sellingPricePerUnit) {
        this.sellingPricePerUnit = sellingPricePerUnit;
    }

    public double getActualPricePerUnit() {
        return actualPricePerUnit;
    }

    public void setActualPricePerUnit(double actualPricePerUnit) {
        this.actualPricePerUnit = actualPricePerUnit;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    // Helper method to format date
    public String getMovementDateFormatted() {
        if (movementDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return sdf.format(movementDate);
        }
        return "";
    }
    
    @Override
    public String toString() {
        return "StockMovement{" +
                "movementId=" + movementId +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", productBrand='" + productBrand + '\'' +
                ", movementDate=" + movementDate +
                ", quantityChanged=" + quantityChanged +
                ", movementType='" + movementType + '\'' +
                ", referenceId=" + referenceId +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", remarks='" + remarks + '\'' +
                '}';
    }
}