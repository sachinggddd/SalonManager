package com.salon.ui.panel;

import com.salon.dao.StockMovementDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class StockSummaryPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JButton btnRefresh;
    private JLabel lblLowStock;

    public StockSummaryPanel() {
        setLayout(new BorderLayout());

        // Top panel with refresh button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createTitledBorder("Current Stock Levels"));
        
        btnRefresh = new JButton("Refresh");
        lblLowStock = new JLabel();
        lblLowStock.setForeground(Color.RED);
        
        topPanel.add(btnRefresh);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(lblLowStock);

        // Table
        model = new DefaultTableModel(
            new String[]{"Product", "Brand", "Total Added", "Total Used", "Available Stock", "Status"}, 
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(model);
        table.setRowHeight(25);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);

        // Custom renderer for status column
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (value != null) {
                    String status = value.toString();
                    if ("LOW STOCK".equals(status)) {
                        c.setForeground(Color.RED);
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if ("OUT OF STOCK".equals(status)) {
                        c.setForeground(Color.RED);
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                        setBackground(new Color(255, 200, 200));
                    } else if ("ADEQUATE".equals(status)) {
                        c.setForeground(new Color(0, 128, 0));
                    } else {
                        c.setForeground(Color.BLACK);
                    }
                }
                
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(240, 240, 240));
                    }
                }
                
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);

        add(topPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        // Load initial data
        loadStockSummary();

        // Button actions
        btnRefresh.addActionListener(e -> loadStockSummary());
    }

    private void loadStockSummary() {
        model.setRowCount(0);
        StockMovementDAO dao = new StockMovementDAO();
        List<Map<String, Object>> summary = dao.getStockSummary();
        
        int lowStockCount = 0;
        int outOfStockCount = 0;
        
        for (Map<String, Object> row : summary) {
            String name = (String) row.get("name");
            String brand = (String) row.get("brand");
            double totalAdded = (Double) row.get("total_added");
            double totalUsed = (Double) row.get("total_used");
            double availableStock = (Double) row.get("available_stock");
            
            // Determine status
            String status;
            if (availableStock <= 0) {
                status = "OUT OF STOCK";
                outOfStockCount++;
            } else if (availableStock <= 5) {
                status = "LOW STOCK";
                lowStockCount++;
            } else if (availableStock <= 10) {
                status = "MODERATE";
            } else {
                status = "ADEQUATE";
            }
            
            model.addRow(new Object[]{
                name,
                brand,
                String.format("%.2f", totalAdded),
                String.format("%.2f", totalUsed),
                String.format("%.2f", availableStock),
                status
            });
        }
        
        // Update low stock warning label
        if (outOfStockCount > 0 || lowStockCount > 0) {
            lblLowStock.setText(String.format(
                "⚠ Warning: %d product(s) out of stock, %d low stock", 
                outOfStockCount, lowStockCount
            ));
        } else {
            lblLowStock.setText("✓ All products adequately stocked");
            lblLowStock.setForeground(new Color(0, 128, 0));
        }
    }
}