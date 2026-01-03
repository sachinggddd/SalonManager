package com.salon.dao;

import com.salon.model.InvoiceService;
import com.salon.model.InvoiceItem;
import com.salon.model.Customer;
import com.salon.model.StockMovement;
import com.salon.util.DBConnection;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.ByteArrayOutputStream;
import java.sql.*;
import java.util.List;
import java.util.Date;

public class InvoiceDAO {

    // Create invoice with services and/or products
	// REPLACE the createInvoice method in InvoiceDAO.java with this fixed version:

	public boolean createInvoice(String customerId, Customer customer, 
	                              List<InvoiceService> services, 
	                              List<InvoiceItem> products,
	                              double serviceDiscount) {

	    String insertInvoice = "INSERT INTO invoices (customer_id, invoice_date, total_services, total_products, total_amount, invoice_pdf, invoice_filename) VALUES (?, NOW(), ?, ?, ?, ?, ?)";
	    
	    // ✅ FIXED: Removed service_id from INSERT (it's auto-increment PRIMARY KEY)
	    String insertService = "INSERT INTO invoice_services (invoice_id, service_name, price, discount_amount, final_price) VALUES (?, ?, ?, ?, ?)";
	    
	    String insertItem = "INSERT INTO invoice_items (invoice_id, product_id, quantity, selling_price_per_unit, actual_price_per_unit, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
	    String insertMovement = "INSERT INTO stock_movements (product_id, movement_date, quantity_changed, movement_type, reference_id, remarks) VALUES (?, NOW(), ?, 'SALE', ?, ?)";

	    Connection conn = null;
	    PreparedStatement psInvoice = null;
	    PreparedStatement psService = null;
	    PreparedStatement psItem = null;
	    PreparedStatement psMovement = null;
	    ResultSet rs = null;

	    try {
	        conn = DBConnection.getConnection();
	        conn.setAutoCommit(false);

	        // Calculate totals with discount
	        double serviceSubtotal = services != null ? services.stream().mapToDouble(InvoiceService::getPrice).sum() : 0.0;
	        double serviceDiscountAmount = serviceSubtotal * (serviceDiscount / 100.0);
	        double serviceTotal = serviceSubtotal - serviceDiscountAmount;

	        double productTotal = products != null ? products.stream().mapToDouble(InvoiceItem::getSubtotal).sum() : 0.0;
	        double grandTotal = serviceTotal + productTotal;

	        // Generate PDF with discount info
	        byte[] pdfBytes = generateInvoicePDF(customerId, customer, services, products, 
	                                      serviceSubtotal, serviceDiscountAmount, 
	                                      serviceTotal, productTotal, grandTotal);
	        String filename = "invoice_" + customerId + ".pdf";

	        // Insert invoice
	        psInvoice = conn.prepareStatement(insertInvoice, Statement.RETURN_GENERATED_KEYS);
	        psInvoice.setString(1, customerId);
	        psInvoice.setDouble(2, serviceTotal); // After discount
	        psInvoice.setDouble(3, productTotal);
	        psInvoice.setDouble(4, grandTotal);
	        psInvoice.setBytes(5, pdfBytes);
	        psInvoice.setString(6, filename);
	        psInvoice.executeUpdate();

	        rs = psInvoice.getGeneratedKeys();
	        int invoiceId = -1;
	        if (rs.next()) invoiceId = rs.getInt(1);

	        if (invoiceId == -1) {
	            throw new SQLException("Failed to get invoice ID");
	        }

	        // ✅ FIXED: Save services WITHOUT service_id (let it auto-increment)
	        if (services != null && !services.isEmpty()) {
	            psService = conn.prepareStatement(insertService);
	            for (InvoiceService s : services) {
	                double basePrice = s.getPrice();
	                double discount = basePrice * (serviceDiscount / 100.0);
	                double finalPrice = basePrice - discount;
	                
	                psService.setInt(1, invoiceId);
	                // REMOVED: psService.setObject(2, s.getServiceId()); 
	                psService.setString(2, s.getServiceName());  // Now parameter 2
	                psService.setDouble(3, basePrice);           // Now parameter 3
	                psService.setDouble(4, discount);            // Now parameter 4
	                psService.setDouble(5, finalPrice);          // Now parameter 5
	                psService.addBatch();
	            }
	            psService.executeBatch();
	        }

	        // Save products and stock movements
	        if (products != null && !products.isEmpty()) {
	            psItem = conn.prepareStatement(insertItem);
	            psMovement = conn.prepareStatement(insertMovement);
	            
	            for (InvoiceItem item : products) {
	                // Insert invoice item
	                psItem.setInt(1, invoiceId);
	                psItem.setInt(2, item.getProductId());
	                psItem.setDouble(3, item.getQuantity());
	                psItem.setDouble(4, item.getSellingPricePerUnit());
	                psItem.setDouble(5, item.getActualPricePerUnit());
	                psItem.setDouble(6, item.getSubtotal());
	                psItem.addBatch();
	                
	                // Insert stock movement (negative quantity for sale)
	                psMovement.setInt(1, item.getProductId());
	                psMovement.setDouble(2, -item.getQuantity());
	                psMovement.setInt(3, invoiceId);
	                psMovement.setString(4, "Product sold to " + customer.getName());
	                psMovement.addBatch();
	            }
	            psItem.executeBatch();
	            psMovement.executeBatch();
	            
	            // Deplete stock in FIFO order for each product sold
	            for (InvoiceItem item : products) {
	                depleteStockForSale(item.getProductId(), item.getQuantity(), conn);
	            }
	        }

	        conn.commit();
	        return true;

	    } catch (Exception e) {
	        e.printStackTrace();
	        try {
	            if (conn != null) conn.rollback();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	        return false;
	    } finally {
	        try { 
	            if (rs != null) rs.close();
	            if (psMovement != null) psMovement.close();
	            if (psItem != null) psItem.close();
	            if (psService != null) psService.close();
	            if (psInvoice != null) psInvoice.close();
	            if (conn != null) {
	                conn.setAutoCommit(true);
	                conn.close();
	            }
	        } catch (SQLException ignored) {}
	    }
	}
    /**
     * 🔥 NEW METHOD: Depletes stock in FIFO order when products are sold
     * Marks oldest stocks as COMPLETED when fully used
     * Handles 150+ stock entries per product efficiently
     * 
     * @param productId Product being sold
     * @param quantitySold Quantity sold
     * @param conn Active database connection (for transaction)
     * @return true if successful
     */
    /**
     * 🔥 FIXED: Depletes stock in FIFO order when products are sold
     * Marks oldest stocks as COMPLETED when fully used
     * DOES NOT change quantity - only updates status
     * Stock summary relies on original quantities + stock_movements
     */
    /**
     * 🔥 FIXED: Depletes stock in FIFO order when products are sold
     * Marks oldest stocks as COMPLETED when fully used
     * Handles 150+ stock entries per product efficiently
     * 
     * Key improvements:
     * - Proper NULL handling for quantity_consumed
     * - Correct FIFO ordering (oldest first)
     * - Efficient batch processing
     */
    private boolean depleteStockForSale(int productId, double quantitySold, Connection conn) 
            throws SQLException {
        
        double remaining = quantitySold;
        
        // FIFO: Get stocks from OLDEST to NEWEST with consumed tracking
        String selectSql = "SELECT stock_id, quantity, COALESCE(quantity_consumed, 0) as consumed " +
                          "FROM stock_entries " +
                          "WHERE product_id = ? AND stock_status = 'RUNNING' " +
                          "ORDER BY add_date ASC, stock_id ASC";
        
        try (PreparedStatement psSelect = conn.prepareStatement(selectSql);
             PreparedStatement psUpdate = conn.prepareStatement(
                 "UPDATE stock_entries SET quantity_consumed = ?, stock_status = ? WHERE stock_id = ?")) {
            
            psSelect.setInt(1, productId);
            ResultSet rs = psSelect.executeQuery();
            
            while (rs.next() && remaining > 0) {
                int stockId = rs.getInt("stock_id");
                double stockQty = rs.getDouble("quantity");
                double consumed = rs.getDouble("consumed");
                
                double availableInThisStock = stockQty - consumed;
                
                // Skip if already fully consumed (safety check)
                if (availableInThisStock <= 0.001) {
                    psUpdate.setDouble(1, stockQty);
                    psUpdate.setString(2, "COMPLETED");
                    psUpdate.setInt(3, stockId);
                    psUpdate.executeUpdate();
                    continue;
                }
                
                if (remaining >= availableInThisStock) {
                    // Fully consume this stock
                    psUpdate.setDouble(1, stockQty); // quantity_consumed = total quantity
                    psUpdate.setString(2, "COMPLETED");
                    psUpdate.setInt(3, stockId);
                    psUpdate.executeUpdate();
                    remaining -= availableInThisStock;
                    
                } else {
                    // Partially consume this stock
                    double newConsumed = consumed + remaining;
                    psUpdate.setDouble(1, newConsumed);
                    psUpdate.setString(2, "RUNNING"); // Keep RUNNING
                    psUpdate.setInt(3, stockId);
                    psUpdate.executeUpdate();
                    remaining = 0;
                }
            }
        }
        
        return remaining <= 0.001; // Success if all quantity was depleted
    }
    // Generate PDF with services and products
    private byte[] generateInvoicePDF(String customerId, Customer customer, 
            List<InvoiceService> services, 
            List<InvoiceItem> products,
            double serviceSubtotal,
            double serviceDiscount,
            double serviceTotal, 
            double productTotal, 
            double grandTotal) throws Exception {

Document doc = new Document();
ByteArrayOutputStream baos = new ByteArrayOutputStream();

try {
PdfWriter.getInstance(doc, baos);
doc.open();

// Title
Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
Paragraph title = new Paragraph("Salon Invoice", titleFont);
title.setAlignment(Element.ALIGN_CENTER);
doc.add(title);
doc.add(new Paragraph(" "));

// Customer Details
Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
doc.add(new Paragraph("Customer ID: " + customerId, normalFont));
doc.add(new Paragraph("Customer: " + customer.getName(), normalFont));
doc.add(new Paragraph("Phone: " + customer.getPhone(), normalFont));
doc.add(new Paragraph("Address: " + (customer.getAddress() != null ? customer.getAddress() : "N/A"), normalFont));
doc.add(new Paragraph("Date: " + new java.util.Date(), normalFont));
doc.add(new Paragraph(" "));

// Services Table (if any)
if (services != null && !services.isEmpty()) {
Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
Paragraph servicesHeader = new Paragraph("Services", headerFont);
doc.add(servicesHeader);
doc.add(new Paragraph(" "));

PdfPTable serviceTable = new PdfPTable(2);
serviceTable.setWidthPercentage(100);
serviceTable.setWidths(new float[]{3, 1});

Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
PdfPCell headerCell1 = new PdfPCell(new Phrase("Service", tableHeaderFont));
PdfPCell headerCell2 = new PdfPCell(new Phrase("Price (₹)", tableHeaderFont));
headerCell1.setBackgroundColor(BaseColor.LIGHT_GRAY);
headerCell2.setBackgroundColor(BaseColor.LIGHT_GRAY);
headerCell1.setHorizontalAlignment(Element.ALIGN_CENTER);
headerCell2.setHorizontalAlignment(Element.ALIGN_CENTER);
serviceTable.addCell(headerCell1);
serviceTable.addCell(headerCell2);

for (InvoiceService s : services) {
PdfPCell serviceCell = new PdfPCell(new Phrase(s.getServiceName(), normalFont));
PdfPCell priceCell = new PdfPCell(new Phrase(String.format("%.2f", s.getPrice()), normalFont));
priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
serviceTable.addCell(serviceCell);
serviceTable.addCell(priceCell);
}

doc.add(serviceTable);

Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

// Show subtotal
Paragraph serviceSubtotalPara = new Paragraph("Services Subtotal: ₹" + String.format("%.2f", serviceSubtotal), normalFont);
serviceSubtotalPara.setAlignment(Element.ALIGN_RIGHT);
doc.add(serviceSubtotalPara);

// Show discount if applicable
if (serviceDiscount > 0) {
Paragraph discountPara = new Paragraph("Discount: -₹" + String.format("%.2f", serviceDiscount), 
FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.RED));
discountPara.setAlignment(Element.ALIGN_RIGHT);
doc.add(discountPara);
}

// Show total after discount
Paragraph serviceTotalPara = new Paragraph("Services Total: ₹" + String.format("%.2f", serviceTotal), totalFont);
serviceTotalPara.setAlignment(Element.ALIGN_RIGHT);
doc.add(serviceTotalPara);
doc.add(new Paragraph(" "));
}


            // Products Table (if any)
            if (products != null && !products.isEmpty()) {
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
                Paragraph productsHeader = new Paragraph("Products", headerFont);
                doc.add(productsHeader);
                doc.add(new Paragraph(" "));
                
                PdfPTable productTable = new PdfPTable(4);
                productTable.setWidthPercentage(100);
                productTable.setWidths(new float[]{3, 1, 1, 1});

                Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
                PdfPCell h1 = new PdfPCell(new Phrase("Product", tableHeaderFont));
                PdfPCell h2 = new PdfPCell(new Phrase("Qty", tableHeaderFont));
                PdfPCell h3 = new PdfPCell(new Phrase("Price (₹)", tableHeaderFont));
                PdfPCell h4 = new PdfPCell(new Phrase("Total (₹)", tableHeaderFont));
                h1.setBackgroundColor(BaseColor.LIGHT_GRAY);
                h2.setBackgroundColor(BaseColor.LIGHT_GRAY);
                h3.setBackgroundColor(BaseColor.LIGHT_GRAY);
                h4.setBackgroundColor(BaseColor.LIGHT_GRAY);
                h1.setHorizontalAlignment(Element.ALIGN_CENTER);
                h2.setHorizontalAlignment(Element.ALIGN_CENTER);
                h3.setHorizontalAlignment(Element.ALIGN_CENTER);
                h4.setHorizontalAlignment(Element.ALIGN_CENTER);
                productTable.addCell(h1);
                productTable.addCell(h2);
                productTable.addCell(h3);
                productTable.addCell(h4);

                for (InvoiceItem item : products) {
                    String productDisplay = item.getProductName() + " (" + item.getProductBrand() + ")";
                    PdfPCell c1 = new PdfPCell(new Phrase(productDisplay, normalFont));
                    PdfPCell c2 = new PdfPCell(new Phrase(String.format("%.2f", item.getQuantity()), normalFont));
                    PdfPCell c3 = new PdfPCell(new Phrase(String.format("%.2f", item.getSellingPricePerUnit()), normalFont));
                    PdfPCell c4 = new PdfPCell(new Phrase(String.format("%.2f", item.getSubtotal()), normalFont));
                    c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c3.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    c4.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    productTable.addCell(c1);
                    productTable.addCell(c2);
                    productTable.addCell(c3);
                    productTable.addCell(c4);
                }

                doc.add(productTable);
                
                Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
                Paragraph productSubtotal = new Paragraph("Products Subtotal: ₹" + String.format("%.2f", productTotal), totalFont);
                productSubtotal.setAlignment(Element.ALIGN_RIGHT);
                doc.add(productSubtotal);
                doc.add(new Paragraph(" "));
            }

            // Grand Total
            Font grandTotalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph totalPara = new Paragraph("Grand Total: ₹" + String.format("%.2f", grandTotal), grandTotalFont);
            totalPara.setAlignment(Element.ALIGN_RIGHT);
            doc.add(totalPara);

            doc.add(new Paragraph("\nThank you for visiting!", normalFont));

        } finally {
            doc.close();
        }
        
        return baos.toByteArray();
    }
}