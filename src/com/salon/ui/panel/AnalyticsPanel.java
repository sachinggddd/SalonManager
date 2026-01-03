package com.salon.ui.panel;

import com.salon.dao.AnalyticsDAO;
import com.salon.model.AnalyticsReport;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AnalyticsPanel extends JPanel {
    private JCheckBox chkCustomers, chkProductSales, chkProductUsage, chkStock;
    private JComboBox<String> cmbDateRange;
    private JButton btnGenerateReport, btnViewReport, btnExportReport, btnRefreshHistory;
    private JTable historyTable;
    private DefaultTableModel historyModel;
    private Integer currentUserId;
    private JLabel lblPreview;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public AnalyticsPanel(Integer userId) {
        this.currentUserId = userId;
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 248, 255));

        // Title
        JLabel title = new JLabel("📊 Business Analytics & Reports", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(40, 60, 120));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Report generation panel
        JPanel generationPanel = createReportGenerationPanel();
        
        // Report history panel
        JPanel historyPanel = createReportHistoryPanel();

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, generationPanel, historyPanel);
        splitPane.setResizeWeight(0.4);
        splitPane.setDividerLocation(280);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // Load initial data
        loadReportHistory();
    }

    private JPanel createReportGenerationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 102, 204), 2),
            "Generate New Report",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(0, 102, 204)
        ));

        // Options panel
        JPanel optionsPanel = new JPanel(new GridBagLayout());
        optionsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Section selection
        Font labelFont = new Font("Segoe UI", Font.BOLD, 12);
        JLabel lblSections = new JLabel("📋 Select Report Sections:");
        lblSections.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        optionsPanel.add(lblSections, gbc);

        // Checkboxes
        chkCustomers = new JCheckBox("Customer Analytics", true);
        chkProductSales = new JCheckBox("Product Sales Analytics", true);
        chkProductUsage = new JCheckBox("Product Usage Analytics", true);
        chkStock = new JCheckBox("Stock Analytics", true);

        Font checkboxFont = new Font("Segoe UI", Font.PLAIN, 11);
        chkCustomers.setFont(checkboxFont);
        chkProductSales.setFont(checkboxFont);
        chkProductUsage.setFont(checkboxFont);
        chkStock.setFont(checkboxFont);

        chkCustomers.setBackground(Color.WHITE);
        chkProductSales.setBackground(Color.WHITE);
        chkProductUsage.setBackground(Color.WHITE);
        chkStock.setBackground(Color.WHITE);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        optionsPanel.add(chkCustomers, gbc);
        gbc.gridx = 1;
        optionsPanel.add(chkProductSales, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        optionsPanel.add(chkProductUsage, gbc);
        gbc.gridx = 1;
        optionsPanel.add(chkStock, gbc);

        // Date range
        JLabel lblDateRange = new JLabel("📅 Date Range:");
        lblDateRange.setFont(labelFont);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 10, 8, 10);
        optionsPanel.add(lblDateRange, gbc);

        cmbDateRange = new JComboBox<>(new String[]{
            "All Time",
            "Last 7 Days",
            "Last 30 Days",
            "Last 3 Months",
            "Last 6 Months",
            "Last 12 Months",
            "This Month",
            "This Year"
        });
        cmbDateRange.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 10, 8, 10);
        optionsPanel.add(cmbDateRange, gbc);

        // Preview label
        lblPreview = new JLabel("Select options above to generate report");
        lblPreview.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblPreview.setForeground(Color.GRAY);
        gbc.gridy = 5;
        gbc.insets = new Insets(15, 10, 8, 10);
        optionsPanel.add(lblPreview, gbc);

        panel.add(optionsPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        btnGenerateReport = new JButton("🔨 Generate Report");
        btnGenerateReport.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGenerateReport.setBackground(new Color(0, 153, 0));
        btnGenerateReport.setForeground(Color.WHITE);
        btnGenerateReport.setFocusPainted(false);
        btnGenerateReport.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        buttonPanel.add(btnGenerateReport);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Button action
        btnGenerateReport.addActionListener(this::generateReport);

        // Update preview on checkbox change
        chkCustomers.addActionListener(e -> updatePreview());
        chkProductSales.addActionListener(e -> updatePreview());
        chkProductUsage.addActionListener(e -> updatePreview());
        chkStock.addActionListener(e -> updatePreview());
        cmbDateRange.addActionListener(e -> updatePreview());

        return panel;
    }

    private JPanel createReportHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(102, 102, 102), 1),
            "Report History",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Segoe UI", Font.BOLD, 13),
            new Color(102, 102, 102)
        ));

        // Table
        historyModel = new DefaultTableModel(
            new String[]{"ID", "Type", "Generated At", "Parameters"},
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        historyTable = new JTable(historyModel);
        historyTable.setRowHeight(25);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(300);

        JScrollPane scroll = new JScrollPane(historyTable);
        panel.add(scroll, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        
        btnViewReport = new JButton("👁️ View");
        btnExportReport = new JButton("💾 Export");
        btnRefreshHistory = new JButton("🔄 Refresh");
        
        buttonPanel.add(btnViewReport);
        buttonPanel.add(btnExportReport);
        buttonPanel.add(btnRefreshHistory);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Button actions
        btnViewReport.addActionListener(this::viewReport);
        btnExportReport.addActionListener(this::exportReport);
        btnRefreshHistory.addActionListener(e -> loadReportHistory());

        return panel;
    }

    private void updatePreview() {
        int sectionCount = 0;
        if (chkCustomers.isSelected()) sectionCount++;
        if (chkProductSales.isSelected()) sectionCount++;
        if (chkProductUsage.isSelected()) sectionCount++;
        if (chkStock.isSelected()) sectionCount++;

        if (sectionCount == 0) {
            lblPreview.setText("⚠️ Please select at least one section");
            lblPreview.setForeground(Color.RED);
            btnGenerateReport.setEnabled(false);
        } else {
            String range = (String) cmbDateRange.getSelectedItem();
            lblPreview.setText(String.format("✓ Report will include %d section(s) | Range: %s", 
                sectionCount, range));
            lblPreview.setForeground(new Color(0, 100, 0));
            btnGenerateReport.setEnabled(true);
        }
    }

    private void generateReport(ActionEvent e) {
        // Validation
        if (!chkCustomers.isSelected() && !chkProductSales.isSelected() &&
            !chkProductUsage.isSelected() && !chkStock.isSelected()) {
            JOptionPane.showMessageDialog(this, 
                "Please select at least one report section!",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show progress
        JDialog progressDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Generating Report", true);
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setString("Generating PDF report...");
        progressBar.setStringPainted(true);
        
        JPanel progressPanel = new JPanel(new BorderLayout(10, 10));
        progressPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        progressPanel.add(new JLabel("Please wait while the report is being generated..."), 
            BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        
        progressDialog.add(progressPanel);
        progressDialog.setSize(400, 120);
        progressDialog.setLocationRelativeTo(this);

        // Generate in background thread
        SwingWorker<byte[], Void> worker = new SwingWorker<byte[], Void>() {
            @Override
            protected byte[] doInBackground() throws Exception {
                AnalyticsDAO dao = new AnalyticsDAO();
                
                // Calculate date range
                Date[] dateRange = calculateDateRange((String) cmbDateRange.getSelectedItem());
                
                return dao.generateBusinessAnalyticsPDF(
                    chkCustomers.isSelected(),
                    chkProductSales.isSelected(),
                    chkProductUsage.isSelected(),
                    chkStock.isSelected(),
                    dateRange[0],
                    dateRange[1]
                );
            }

            @Override
            protected void done() {
                progressDialog.dispose();
                
                try {
                    byte[] pdfData = get();
                    
                    if (pdfData != null && pdfData.length > 0) {
                        // Save to database
                        AnalyticsReport report = new AnalyticsReport();
                        report.setReportType("BUSINESS_ANALYTICS");
                        report.setReportTitle("Business Analytics Report");
                        report.setReportPdfData(pdfData);
                        
                        // Build params JSON
                        StringBuilder params = new StringBuilder();
                        params.append("{");
                        params.append("\"dateRange\":\"").append(cmbDateRange.getSelectedItem()).append("\",");
                        params.append("\"sections\":[");
                        if (chkCustomers.isSelected()) params.append("\"customers\",");
                        if (chkProductSales.isSelected()) params.append("\"sales\",");
                        if (chkProductUsage.isSelected()) params.append("\"usage\",");
                        if (chkStock.isSelected()) params.append("\"stock\",");
                        if (params.charAt(params.length() - 1) == ',') {
                            params.deleteCharAt(params.length() - 1);
                        }
                        params.append("]}");
                        
                        report.setParamsJson(params.toString());
                        report.setReportPdfPath("report_" + System.currentTimeMillis() + ".pdf");
                        
                        AnalyticsDAO dao = new AnalyticsDAO();
                        boolean saved = dao.saveReport(report, currentUserId);
                        
                        if (saved) {
                            JOptionPane.showMessageDialog(AnalyticsPanel.this,
                                "✅ Report generated successfully!\n\n" +
                                "Size: " + String.format("%.2f KB", pdfData.length / 1024.0) + "\n" +
                                "The report has been saved to history.",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                            
                            loadReportHistory();
                            
                            // Ask if user wants to view now
                            int choice = JOptionPane.showConfirmDialog(AnalyticsPanel.this,
                                "Would you like to view the report now?",
                                "View Report",
                                JOptionPane.YES_NO_OPTION);
                            
                            if (choice == JOptionPane.YES_OPTION) {
                                viewReportDirect(pdfData, report.getReportPdfPath());
                            }
                        } else {
                            JOptionPane.showMessageDialog(AnalyticsPanel.this,
                                "Report generated but failed to save to database.",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                        }
                        
                    } else {
                        JOptionPane.showMessageDialog(AnalyticsPanel.this,
                            "Failed to generate report. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(AnalyticsPanel.this,
                        "Error generating report: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
        progressDialog.setVisible(true);
    }

    private Date[] calculateDateRange(String rangeType) {
        Calendar cal = Calendar.getInstance();
        Date endDate = cal.getTime();
        Date startDate = null;

        switch (rangeType) {
            case "Last 7 Days":
                cal.add(Calendar.DAY_OF_MONTH, -7);
                startDate = cal.getTime();
                break;
            case "Last 30 Days":
                cal.add(Calendar.DAY_OF_MONTH, -30);
                startDate = cal.getTime();
                break;
            case "Last 3 Months":
                cal.add(Calendar.MONTH, -3);
                startDate = cal.getTime();
                break;
            case "Last 6 Months":
                cal.add(Calendar.MONTH, -6);
                startDate = cal.getTime();
                break;
            case "Last 12 Months":
                cal.add(Calendar.MONTH, -12);
                startDate = cal.getTime();
                break;
            case "This Month":
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                startDate = cal.getTime();
                break;
            case "This Year":
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                startDate = cal.getTime();
                break;
            case "All Time":
            default:
                startDate = null;
                endDate = null;
                break;
        }

        return new Date[]{startDate, endDate};
    }

    private void loadReportHistory() {
        historyModel.setRowCount(0);
        AnalyticsDAO dao = new AnalyticsDAO();
        List<AnalyticsReport> reports = dao.getAllReports();
        
        for (AnalyticsReport report : reports) {
            historyModel.addRow(new Object[]{
                report.getReportId(),
                report.getReportType(),
                dateFormat.format(report.getExportedAt()),
                report.getParamsJson() != null ? 
                    formatParams(report.getParamsJson()) : "N/A"
            });
        }
    }

    private String formatParams(String paramsJson) {
        // Simple formatting of JSON params for display
        try {
            if (paramsJson == null || paramsJson.isEmpty()) {
                return "N/A";
            }
            
            // Extract date range and sections
            String dateRange = "All Time";
            if (paramsJson.contains("\"dateRange\":\"")) {
                int start = paramsJson.indexOf("\"dateRange\":\"") + 13;
                int end = paramsJson.indexOf("\"", start);
                if (end > start) {
                    dateRange = paramsJson.substring(start, end);
                }
            }
            
            int sectionCount = 0;
            if (paramsJson.contains("\"customers\"")) sectionCount++;
            if (paramsJson.contains("\"sales\"")) sectionCount++;
            if (paramsJson.contains("\"usage\"")) sectionCount++;
            if (paramsJson.contains("\"stock\"")) sectionCount++;
            
            return dateRange + " | " + sectionCount + " section(s)";
            
        } catch (Exception e) {
            return "N/A";
        }
    }

    private void viewReport(ActionEvent e) {
        int row = historyTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a report from the history to view.",
                "No Selection",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int reportId = (int) historyModel.getValueAt(row, 0);
        
        // For now, show message that view functionality needs report PDF storage
        // In production, you'd fetch the PDF from database
        JOptionPane.showMessageDialog(this,
            "View functionality requires storing PDF data in database.\n" +
            "Report ID: " + reportId + "\n\n" +
            "Please use Export to save and view the report.",
            "View Report",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void viewReportDirect(byte[] pdfData, String filename) {
        if (pdfData == null || pdfData.length == 0) {
            JOptionPane.showMessageDialog(this,
                "No report data available.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Create temporary file
            File tempFile = File.createTempFile("salon_report_", ".pdf");
            tempFile.deleteOnExit();
            
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(pdfData);
            }
            
            // Open with default PDF viewer
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tempFile);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Cannot open PDF automatically.\n" +
                    "File saved to: " + tempFile.getAbsolutePath(),
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error viewing report: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportReport(ActionEvent e) {
        int row = historyTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a report from the history to export.",
                "No Selection",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int reportId = (int) historyModel.getValueAt(row, 0);
        
        // Show dialog to generate fresh report for export
        int choice = JOptionPane.showConfirmDialog(this,
            "Do you want to export this report?\n\n" +
            "Note: A fresh copy will be generated with current data.",
            "Export Report",
            JOptionPane.YES_NO_OPTION);
        
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        // File chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Report");
        fileChooser.setSelectedFile(new File("Business_Analytics_Report_" + 
            new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".pdf"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            
            // Ensure .pdf extension
            if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
            }

            // Show progress
            JDialog progressDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                "Exporting Report", true);
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressBar.setString("Generating and exporting report...");
            progressBar.setStringPainted(true);
            
            JPanel progressPanel = new JPanel(new BorderLayout(10, 10));
            progressPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            progressPanel.add(new JLabel("Please wait..."), BorderLayout.NORTH);
            progressPanel.add(progressBar, BorderLayout.CENTER);
            
            progressDialog.add(progressPanel);
            progressDialog.setSize(400, 120);
            progressDialog.setLocationRelativeTo(this);

            File finalFileToSave = fileToSave;
            
            // Export in background thread
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    try {
                        AnalyticsDAO dao = new AnalyticsDAO();
                        
                        // Generate fresh report with all sections
                        byte[] pdfData = dao.generateBusinessAnalyticsPDF(
                            true,  // customers
                            true,  // product sales
                            true,  // product usage
                            true,  // stock
                            null,  // all time
                            null
                        );
                        
                        if (pdfData != null && pdfData.length > 0) {
                            try (FileOutputStream fos = new FileOutputStream(finalFileToSave)) {
                                fos.write(pdfData);
                            }
                            return true;
                        }
                        return false;
                        
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return false;
                    }
                }

                @Override
                protected void done() {
                    progressDialog.dispose();
                    
                    try {
                        boolean success = get();
                        
                        if (success) {
                            int openChoice = JOptionPane.showConfirmDialog(AnalyticsPanel.this,
                                "✅ Report exported successfully to:\n" + 
                                finalFileToSave.getAbsolutePath() + "\n\n" +
                                "Would you like to open the report now?",
                                "Export Success",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.INFORMATION_MESSAGE);
                            
                            if (openChoice == JOptionPane.YES_OPTION) {
                                try {
                                    if (Desktop.isDesktopSupported()) {
                                        Desktop.getDesktop().open(finalFileToSave);
                                    }
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(AnalyticsPanel.this,
                                        "Report saved but cannot open automatically.",
                                        "Info",
                                        JOptionPane.INFORMATION_MESSAGE);
                                }
                            }
                        } else {
                            JOptionPane.showMessageDialog(AnalyticsPanel.this,
                                "Failed to export report. Please try again.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        }
                        
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(AnalyticsPanel.this,
                            "Error during export: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            worker.execute();
            progressDialog.setVisible(true);
        }
    }
}