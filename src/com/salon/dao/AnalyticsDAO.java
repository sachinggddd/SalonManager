package com.salon.dao;

import com.salon.model.AnalyticsReport;
import com.salon.model.StockMovement;
import com.salon.util.DBConnection;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.ByteArrayOutputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.Date;

public class AnalyticsDAO {
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // Save report to database
    public boolean saveReport(AnalyticsReport report, Integer userId) {
        String sql = "INSERT INTO reports (report_type, params_json, exported_by, exported_at, report_pdf_path) " +
                     "VALUES (?, ?, ?, NOW(), ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, report.getReportType());
            ps.setString(2, report.getParamsJson());
            ps.setObject(3, userId);
            ps.setString(4, report.getReportPdfPath());
            
            int rows = ps.executeUpdate();
            
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    report.setReportId(rs.getInt(1));
                }
                return true;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get all generated reports
    public List<AnalyticsReport> getAllReports() {
        List<AnalyticsReport> reports = new ArrayList<>();
        String sql = "SELECT r.report_id, r.report_type, r.params_json, r.exported_at, " +
                     "r.report_pdf_path, u.full_name " +
                     "FROM reports r " +
                     "LEFT JOIN users u ON r.exported_by = u.user_id " +
                     "ORDER BY r.exported_at DESC";
        
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                AnalyticsReport report = new AnalyticsReport();
                report.setReportId(rs.getInt("report_id"));
                report.setReportType(rs.getString("report_type"));
                report.setParamsJson(rs.getString("params_json"));
                report.setExportedAt(rs.getTimestamp("exported_at"));
                report.setReportPdfPath(rs.getString("report_pdf_path"));
                reports.add(report);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reports;
    }

    // Generate comprehensive business analytics PDF
    public byte[] generateBusinessAnalyticsPDF(boolean includeCustomers, boolean includeProductSales, 
                                               boolean includeProductUsage, boolean includeStock,
                                               Date startDate, Date endDate) throws Exception {
        
        Document document = new Document(PageSize.A4, 36, 36, 54, 36);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            
            // Add header/footer
            writer.setPageEvent(new PDFHeaderFooter());
            
            document.open();
            
            // Title
            addReportTitle(document, startDate, endDate);
            
            // Executive Summary
            addExecutiveSummary(document, startDate, endDate);
            
            // Customer Analytics
            if (includeCustomers) {
                document.newPage();
                addCustomerAnalytics(document, startDate, endDate);
            }
            
            // Product Sales Analytics
            if (includeProductSales) {
                document.newPage();
                addProductSalesAnalytics(document, startDate, endDate);
            }
            
            // Product Usage Analytics
            if (includeProductUsage) {
                document.newPage();
                addProductUsageAnalytics(document, startDate, endDate);
            }
            
            // Stock Analytics
            if (includeStock) {
                document.newPage();
                addStockAnalytics(document);
            }
            
        } finally {
            document.close();
        }
        
