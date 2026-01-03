package com.salon.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import com.salon.dao.CustomerDAO;
import com.salon.dao.InvoiceDAO;
import com.salon.dao.MembershipDAO;
import com.salon.dao.ProductDAO;
import com.salon.dao.ServiceDAO;
import com.salon.dao.StockDAO;
import com.salon.dao.StockMovementDAO;
import com.salon.model.Customer;
import com.salon.model.InvoiceItem;
import com.salon.model.InvoiceService;
import com.salon.model.MembershipPlan;
import com.salon.model.Service;
import com.salon.model.StockEntry;

public class CustomerPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> timeFilterCombo;
    private JLabel customerCountLabel;

    public CustomerPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 248, 255));

        JLabel title = new JLabel("💇 Customer & Invoice Management", SwingConstants.CENTER);
        title.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20));
        title.setForeground(new Color(40, 60, 120));
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"ID", "Name", "Phone", "Address"}, 0);
        table = new JTable(model);
        table.setRowHeight(25);
        
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Registered Customers"));
        add(scroll, BorderLayout.CENTER);

        // ✅ MOVED: Initialize bottom panel BEFORE calling loadCustomers()
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Left side: Time filter
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.add(new JLabel("📅 View:"));

        String[] timeOptions = {
            "All Time", 
            "Today", 
            "Past 3 Days", 
            "Past 7 Days", 
            "Past 15 Days", 
            "Past 1 Month", 
            "Past 3 Months"
        };
        timeFilterCombo = new JComboBox<>(timeOptions);
        timeFilterCombo.addActionListener(e -> loadCustomersByTimeFilter());
        filterPanel.add(timeFilterCombo);

        customerCountLabel = new JLabel("Total: 0");
        customerCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        customerCountLabel.setForeground(new Color(0, 102, 204));
        filterPanel.add(customerCountLabel);

        // Right side: Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        JButton btnAdd = new JButton("➕ Add Customer & Generate Invoice");
        JButton btnView = new JButton("👁️ View Invoice");
        JButton btnExport = new JButton("💾 Export Invoice");

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnView);
        buttonPanel.add(btnExport);

        bottomPanel.add(filterPanel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        btnAdd.addActionListener(this::openAddCustomerInvoiceDialog);
        btnView.addActionListener(this::viewInvoice);
        btnExport.addActionListener(this::exportInvoice);
        
        // ✅ NOW SAFE: Load customers AFTER timeFilterCombo is initialized
        loadCustomers();
    }
    
    private void loadCustomersByTimeFilter() {
        String selectedFilter = (String) timeFilterCombo.getSelectedItem();
        
        CustomerDAO dao = new CustomerDAO();
        List<Customer> allCustomers = dao.getAllCustomers();
        
        // Calculate date threshold based on selection
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date thresholdDate = null;
        
        switch (selectedFilter) {
            case "Today":
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                thresholdDate = cal.getTime();
                break;
            case "Past 3 Days":
                cal.add(java.util.Calendar.DAY_OF_MONTH, -3);
                thresholdDate = cal.getTime();
                break;
            case "Past 7 Days":
                cal.add(java.util.Calendar.DAY_OF_MONTH, -7);
                thresholdDate = cal.getTime();
                break;
            case "Past 15 Days":
                cal.add(java.util.Calendar.DAY_OF_MONTH, -15);
                thresholdDate = cal.getTime();
                break;
            case "Past 1 Month":
                cal.add(java.util.Calendar.MONTH, -1);
                thresholdDate = cal.getTime();
                break;
            case "Past 3 Months":
                cal.add(java.util.Calendar.MONTH, -3);
                thresholdDate = cal.getTime();
                break;
            case "All Time":
            default:
                thresholdDate = null; // Show all
                break;
        }
        
        // Filter and display customers
        model.setRowCount(0);
        int count = 0;
        
        for (Customer c : allCustomers) {
            boolean includeCustomer = false;
            
            if (thresholdDate == null) {
                includeCustomer = true; // All time
            } else {
                // Compare customer creation date with threshold
                java.util.Date createdAt = c.getCreatedAt();
                if (createdAt != null && createdAt.after(thresholdDate)) {
                    includeCustomer = true;
                }
            }
            
            if (includeCustomer) {
                model.addRow(new Object[]{
                    c.getCustomerId(), 
                    c.getName(), 
                    c.getPhone(), 
                    c.getAddress()
                });
                count++;
            }
        }
        
        customerCountLabel.setText("Total: " + count);
    }

    private void loadCustomers() {
        loadCustomersByTimeFilter(); // Use the new filtered loading
    } 

 // REPLACE the openAddCustomerInvoiceDialog method in CustomerPanel with this:
    private void openAddCustomerInvoiceDialog(ActionEvent e) {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setPreferredSize(new Dimension(950, 750)); // Increased height

        // ==================== CUSTOMER INFO PANEL ====================
        JPanel customerPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        customerPanel.setBorder(BorderFactory.createTitledBorder("Customer Details"));
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField addressField = new JTextField();
        
        // Membership selection
        JComboBox<MembershipPlan> membershipCombo = new JComboBox<>();
        MembershipDAO membershipDao = new MembershipDAO();
        List<MembershipPlan> plans = membershipDao.getActivePlans();
        for (MembershipPlan plan : plans) {
            membershipCombo.addItem(plan);
        }
        
        // Auto-detection: Add phone field listener
        phoneField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                String phone = phoneField.getText().trim();
                
                if (phone.length() >= 10) {
                    CustomerDAO dao = new CustomerDAO();
                    Customer existingCustomer = dao.getCustomerByPhone(phone);
                    
                    if (existingCustomer != null) {
                        if (nameField.getText().trim().isEmpty()) {
                            nameField.setText(existingCustomer.getName());
                        }
                        if (addressField.getText().trim().isEmpty()) {
                            addressField.setText(existingCustomer.getAddress());
                        }
                        
                        Integer planId = existingCustomer.getMembershipPlanId();
                        if (planId != null && membershipCombo.getSelectedIndex() == 0) {
                            for (int i = 0; i < membershipCombo.getItemCount(); i++) {
                                MembershipPlan plan = membershipCombo.getItemAt(i);
                                if (plan.getPlanId() == planId) {
                                    membershipCombo.setSelectedIndex(i);
                                    break;
                                }
                            }
                        }
                        phoneField.setBackground(new Color(230, 240, 255));
                    } else {
                        phoneField.setBackground(Color.WHITE);
                    }
                } else {
                    phoneField.setBackground(Color.WHITE);
                }
            }
        });
        
        customerPanel.add(new JLabel("Name: *"));
        customerPanel.add(nameField);
        customerPanel.add(new JLabel("Phone: *"));
        customerPanel.add(phoneField);
        customerPanel.add(new JLabel("Address:"));
        customerPanel.add(addressField);
        customerPanel.add(new JLabel("Membership:"));
        customerPanel.add(membershipCombo);

        // ==================== SERVICES PANEL ====================
        JPanel servicePanel = new JPanel(new BorderLayout(5, 5));
        servicePanel.setBorder(BorderFactory.createTitledBorder("Select Services"));
        
        JPanel serviceSelectionPanel = new JPanel(new GridLayout(0, 3, 8, 8));
        JScrollPane serviceScroll = new JScrollPane(serviceSelectionPanel);
        serviceScroll.setPreferredSize(new Dimension(900, 140)); // Reduced height
        
        ServiceDAO serviceDao = new ServiceDAO();
        List<Service> availableServices = serviceDao.getActiveServices();
        
        Map<Integer, JComponent[]> serviceSelections = new HashMap<>();
        
        for (Service service : availableServices) {
            JCheckBox chkService = new JCheckBox(service.getServiceName());
            JLabel priceLabel = new JLabel(String.format("₹%.2f", service.getBasePrice()));
            priceLabel.setForeground(new Color(0, 100, 0));
            JLabel categoryLabel = new JLabel(service.getServiceCategory() != null ? 
                "(" + service.getServiceCategory() + ")" : "");
            categoryLabel.setFont(new Font("Segoe UI", Font.ITALIC, 9));
            categoryLabel.setForeground(Color.GRAY);
            
            serviceSelections.put(service.getServiceId(), new JComponent[]{chkService, priceLabel});
            
            serviceSelectionPanel.add(chkService);
            serviceSelectionPanel.add(priceLabel);
            serviceSelectionPanel.add(categoryLabel);
        }
        
        servicePanel.add(serviceScroll, BorderLayout.CENTER);
        
        // Membership discount label
        JLabel membershipDiscountLabel = new JLabel("Membership Discount: 0%");
        membershipDiscountLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        membershipDiscountLabel.setForeground(new Color(204, 0, 0));
        
        membershipCombo.addActionListener(a -> {
            MembershipPlan selected = (MembershipPlan) membershipCombo.getSelectedItem();
            if (selected != null) {
                membershipDiscountLabel.setText(String.format("Membership Discount: %.0f%%", 
                    selected.getDiscountPercentage()));
            }
        });

        // ==================== PRODUCTS PANEL WITH SEARCH ====================
        JPanel productMainPanel = new JPanel(new BorderLayout(5, 5));
        productMainPanel.setBorder(BorderFactory.createTitledBorder("Select Products to Sell"));
        
        // Search panel at top
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.add(new JLabel("🔍 Search:"));
        JTextField searchField = new JTextField(20);
        searchPanel.add(searchField);
     // NEW CODE - Manual type entry:
        JTextField typeFilterField = new JTextField(15);
        typeFilterField.setToolTipText("Type product type to filter (e.g., face, skin, hair)");
        searchPanel.add(new JLabel("Type:"));
        searchPanel.add(typeFilterField);
        productMainPanel.add(searchPanel, BorderLayout.NORTH);
        
        // Product selection area
        JPanel productSelectionPanel = new JPanel(new GridLayout(0, 4, 5, 5));
        JScrollPane productScroll = new JScrollPane(productSelectionPanel);
        productScroll.setPreferredSize(new Dimension(900, 200)); // Fixed height
        
        ProductDAO productDao = new ProductDAO();
        StockMovementDAO movementDao = new StockMovementDAO();
        List<String[]> availableProducts = productDao.getProductList();
        
        // ✅ ProductInfo class to store all product data
        class ProductInfo {
            int productId;
            String productName;
            String brandName;
            String productType;
            double availableStock;
            double sellingPrice;
            double actualPrice;
            JCheckBox checkbox;
            JTextField qtyField;
            JLabel priceLabel;
            JLabel stockLabel;
            JPanel infoPanel;
            
            ProductInfo(int id, String name, String brand, String type, double stock, double selling, double actual) {
                this.productId = id;
                this.productName = name;
                this.brandName = brand;
                this.productType = type;
                this.availableStock = stock;
                this.sellingPrice = selling;
                this.actualPrice = actual;
                
                // Create UI components
                checkbox = new JCheckBox(name + " (" + brand + ")");
                qtyField = new JTextField(5);
                qtyField.setEnabled(false);
                priceLabel = new JLabel(String.format("₹%.2f", selling));
                priceLabel.setForeground(new Color(0, 100, 0));
                stockLabel = new JLabel(String.format("Stock: %.2f", stock));
                stockLabel.setForeground(new Color(100, 100, 100));
                
                infoPanel = new JPanel(new GridLayout(2, 1));
                infoPanel.add(priceLabel);
                infoPanel.add(stockLabel);
                
                checkbox.addActionListener(a -> qtyField.setEnabled(checkbox.isSelected()));
            }
            
         // NEW CODE - Partial match for type:
            boolean matchesFilter(String searchText, String typeFilter) {
                boolean matchesSearch = searchText.isEmpty() || 
                    (productName.toLowerCase().contains(searchText) || 
                     brandName.toLowerCase().contains(searchText));
                
                boolean matchesType = typeFilter.isEmpty() || 
                    (productType != null && productType.toLowerCase().contains(typeFilter));
                
                return matchesSearch && matchesType;
            }
            void addToPanel(JPanel panel) {
                panel.add(checkbox);
                panel.add(new JLabel("Qty:"));
                panel.add(qtyField);
                panel.add(infoPanel);
            }
        }
        
        // Store all products with their complete information
        List<ProductInfo> allProducts = new ArrayList<>();
        
        // Load all products
        for (String[] p : availableProducts) {
            int productId = Integer.parseInt(p[0]);
            String productName = p[1];
            String brandName = p[2];
            
            double availableStock = movementDao.getAvailableStock(productId);
            
            if (availableStock > 0) {
                double sellingPrice = getSellingPriceForProduct(productId);
                double actualPrice = getActualPriceForProduct(productId);
                String productType = getProductType(productId);
                
                ProductInfo info = new ProductInfo(productId, productName, brandName, 
                    productType, availableStock, sellingPrice, actualPrice);
                
                allProducts.add(info);
                info.addToPanel(productSelectionPanel); // Add initially
            }
        }
        
        productMainPanel.add(productScroll, BorderLayout.CENTER);
        
        // ✅ Search functionality that maintains component references
     // NEW CODE:
        Runnable filterProducts = () -> {
            String searchText = searchField.getText().trim().toLowerCase();
            String typeFilter = typeFilterField.getText().trim().toLowerCase(); // Changed
            
            productSelectionPanel.removeAll();
            
            for (ProductInfo info : allProducts) {
                if (info.matchesFilter(searchText, typeFilter)) {
                    info.addToPanel(productSelectionPanel);
                }
            }
            
            productSelectionPanel.revalidate();
            productSelectionPanel.repaint();
        };

        // NEW listener - triggers on typing:
    
        typeFilterField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                filterProducts.run();
            }
        });

        // ✅ ADD THIS - Search field listener (was missing):
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                filterProducts.run();
            }
        });
        
        // ==================== TOTAL CALCULATION PANEL ====================
        JPanel totalPanel = new JPanel(new GridLayout(5, 1, 0, 2));
        totalPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel lblServiceSubtotal = new JLabel("Service Subtotal: ₹0.00");
        JLabel lblServiceDiscount = new JLabel("Service Discount: -₹0.00");
        lblServiceDiscount.setForeground(new Color(204, 0, 0));
        JLabel lblServiceTotal = new JLabel("Service Total: ₹0.00");
        lblServiceTotal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel lblProductTotal = new JLabel("Product Total: ₹0.00");
        JLabel lblGrandTotal = new JLabel("Grand Total: ₹0.00");
        lblGrandTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblGrandTotal.setForeground(new Color(0, 102, 0));
        
        totalPanel.add(lblServiceSubtotal);
        totalPanel.add(lblServiceDiscount);
        totalPanel.add(lblServiceTotal);
        totalPanel.add(lblProductTotal);
        totalPanel.add(lblGrandTotal);

        // Real-time calculation
        KeyAdapter recalc = new KeyAdapter() {
            public void keyReleased(KeyEvent e1) {
                double serviceSubtotal = 0;
                double productTotal = 0;
                
                for (Map.Entry<Integer, JComponent[]> entry : serviceSelections.entrySet()) {
                    JCheckBox chk = (JCheckBox) entry.getValue()[0];
                    JLabel price = (JLabel) entry.getValue()[1];
                    
                    if (chk.isSelected()) {
                        try {
                            String priceText = price.getText().replace("₹", "").trim();
                            serviceSubtotal += Double.parseDouble(priceText);
                        } catch (Exception ignored) {}
                    }
                }
                
                MembershipPlan selected = (MembershipPlan) membershipCombo.getSelectedItem();
                double discountPercent = selected != null ? selected.getDiscountPercentage() : 0.0;
                double serviceDiscount = serviceSubtotal * (discountPercent / 100.0);
                double serviceTotal = serviceSubtotal - serviceDiscount;
                
                // Calculate product total from ProductInfo objects
                for (ProductInfo info : allProducts) {
                    if (info.checkbox.isSelected()) {
                        try {
                            double qtyVal = Double.parseDouble(info.qtyField.getText());
                            productTotal += qtyVal * info.sellingPrice;
                        } catch (Exception ignored) {}
                    }
                }
                
                double grandTotal = serviceTotal + productTotal;
                
                lblServiceSubtotal.setText(String.format("Service Subtotal: ₹%.2f", serviceSubtotal));
                lblServiceDiscount.setText(String.format("Service Discount (%.0f%%): -₹%.2f", 
                    discountPercent, serviceDiscount));
                lblServiceTotal.setText(String.format("Service Total: ₹%.2f", serviceTotal));
                lblProductTotal.setText(String.format("Product Total: ₹%.2f", productTotal));
                lblGrandTotal.setText(String.format("Grand Total: ₹%.2f", grandTotal));
            }
        };
        
        membershipCombo.addActionListener(a -> recalc.keyReleased(null));
        for (JComponent[] components : serviceSelections.values()) {
            ((JCheckBox) components[0]).addActionListener(a -> recalc.keyReleased(null));
        }
        
        // Add listeners to product components
        for (ProductInfo info : allProducts) {
            info.checkbox.addActionListener(a -> recalc.keyReleased(null));
            info.qtyField.addKeyListener(recalc);
        }

        // ==================== ASSEMBLE MAIN PANEL ====================
        // Create a scrollable center panel for services and products
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(servicePanel);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(membershipDiscountLabel);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(productMainPanel); // ✅ This includes search bar + products
        
        JScrollPane centerScroll = new JScrollPane(centerPanel);
        centerScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        centerScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        mainPanel.add(customerPanel, BorderLayout.NORTH);
        mainPanel.add(centerScroll, BorderLayout.CENTER);
        mainPanel.add(totalPanel, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(this, mainPanel, "Add Customer & Generate Invoice",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();
            
            if (name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and phone are required!");
                return;
            }

            // Collect services
            List<InvoiceService> services = new ArrayList<>();
            for (Map.Entry<Integer, JComponent[]> entry : serviceSelections.entrySet()) {
                JCheckBox chk = (JCheckBox) entry.getValue()[0];
                JLabel priceLabel = (JLabel) entry.getValue()[1];
                
                if (chk.isSelected()) {
                    try {
                        int serviceId = entry.getKey();
                        String serviceName = chk.getText();
                        String priceText = priceLabel.getText().replace("₹", "").trim();
                        double price = Double.parseDouble(priceText);
                        
                        services.add(new InvoiceService(serviceId, serviceName, price));
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Invalid service data!");
                        return;
                    }
                }
            }

            // ✅ Collect products using ProductInfo objects
            List<InvoiceItem> products = new ArrayList<>();
            StringBuilder validationErrors = new StringBuilder();
            
            for (ProductInfo info : allProducts) {
                if (info.checkbox.isSelected()) {
                    try {
                        String qtyText = info.qtyField.getText().trim();
                        if (qtyText.isEmpty()) {
                            validationErrors.append("- ").append(info.productName)
                                .append(": Please enter quantity\n");
                            continue;
                        }
                        
                        double quantity = Double.parseDouble(qtyText);
                        
                        if (quantity <= 0) {
                            validationErrors.append("- ").append(info.productName)
                                .append(": Quantity must be > 0\n");
                            continue;
                        }
                        
                        if (quantity > info.availableStock) {
                            validationErrors.append("- ").append(info.productName)
                                .append(": Requested ").append(String.format("%.2f", quantity))
                                .append(" but only ").append(String.format("%.2f", info.availableStock))
                                .append(" available\n");
                            continue;
                        }
                        
                        InvoiceItem item = new InvoiceItem(
                            info.productId,
                            info.productName,
                            info.brandName,
                            quantity,
                            info.sellingPrice,
                            info.actualPrice
                        );
                        products.add(item);
                        
                    } catch (NumberFormatException ex) {
                        validationErrors.append("- ").append(info.productName)
                            .append(": Invalid quantity\n");
                    }
                }
            }
            
            if (validationErrors.length() > 0) {
                JOptionPane.showMessageDialog(this, 
                    "Product validation errors:\n\n" + validationErrors.toString(),
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (services.isEmpty() && products.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select at least one service or product!");
                return;
            }

            // Create customer and invoice
            Customer c = new Customer();
            c.setName(name);
            c.setPhone(phone);
            c.setAddress(address);

            CustomerDAO cdao = new CustomerDAO();
            boolean customerAdded = cdao.addCustomer(c);
            
            if (customerAdded) {
                MembershipPlan selectedPlan = (MembershipPlan) membershipCombo.getSelectedItem();
                if (selectedPlan != null && selectedPlan.getPlanId() > 1) {
                    cdao.updateCustomerMembership(c.getCustomerId(), selectedPlan.getPlanId());
                }
                
                double discountPercent = selectedPlan != null ? selectedPlan.getDiscountPercentage() : 0.0;
                
                InvoiceDAO idao = new InvoiceDAO();
                boolean invoiceCreated = idao.createInvoice(c.getCustomerId(), c, services, products, discountPercent);
                
                if (invoiceCreated) {
                    String message = "✅ Customer added and invoice generated successfully!\n\n";
                    if (!services.isEmpty()) {
                        message += "Services: " + services.size() + "\n";
                        if (discountPercent > 0) {
                            message += "Membership discount applied: " + String.format("%.0f%%\n", discountPercent);
                        }
                    }
                    if (!products.isEmpty()) {
                        message += "Products sold: " + products.size() + "\n";
                        message += "Stock automatically updated!";
                    }
                    JOptionPane.showMessageDialog(this, message);
                    loadCustomers();
                } else {
                    JOptionPane.showMessageDialog(this, "Customer added but invoice generation failed!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add customer!");
            }
        }
    }
    
 // ✅ NEW: Get product type for filtering
    private String getProductType(int productId) {
        ProductDAO productDao = new ProductDAO();
        return productDao.getProductType(productId);
    }
   
    // Helper methods - ADD THESE TO CustomerPanel class:

    private double getSellingPriceForProduct(int productId) {
        StockDAO stockDao = new StockDAO();
        List<StockEntry> entries = stockDao.getAllStockEntries();
        
        double totalPrice = 0.0;
        int count = 0;
        
        for (StockEntry entry : entries) {
            if (entry.getProductId() == productId && "RUNNING".equals(entry.getStockStatus())) {
                totalPrice += entry.getSellingPricePerUnit();
                count++;
            }
        }
        
        return count > 0 ? totalPrice / count : 0.0;
    }

    private double getActualPriceForProduct(int productId) {
        StockDAO stockDao = new StockDAO();
        List<StockEntry> entries = stockDao.getAllStockEntries();
        
        double totalPrice = 0.0;
        int count = 0;
        
        for (StockEntry entry : entries) {
            if (entry.getProductId() == productId && "RUNNING".equals(entry.getStockStatus())) {
                totalPrice += entry.getActualPricePerUnit();
                count++;
            }
        }
        
        return count > 0 ? totalPrice / count : 0.0;
    }
    // View invoice directly from database
    private void viewInvoice(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer to view invoice.");
            return;
        }

        String customerId = (String) model.getValueAt(row, 0);
        CustomerDAO dao = new CustomerDAO();
        byte[] pdfData = dao.getInvoicePdfByCustomerId(customerId);

        if (pdfData == null || pdfData.length == 0) {
            JOptionPane.showMessageDialog(this, "No invoice found for this customer.");
            return;
        }

        try {
            // Create temporary file to view
            File tempFile = File.createTempFile("invoice_view_", ".pdf");
            tempFile.deleteOnExit();
            
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(pdfData);
            }
            
            Desktop.getDesktop().open(tempFile);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Unable to open PDF: " + ex.getMessage());
        }
    }

    // Export invoice to selected location
    private void exportInvoice(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a customer to export invoice.");
            return;
        }

        String customerId = (String) model.getValueAt(row, 0);
        CustomerDAO dao = new CustomerDAO();
        byte[] pdfData = dao.getInvoicePdfByCustomerId(customerId);
        String filename = dao.getInvoiceFilenameByCustomerId(customerId);

        if (pdfData == null || pdfData.length == 0) {
            JOptionPane.showMessageDialog(this, "No invoice found for this customer.");
            return;
        }

        // File chooser to select export location
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Invoice");
        fileChooser.setSelectedFile(new File(filename != null ? filename : "invoice_" + customerId + ".pdf"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            
            // Ensure .pdf extension
            if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
            }

            try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
                fos.write(pdfData);
                JOptionPane.showMessageDialog(this, "✅ Invoice exported successfully to:\n" + fileToSave.getAbsolutePath());
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to export PDF: " + ex.getMessage());
            }
        }
    }
}