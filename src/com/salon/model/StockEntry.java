package com.salon.model;

import java.util.Date;

public class StockEntry {
    private int stockId;
    private int productId;
    private String productName; // for displaying joined data
    private String productBrand; // ADDED: Brand field
    private Date addDate;
    private double quantity;
    private double actualPricePerUnit;
    private double sellingPricePerUnit;
    private String notes;
    private String stockStatus;

    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    // ADDED: Brand getter/setter
    public String getProductBrand() { return productBrand; }
    public void setProductBrand(String productBrand) { this.productBrand = productBrand; }

    public Date getAddDate() { return addDate; }
    public void setAddDate(Date addDate) { this.addDate = addDate; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public double getActualPricePerUnit() { return actualPricePerUnit; }
    public void setActualPricePerUnit(double actualPricePerUnit) { this.actualPricePerUnit = actualPricePerUnit; }

    public double getSellingPricePerUnit() { return sellingPricePerUnit; }
    public void setSellingPricePerUnit(double sellingPricePerUnit) { this.sellingPricePerUnit = sellingPricePerUnit; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }
}
