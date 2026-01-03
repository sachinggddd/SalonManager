package com.salon.ui.panel;

import com.salon.dao.StockMovementDAO;
import com.salon.dao.StockDAO;
import com.salon.model.StockMovement;
import com.salon.model.StockEntry;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProductSalePanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JButton btnRefresh;
    private JLabel lblTotalQtySold, lblTotalRevenue, lblTotalProfit;

    public ProductSalePanel() {
        setLayout(new BorderLayout());

        // Top Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createTitledBorder("Product Sales History"));
        
        btnRefresh = new JButton("🔄 Refresh");
        topPanel.add(btnRefresh);

        // Table
        model = new DefaultTableModel(
            new String[]{"Date", "Customer", "Product", "Brand", "Qty Sold", 
                        "Selling Price", "Total Amount", "Actual Cost", "Profit"}, 
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
        table.getColumnModel().getColumn(0).setPreferredWidth(120);  // Date
        table.getColumnModel().getColumn(1).setPreferredWidth(120);  // Customer
        table.getColumnModel().getColumn(2).setPreferredWidth(150);  // Product
        table.getColumnModel().getColumn(3).setPreferredWidth(100);  // Brand
        table.getColumnModel().getColumn(4).setPreferredWidth(80);   // Qty
        table.getColumnModel().getColumn(5).setPreferredWidth(100);  // Price
        table.getColumnModel().getColumn(6).setPreferredWidth(100);  // Total
        table.getColumnModel().getColumn(7).setPreferredWidth(100);  // Cost
        table.getColumnModel().getColumn(8).setPreferredWidth(100);  // Profit

        JScrollPane scroll = new JScrollPane(table);

        // Bottom panel for totals
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Sales Summary"));
        
        lblTotalQtySold = new JLabel("Total Qty Sold: 0.00");
        lblTotalQtySold.setFont(new Font("Arial", Font.BOLD, 13));
        
        lblTotalRevenue = new JLabel("Total Revenue: ₹0.00");
        lblTotalRevenue.setFont(new Font("Arial", Font.BOLD, 13));
        lblTotalRevenue.setForeground(new Color(0, 100, 0));
        
        lblTotalProfit = new JLabel("Total Profit: ₹0.00");
        lblTotalProfit.setFont(new Font("Arial", Font.BOLD, 13));
        lblTotalProfit.setForeground(new Color(0, 0, 139));
        
        bottomPanel.add(lblTotalQtySold);
        bottomPanel.add(new JLabel(" | "));
        bottomPanel.add(lblTotalRevenue);
        bottomPanel.add(new JLabel(" | "));
        bottomPanel.add(lblTotalProfit);

        add(topPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // Load data
        loadSalesData();

        // Button action
        btnRefresh.addActionListener(e -> loadSalesData());
    }

    private void loadSalesData() {
        model.setRowCount(0);
        StockMovementDAO movementDao = new StockMovementDAO();
        StockDAO stockDao = new StockDAO();
        
        // Get only SALE type movements
        List<StockMovement> sales = movementDao.getSaleMovements();
        
        double totalQty = 0.0;
        double totalRevenue = 0.0;
        double totalProfit = 0.0;
        
        for (StockMovement sale : sales) {
            double qtySold = Math.abs(sale.getQuantityChanged());
            
            // Get prices from invoice_items (already fetched in getSaleMovements)
            double sellingPrice = sale.getSellingPricePerUnit();
            double actualCost = sale.getActualPricePerUnit();
            
            // If prices are 0, try to get average from stock entries as fallback
            if (sellingPrice == 0.0 || actualCost == 0.0) {
                sellingPrice = getAveragePriceForProduct(sale.getProductId(), stockDao, false);
                actualCost = getAveragePriceForProduct(sale.getProductId(), stockDao, true);
            }
            
            double totalAmount = qtySold * sellingPrice;
            double totalCost = qtySold * actualCost;
            double profit = totalAmount - totalCost;
            
            totalQty += qtySold;
            totalRevenue += totalAmount;
            totalProfit += profit;
            
            model.addRow(new Object[]{
                sale.getMovementDateFormatted(),
                sale.getCustomerName() != null ? sale.getCustomerName() : "Walk-in",
                sale.getProductName(),
                sale.getProductBrand() != null ? sale.getProductBrand() : "N/A",
                String.format("%.2f", qtySold),
                String.format("₹%.2f", sellingPrice),
                String.format("₹%.2f", totalAmount),
                String.format("₹%.2f", totalCost),
                String.format("₹%.2f", profit)
            });
        }
        
        // Update totals
        lblTotalQtySold.setText(String.format("Total Qty Sold: %.2f", totalQty));
        lblTotalRevenue.setText(String.format("Total Revenue: ₹%.2f", totalRevenue));
        lblTotalProfit.setText(String.format("Total Profit: ₹%.2f", totalProfit));
    }
    
    // Helper method to get average price for a product (fallback)
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
}