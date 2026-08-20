package com.aml.transaction.service;

import com.aml.common.dto.request.CreateCustomerRequest;
import com.aml.common.dto.request.UpdateCustomerRequest;
import com.aml.common.dto.response.CustomerResponse;
import com.aml.common.entity.Account;
import com.aml.common.entity.Customer;
import com.aml.common.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // ============================================================
    // CREATE CUSTOMER
    // ============================================================

    @Transactional
    public CustomerResponse createCustomer(
            CreateCustomerRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Customer request cannot be null"
            );
        }

        Customer customer = new Customer();

        customer.setId(UUID.randomUUID().toString());

        customer.setAccountHolderType(
                request.getAccountHolderType()
        );

        customer.setKycVerificationStatus(
                request.getKycVerificationStatus()
        );

        customer.setRiskCountryFlag(
                request.getRiskCountryFlag() != null
                        ? request.getRiskCountryFlag()
                        : false
        );

        if (customer.getAccounts() == null) {
            customer.setAccounts(new ArrayList<>());
        }

        Customer savedCustomer =
                customerRepository.save(customer);

        return mapToResponse(savedCustomer);
    }

    // ============================================================
    // GET CUSTOMER
    // ============================================================

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(String id) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer ID cannot be null or blank"
            );
        }

        Customer customer =
                customerRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Customer not found: " + id
                                )
                        );

        return mapToResponse(customer);
    }

    // ============================================================
    // GET ALL CUSTOMERS
    // ============================================================

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {

        return customerRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ============================================================
    // UPDATE CUSTOMER
    // ============================================================

    @Transactional
    public CustomerResponse updateCustomer(
            String id,
            UpdateCustomerRequest request
    ) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer ID cannot be null or blank"
            );
        }

        if (request == null) {
            throw new IllegalArgumentException(
                    "Update request cannot be null"
            );
        }

        Customer customer =
                customerRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Customer not found: " + id
                                )
                        );

        if (request.getAccountHolderType() != null
                && !request.getAccountHolderType().isBlank()) {

            customer.setAccountHolderType(
                    request.getAccountHolderType()
            );
        }

        if (request.getKycVerificationStatus() != null
                && !request.getKycVerificationStatus().isBlank()) {

            customer.setKycVerificationStatus(
                    request.getKycVerificationStatus()
            );
        }

        if (request.getRiskCountryFlag() != null) {
            customer.setRiskCountryFlag(
                    request.getRiskCountryFlag()
            );
        }

        Customer updatedCustomer =
                customerRepository.save(customer);

        return mapToResponse(updatedCustomer);
    }

    // ============================================================
    // DELETE CUSTOMER
    // ============================================================

    @Transactional
    public void deleteCustomer(String id) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Customer ID cannot be null or blank"
            );
        }

        Customer customer =
                customerRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Customer not found: " + id
                                )
                        );

        customerRepository.delete(customer);
    }

    // ============================================================
    // ENTITY -> RESPONSE
    // ============================================================

    private CustomerResponse mapToResponse(
            Customer customer
    ) {

        if (customer == null) {
            throw new IllegalArgumentException(
                    "Customer cannot be null"
            );
        }

        List<Account> accounts =
                customer.getAccounts() != null
                        ? customer.getAccounts()
                        : new ArrayList<>();

        List<String> accountIds = accounts.stream()
                .filter(account -> account != null)
                .map(Account::getId)
                .filter(accountId -> accountId != null)
                .collect(Collectors.toList());

        CustomerResponse response =
                new CustomerResponse();

        response.setId(customer.getId());

        response.setAccountIds(accountIds);

        response.setAccountHolderType(
                customer.getAccountHolderType()
        );

        response.setKycVerificationStatus(
                customer.getKycVerificationStatus()
        );

        response.setRiskCountryFlag(
                customer.getRiskCountryFlag() != null
                        ? customer.getRiskCountryFlag()
                        : false
        );

        response.setCreatedAt(
                customer.getCreatedAt()
        );

        return response;
    }
}