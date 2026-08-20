package com.aml.transaction.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aml.common.dto.common.ApiResponse;
import com.aml.common.dto.request.CreateCustomerRequest;
import com.aml.common.dto.request.UpdateCustomerRequest;
import com.aml.common.dto.response.CustomerResponse;
import com.aml.transaction.service.CustomerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    
    private final CustomerService customerService;

    public CustomerController(
            CustomerService customerService
    ) {
        this.customerService = customerService;
    }

    // ============================================================
    // CREATE CUSTOMER
    // ============================================================

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>>
    createCustomer(
            @Valid @RequestBody CreateCustomerRequest request
    ) {

        CustomerResponse response =
                customerService.createCustomer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                response,
                                "Customer created successfully"
                        )
                );
    }

    // ============================================================
    // GET CUSTOMER BY ID
    // ============================================================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>>
    getCustomerById(
            @PathVariable String id
    ) {

        CustomerResponse response =
                customerService.getCustomerById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Customer retrieved successfully"
                )
        );
    }

    // ============================================================
    // GET ALL CUSTOMERS
    // ============================================================

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>>
    getAllCustomers() {

        List<CustomerResponse> response =
                customerService.getAllCustomers();

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Customers retrieved successfully"
                )
        );
    }

    // ============================================================
    // UPDATE CUSTOMER
    // ============================================================

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>>
    updateCustomer(
            @PathVariable String id,
            @Valid @RequestBody UpdateCustomerRequest request
    ) {

        CustomerResponse response =
                customerService.updateCustomer(
                        id,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Customer updated successfully"
                )
        );
    }

    // ============================================================
    // DELETE CUSTOMER
    // ============================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>>
    deleteCustomer(
            @PathVariable String id
    ) {

        customerService.deleteCustomer(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        null,
                        "Customer deleted successfully"
                )
        );
    }
}