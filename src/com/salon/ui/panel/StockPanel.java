package com.salon.ui.panel;

import com.salon.dao.StockDAO;
import com.salon.dao.ProductDAO;
import com.salon.model.StockEntry;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class StockPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> cmbProducts;
    private JTextField txtQty, txtActualPrice, txtSellingPrice, txtNotes;
    private JButton btnAddStock, btnRefresh, btnLoadView, btnRefreshProducts;
    private JComboBox<String> cmbViewFilter;
    
    // Labels for totals
    private JLabel lblGrandTotalActual, lblGrandTotalSelling, lblRecordCount;
    
    // Timer for auto-refresh checking
    private Timer refreshTimer;

    public StockPanel() {
        setLayout(new BorderLayout());

        // Top Form Panel
        JPanel formPanel = new JPanel(new GridLayout(3, 4, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add Stock Entry"));

        cmbProducts = new JComboBox<>();
        txtQty = new JTextField();
        txtActualPrice = new JTextField();
        txtSellingPrice = new JTextField();
        txtNotes = new JTextField();

        btnAddStock = new JButton("Add Stock");
        btnRefresh = new JButton("Refresh Table");
        btnRefreshProducts = new JButton("🔄 Refresh Products");
        btnRefreshProducts.setToolTipText("Reload product list from database");

        formPanel.add(new JLabel("Product:"));
        formPanel.add(cmbProducts);
        formPanel.add(new JLabel("Quantity:"));
        formPanel.add(txtQty);
        formPanel.add(new JLabel("Actual Price/Unit:"));
        formPanel.add(txtActualPrice);
        formPanel.add(new JLabel("Selling Price/Unit:"));
        formPanel.add(txtSellingPrice);
        formPanel.add(new JLabel("Notes:"));
        formPanel.add(txtNotes);
        formPanel.add(btnAddStock);
        formPanel.add(btnRefresh);

        // Filter Panel with Refresh Products button
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("View Options"));
        
        cmbViewFilter = new JComboBox<>(new String[]{
            "Recent 5 Entries",
            "Recent 10 Entries", 
            "Recent 15 Entries",
            "All Entries"
        });
        btnLoadView = new JButton("Load Selected View");
        lblRecordCount = new JLabel("Showing: 0 records");
        lblRecordCount.setFont(new Font("Arial", Font.BOLD, 12));
        
        filterPanel.add(new JLabel("Display:"));
        filterPanel.add(cmbViewFilter);
        filterPanel.add(btnLoadView);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(btnRefreshProducts);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(lblRecordCount);

        // Table
     // In StockPanel constructor - MODIFY table model:
        model = new DefaultTableModel(
            new String[]{"ID", "Product", "Brand", "Date", "Qty", "Actual/Unit", "Selling/Unit", 
                         "Total Actual", "Total Selling", "Status", "Notes"}, // ADDED Brand column
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);

        // MODIFIED: Set column widths (added Brand column)
        table.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(150);  // Product
        table.getColumnModel().getColumn(2).setPreferredWidth(100);  // Brand (NEW)
        table.getColumnModel().getColumn(3).setPreferredWidth(100);  // Date
        table.getColumnModel().getColumn(4).setPreferredWidth(60);   // Qty
        table.getColumnModel().getColumn(5).setPreferredWidth(90);   // Actual/Unit
        table.getColumnModel().getColumn(6).setPreferredWidth(90);   // Selling/Unit
        table.getColumnModel().getColumn(7).setPreferredWidth(100);  // Total Actual
        table.getColumnModel().getColumn(8).setPreferredWidth(100);  // Total Selling
        table.getColumnModel().getColumn(9).setPreferredWidth(100);  // Status
        table.getColumnModel().getColumn(10).setPreferredWidth(200); // Notes   
        
        JScrollPane scroll = new JScrollPane(table);

        // Bottom panel for grand totals
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Grand Totals"));
        
        lblGrandTotalActual = new JLabel("Total Actual Cost: ₹0.00");
        lblGrandTotalActual.setFont(new Font("Arial", Font.BOLD, 14));
        lblGrandTotalActual.setForeground(new Color(0, 100, 0));
        
        lblGrandTotalSelling = new JLabel("Total Selling Value: ₹0.00");
        lblGrandTotalSelling.setFont(new Font("Arial", Font.BOLD, 14));
        lblGrandTotalSelling.setForeground(new Color(0, 0, 150));
        
        bottomPanel.add(lblGrandTotalActual);
        bottomPanel.add(new JLabel("  |  "));
        bottomPanel.add(lblGrandTotalSelling);

        // Layout
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(formPanel, BorderLayout.NORTH);
        topContainer.add(filterPanel, BorderLayout.SOUTH);
        
        add(topContainer, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadProducts();
        loadStockEntries();

        // Auto-refresh timer - checks every 2 seconds when panel is visible
        refreshTimer = new Timer(2000, e -> checkAndRefreshProducts());
        refreshTimer.start();

        btnAddStock.addActionListener(e -> addStock());
        btnRefresh.addActionListener(e -> loadStockEntries());
        btnLoadView.addActionListener(e -> loadStockEntries());
        btnRefreshProducts.addActionListener(e -> {
            loadProducts();
            JOptionPane.showMessageDialog(this, 
                "✓ Product list refreshed!", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
        });
    }

    // Store last known product count
    private int lastProductCount = 0;
    
    private void checkAndRefreshProducts() {
        // Only check if panel is visible
        if (!isVisible()) {
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            ProductDAO dao = new ProductDAO();
            int currentCount = dao.getProductList().size();
            
            // Refresh only if count changed
            if (lastProductCount != 0 && currentCount != lastProductCount) {
                loadProducts();
            }
            lastProductCount = currentCount;
        });
    }

    private void loadProducts() {
        String selectedItem = (String) cmbProducts.getSelectedItem();
        cmbProducts.removeAllItems();
        
        ProductDAO dao = new ProductDAO();
        java.util.List<String[]> products = dao.getProductList();
        
        lastProductCount = products.size(); // Update count
        
        if (products.isEmpty()) {
            cmbProducts.addItem("No products available");
            btnAddStock.setEnabled(false);
            return;
        }
        
        btnAddStock.setEnabled(true);
        for (String[] p : products) {
            // MODIFIED: Display format now includes brand
            // p[0] = product_id, p[1] = name, p[2] = brand
            String item = p[0] + " - " + p[1] + " (" + p[2] + ")";
            cmbProducts.addItem(item);
            
            // Restore previous selection if still exists
            if (selectedItem != null && item.equals(selectedItem)) {
                cmbProducts.setSelectedItem(item);
            }
        }
    }

        
    private void loadStockEntries() {
        model.setRowCount(0);
        StockDAO dao = new StockDAO();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        
        double grandTotalActual = 0.0;
        double grandTotalSelling = 0.0;
        
        // Determine limit from selected filter
        String selected = (String) cmbViewFilter.getSelectedItem();
        java.util.List<StockEntry> entries;
        
        if (selected != null && selected.contains("5")) {
            entries = dao.getRecentStockEntries(5);
        } else if (selected != null && selected.contains("10")) {
            entries = dao.getRecentStockEntries(10);
        } else if (selected != null && selected.contains("15")) {
            entries = dao.getRecentStockEntries(15);
        } else {
            entries = dao.getAllStockEntries();
        }
        
        // Populate table - MODIFIED to include Brand
        for (StockEntry s : entries) {
            double totalActual = s.getQuantity() * s.getActualPricePerUnit();
            double totalSelling = s.getQuantity() * s.getSellingPricePerUnit();
            
            grandTotalActual += totalActual;
            grandTotalSelling += totalSelling;
            
            model.addRow(new Object[]{
                    s.getStockId(),
                    s.getProductName(),
                    s.getProductBrand(), // ADDED: Brand column
                    df.format(s.getAddDate()),
                    s.getQuantity(),
                    String.format("₹%.2f", s.getActualPricePerUnit()),
                    String.format("₹%.2f", s.getSellingPricePerUnit()),
                    String.format("₹%.2f", totalActual),
                    String.format("₹%.2f", totalSelling),
                    s.getStockStatus() != null ? s.getStockStatus() : "RUNNING",
                    s.getNotes()
            });
        }
        
        // Update labels
        lblRecordCount.setText(String.format("Showing: %d records", entries.size()));
        lblGrandTotalActual.setText(String.format("Total Actual Cost: ₹%.2f", grandTotalActual));
        lblGrandTotalSelling.setText(String.format("Total Selling Value: ₹%.2f", grandTotalSelling));
    }

    private void addStock() {
        try {
            // === VALIDATION: Product Selection ===
            if (cmbProducts.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Select a product.");
                return;
            }

            String selected = cmbProducts.getSelectedItem().toString();
            if (selected.equals("No products available")) {
                JOptionPane.showMessageDialog(this, "No products available. Please add products first.");
                return;
            }

            int productId = Integer.parseInt(selected.split(" - ")[0]);
            StockDAO dao = new StockDAO();
            
            // === FIX 1: GET LAST STOCK PRICES (ANY STATUS - RUNNING OR COMPLETED) ===
            // Changed method call to get ANY status stock entry for price checking
            StockEntry lastStockPrices = dao.getLastStockPricesAnyStatus(productId); // NEW METHOD
            
            // Get current RUNNING stock for quantity tracking
            StockEntry lastRunningStock = dao.getLastRunningStock(productId);
            
            if (lastStockPrices != null) {
                // Product has historical stock entries - enforce price consistency
                
                double historicalActual = lastStockPrices.getActualPricePerUnit();
                double historicalSelling = lastStockPrices.getSellingPricePerUnit();
                
                // Check if user entered different prices
                boolean pricesEntered = !txtActualPrice.getText().trim().isEmpty() && 
                                       !txtSellingPrice.getText().trim().isEmpty();
                
                if (pricesEntered) {
                    double enteredActual = Double.parseDouble(txtActualPrice.getText());
                    double enteredSelling = Double.parseDouble(txtSellingPrice.getText());
                    
                    // Detect price mismatch (tolerance: ₹0.01)
                    boolean priceChanged = Math.abs(enteredActual - historicalActual) > 0.01 ||
                                          Math.abs(enteredSelling - historicalSelling) > 0.01;
                    
                    if (priceChanged) {
                        // === PRICE CHANGE DIALOG ===
                        int choice = JOptionPane.showConfirmDialog(this,
                            String.format("⚠️ PRICE CHANGE DETECTED!\n\n" +
                                "Historical Prices (Last Entry):\n" +
                                "  Actual:  ₹%.2f → ₹%.2f (%s)\n" +
                                "  Selling: ₹%.2f → ₹%.2f (%s)\n\n" +
                                "📌 SYSTEM POLICY:\n" +
                                "   • Same SKU = Same Price (for accurate cost tracking)\n" +
                                "   • Price changes require new SKU/Product\n\n" +
                                "To use new prices:\n" +
                                "   1. Mark current product as DISCONTINUED\n" +
                                "   2. Create new product with new SKU\n\n" +
                                "Continue with HISTORICAL prices (₹%.2f / ₹%.2f)?",
                                historicalActual, enteredActual,
                                enteredActual > historicalActual ? "↑" : "↓",
                                historicalSelling, enteredSelling,
                                enteredSelling > historicalSelling ? "↑" : "↓",
                                historicalActual, historicalSelling),
                            "Price Change Not Allowed",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                        
                        if (choice != JOptionPane.YES_OPTION) {
                            return; // User cancelled - abort stock addition
                        }
                    }
                }
                
                // === AUTO-FILL & LOCK PRICES ===
                // Always use historical prices for consistency
                txtActualPrice.setText(String.format("%.2f", historicalActual));
                txtSellingPrice.setText(String.format("%.2f", historicalSelling));
                txtActualPrice.setEditable(false);
                txtSellingPrice.setEditable(false);
                
            } else {
                // === NEW PRODUCT - ALLOW PRICE ENTRY ===
                txtActualPrice.setEditable(true);
                txtSellingPrice.setEditable(true);
            }

            // === VALIDATION: Quantity and Prices ===
            double qty = Double.parseDouble(txtQty.getText());
            double actual = Double.parseDouble(txtActualPrice.getText());
            double selling = Double.parseDouble(txtSellingPrice.getText());
            String notes = txtNotes.getText();

            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.");
                return;
            }
            
            if (actual <= 0 || selling <= 0) {
                JOptionPane.showMessageDialog(this, "Prices must be greater than 0.");
                return;
            }

            // === CREATE STOCK ENTRY ===
            StockEntry entry = new StockEntry();
            entry.setProductId(productId);
            entry.setAddDate(new java.util.Date());
            entry.setQuantity(qty);
            entry.setActualPricePerUnit(actual);
            entry.setSellingPricePerUnit(selling);
            entry.setNotes(notes);

            // === SAVE TO DATABASE ===
            boolean success = dao.addStockEntry(entry);
            
            if (success) {
                // === SUCCESS FEEDBACK ===
                double totalActual = qty * actual;
                double totalSelling = qty * selling;
                
                String message = String.format("✓ Stock added successfully!\n\n" +
                                "Quantity Added: %.2f units\n" +
                                "Total Actual Cost: ₹%.2f\n" +
                                "Total Selling Value: ₹%.2f", 
                                qty, totalActual, totalSelling);
                
                // Show status of previous stock
                if (lastRunningStock != null) {
                    message += "\n\n📦 Status: Previous stock still RUNNING";
                } else if (lastStockPrices != null) {
                    message += "\n\n📦 Status: First stock after depletion";
                } else {
                    message += "\n\n📦 Status: First stock entry for product";
                }
                
                JOptionPane.showMessageDialog(this, message);
                
                // === RESET FORM ===
                txtQty.setText("");
                txtActualPrice.setText("");
                txtSellingPrice.setText("");
                txtNotes.setText("");
                txtActualPrice.setEditable(true);
                txtSellingPrice.setEditable(true);
                
                // === REFRESH TABLE ===
                loadStockEntries();
                
            } else {
                JOptionPane.showMessageDialog(this, "❌ Error adding stock to database.");
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, 
                "❌ Invalid input. Please enter valid numbers for quantity and prices.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}