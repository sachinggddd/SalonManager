package com.salon.model;

import java.util.Date;

public class AnalyticsReport {
    private int reportId;
    private String reportType;
    private String paramsJson;
    private Integer exportedBy;
    private Date exportedAt;
    private String reportPdfPath;
    private byte[] reportPdfData;
    
    // Report metadata
    private String reportTitle;
    private Date reportStartDate;
    private Date reportEndDate;
    
    // Constructors
    public AnalyticsReport() {}
    
    public AnalyticsReport(String reportType, String reportTitle) {
        this.reportType = reportType;
        this.reportTitle = reportTitle;
    }

    // Getters and Setters
    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getParamsJson() {
        return paramsJson;
    }

    public void setParamsJson(String paramsJson) {
        this.paramsJson = paramsJson;
    }

    public Integer getExportedBy() {
        return exportedBy;
    }

    public void setExportedBy(Integer exportedBy) {
        this.exportedBy = exportedBy;
    }

    public Date getExportedAt() {
        return exportedAt;
    }

    public void setExportedAt(Date exportedAt) {
        this.exportedAt = exportedAt;
    }

    public String getReportPdfPath() {
        return reportPdfPath;
    }

    public void setReportPdfPath(String reportPdfPath) {
        this.reportPdfPath = reportPdfPath;
    }

    public byte[] getReportPdfData() {
        return reportPdfData;
    }

    public void setReportPdfData(byte[] reportPdfData) {
        this.reportPdfData = reportPdfData;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }

    public Date getReportStartDate() {
        return reportStartDate;
    }

    public void setReportStartDate(Date reportStartDate) {
        this.reportStartDate = reportStartDate;
    }

    public Date getReportEndDate() {
        return reportEndDate;
    }

    public void setReportEndDate(Date reportEndDate) {
        this.reportEndDate = reportEndDate;
    }
}