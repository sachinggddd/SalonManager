package com.salon.dao;

import com.salon.model.Service;
import com.salon.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    // Get all active services
    public List<Service> getActiveServices() {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT * FROM services WHERE is_active = 1 ORDER BY service_category, service_name";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                services.add(mapResultSetToService(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return services;
    }

    // Get all services (including inactive)
    public List<Service> getAllServices() {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT * FROM services ORDER BY is_active DESC, service_category, service_name";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                services.add(mapResultSetToService(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return services;
    }

    // Add new service
    public boolean addService(Service service) {
        String sql = "INSERT INTO services (service_name, service_category, base_price, description, " +
                    "duration_minutes, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, service.getServiceName());
            ps.setString(2, service.getServiceCategory());
            ps.setDouble(3, service.getBasePrice());
            ps.setString(4, service.getDescription());
            ps.setObject(5, service.getDurationMinutes());
            ps.setBoolean(6, service.isActive());
            
            int rows = ps.executeUpdate();
            
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    service.setServiceId(rs.getInt(1));
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Update service
    public boolean updateService(Service service) {
        String sql = "UPDATE services SET service_name = ?, service_category = ?, base_price = ?, " +
                    "description = ?, duration_minutes = ?, is_active = ? WHERE service_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, service.getServiceName());
            ps.setString(2, service.getServiceCategory());
            ps.setDouble(3, service.getBasePrice());
            ps.setString(4, service.getDescription());
            ps.setObject(5, service.getDurationMinutes());
            ps.setBoolean(6, service.isActive());
            ps.setInt(7, service.getServiceId());
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Discontinue service (soft delete)
    public boolean discontinueService(int serviceId) {
        String sql = "UPDATE services SET is_active = 0 WHERE service_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, serviceId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Reactivate service
    public boolean reactivateService(int serviceId) {
        String sql = "UPDATE services SET is_active = 1 WHERE service_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, serviceId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get service by ID
    public Service getServiceById(int serviceId) {
        String sql = "SELECT * FROM services WHERE service_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, serviceId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToService(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get distinct categories
    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT service_category FROM services WHERE service_category IS NOT NULL ORDER BY service_category";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                categories.add(rs.getString("service_category"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return categories;
    }

    // Helper method to map ResultSet to Service object
    private Service mapResultSetToService(ResultSet rs) throws SQLException {
        Service service = new Service();
        service.setServiceId(rs.getInt("service_id"));
        service.setServiceName(rs.getString("service_name"));
        service.setServiceCategory(rs.getString("service_category"));
        service.setBasePrice(rs.getDouble("base_price"));
        service.setDescription(rs.getString("description"));
        
        Integer duration = (Integer) rs.getObject("duration_minutes");
        service.setDurationMinutes(duration);
        
        service.setActive(rs.getBoolean("is_active"));
        service.setCreatedAt(rs.getTimestamp("created_at"));
        service.setUpdatedAt(rs.getTimestamp("updated_at"));
        
        return service;
    }
}