        return baos.toByteArray();
    }

    private void addReportTitle(Document document, Date startDate, Date endDate) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, BaseColor.DARK_GRAY);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.GRAY);
        
        Paragraph title = new Paragraph("Business Analytics Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);
        
        String period = "All Time";
        if (startDate != null && endDate != null) {
            period = dateFormat.format(startDate) + " to " + dateFormat.format(endDate);
        }
        
        Paragraph subtitle = new Paragraph("Period: " + period, subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(5);
        document.add(subtitle);
        
        Paragraph generated = new Paragraph("Generated: " + dateTimeFormat.format(new Date()), subtitleFont);
        generated.setAlignment(Element.ALIGN_CENTER);
        generated.setSpacingAfter(20);
        document.add(generated);
        
        addHorizontalLine(document);
    }

    private void addExecutiveSummary(Document document, Date startDate, Date endDate) throws Exception {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new BaseColor(0, 51, 102));
        Paragraph header = new Paragraph("📊 Executive Summary", headerFont);
        header.setSpacingBefore(10);
        header.setSpacingAfter(15);
        document.add(header);
        
        // Fetch summary data
        Map<String, Object> summary = getExecutiveSummaryData(startDate, endDate);
        
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 1});
        table.setSpacingAfter(20);
        
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
        
        // Total Customers
        addSummaryRow(table, "Total Customers", summary.get("totalCustomers").toString(), labelFont, valueFont);
        
        // Total Revenue
        addSummaryRow(table, "Total Revenue (Services + Products)", 
                     String.format("₹%.2f", summary.get("totalRevenue")), labelFont, valueFont);
        
        // Service Revenue
        addSummaryRow(table, "  └─ Service Revenue", 
                     String.format("₹%.2f", summary.get("serviceRevenue")), labelFont, valueFont);
        
        // Product Sales Revenue
        addSummaryRow(table, "  └─ Product Sales Revenue", 
                     String.format("₹%.2f", summary.get("productSalesRevenue")), labelFont, valueFont);
        
        // Total Products Sold
        addSummaryRow(table, "Total Products Sold (Quantity)", 
                     String.format("%.2f units", summary.get("totalProductsSold")), labelFont, valueFont);
        
        // Product Usage
        addSummaryRow(table, "Total Products Used (Quantity)", 
                     String.format("%.2f units", summary.get("totalProductsUsed")), labelFont, valueFont);
        
        // Gross Profit from Sales
        addSummaryRow(table, "Gross Profit from Product Sales", 
                     String.format("₹%.2f", summary.get("grossProfit")), labelFont, valueFont);
        
        // Stock Value
        addSummaryRow(table, "Current Stock Value (Actual Cost)", 
                     String.format("₹%.2f", summary.get("stockValueActual")), labelFont, valueFont);
        
        // Stock Value (Selling)
        addSummaryRow(table, "Current Stock Value (Selling Price)", 
                     String.format("₹%.2f", summary.get("stockValueSelling")), labelFont, valueFont);
        
        document.add(table);
    }

    private void addSummaryRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(8);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(8);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private Map<String, Object> getExecutiveSummaryData(Date startDate, Date endDate) {
        Map<String, Object> summary = new HashMap<>();
        
        try (Connection conn = DBConnection.getConnection()) {
            // Total customers
            String customerSql = "SELECT COUNT(*) as total FROM customers";
            if (startDate != null && endDate != null) {
                customerSql += " WHERE created_at BETWEEN ? AND ?";
            }
            
            try (PreparedStatement ps = conn.prepareStatement(customerSql)) {
                if (startDate != null && endDate != null) {
                    ps.setTimestamp(1, new Timestamp(startDate.getTime()));
                    ps.setTimestamp(2, new Timestamp(endDate.getTime()));
                }
                ResultSet rs = ps.executeQuery();
                summary.put("totalCustomers", rs.next() ? rs.getInt("total") : 0);
            }
            
            // Service revenue
            String serviceSql = "SELECT COALESCE(SUM(total_services), 0) as total FROM invoices";
            if (startDate != null && endDate != null) {
                serviceSql += " WHERE invoice_date BETWEEN ? AND ?";
            }
            
            try (PreparedStatement ps = conn.prepareStatement(serviceSql)) {
                if (startDate != null && endDate != null) {
                    ps.setTimestamp(1, new Timestamp(startDate.getTime()));
                    ps.setTimestamp(2, new Timestamp(endDate.getTime()));
                }
                ResultSet rs = ps.executeQuery();
                double serviceRevenue = rs.next() ? rs.getDouble("total") : 0.0;
                summary.put("serviceRevenue", serviceRevenue);
            }
            
            // Product sales revenue and profit
            String salesSql = "SELECT " +
                             "COALESCE(SUM(ii.subtotal), 0) as revenue, " +
                             "COALESCE(SUM(ii.quantity * ii.actual_price_per_unit), 0) as cost, " +
                             "COALESCE(SUM(ii.quantity), 0) as qty " +
                             "FROM invoice_items ii " +
                             "JOIN invoices inv ON ii.invoice_id = inv.invoice_id";
            if (startDate != null && endDate != null) {
                salesSql += " WHERE inv.invoice_date BETWEEN ? AND ?";
            }
            
            try (PreparedStatement ps = conn.prepareStatement(salesSql)) {
                if (startDate != null && endDate != null) {
                    ps.setTimestamp(1, new Timestamp(startDate.getTime()));
                    ps.setTimestamp(2, new Timestamp(endDate.getTime()));
                }
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    double productRevenue = rs.getDouble("revenue");
                    double productCost = rs.getDouble("cost");
                    summary.put("productSalesRevenue", productRevenue);
                    summary.put("totalProductsSold", rs.getDouble("qty"));
                    summary.put("grossProfit", productRevenue - productCost);
                } else {
                    summary.put("productSalesRevenue", 0.0);
                    summary.put("totalProductsSold", 0.0);
                    summary.put("grossProfit", 0.0);
                }
            }
            
            // Total revenue
            double totalRevenue = (double) summary.get("serviceRevenue") + 
                                 (double) summary.get("productSalesRevenue");
            summary.put("totalRevenue", totalRevenue);
            
            // Product usage
            String usageSql = "SELECT COALESCE(SUM(ABS(quantity_changed)), 0) as total " +
                             "FROM stock_movements WHERE movement_type = 'USAGE'";
            if (startDate != null && endDate != null) {
                usageSql += " AND movement_date BETWEEN ? AND ?";
            }
            
            try (PreparedStatement ps = conn.prepareStatement(usageSql)) {
                if (startDate != null && endDate != null) {
                    ps.setTimestamp(1, new Timestamp(startDate.getTime()));
                    ps.setTimestamp(2, new Timestamp(endDate.getTime()));
                }
                ResultSet rs = ps.executeQuery();
                summary.put("totalProductsUsed", rs.next() ? rs.getDouble("total") : 0.0);
            }
            
            // Current stock value
            String stockSql = "SELECT " +
                             "COALESCE(SUM(se.quantity * se.actual_price_per_unit), 0) as actual_value, " +
                             "COALESCE(SUM(se.quantity * se.selling_price_per_unit), 0) as selling_value " +
                             "FROM stock_entries se " +
                             "LEFT JOIN (" +
                             "    SELECT product_id, SUM(ABS(quantity_changed)) as used " +
                             "    FROM stock_movements " +
                             "    WHERE movement_type IN ('USAGE', 'SALE') " +
                             "    GROUP BY product_id" +
                             ") sm ON se.product_id = sm.product_id";
            
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(stockSql)) {
                if (rs.next()) {
                    summary.put("stockValueActual", rs.getDouble("actual_value"));
                    summary.put("stockValueSelling", rs.getDouble("selling_value"));
                } else {
                    summary.put("stockValueActual", 0.0);
                    summary.put("stockValueSelling", 0.0);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            // Set default values on error
            summary.put("totalCustomers", 0);
            summary.put("serviceRevenue", 0.0);
            summary.put("productSalesRevenue", 0.0);
            summary.put("totalRevenue", 0.0);
            summary.put("totalProductsSold", 0.0);
            summary.put("totalProductsUsed", 0.0);
            summary.put("grossProfit", 0.0);
            summary.put("stockValueActual", 0.0);
            summary.put("stockValueSelling", 0.0);
        }
        
        return summary;
    }

    private void addCustomerAnalytics(Document document, Date startDate, Date endDate) throws Exception {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new BaseColor(0, 51, 102));
        Paragraph header = new Paragraph("👥 Customer Analytics", headerFont);
        header.setSpacingBefore(10);
        header.setSpacingAfter(15);
        document.add(header);
        
        // Customer summary
        Map<String, Object> custData = getCustomerData(startDate, endDate);
        
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        
        document.add(new Paragraph("Total Customers: " + custData.get("totalCustomers"), boldFont));
        document.add(new Paragraph("Total Service Revenue: ₹" + 
                     String.format("%.2f", custData.get("totalServiceRevenue")), boldFont));
        document.add(new Paragraph(" ", normalFont));
        
        // Top customers table
        Paragraph subHeader = new Paragraph("Top 10 Customers by Revenue", boldFont);
        subHeader.setSpacingBefore(10);
        subHeader.setSpacingAfter(10);
        document.add(subHeader);
        
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 1, 1, 1, 1});
        
        // Headers
        addTableHeader(table, new String[]{"Customer", "Phone", "Invoices", "Services (₹)", "Products (₹)"});
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topCustomers = (List<Map<String, Object>>) custData.get("topCustomers");
        
        for (Map<String, Object> customer : topCustomers) {
            addTableCell(table, customer.get("name").toString(), normalFont);
            addTableCell(table, customer.get("phone").toString(), normalFont);
            addTableCell(table, customer.get("invoiceCount").toString(), normalFont);
            addTableCell(table, String.format("₹%.2f", customer.get("serviceTotal")), normalFont);
            addTableCell(table, String.format("₹%.2f", customer.get("productTotal")), normalFont);
        }
        
        document.add(table);
    }

    private Map<String, Object> getCustomerData(Date startDate, Date endDate) {
        Map<String, Object> data = new HashMap<>();
        
        try (Connection conn = DBConnection.getConnection()) {
            // Total customers and revenue
            String summarySql = "SELECT COUNT(DISTINCT c.customer_id) as total_customers, " +
                               "COALESCE(SUM(i.total_services), 0) as service_revenue " +
                               "FROM customers c " +
                               "LEFT JOIN invoices i ON c.customer_id = i.customer_id";
            
            if (startDate != null && endDate != null) {
                summarySql += " WHERE i.invoice_date BETWEEN ? AND ?";
            }
            
            try (PreparedStatement ps = conn.prepareStatement(summarySql)) {
                if (startDate != null && endDate != null) {
                    ps.setTimestamp(1, new Timestamp(startDate.getTime()));
                    ps.setTimestamp(2, new Timestamp(endDate.getTime()));
                }
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    data.put("totalCustomers", rs.getInt("total_customers"));
                    data.put("totalServiceRevenue", rs.getDouble("service_revenue"));
                }
            }
            
            // Top customers
            String topSql = "SELECT c.name, c.phone, " +
                           "COUNT(i.invoice_id) as invoice_count, " +
                           "COALESCE(SUM(i.total_services), 0) as service_total, " +
                           "COALESCE(SUM(i.total_products), 0) as product_total " +
                           "FROM customers c " +
                           "LEFT JOIN invoices i ON c.customer_id = i.customer_id ";
            
            if (startDate != null && endDate != null) {
                topSql += "WHERE i.invoice_date BETWEEN ? AND ? ";
            }
            
            topSql += "GROUP BY c.customer_id, c.name, c.phone " +
                     "ORDER BY (COALESCE(SUM(i.total_services), 0) + COALESCE(SUM(i.total_products), 0)) DESC " +
                     "LIMIT 10";
            
            List<Map<String, Object>> topCustomers = new ArrayList<>();
            
            try (PreparedStatement ps = conn.prepareStatement(topSql)) {
                if (startDate != null && endDate != null) {
                    ps.setTimestamp(1, new Timestamp(startDate.getTime()));
                    ps.setTimestamp(2, new Timestamp(endDate.getTime()));
                }
                ResultSet rs = ps.executeQuery();
                
                while (rs.next()) {
                    Map<String, Object> customer = new HashMap<>();
                    customer.put("name", rs.getString("name"));
                    customer.put("phone", rs.getString("phone"));
                    customer.put("invoiceCount", rs.getInt("invoice_count"));
                    customer.put("serviceTotal", rs.getDouble("service_total"));
                    customer.put("productTotal", rs.getDouble("product_total"));
                    topCustomers.add(customer);
                }
            }
            
            data.put("topCustomers", topCustomers);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return data;
    }

    private void addProductSalesAnalytics(Document document, Date startDate, Date endDate) throws Exception {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new BaseColor(0, 51, 102));
        Paragraph header = new Paragraph("💰 Product Sales Analytics", headerFont);
        header.setSpacingBefore(10);
        header.setSpacingAfter(15);
        document.add(header);
        
        Map<String, Object> salesData = getProductSalesData(startDate, endDate);
        
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        
        document.add(new Paragraph("Total Quantity Sold: " + 
                     String.format("%.2f units", salesData.get("totalQty")), boldFont));
        document.add(new Paragraph("Total Revenue: ₹" + 
                     String.format("%.2f", salesData.get("totalRevenue")), boldFont));
        document.add(new Paragraph("Total Profit: ₹" + 
                     String.format("%.2f", salesData.get("totalProfit")), boldFont));
        document.add(new Paragraph(" ", normalFont));
        
        // Top products table
        Paragraph subHeader = new Paragraph("Top Selling Products", boldFont);
        subHeader.setSpacingBefore(10);
        subHeader.setSpacingAfter(10);
        document.add(subHeader);
        
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 1, 0.8f, 1, 1, 1});
        
        addTableHeader(table, new String[]{"Product", "Brand", "Qty", "Revenue", "Cost", "Profit"});
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topProducts = (List<Map<String, Object>>) salesData.get("topProducts");
        
        for (Map<String, Object> product : topProducts) {
            addTableCell(table, product.get("name").toString(), normalFont);
            addTableCell(table, product.get("brand").toString(), normalFont);
            addTableCell(table, String.format("%.2f", product.get("totalQty")), normalFont);
            addTableCell(table, String.format("₹%.2f", product.get("revenue")), normalFont);
            addTableCell(table, String.format("₹%.2f", product.get("cost")), normalFont);
            addTableCell(table, String.format("₹%.2f", product.get("profit")), normalFont);
        }
        
        document.add(table);
    }

    private Map<String, Object> getProductSalesData(Date startDate, Date endDate) {
        Map<String, Object> data = new HashMap<>();
        
        try (Connection conn = DBConnection.getConnection()) {
            // Total sales summary
            String summarySql = "SELECT " +
                               "COALESCE(SUM(ii.quantity), 0) as total_qty, " +
                               "COALESCE(SUM(ii.subtotal), 0) as total_revenue, " +
                               "COALESCE(SUM(ii.quantity * ii.actual_price_per_unit), 0) as total_cost " +
                               "FROM invoice_items ii " +
                               "JOIN invoices inv ON ii.invoice_id = inv.invoice_id";
            
            if (startDate != null && endDate != null) {
                summarySql += " WHERE inv.invoice_date BETWEEN ? AND ?";
            }
            
            try (PreparedStatement ps = conn.prepareStatement(summarySql)) {
                if (startDate != null && endDate != null) {
                    ps.setTimestamp(1, new Timestamp(startDate.getTime()));
                    ps.setTimestamp(2, new Timestamp(endDate.getTime()));
                }
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    double totalRevenue = rs.getDouble("total_revenue");
                    double totalCost = rs.getDouble("total_cost");
                    data.put("totalQty", rs.getDouble("total_qty"));
                    data.put("totalRevenue", totalRevenue);
                    data.put("totalProfit", totalRevenue - totalCost);
                }
            }
            
            // Top selling products
            String topSql = "SELECT p.name, p.brand, " +
                           "SUM(ii.quantity) as total_qty, " +
                           "SUM(ii.subtotal) as revenue, " +
                           "SUM(ii.quantity * ii.actual_price_per_unit) as cost " +
                           "FROM invoice_items ii " +
                           "JOIN products p ON ii.product_id = p.product_id " +
                           "JOIN invoices inv ON ii.invoice_id = inv.invoice_id ";
            
            if (startDate != null && endDate != null) {
                topSql += "WHERE inv.invoice_date BETWEEN ? AND ? ";
            }
            
            topSql += "GROUP BY p.product_id, p.name, p.brand " +
                     "ORDER BY revenue DESC " +
                     "LIMIT 15";
            
            List<Map<String, Object>> topProducts = new ArrayList<>();
            
            try (PreparedStatement ps = conn.prepareStatement(topSql)) {
                if (startDate != null && endDate != null) {
                    ps.setTimestamp(1, new Timestamp(startDate.getTime()));
                    ps.setTimestamp(2, new Timestamp(endDate.getTime()));
                }
                ResultSet rs = ps.executeQuery();
                
                while (rs.next()) {
                    Map<String, Object> product = new HashMap<>();
                    product.put("name", rs.getString("name"));
                    product.put("brand", rs.getString("brand"));
                    product.put("totalQty", rs.getDouble("total_qty"));
                    double revenue = rs.getDouble("revenue");
                    double cost = rs.getDouble("cost");
                    product.put("revenue", revenue);
                    product.put("cost", cost);
                    product.put("profit", revenue - cost);
                    topProducts.add(product);
                }
            }
            
            data.put("topProducts", topProducts);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return data;
    }

    private void addProductUsageAnalytics(Document document, Date startDate, Date endDate) throws Exception {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new BaseColor(0, 51, 102));
        Paragraph header = new Paragraph("🔧 Product Usage Analytics", headerFont);
        header.setSpacingBefore(10);
        header.setSpacingAfter(15);
