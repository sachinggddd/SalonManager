package com.salon.dao;

import com.salon.model.MembershipPlan;
import com.salon.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MembershipDAO {

    // Get all active membership plans
    public List<MembershipPlan> getActivePlans() {
        List<MembershipPlan> plans = new ArrayList<>();
        String sql = "SELECT * FROM membership_plans WHERE is_active = 1 ORDER BY discount_percentage ASC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                plans.add(mapResultSetToPlan(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plans;
    }

    // Get all membership plans (including inactive)
    public List<MembershipPlan> getAllPlans() {
        List<MembershipPlan> plans = new ArrayList<>();
        String sql = "SELECT * FROM membership_plans ORDER BY is_active DESC, discount_percentage ASC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                plans.add(mapResultSetToPlan(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return plans;
    }

    // Get membership plan by ID
    public MembershipPlan getPlanById(int planId) {
        String sql = "SELECT * FROM membership_plans WHERE plan_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, planId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToPlan(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Add new membership plan
    public boolean addPlan(MembershipPlan plan) {
        String sql = "INSERT INTO membership_plans (plan_name, discount_percentage, description, is_active) " +
                    "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, plan.getPlanName());
            ps.setDouble(2, plan.getDiscountPercentage());
            ps.setString(3, plan.getDescription());
            ps.setBoolean(4, plan.isActive());
            
            int rows = ps.executeUpdate();
            
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    plan.setPlanId(rs.getInt(1));
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Update membership plan
    public boolean updatePlan(MembershipPlan plan) {
        String sql = "UPDATE membership_plans SET plan_name = ?, discount_percentage = ?, " +
                    "description = ?, is_active = ? WHERE plan_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, plan.getPlanName());
            ps.setDouble(2, plan.getDiscountPercentage());
            ps.setString(3, plan.getDescription());
            ps.setBoolean(4, plan.isActive());
            ps.setInt(5, plan.getPlanId());
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Deactivate membership plan
    public boolean deactivatePlan(int planId) {
        String sql = "UPDATE membership_plans SET is_active = 0 WHERE plan_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, planId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Helper method to map ResultSet to MembershipPlan
    private MembershipPlan mapResultSetToPlan(ResultSet rs) throws SQLException {
        MembershipPlan plan = new MembershipPlan();
        plan.setPlanId(rs.getInt("plan_id"));
        plan.setPlanName(rs.getString("plan_name"));
        plan.setDiscountPercentage(rs.getDouble("discount_percentage"));
        plan.setDescription(rs.getString("description"));
        plan.setActive(rs.getBoolean("is_active"));
        plan.setCreatedAt(rs.getTimestamp("created_at"));
        return plan;
    }
}