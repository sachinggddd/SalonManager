package com.salon.dao;

import com.salon.model.StockEntry;
import com.salon.util.DBConnection;
import java.sql.*;
import java.util.*;

public class StockDAO {
    
	// 🧾 Fetch all stock entries joined with product info (MODIFIED - Added brand)
	public List<StockEntry> getAllStockEntries() {
	    List<StockEntry> list = new ArrayList<>();
	    String sql = "SELECT s.stock_id, s.product_id, p.name AS product_name, p.brand AS product_brand, s.add_date, " +
	                 "s.quantity, s.actual_price_per_unit, s.selling_price_per_unit, s.notes, s.stock_status " +
	                 "FROM stock_entries s " +
	                 "JOIN products p ON s.product_id = p.product_id " +
	                 "ORDER BY s.add_date DESC";
	    
	    try (Connection conn = DBConnection.getConnection();
	         Statement st = conn.createStatement();
	         ResultSet rs = st.executeQuery(sql)) {
	        
	        while (rs.next()) {
	            StockEntry entry = new StockEntry();
	            entry.setStockId(rs.getInt("stock_id"));
	            entry.setProductId(rs.getInt("product_id"));
	            entry.setProductName(rs.getString("product_name"));
	            entry.setProductBrand(rs.getString("product_brand")); // ADDED
	            entry.setAddDate(rs.getDate("add_date"));
	            entry.setQuantity(rs.getDouble("quantity"));
	            entry.setActualPricePerUnit(rs.getDouble("actual_price_per_unit"));
	            entry.setSellingPricePerUnit(rs.getDouble("selling_price_per_unit"));
	            entry.setNotes(rs.getString("notes"));
	            entry.setStockStatus(rs.getString("stock_status"));
	            list.add(entry);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return list;
	}

	// 🧾 Fetch recent stock entries (limited) - MODIFIED - Added brand
	public List<StockEntry> getRecentStockEntries(int limit) {
	    List<StockEntry> list = new ArrayList<>();
	    String sql = "SELECT s.stock_id, s.product_id, p.name AS product_name, p.brand AS product_brand, s.add_date, " +
	                 "s.quantity, s.actual_price_per_unit, s.selling_price_per_unit, s.notes, s.stock_status " +
	                 "FROM stock_entries s " +
	                 "JOIN products p ON s.product_id = p.product_id " +
	                 "ORDER BY s.add_date DESC LIMIT ?";
	    
	    try (Connection conn = DBConnection.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql)) {
	        
	        ps.setInt(1, limit);
	        ResultSet rs = ps.executeQuery();
	        
	        while (rs.next()) {
	            StockEntry entry = new StockEntry();
	            entry.setStockId(rs.getInt("stock_id"));
	            entry.setProductId(rs.getInt("product_id"));
	            entry.setProductName(rs.getString("product_name"));
	            entry.setProductBrand(rs.getString("product_brand")); // ADDED
	            entry.setAddDate(rs.getDate("add_date"));
	            entry.setQuantity(rs.getDouble("quantity"));
	            entry.setActualPricePerUnit(rs.getDouble("actual_price_per_unit"));
	            entry.setSellingPricePerUnit(rs.getDouble("selling_price_per_unit"));
	            entry.setNotes(rs.getString("notes"));
	            entry.setStockStatus(rs.getString("stock_status"));
	            list.add(entry);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return list;
	}
    
    // Update stock status to COMPLETED when stock is depleted
    public boolean updateStockStatus(int productId, String status) {
        String sql = "UPDATE stock_entries SET stock_status = ? WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // ➕ Add new stock entry - OPTIMIZED VERSION
 // Add new stock entry - allows multiple RUNNING stocks (no auto-completion)
 // Previous stocks will be completed only when depleted during usage
    public boolean addStockEntry(StockEntry entry) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            // NO STATUS UPDATE - Allow multiple RUNNING stocks
            // Previous stocks will auto-complete when depleted during usage
            
            String insertSql = "INSERT INTO stock_entries (product_id, add_date, quantity, " +
                              "actual_price_per_unit, selling_price_per_unit, notes, stock_status) " +
                              "VALUES (?, ?, ?, ?, ?, ?, 'RUNNING')";
            try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                psInsert.setInt(1, entry.getProductId());
                psInsert.setDate(2, new java.sql.Date(entry.getAddDate().getTime()));
                psInsert.setDouble(3, entry.getQuantity());
                psInsert.setDouble(4, entry.getActualPricePerUnit());
                psInsert.setDouble(5, entry.getSellingPricePerUnit());
                psInsert.setString(6, entry.getNotes());
                psInsert.executeUpdate();
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
 // Get the oldest RUNNING stock for a product (FIFO - First In, First Out)
    public StockEntry getLastRunningStock(int productId) {
        String sql = "SELECT stock_id, quantity, actual_price_per_unit, selling_price_per_unit " +
                     "FROM stock_entries " +
                     "WHERE product_id = ? AND stock_status = 'RUNNING' " +
                     "ORDER BY add_date ASC LIMIT 1";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                StockEntry entry = new StockEntry();
                entry.setStockId(rs.getInt("stock_id"));
                entry.setQuantity(rs.getDouble("quantity"));
                entry.setActualPricePerUnit(rs.getDouble("actual_price_per_unit"));
                entry.setSellingPricePerUnit(rs.getDouble("selling_price_per_unit"));
                return entry;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
 // Mark a specific stock entry as COMPLETED when fully depleted
    public boolean completeStock(int stockId) {
        String sql = "UPDATE stock_entries SET stock_status = 'COMPLETED' WHERE stock_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stockId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
 // 📌 NEW METHOD: Get the LATEST stock entry for price checking (ANY STATUS)
 // This ensures price change detection works even after stock depletion
 // Used for: Price consistency validation across all stock entries
 public StockEntry getLastStockPricesAnyStatus(int productId) {
     String sql = "SELECT stock_id, quantity, actual_price_per_unit, selling_price_per_unit " +
                  "FROM stock_entries " +
                  "WHERE product_id = ? " +  // NO STATUS FILTER - checks ALL stocks
                  "ORDER BY add_date DESC LIMIT 1";  // Get most recent entry
     
     try (Connection conn = DBConnection.getConnection();
          PreparedStatement ps = conn.prepareStatement(sql)) {
         
         ps.setInt(1, productId);
         ResultSet rs = ps.executeQuery();
         
         if (rs.next()) {
             StockEntry entry = new StockEntry();
             entry.setStockId(rs.getInt("stock_id"));
             entry.setQuantity(rs.getDouble("quantity"));
             entry.setActualPricePerUnit(rs.getDouble("actual_price_per_unit"));
             entry.setSellingPricePerUnit(rs.getDouble("selling_price_per_unit"));
             return entry;
         }
     } catch (Exception e) {
         e.printStackTrace();
     }
     return null;
 }

 // Keep the existing getLastRunningStock() method as-is for FIFO stock usage
}