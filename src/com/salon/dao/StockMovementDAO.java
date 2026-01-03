package com.salon.dao;

import com.salon.model.StockMovement;
import com.salon.util.DBConnection;
import java.sql.*;
import java.util.*;

public class StockMovementDAO {

    // 📝 Add new stock movement
    public boolean addStockMovement(StockMovement movement) {
        String sql = "INSERT INTO stock_movements (product_id, movement_date, quantity_changed, " +
                     "movement_type, reference_id, user_id, remarks) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, movement.getProductId());
            ps.setTimestamp(2, new Timestamp(movement.getMovementDate().getTime()));
            ps.setDouble(3, movement.getQuantityChanged());
            ps.setString(4, movement.getMovementType());
            ps.setObject(5, movement.getReferenceId()); // Can be null
            ps.setObject(6, movement.getUserId()); // Can be null
            ps.setString(7, movement.getRemarks());
            
            return ps.executeUpdate() > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 📊 Get all USAGE movements with product and user info
 // 📊 Get all USAGE movements with product and user info (MODIFIED - Added brand)
    public List<StockMovement> getUsageMovements() {
        List<StockMovement> list = new ArrayList<>();
        String sql = "SELECT sm.movement_id, sm.product_id, p.name AS product_name, p.brand AS product_brand, " +
                     "sm.movement_date, sm.quantity_changed, sm.movement_type, " +
                     "sm.user_id, u.full_name AS user_name, sm.remarks " +
                     "FROM stock_movements sm " +
                     "JOIN products p ON sm.product_id = p.product_id " +
                     "LEFT JOIN users u ON sm.user_id = u.user_id " +
                     "WHERE sm.movement_type = 'USAGE' " +
                     "ORDER BY sm.movement_date DESC";
        
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                StockMovement movement = new StockMovement();
                movement.setMovementId(rs.getInt("movement_id"));
                movement.setProductId(rs.getInt("product_id"));
                movement.setProductName(rs.getString("product_name"));
                movement.setProductBrand(rs.getString("product_brand")); // ADDED
                movement.setMovementDate(rs.getTimestamp("movement_date"));
                movement.setQuantityChanged(rs.getDouble("quantity_changed"));
                movement.setMovementType(rs.getString("movement_type"));
                movement.setUserId(rs.getInt("user_id"));
                movement.setUserName(rs.getString("user_name"));
                movement.setRemarks(rs.getString("remarks"));
                list.add(movement);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
 // Get all SALE movements with product and customer info
 // Add this method to StockMovementDAO.java
 // Replace the existing getSaleMovements() if you added it earlier

 public List<StockMovement> getSaleMovements() {
     List<StockMovement> list = new ArrayList<>();
     String sql = "SELECT " +
                  "    sm.movement_id, " +
                  "    sm.product_id, " +
                  "    p.name AS product_name, " +
                  "    p.brand AS product_brand, " +
                  "    sm.movement_date, " +
                  "    sm.quantity_changed, " +
                  "    sm.movement_type, " +
                  "    sm.reference_id, " +
                  "    c.name AS customer_name, " +
                  "    sm.remarks, " +
                  "    ii.selling_price_per_unit, " +
                  "    ii.actual_price_per_unit " +
                  "FROM stock_movements sm " +
                  "JOIN products p ON sm.product_id = p.product_id " +
                  "LEFT JOIN invoices inv ON sm.reference_id = inv.invoice_id " +
                  "LEFT JOIN customers c ON inv.customer_id = c.customer_id " +
                  "LEFT JOIN invoice_items ii ON (sm.reference_id = ii.invoice_id AND sm.product_id = ii.product_id) " +
                  "WHERE sm.movement_type = 'SALE' " +
                  "ORDER BY sm.movement_date DESC";
     
     try (Connection conn = DBConnection.getConnection();
          Statement st = conn.createStatement();
          ResultSet rs = st.executeQuery(sql)) {
         
         while (rs.next()) {
             StockMovement movement = new StockMovement();
             movement.setMovementId(rs.getInt("movement_id"));
             movement.setProductId(rs.getInt("product_id"));
             movement.setProductName(rs.getString("product_name"));
             movement.setProductBrand(rs.getString("product_brand"));
             movement.setMovementDate(rs.getTimestamp("movement_date"));
             movement.setQuantityChanged(rs.getDouble("quantity_changed"));
             movement.setMovementType(rs.getString("movement_type"));
             movement.setReferenceId(rs.getInt("reference_id"));
             movement.setCustomerName(rs.getString("customer_name"));
             movement.setRemarks(rs.getString("remarks"));
             
             // Get prices from invoice_items
             double sellingPrice = rs.getDouble("selling_price_per_unit");
             double actualPrice = rs.getDouble("actual_price_per_unit");
             
             movement.setSellingPricePerUnit(sellingPrice);
             movement.setActualPricePerUnit(actualPrice);
             
             list.add(movement);
         }
         
     } catch (Exception e) {
         e.printStackTrace();
     }
     return list;
 }
    // 📦 Calculate available stock for a product
    public double getAvailableStock(int productId) {
        double totalStock = 0.0;
        
        // Get total stock from stock_entries
        String sqlEntries = "SELECT COALESCE(SUM(quantity), 0) AS total FROM stock_entries WHERE product_id = ?";
        
        // Get total used/sold from stock_movements
        String sqlMovements = "SELECT COALESCE(SUM(quantity_changed), 0) AS total FROM stock_movements " +
                              "WHERE product_id = ? AND movement_type IN ('USAGE', 'SALE')";
        
        try (Connection conn = DBConnection.getConnection()) {
            // Get added stock
            try (PreparedStatement ps = conn.prepareStatement(sqlEntries)) {
                ps.setInt(1, productId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    totalStock = rs.getDouble("total");
                }
            }
            
            // Subtract used/sold stock
            try (PreparedStatement ps = conn.prepareStatement(sqlMovements)) {
                ps.setInt(1, productId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    totalStock += rs.getDouble("total"); // quantity_changed is negative for USAGE/SALE
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return totalStock;
    }

    // 📊 Get all movements (for reports/admin view)
    public List<StockMovement> getAllMovements() {
        List<StockMovement> list = new ArrayList<>();
        String sql = "SELECT sm.movement_id, sm.product_id, p.name AS product_name, " +
                     "sm.movement_date, sm.quantity_changed, sm.movement_type, " +
                     "sm.reference_id, sm.user_id, u.full_name AS user_name, sm.remarks " +
                     "FROM stock_movements sm " +
                     "JOIN products p ON sm.product_id = p.product_id " +
                     "LEFT JOIN users u ON sm.user_id = u.user_id " +
                     "ORDER BY sm.movement_date DESC";
        
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                StockMovement movement = new StockMovement();
                movement.setMovementId(rs.getInt("movement_id"));
                movement.setProductId(rs.getInt("product_id"));
                movement.setProductName(rs.getString("product_name"));
                movement.setMovementDate(rs.getTimestamp("movement_date"));
                movement.setQuantityChanged(rs.getDouble("quantity_changed"));
                movement.setMovementType(rs.getString("movement_type"));
                movement.setReferenceId(rs.getInt("reference_id"));
                movement.setUserId(rs.getInt("user_id"));
                movement.setUserName(rs.getString("user_name"));
                movement.setRemarks(rs.getString("remarks"));
                list.add(movement);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 📈 Get stock summary for all products - FIXED VERSION
    public List<Map<String, Object>> getStockSummary() {
        List<Map<String, Object>> summary = new ArrayList<>();
        
        String sql = 
            "SELECT " +
            "    p.product_id, " +
            "    p.name, " +
            "    p.brand, " +
            "    COALESCE(se_sum.total_added, 0) AS total_added, " +
            "    COALESCE(ABS(sm_sum.total_used), 0) AS total_used, " +
            "    (COALESCE(se_sum.total_added, 0) - COALESCE(ABS(sm_sum.total_used), 0)) AS available_stock " +
            "FROM products p " +
            "LEFT JOIN ( " +
            "    SELECT product_id, SUM(quantity) AS total_added " +
            "    FROM stock_entries " +
            "    GROUP BY product_id " +
            ") se_sum ON p.product_id = se_sum.product_id " +
            "LEFT JOIN ( " +
            "    SELECT product_id, SUM(quantity_changed) AS total_used " +
            "    FROM stock_movements " +
            "    WHERE movement_type IN ('USAGE', 'SALE') " +
            "    GROUP BY product_id " +
            ") sm_sum ON p.product_id = sm_sum.product_id " +
            "WHERE p.is_discontinued = 0 " +
            "ORDER BY p.name";
        
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("product_id", rs.getInt("product_id"));
                row.put("name", rs.getString("name"));
                row.put("brand", rs.getString("brand"));
                row.put("total_added", rs.getDouble("total_added"));
                row.put("total_used", rs.getDouble("total_used"));
                row.put("available_stock", rs.getDouble("available_stock"));
                summary.add(row);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return summary;
    }
    
 // 🔥 FIX 2: FIFO Stock Depletion with Proper Status Updates
 // Automatically marks oldest stock as COMPLETED when fully depleted
 // Handles: 150+ stock entries per product efficiently
    public boolean recordUsageWithStockDepletion(int productId, double quantityUsed, 
            Integer userId, String remarks) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            double remaining = quantityUsed;

            // === FIFO: Get stocks from OLDEST to NEWEST ===
            String sql = "SELECT stock_id, quantity FROM stock_entries " +
                         "WHERE product_id = ? AND stock_status = 'RUNNING' " +
                         "ORDER BY add_date ASC";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, productId);
                ResultSet rs = ps.executeQuery();

                while (rs.next() && remaining > 0) {
                    int stockId = rs.getInt("stock_id");
                    double stockQty = rs.getDouble("quantity");

                    // ✅ FIXED: Correct comparison logic
                    if (remaining >= stockQty) {
                        // === CASE 1: Stock fully consumed - Mark as COMPLETED ===
                        String completeSql = "UPDATE stock_entries SET stock_status = 'COMPLETED' " +
                                           "WHERE stock_id = ?";
                        try (PreparedStatement psComplete = conn.prepareStatement(completeSql)) {
                            psComplete.setInt(1, stockId);
                            psComplete.executeUpdate();
                        }
                        remaining -= stockQty;
                        
                    } else {
                        // === CASE 2: Partial consumption - Stock stays RUNNING ===
                        // Do nothing - quantity unchanged
                        remaining = 0;
                    }
                }
            }

            // === Record movement in stock_movements table ===
            StockMovement movement = new StockMovement();
            movement.setProductId(productId);
            movement.setMovementDate(new java.util.Date());
            movement.setQuantityChanged(-quantityUsed);  // Negative for usage
            movement.setMovementType("USAGE");
            movement.setUserId(userId);
            movement.setRemarks(remarks);

            String insertSql = "INSERT INTO stock_movements (product_id, movement_date, quantity_changed, " +
                              "movement_type, user_id, remarks) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, movement.getProductId());
                ps.setTimestamp(2, new Timestamp(movement.getMovementDate().getTime()));
                ps.setDouble(3, movement.getQuantityChanged());
                ps.setString(4, movement.getMovementType());
                ps.setObject(5, movement.getUserId());
                ps.setString(6, movement.getRemarks());
                ps.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}