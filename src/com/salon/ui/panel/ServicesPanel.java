package com.salon.ui.panel;

import com.salon.dao.ServiceDAO;
import com.salon.model.Service;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class ServicesPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private ServiceDAO serviceDAO;

    public ServicesPanel() {
        this.serviceDAO = new ServiceDAO();
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 248, 255));

        // Title
        JLabel title = new JLabel("💇 Services Management", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(40, 60, 120));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(
            new String[]{"ID", "Service Name", "Category", "Base Price", "Duration (min)", "Status"},
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(model);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);

        loadServices();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Service List"));
        add(scroll, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(245, 248, 255));
        
        JButton btnAdd = new JButton("➕ Add Service");
        JButton btnEdit = new JButton("✏️ Edit Service");
        JButton btnDiscontinue = new JButton("🚫 Discontinue");
        JButton btnReactivate = new JButton("✅ Reactivate");
        JButton btnRefresh = new JButton("🔄 Refresh");
        
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDiscontinue.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnReactivate.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        btnAdd.setBackground(new Color(0, 153, 0));
        btnAdd.setForeground(Color.WHITE);
        btnDiscontinue.setBackground(new Color(204, 0, 0));
        btnDiscontinue.setForeground(Color.WHITE);
        btnReactivate.setBackground(new Color(0, 102, 204));
        btnReactivate.setForeground(Color.WHITE);
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDiscontinue);
        buttonPanel.add(btnReactivate);
        buttonPanel.add(btnRefresh);
        
        add(buttonPanel, BorderLayout.SOUTH);

        // Button actions
        btnAdd.addActionListener(this::addService);
        btnEdit.addActionListener(this::editService);
        btnDiscontinue.addActionListener(this::discontinueService);
        btnReactivate.addActionListener(this::reactivateService);
        btnRefresh.addActionListener(e -> loadServices());
    }

    private void loadServices() {
        model.setRowCount(0);
        List<Service> services = serviceDAO.getAllServices();
        
        for (Service service : services) {
            model.addRow(new Object[]{
                service.getServiceId(),
                service.getServiceName(),
                service.getServiceCategory(),
                String.format("₹%.2f", service.getBasePrice()),
                service.getDurationMinutes() != null ? service.getDurationMinutes() : "-",
                service.isActive() ? "Active" : "Discontinued"
            });
        }
    }

    private void addService(ActionEvent e) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField nameField = new JTextField(20);
        JComboBox<String> categoryCombo = new JComboBox<>();
        categoryCombo.setEditable(true);
        
        // Load existing categories
        List<String> categories = serviceDAO.getCategories();
        for (String cat : categories) {
            categoryCombo.addItem(cat);
        }
        
        JTextField priceField = new JTextField(10);
        JTextField durationField = new JTextField(10);
        JTextArea descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        
        panel.add(new JLabel("Service Name: *"));
        panel.add(nameField);
        panel.add(new JLabel("Category:"));
        panel.add(categoryCombo);
        panel.add(new JLabel("Base Price (₹): *"));
        panel.add(priceField);
        panel.add(new JLabel("Duration (minutes):"));
        panel.add(durationField);
        panel.add(new JLabel("Description:"));
        panel.add(descScroll);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Service",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String category = categoryCombo.getSelectedItem() != null ? 
                categoryCombo.getSelectedItem().toString().trim() : null;
            String priceStr = priceField.getText().trim();
            String durationStr = durationField.getText().trim();
            String desc = descArea.getText().trim();
            
            // Validation
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Service name is required!", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Base price is required!", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                double price = Double.parseDouble(priceStr);
                
                if (price < 0) {
                    JOptionPane.showMessageDialog(this, "Price cannot be negative!", 
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                Service service = new Service(name, category, price);
                service.setDescription(desc.isEmpty() ? null : desc);
                
                if (!durationStr.isEmpty()) {
                    try {
                        int duration = Integer.parseInt(durationStr);
                        service.setDurationMinutes(duration);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Invalid duration value!", 
                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                
                boolean success = serviceDAO.addService(service);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, 
                        "✅ Service added successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadServices();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to add service. Service name may already exist.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid price format!", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editService(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a service to edit.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int serviceId = (int) model.getValueAt(row, 0);
        Service service = serviceDAO.getServiceById(serviceId);
        
        if (service == null) {
            JOptionPane.showMessageDialog(this, "Service not found!",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField nameField = new JTextField(service.getServiceName(), 20);
        JComboBox<String> categoryCombo = new JComboBox<>();
        categoryCombo.setEditable(true);
        
        List<String> categories = serviceDAO.getCategories();
        for (String cat : categories) {
            categoryCombo.addItem(cat);
        }
        categoryCombo.setSelectedItem(service.getServiceCategory());
        
        JTextField priceField = new JTextField(String.valueOf(service.getBasePrice()), 10);
        JTextField durationField = new JTextField(
            service.getDurationMinutes() != null ? String.valueOf(service.getDurationMinutes()) : "",
            10
        );
        JTextArea descArea = new JTextArea(service.getDescription() != null ? service.getDescription() : "", 3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        
        panel.add(new JLabel("Service Name: *"));
        panel.add(nameField);
        panel.add(new JLabel("Category:"));
        panel.add(categoryCombo);
        panel.add(new JLabel("Base Price (₹): *"));
        panel.add(priceField);
        panel.add(new JLabel("Duration (minutes):"));
        panel.add(durationField);
        panel.add(new JLabel("Description:"));
        panel.add(descScroll);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Service",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String category = categoryCombo.getSelectedItem() != null ? 
                categoryCombo.getSelectedItem().toString().trim() : null;
            String priceStr = priceField.getText().trim();
            String durationStr = durationField.getText().trim();
            String desc = descArea.getText().trim();
            
            if (name.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Service name and price are required!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                double price = Double.parseDouble(priceStr);
                
                if (price < 0) {
                    JOptionPane.showMessageDialog(this, "Price cannot be negative!",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                service.setServiceName(name);
                service.setServiceCategory(category);
                service.setBasePrice(price);
                service.setDescription(desc.isEmpty() ? null : desc);
                
                if (!durationStr.isEmpty()) {
                    try {
                        int duration = Integer.parseInt(durationStr);
                        service.setDurationMinutes(duration);
                    } catch (NumberFormatException ex) {
                        service.setDurationMinutes(null);
                    }
                } else {
                    service.setDurationMinutes(null);
                }
                
                boolean success = serviceDAO.updateService(service);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, 
                        "✅ Service updated successfully!", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadServices();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to update service.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid price format!",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void discontinueService(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a service to discontinue.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String status = (String) model.getValueAt(row, 5);
        if ("Discontinued".equals(status)) {
            JOptionPane.showMessageDialog(this, "This service is already discontinued.",
                "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int serviceId = (int) model.getValueAt(row, 0);
        String serviceName = (String) model.getValueAt(row, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to discontinue:\n" + serviceName + "?\n\n" +
            "This service will no longer appear in the billing panel.",
            "Confirm Discontinue",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = serviceDAO.discontinueService(serviceId);
            
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "✅ Service discontinued successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadServices();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to discontinue service.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void reactivateService(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a service to reactivate.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String status = (String) model.getValueAt(row, 5);
        if ("Active".equals(status)) {
            JOptionPane.showMessageDialog(this, "This service is already active.",
                "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int serviceId = (int) model.getValueAt(row, 0);
        String serviceName = (String) model.getValueAt(row, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to reactivate:\n" + serviceName + "?",
            "Confirm Reactivate",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = serviceDAO.reactivateService(serviceId);
            
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "✅ Service reactivated successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadServices();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to reactivate service.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}