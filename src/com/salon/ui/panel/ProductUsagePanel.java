package com.salon.ui.panel;

import com.salon.dao.ProductDAO;
import com.salon.dao.StockMovementDAO;
import com.salon.dao.StockDAO;
import com.salon.model.StockMovement;
import com.salon.model.StockEntry;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;
import java.util.List;

public class ProductUsagePanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private Integer currentUserId;
    
    // Labels for totals
    private JLabel lblTotalQtyUsed, lblTotalActualCost, lblTotalSellingValue;

    public ProductUsagePanel(Integer userId) {
        this.currentUserId = userId;
        setLayout(new BorderLayout());

        // Top Button Panel (compact)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton btnRecordUsage = new JButton("Record Product Usage");
        JButton btnRefresh = new JButton("Refresh");
        
        topPanel.add(btnRecordUsage);
        topPanel.add(btnRefresh);

        // Table with additional columns
        model = new DefaultTableModel(
            new String[]{"Date", "Product", "Brand", "Qty Used", "Actual Cost/Unit", "Selling Price/Unit", 
                        "Total Actual", "Total Selling", "User", "Remarks"},
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);

        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(100);  // Date
        table.getColumnModel().getColumn(1).setPreferredWidth(150);  // Product
        table.getColumnModel().getColumn(2).setPreferredWidth(100);  // Brand
        table.getColumnModel().getColumn(3).setPreferredWidth(70);   // Qty Used
        table.getColumnModel().getColumn(4).setPreferredWidth(100);  // Actual/Unit
        table.getColumnModel().getColumn(5).setPreferredWidth(100);  // Selling/Unit
        table.getColumnModel().getColumn(6).setPreferredWidth(100);  // Total Actual
        table.getColumnModel().getColumn(7).setPreferredWidth(100);  // Total Selling
        table.getColumnModel().getColumn(8).setPreferredWidth(100);  // User
        table.getColumnModel().getColumn(9).setPreferredWidth(200);  // Remarks

        JScrollPane scroll = new JScrollPane(table);

        // Bottom panel for totals
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Usage Totals"));
        
        lblTotalQtyUsed = new JLabel("Total Qty Used: 0.00");
        lblTotalQtyUsed.setFont(new Font("Arial", Font.BOLD, 13));
        
        lblTotalActualCost = new JLabel("Total Actual Cost: ₹0.00");
        lblTotalActualCost.setFont(new Font("Arial", Font.BOLD, 13));
        lblTotalActualCost.setForeground(new Color(139, 0, 0)); // Dark red
        
        lblTotalSellingValue = new JLabel("Total Selling Value: ₹0.00");
        lblTotalSellingValue.setFont(new Font("Arial", Font.BOLD, 13));
        lblTotalSellingValue.setForeground(new Color(0, 0, 139)); // Dark blue
        
        bottomPanel.add(lblTotalQtyUsed);
        bottomPanel.add(new JLabel(" | "));
        bottomPanel.add(lblTotalActualCost);
        bottomPanel.add(new JLabel(" | "));
        bottomPanel.add(lblTotalSellingValue);

        add(topPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Load data
        loadUsageHistory();

        // Button actions
        btnRecordUsage.addActionListener(e -> openRecordUsageDialog());
        btnRefresh.addActionListener(e -> loadUsageHistory());
    }

    private void openRecordUsageDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Record Product Usage", true);
        dialog.setLayout(new BorderLayout(10, 10));
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> cmbProducts = new JComboBox<>();
        JTextField txtQuantity = new JTextField(15);
        JTextArea txtRemarks = new JTextArea(3, 15);
        txtRemarks.setLineWrap(true);
        txtRemarks.setWrapStyleWord(true);
        JScrollPane remarksScroll = new JScrollPane(txtRemarks);

        // Load products
        ProductDAO dao = new ProductDAO();
        List<String[]> products = dao.getProductList();
        
        if (products.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, 
                "No active products available.", 
                "Info", 
                JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
            return;
        }
        
        for (String[] p : products) {
            cmbProducts.addItem(p[0] + " - " + p[1] + " (" + p[2] + ")");
        }

        // Layout
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Product:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(cmbProducts, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Quantity Used:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtQuantity, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Remarks:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(remarksScroll, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");
        
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Button Actions
        btnSave.addActionListener(e -> {
            if (recordUsage(cmbProducts, txtQuantity, txtRemarks, dialog)) {
                dialog.dispose();
            }
        });
        
        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
        
    private void loadUsageHistory() {
        model.setRowCount(0);
        StockMovementDAO movementDao = new StockMovementDAO();
        StockDAO stockDao = new StockDAO();
        List<StockMovement> movements = movementDao.getUsageMovements();
        
        double totalQtyUsed = 0.0;
        double totalActualCost = 0.0;
        double totalSellingValue = 0.0;
        
        for (StockMovement m : movements) {
            double qtyUsed = Math.abs(m.getQuantityChanged());
            
            double actualPrice = getAveragePriceForProduct(m.getProductId(), stockDao, true);
            double sellingPrice = getAveragePriceForProduct(m.getProductId(), stockDao, false);
            
            double totalActual = qtyUsed * actualPrice;
            double totalSelling = qtyUsed * sellingPrice;
            
            totalQtyUsed += qtyUsed;
            totalActualCost += totalActual;
            totalSellingValue += totalSelling;
            
            model.addRow(new Object[]{
                m.getMovementDateFormatted(),
                m.getProductName(),
                m.getProductBrand(),
                String.format("%.2f", qtyUsed),
                String.format("₹%.2f", actualPrice),
                String.format("₹%.2f", sellingPrice),
                String.format("₹%.2f", totalActual),
                String.format("₹%.2f", totalSelling),
                m.getUserName() != null ? m.getUserName() : "System",
                m.getRemarks()
            });
        }
        
        lblTotalQtyUsed.setText(String.format("Total Qty Used: %.2f", totalQtyUsed));
        lblTotalActualCost.setText(String.format("Total Actual Cost: ₹%.2f", totalActualCost));
        lblTotalSellingValue.setText(String.format("Total Selling Value: ₹%.2f", totalSellingValue));
    }
    
    private double getAveragePriceForProduct(int productId, StockDAO stockDao, boolean isActual) {
        List<StockEntry> allEntries = stockDao.getAllStockEntries();
        double totalPrice = 0.0;
        int count = 0;
        
        for (StockEntry entry : allEntries) {
            if (entry.getProductId() == productId) {
                if (isActual) {
                    totalPrice += entry.getActualPricePerUnit();
                } else {
                    totalPrice += entry.getSellingPricePerUnit();
                }
                count++;
            }
        }
        
        return count > 0 ? totalPrice / count : 0.0;
    }

    private boolean recordUsage(JComboBox<String> cmbProducts, JTextField txtQuantity, 
                                JTextArea txtRemarks, JDialog dialog) {
        try {
            if (cmbProducts.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(dialog, "Please select a product.");
                return false;
            }

            String qtyText = txtQuantity.getText().trim();
            if (qtyText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter quantity used.");
                return false;
            }

            double quantity = Double.parseDouble(qtyText);
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(dialog, "Quantity must be greater than 0.");
                return false;
            }

            String selected = cmbProducts.getSelectedItem().toString();
            int productId = Integer.parseInt(selected.split(" - ")[0]);

            StockMovementDAO dao = new StockMovementDAO();
            double availableStock = dao.getAvailableStock(productId);
            
            if (quantity > availableStock) {
                JOptionPane.showMessageDialog(dialog, 
                    String.format("Insufficient stock!\n\n" +
                                "Available Stock: %.2f\n" +
                                "Requested Quantity: %.2f\n\n" +
                                "Action cancelled. Please enter a quantity less than or equal to available stock.",
                        availableStock, quantity),
                    "Insufficient Stock", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            if (quantity == availableStock) {
                int confirm = JOptionPane.showConfirmDialog(dialog, 
                    String.format("Warning: This will use all remaining stock!\n\n" +
                                "Available Stock: %.2f\n" +
                                "Using: %.2f\n" +
                                "Remaining after use: 0.00\n\n" +
                                "Do you want to continue?", 
                        availableStock, quantity),
                    "Stock Depletion Warning", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (confirm != JOptionPane.YES_OPTION) {
                    return false;
                }
            }

            StockDAO stockDao = new StockDAO();
            double actualPrice = getAveragePriceForProduct(productId, stockDao, true);
            double sellingPrice = getAveragePriceForProduct(productId, stockDao, false);
            
            StockMovement movement = new StockMovement();
            movement.setProductId(productId);
            movement.setMovementDate(new Date());
            movement.setQuantityChanged(-quantity);
            movement.setMovementType("USAGE");
            movement.setUserId(currentUserId);
            movement.setRemarks(txtRemarks.getText().trim());

            boolean success = dao.addStockMovement(movement);
            
            if (success) {
                double totalActual = quantity * actualPrice;
                double totalSelling = quantity * sellingPrice;
                double remainingStock = availableStock - quantity;
                
                if (remainingStock == 0) {
                    stockDao.updateStockStatus(productId, "COMPLETED");
                }
                
                JOptionPane.showMessageDialog(dialog, 
                    String.format("✓ Usage recorded successfully!\n\n" +
                                "Quantity Used: %.2f\n" +
                                "Actual Cost: ₹%.2f\n" +
                                "Selling Value: ₹%.2f\n" +
                                "Remaining Stock: %.2f" +
                                (remainingStock == 0 ? "\n\n⚠ Stock depleted - Status updated to COMPLETED" : ""), 
                        quantity, totalActual, totalSelling, remainingStock),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
                loadUsageHistory();
                return true;
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "Failed to record usage.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialog, 
                "Please enter a valid number for quantity.",
                "Invalid Input",
                JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dialog, 
                "Error: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return false;
        }
    }
}