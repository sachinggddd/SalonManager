package com.salon.model;

import java.util.Date;

public class MembershipPlan {
    private int planId;
    private String planName;
    private double discountPercentage;
    private String description;
    private boolean isActive;
    private Date createdAt;

    // Constructors
    public MembershipPlan() {}

    public MembershipPlan(int planId, String planName, double discountPercentage) {
        this.planId = planId;
        this.planName = planName;
        this.discountPercentage = discountPercentage;
        this.isActive = true;
    }

    // Getters and Setters
    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        if (discountPercentage == 0) {
            return planName;
        }
        return planName + " (" + String.format("%.0f", discountPercentage) + "% discount)";
    }
}