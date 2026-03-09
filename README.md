# 💇 Salon Billing and Inventory Manager

A **Java-based desktop application** designed to manage **salon billing, product inventory, customer records, and business analytics**.  
The system helps salon owners manage salon services, product sales, inventory, and customer memberships efficiently.

---

## 🚀 Features

### 🔐 Role-Based Login
- Separate login for **Admin** and **Staff**.
- Login using **username and password**.
- Currently both roles have the same features but can be customized with different permissions in the future.

---

### 👤 Customer Management
- Add and manage customer details.
- Automatically fetch **existing customer information** by entering the phone number.
- Store customer history and membership details.
- Apply **membership-based discounts** during billing.

---

### 🧾 Billing System
Supports billing for:
- **Salon services**
- **Product sales**

Features:
- Generate **PDF invoices**
- Automatic customer detail retrieval
- Apply membership discounts
- Maintain service and product billing records

---

### 📦 Product Management
- Add new products with **SKU (Stock Keeping Unit)**.
- Enable **Continue / Discontinue product status**.
- Manage product availability and details.

---

### 📥 Stock Management
- Add stock entries for products.
- Store:
  - Actual purchase price
  - Total stock value
- Detects **price changes automatically**.
- If a price change is detected:
  - The system shows a warning.
  - Suggests adding a **new product with a different SKU**.
- Allows **multiple stock entries for the same product**.

---


### 🧴 Product Usage Tracking
Tracks products that are **consumed during salon services**, such as:
- Shampoo
- Hair color
- Creams
- Other salon consumables

Helps monitor internal product usage.

---

### 💰 Product Sales Tracking
- Record product sale entries.
- Automatically calculates:
  - Revenue
  - Profit

---

### 📊 Stock Summary
- Provides a **real-time overview of current product stock**.
- Automatically updates stock based on:
  - **Product sales**
  - **Product consumption in salon services**
- Displays the **actual remaining stock quantity**.
- Helps track accurate inventory levels and prevent stock shortages.
- Ensures that both **sales and internal usage** are reflected in stock calculations.

---

### 📊 Business Analytics
Generate **PDF analytics reports** that provide insights such as:

- Total customers
- Total revenue
- Revenue from product sales
- Revenue from salon services
- Product consumption analysis
- Top selling products
- Top customers
- Reports for different time periods

---

### ✂️ Salon Service Management
- Add new salon services.
- Update service details.
- Discontinue services when needed.

Examples include:
- Haircut
- Facial
- Hair coloring
- Spa services

---

## 🛠 Technologies Used
- **Java (Swing)** – Desktop application UI
- **JDBC** – Database connectivity
- **SQL Database**
- **PDF Generation Library** – For invoices and analytics reports

---

## 📂 Project Structure

SalonManager
│
├── src
│ ├── com.salon.dao
│ │ ├── AnalyticsDAO.java
│ │ ├── CustomerDAO.java
│ │ ├── InvoiceDAO.java
│ │ ├── MembershipDAO.java
│ │ ├── ProductDAO.java
│ │ ├── ServiceDAO.java
│ │ ├── StockDAO.java
│ │ ├── StockMovementDAO.java
│ │ └── UserDAO.java
│ │
│ ├── com.salon.model
│ │ ├── AnalyticsReport.java
│ │ ├── Customer.java
│ │ ├── Invoice.java
│ │ ├── InvoiceItem.java
│ │ ├── InvoiceService.java
│ │ ├── MembershipPlan.java
│ │ ├── Product.java
│ │ ├── Service.java
│ │ ├── StockEntry.java
│ │ ├── StockMovement.java
│ │ └── User.java
│ │
│ ├── com.salon.ui
│ │ ├── AdminDashboard.java
│ │ ├── LoginFrame.java
│ │ └── StaffDashboard.java
│ │
│ ├── com.salon.ui.panel
│ │ ├── AnalyticsPanel.java
│ │ ├── CustomerPanel.java
│ │ ├── ProductPanel.java
│ │ ├── ProductSalePanel.java
│ │ ├── ProductUsagePanel.java
│ │ ├── ServicesPanel.java
│ │ ├── StockPanel.java
│ │ └── StockSummaryPanel.java
│ │
│ └── com.salon.util
│ └── DBConnection.java
│
└── README.md