document.add(header);
        
        Map<String, Object> usageData = getProductUsageData(startDate, endDate);
        
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        
        document.add(new Paragraph("Total Quantity Used: " + 
                     String.format("%.2f units", usageData.get("totalQty")), boldFont));
        document.add(new Paragraph("Total Actual Cost: ₹" + 
                     String.format("%.2f", usageData.get("totalActualCost")), boldFont));
        document.add(new Paragraph("Total Selling Value: ₹" + 
                     String.format("%.2f", usageData.get("totalSellingValue")), boldFont));
        document.add(new Paragraph(" ", normalFont));
        
        // Top used products table
        Paragraph subHeader = new Paragraph("Most Used Products", boldFont);
        subHeader.setSpacingBefore(10);
        subHeader.setSpacingAfter(10);
        document.add(subHeader);
        
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 1, 1, 1, 1});
        
        addTableHeader(table, new String[]{"Product", "Brand", "Qty Used", "Actual Cost", "Selling Value"});
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topUsed = (List<Map<String, Object>>) usageData.get("topUsed");
        
        for (Map<String, Object> product : topUsed) {
            addTableCell(table, product.get("name").toString(), normalFont);
            addTableCell(table, product.get("brand").toString(), normalFont);
            addTableCell(table, String.format("%.2f", product.get("totalQty")), normalFont);
            addTableCell(table, String.format("₹%.2f", product.get("actualCost")), normalFont);
            addTableCell(table, String.format("₹%.2f", product.get("sellingValue")), normalFont);
        }
        
        document.add(table);
    }

    private Map<String, Object> getProductUsageData(Date startDate, Date endDate) {
        Map<String, Object> data = new HashMap<>();
        
        try (Connection conn = DBConnection.getConnection()) {
            // Total usage summary
            String summarySql = "SELECT " +
                               "COALESCE(SUM(ABS(sm.quantity_changed)), 0) as total_qty, " +
                               "COALESCE(SUM(ABS(sm.quantity_changed) * " +
                               "    (SELECT AVG(se.actual_price_per_unit) " +
                               "     FROM stock_entries se " +
                               "     WHERE se.product_id = sm.product_id)), 0) as actual_cost, " +
                               "COALESCE(SUM(ABS(sm.quantity_changed) * " +
                               "    (SELECT AVG(se.selling_price_per_unit) " +
                               "     FROM stock_entries se " +
                               "     WHERE se.product_id = sm.product_id)), 0) as selling_value " +
                               "FROM stock_movements sm " +
                               "WHERE sm.movement_type = 'USAGE'";
            
            if (startDate != null && endDate != null) {
                summarySql += " AND sm.movement_date BETWEEN ? AND ?";
            }
            
            try (PreparedStatement ps = conn.prepareStatement(summarySql)) {
                if (startDate != null && endDate != null) {
                    ps.setTimestamp(1, new Timestamp(startDate.getTime()));
                    ps.setTimestamp(2, new Timestamp(endDate.getTime()));
                }
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    data.put("totalQty", rs.getDouble("total_qty"));
                    data.put("totalActualCost", rs.getDouble("actual_cost"));
                    data.put("totalSellingValue", rs.getDouble("selling_value"));
                }
            }
            
            // Top used products
            String topSql = "SELECT p.name, p.brand, " +
                           "SUM(ABS(sm.quantity_changed)) as total_qty, " +
                           "SUM(ABS(sm.quantity_changed) * " +
                           "    (SELECT AVG(se.actual_price_per_unit) " +
                           "     FROM stock_entries se " +
                           "     WHERE se.product_id = p.product_id)) as actual_cost, " +
                           "SUM(ABS(sm.quantity_changed) * " +
                           "    (SELECT AVG(se.selling_price_per_unit) " +
                           "     FROM stock_entries se " +
                           "     WHERE se.product_id = p.product_id)) as selling_value " +
                           "FROM stock_movements sm " +
                           "JOIN products p ON sm.product_id = p.product_id " +
                           "WHERE sm.movement_type = 'USAGE'";
            
            if (startDate != null && endDate != null) {
                topSql += " AND sm.movement_date BETWEEN ? AND ?";
            }
            
            topSql += " GROUP BY p.product_id, p.name, p.brand " +
                     "ORDER BY total_qty DESC " +
                     "LIMIT 15";
            
            List<Map<String, Object>> topUsed = new ArrayList<>();
            
            try (PreparedStatement ps = conn.prepareStatement(topSql)) {
                if (startDate != null && endDate != null) {
                    ps.setTimestamp(1, new Timestamp(startDate.getTime()));
                    ps.setTimestamp(2, new Timestamp(endDate.getTime()));
                }
                ResultSet rs = ps.executeQuery();
                
                while (rs.next()) {
                    Map<String, Object> product = new HashMap<>();
                    product.put("name", rs.getString("name"));
                    product.put("brand", rs.getString("brand"));
                    product.put("totalQty", rs.getDouble("total_qty"));
                    product.put("actualCost", rs.getDouble("actual_cost"));
                    product.put("sellingValue", rs.getDouble("selling_value"));
                    topUsed.add(product);
                }
            }
            
            data.put("topUsed", topUsed);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return data;
    }

    private void addStockAnalytics(Document document) throws Exception {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new BaseColor(0, 51, 102));
        Paragraph header = new Paragraph("📦 Stock Analytics", headerFont);
        header.setSpacingBefore(10);
        header.setSpacingAfter(15);
        document.add(header);
        
        Map<String, Object> stockData = getStockData();
        
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        
        document.add(new Paragraph("Total Stock Value (Actual): ₹" + 
                     String.format("%.2f", stockData.get("totalActualValue")), boldFont));
        document.add(new Paragraph("Total Stock Value (Selling): ₹" + 
                     String.format("%.2f", stockData.get("totalSellingValue")), boldFont));
        document.add(new Paragraph("Potential Profit Margin: ₹" + 
                     String.format("%.2f", stockData.get("potentialProfit")), boldFont));
        document.add(new Paragraph(" ", normalFont));
        
        // Current stock levels table
        Paragraph subHeader = new Paragraph("Current Stock Levels", boldFont);
        subHeader.setSpacingBefore(10);
        subHeader.setSpacingAfter(10);
        document.add(subHeader);
        
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 1, 1, 1, 1, 1});
        
        addTableHeader(table, new String[]{"Product", "Brand", "Available", "Actual Value", "Selling Value", "Status"});
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stockLevels = (List<Map<String, Object>>) stockData.get("stockLevels");
        
        for (Map<String, Object> stock : stockLevels) {
            addTableCell(table, stock.get("name").toString(), normalFont);
            addTableCell(table, stock.get("brand").toString(), normalFont);
            addTableCell(table, String.format("%.2f", stock.get("available")), normalFont);
            addTableCell(table, String.format("₹%.2f", stock.get("actualValue")), normalFont);
            addTableCell(table, String.format("₹%.2f", stock.get("sellingValue")), normalFont);
            
            double available = (double) stock.get("available");
            String status = available <= 0 ? "Out of Stock" : 
                          available < 10 ? "Low Stock" : "In Stock";
            BaseColor statusColor = available <= 0 ? BaseColor.RED : 
                                   available < 10 ? BaseColor.ORANGE : BaseColor.GREEN;
            
            PdfPCell statusCell = new PdfPCell(new Phrase(status, 
                FontFactory.getFont(FontFactory.HELVETICA, 9, statusColor)));
            statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            statusCell.setPadding(5);
            table.addCell(statusCell);
        }
        
        document.add(table);
        
        // Low stock warning
        document.add(new Paragraph(" ", normalFont));
        Paragraph warning = new Paragraph("⚠️ Low Stock Alert", 
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.RED));
        warning.setSpacingBefore(10);
        document.add(warning);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lowStock = (List<Map<String, Object>>) stockData.get("lowStock");
        
        if (lowStock.isEmpty()) {
            document.add(new Paragraph("✓ All products are adequately stocked.", normalFont));
        } else {
            for (Map<String, Object> item : lowStock) {
                document.add(new Paragraph("• " + item.get("name") + " (" + item.get("brand") + "): " +
                    String.format("%.2f units remaining", item.get("available")), normalFont));
            }
        }
    }

    private Map<String, Object> getStockData() {
        Map<String, Object> data = new HashMap<>();
        
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT " +
                        "    p.product_id, " +
                        "    p.name, " +
                        "    p.brand, " +
                        "    COALESCE(se_sum.total_added, 0) AS total_added, " +
                        "    COALESCE(ABS(sm_sum.total_used), 0) AS total_used, " +
                        "    (COALESCE(se_sum.total_added, 0) - COALESCE(ABS(sm_sum.total_used), 0)) AS available, " +
                        "    COALESCE(se_sum.avg_actual, 0) AS avg_actual, " +
                        "    COALESCE(se_sum.avg_selling, 0) AS avg_selling " +
                        "FROM products p " +
                        "LEFT JOIN ( " +
                        "    SELECT product_id, " +
                        "           SUM(quantity) AS total_added, " +
                        "           AVG(actual_price_per_unit) AS avg_actual, " +
                        "           AVG(selling_price_per_unit) AS avg_selling " +
                        "    FROM stock_entries " +
                        "    GROUP BY product_id " +
                        ") se_sum ON p.product_id = se_sum.product_id " +
                        "LEFT JOIN ( " +
                        "    SELECT product_id, SUM(quantity_changed) AS total_used " +
                        "    FROM stock_movements " +
                        "    WHERE movement_type IN ('USAGE', 'SALE') " +
                        "    GROUP BY product_id " +
                        ") sm_sum ON p.product_id = sm_sum.product_id " +
                        "WHERE p.is_discontinued = 0 " +
                        "ORDER BY available ASC";
            
            List<Map<String, Object>> stockLevels = new ArrayList<>();
            List<Map<String, Object>> lowStock = new ArrayList<>();
            double totalActualValue = 0.0;
            double totalSellingValue = 0.0;
            
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                
                while (rs.next()) {
                    Map<String, Object> stock = new HashMap<>();
                    stock.put("name", rs.getString("name"));
                    stock.put("brand", rs.getString("brand"));
                    double available = rs.getDouble("available");
                    double avgActual = rs.getDouble("avg_actual");
                    double avgSelling = rs.getDouble("avg_selling");
                    
                    stock.put("available", available);
                    stock.put("actualValue", available * avgActual);
                    stock.put("sellingValue", available * avgSelling);
                    
                    stockLevels.add(stock);
                    
                    totalActualValue += available * avgActual;
                    totalSellingValue += available * avgSelling;
                    
                    if (available > 0 && available < 10) {
                        lowStock.add(stock);
                    }
                }
            }
            
            data.put("stockLevels", stockLevels);
            data.put("lowStock", lowStock);
            data.put("totalActualValue", totalActualValue);
            data.put("totalSellingValue", totalSellingValue);
            data.put("potentialProfit", totalSellingValue - totalActualValue);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return data;
    }

    private void addTableHeader(PdfPTable table, String[] headers) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE);
        
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new BaseColor(0, 51, 102));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }
    }

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addHorizontalLine(Document document) throws DocumentException {
        LineSeparator line = new LineSeparator();
        line.setLineColor(new BaseColor(200, 200, 200));
        document.add(new Chunk(line));
    }

    // PDF Header/Footer class
    class PDFHeaderFooter extends PdfPageEventHelper {
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);
        
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable footer = new PdfPTable(2);
            try {
                footer.setWidths(new int[]{1, 1});
                footer.setTotalWidth(527);
                footer.setLockedWidth(true);
                footer.getDefaultCell().setBorder(Rectangle.TOP);
                footer.getDefaultCell().setFixedHeight(20);
                
                PdfPCell leftCell = new PdfPCell(new Phrase("Salon Management System", footerFont));
                leftCell.setBorder(Rectangle.TOP);
                leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                footer.addCell(leftCell);
                
                PdfPCell rightCell = new PdfPCell(new Phrase("Page " + writer.getPageNumber(), footerFont));
                rightCell.setBorder(Rectangle.TOP);
                rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                footer.addCell(rightCell);
                
                footer.writeSelectedRows(0, -1, 34, 50, writer.getDirectContent());
            } catch (DocumentException de) {
                de.printStackTrace();
            }
        }
    }
}