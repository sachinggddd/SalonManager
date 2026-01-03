package com.salon.ui;

import com.salon.model.User;
import com.salon.ui.panel.*;
import javax.swing.*;

public class StaffDashboard extends JFrame {
    private User currentUser;
    
    public StaffDashboard(User user) {
        this.currentUser = user;
        
        setTitle("Staff Dashboard - " + user.getFullName());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JTabbedPane tabs = new JTabbedPane();
        
        // Existing tabs
        tabs.add("Customers", new CustomerPanel());
        tabs.add("Products", new ProductPanel());
        tabs.add("Add Stock", new StockPanel());
        
        // New Product Management tabs
        tabs.add("Product Usage", new ProductUsagePanel(user.getUserId()));
        tabs.add("Stock Summary", new StockSummaryPanel());
        tabs.add("Product Sale" , new ProductSalePanel());
        tabs.add("Analytics ", new AnalyticsPanel(user.getUserId()));
        tabs.add("Services", new ServicesPanel());
        add(tabs);
        setVisible(true);
    }
}