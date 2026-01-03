package com.salon.ui;

import com.salon.model.User;
import javax.swing.*;

public class AdminDashboard extends JFrame {
    public AdminDashboard(User user) {
        setTitle("Admin Dashboard - " + user.getFullName());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Customers", new com.salon.ui.panel.CustomerPanel());
        tabs.add("Products", new com.salon.ui.panel.ProductPanel());
        tabs.add("Stock", new com.salon.ui.panel.StockPanel());
        // Reports tab could be added later

        add(tabs);
        setVisible(true);
    }
}
