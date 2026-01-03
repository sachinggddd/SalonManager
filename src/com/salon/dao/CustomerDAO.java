package com.salon.dao;

import com.salon.model.Customer;
import com.salon.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CustomerDAO {

    // Add a new customer
    public boolean addCustomer(Customer customer) {
        String sql = "INSERT INTO customers (customer_id, name, phone, address, created_at) VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String customerId = "CUST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            ps.setString(1, customerId);
            ps.setString(2, customer.getName());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getAddress());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                customer.setCustomerId(customerId);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get invoice PDF data by customer ID
    public byte[] getInvoicePdfByCustomerId(String customerId) {
        String sql = "SELECT invoice_pdf FROM invoices WHERE customer_id = ? ORDER BY invoice_id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBytes("invoice_pdf");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get invoice filename by customer ID
    public String getInvoiceFilenameByCustomerId(String customerId) {
        String sql = "SELECT invoice_filename FROM invoices WHERE customer_id = ? ORDER BY invoice_id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("invoice_filename");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get all customers
    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT customer_id, name, phone, address, created_at FROM customers ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Customer c = new Customer();
                c.setCustomerId(rs.getString("customer_id"));
                c.setName(rs.getString("name"));
                c.setPhone(rs.getString("phone"));
                c.setAddress(rs.getString("address"));
                c.setCreatedAt(rs.getTimestamp("created_at")); // ✅ Added this line
                list.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Get customer by ID
    public Customer getCustomerById(String customerId) {
        String sql = "SELECT customer_id, name, phone, address FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Customer c = new Customer();
                c.setCustomerId(rs.getString("customer_id"));
                c.setName(rs.getString("name"));
                c.setPhone(rs.getString("phone"));
                c.setAddress(rs.getString("address"));
                return c;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
 // ADD THESE METHODS TO YOUR EXISTING CustomerDAO class:

    // Update customer membership
    public boolean updateCustomerMembership(String customerId, Integer membershipPlanId) {
        String sql = "UPDATE customers SET membership_plan_id = ? WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setObject(1, membershipPlanId);
            ps.setString(2, customerId);
            
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get customer's membership plan ID
    public Integer getCustomerMembershipPlanId(String customerId) {
        String sql = "SELECT membership_plan_id FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, customerId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                Object planId = rs.getObject("membership_plan_id");
                return planId != null ? (Integer) planId : null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
 // Get customer by phone number (for auto-suggestion)
    public Customer getCustomerByPhone(String phone) {
        String sql = "SELECT customer_id, name, phone, address, membership_plan_id FROM customers WHERE phone = ? ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Customer c = new Customer();
                c.setCustomerId(rs.getString("customer_id"));
                c.setName(rs.getString("name"));
                c.setPhone(rs.getString("phone"));
                c.setAddress(rs.getString("address"));
                
                // Get membership plan ID
                Object planId = rs.getObject("membership_plan_id");
                if (planId != null) {
                    c.setMembershipPlanId((Integer) planId);
                }
                
                return c;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}