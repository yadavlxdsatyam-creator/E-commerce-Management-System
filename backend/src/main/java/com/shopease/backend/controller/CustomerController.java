package com.shopease.backend.controller;

import com.shopease.backend.model.Customer;
import com.shopease.backend.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {
    private final CustomerService service;

    public CustomerController(CustomerService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<List<Customer>> getAll() {
        return ResponseEntity.ok(service.getAllCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getOne(@PathVariable int id) {
        return ResponseEntity.ok(service.getCustomer(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable int id, @RequestBody Customer customer) {
        Customer updated = new Customer(
                id,
                customer.getUserId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getCity(),
                customer.getState(),
                customer.getPincode()
        );
        service.updateCustomer(updated);
        return ResponseEntity.ok("Customer updated successfully");
    }
}
