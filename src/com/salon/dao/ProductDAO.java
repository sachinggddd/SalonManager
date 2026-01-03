package com.salon.dao;

import com.salon.model.Product;
import com.salon.util.DBConnection;
import java.sql.*;
import java.util.*;

public class ProductDAO {
    
    // 🔹 Add new product (default: CONTINUED)
    public boolean addProduct(Product p) {
        String sql = "INSERT INTO products (sku, name, brand, product_type, description, is_discontinued) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getSku());
            ps.setString(2, p.getName());
            ps.setString(3, p.getBrand());
            ps.setString(4, p.getProductType());
            ps.setString(5, p.getDescription());
            ps.setBoolean(6, p.isDiscontinued()); // Default false = CONTINUED
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // 🔹 Get all products
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getInt("product_id"));
                p.setSku(rs.getString("sku"));
                p.setName(rs.getString("name"));
                p.setBrand(rs.getString("brand"));
                p.setProductType(rs.getString("product_type"));
                p.setDescription(rs.getString("description"));
                p.setDiscontinued(rs.getBoolean("is_discontinued"));
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // 🔹 Helper for StockPanel - Get ONLY CONTINUED products
 // 🔹 Helper for StockPanel - Get ONLY CONTINUED products WITH BRAND
    public List<String[]> getProductList() {
        List<String[]> list = new ArrayList<>();
        // MODIFIED: Added brand to query
        String sql = "SELECT product_id, name, brand FROM products WHERE is_discontinued = 0 ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("product_id")), 
                    rs.getString("name"),
                    rs.getString("brand") // ADDED: Brand field
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }  
    // 🔹 Update product status (CONTINUED ↔ DISCONTINUED)
    public boolean updateProductStatus(int productId, boolean discontinued) {
        String sql = "UPDATE products SET is_discontinued = ? WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, discontinued);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
 // ✅ NEW: Get product type by ID for search by type at customer panel
    public String getProductType(int productId) {
        String sql = "SELECT product_type FROM products WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("product_type");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Other";
    }
}