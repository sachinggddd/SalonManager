package com.salon.ui.panel;

import com.salon.dao.ProductDAO;
import com.salon.model.Product;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Random;

public class ProductPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JCheckBox chkShowDiscontinued;
    
    public ProductPanel() {
        setLayout(new BorderLayout());
        
        // Table with Status column
        model = new DefaultTableModel(
            new String[]{"ID", "SKU", "Name", "Brand", "Type", "Status"}, 
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        
        // Top panel with filter - INITIALIZE BEFORE loadProducts()
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        chkShowDiscontinued = new JCheckBox("Show Discontinued Products");
        chkShowDiscontinued.addActionListener(e -> loadProducts());
        topPanel.add(chkShowDiscontinued);
        
        // NOW load products after checkbox is initialized
        loadProducts();
        
        // Bottom panel with buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton btnAdd = new JButton("Add Product");
        JButton btnMarkDiscontinued = new JButton("Mark as Discontinued");
        JButton btnMarkContinued = new JButton("Mark as Continued");
        JButton btnRefresh = new JButton("Refresh");
        
        btnAdd.addActionListener(e -> openAddProductDialog());
        btnMarkDiscontinued.addActionListener(e -> markProductStatus(true));
        btnMarkContinued.addActionListener(e -> markProductStatus(false));
        btnRefresh.addActionListener(e -> loadProducts());
        
        bottomPanel.add(btnAdd);
        bottomPanel.add(btnMarkDiscontinued);
        bottomPanel.add(btnMarkContinued);
        bottomPanel.add(btnRefresh);
        
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void loadProducts() {
        ProductDAO dao = new ProductDAO();
        List<Product> list = dao.getAllProducts();
        model.setRowCount(0);
        
        boolean showDiscontinued = chkShowDiscontinued.isSelected();
        
        for (Product p : list) {
            // Filter based on checkbox
            if (!showDiscontinued && p.isDiscontinued()) {
                continue;
            }
            
            String status = p.isDiscontinued() ? "DISCONTINUED" : "CONTINUED";
            model.addRow(new Object[]{
                p.getProductId(), 
                p.getSku(), 
                p.getName(), 
                p.getBrand(), 
                p.getProductType(),
                status
            });
        }
    }
    
    private void markProductStatus(boolean discontinued) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a product from the table.", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int productId = (int) model.getValueAt(selectedRow, 0);
        String productName = (String) model.getValueAt(selectedRow, 2);
        String currentStatus = (String) model.getValueAt(selectedRow, 5);
        
        // Check if already in desired state
        if (discontinued && currentStatus.equals("DISCONTINUED")) {
            JOptionPane.showMessageDialog(this, 
                "Product is already marked as DISCONTINUED.", 
                "Info", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!discontinued && currentStatus.equals("CONTINUED")) {
            JOptionPane.showMessageDialog(this, 
                "Product is already marked as CONTINUED.", 
                "Info", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String action = discontinued ? "DISCONTINUED" : "CONTINUED";
        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Mark '%s' as %s?", productName, action),
            "Confirm Status Change",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            ProductDAO dao = new ProductDAO();
            if (dao.updateProductStatus(productId, discontinued)) {
                JOptionPane.showMessageDialog(this, 
                    String.format("✓ Product marked as %s successfully!", action));
                loadProducts();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to update product status.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // Generate random SKU
    private String generateSKU() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sku = new StringBuilder("SKU-");
        for (int i = 0; i < 8; i++) {
            sku.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sku.toString();
    }
    
    private void openAddProductDialog() {
        JTextField skuField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField brandField = new JTextField();
        JTextField typeField = new JTextField();
        JTextArea descArea = new JTextArea(3, 15);
        JButton btnGenerateSKU = new JButton("Auto Generate");
        
        // Auto-generate SKU by default
        skuField.setText(generateSKU());
        skuField.setEditable(false);
        
        btnGenerateSKU.addActionListener(e -> skuField.setText(generateSKU()));
        
        JPanel skuPanel = new JPanel(new BorderLayout(5, 0));
        skuPanel.add(skuField, BorderLayout.CENTER);
        skuPanel.add(btnGenerateSKU, BorderLayout.EAST);
        
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("SKU (Auto-Generated):"));
        panel.add(skuPanel);
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Brand:"));
        panel.add(brandField);
        panel.add(new JLabel("Type:"));
        panel.add(typeField);
        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(descArea));
        
        int result = JOptionPane.showConfirmDialog(null, panel, 
            "Add Product", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            // Validate inputs
            if (nameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Product Name is required.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Product p = new Product();
            p.setSku(skuField.getText().trim());
            p.setName(nameField.getText().trim());
            p.setBrand(brandField.getText().trim());
            p.setProductType(typeField.getText().trim());
            p.setDescription(descArea.getText().trim());
            p.setDiscontinued(false); // Default: CONTINUED
            
            ProductDAO dao = new ProductDAO();
            if (dao.addProduct(p)) {
                JOptionPane.showMessageDialog(this, 
                    String.format("✓ Product added successfully!\n\nSKU: %s\nStatus: CONTINUED", 
                    p.getSku()));
                loadProducts();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Failed to add product. SKU might already exist.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}