package com.shopease.backend.service;

import com.shopease.backend.model.Customer;
import com.shopease.backend.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {
    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }

    public List<Customer> getAllCustomers() { return repository.findAll(); }

    public Customer getCustomer(int id) {
        Customer customer = repository.findById(id);
        if (customer == null) throw new RuntimeException("Customer not found");
        return customer;
    }

    public void updateCustomer(Customer customer) {
        if (customer == null || customer.getCustomerId() <= 0)
            throw new RuntimeException("Invalid customer");

        if (repository.findById(customer.getCustomerId()) == null)
            throw new RuntimeException("Customer not found");

        if (customer.getPhone() != null && !customer.getPhone().matches("^[0-9]{10}$"))
            throw new RuntimeException("Phone must contain 10 digits");

        if (customer.getPincode() != null && !customer.getPincode().isBlank() &&
                !customer.getPincode().matches("^[0-9]{6}$"))
            throw new RuntimeException("Pincode must contain 6 digits");

        if (repository.updateCustomer(customer) == 0)
            throw new RuntimeException("Customer was not updated");
    }
}
