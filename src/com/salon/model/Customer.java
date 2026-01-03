package com.salon.model;

public class Customer {
    private String customerId;
    private String name;
    private String phone;
    private String address;
    private java.util.Date createdAt;
    private Integer membershipPlanId;

    // --- Getters & Setters ---
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    

public java.util.Date getCreatedAt() {
    return createdAt;
}

public void setCreatedAt(java.util.Date createdAt) {
    this.createdAt = createdAt;
}

public Integer getMembershipPlanId() {
    return membershipPlanId;
}

public void setMembershipPlanId(Integer membershipPlanId) {
    this.membershipPlanId = membershipPlanId;
}
}

