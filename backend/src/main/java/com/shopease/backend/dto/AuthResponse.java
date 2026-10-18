package com.shopease.backend.dto;

public class AuthResponse {
    private int userId;
    private String name;
    private String email;
    private String role;
    private int customerId;

    public AuthResponse(int userId, String name, String email, String role, int customerId) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.customerId = customerId;
    }

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public int getCustomerId() { return customerId; }
}
