package com.shopease.backend.model;

public class Customer {
    private int customerId;
    private int userId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String pincode;

    public Customer() {}

    public Customer(int customerId, int userId, String name, String email, String phone,
                    String address, String city, String state, String pincode) {
        this.customerId = customerId;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
    }

    public int getCustomerId() { return customerId; }
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPincode() { return pincode; }
}
