package com.salon.model;

public class InvoiceItem {
    private int itemId;
    private int invoiceId;
    private int productId;
    private String productName; // For display
    private String productBrand; // For display
    private double quantity;
    private double sellingPricePerUnit;
    private double actualPricePerUnit;
    private double subtotal;
    
    // Constructors
    public InvoiceItem() {
    }
    
    public InvoiceItem(int productId, String productName, String productBrand, 
                      double quantity, double sellingPrice, double actualPrice) {
        this.productId = productId;
        this.productName = productName;
        this.productBrand = productBrand;
        this.quantity = quantity;
        this.sellingPricePerUnit = sellingPrice;
        this.actualPricePerUnit = actualPrice;
        this.subtotal = quantity * sellingPrice;
    }
    
    // Getters and Setters
    public int getItemId() {
        return itemId;
    }
    
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
    
    public int getInvoiceId() {
        return invoiceId;
    }
    
    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
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
    
    public String getProductBrand() {
        return productBrand;
    }
    
    public void setProductBrand(String productBrand) {
        this.productBrand = productBrand;
    }
    
    public double getQuantity() {
        return quantity;
    }
    
    public void setQuantity(double quantity) {
        this.quantity = quantity;
        this.subtotal = quantity * sellingPricePerUnit;
    }
    
    public double getSellingPricePerUnit() {
        return sellingPricePerUnit;
    }
    
    public void setSellingPricePerUnit(double sellingPricePerUnit) {
        this.sellingPricePerUnit = sellingPricePerUnit;
        this.subtotal = quantity * sellingPricePerUnit;
    }
    
    public double getActualPricePerUnit() {
        return actualPricePerUnit;
    }
    
    public void setActualPricePerUnit(double actualPricePerUnit) {
        this.actualPricePerUnit = actualPricePerUnit;
    }
    
    public double getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
    
    @Override
    public String toString() {
        return "InvoiceItem{" +
                "productName='" + productName + '\'' +
                ", brand='" + productBrand + '\'' +
                ", quantity=" + quantity +
                ", price=" + sellingPricePerUnit +
                ", subtotal=" + subtotal +
                '}';
    }
}