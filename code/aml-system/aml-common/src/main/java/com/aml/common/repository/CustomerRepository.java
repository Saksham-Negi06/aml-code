package com.aml.common.repository;

import com.aml.common.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CustomerRepository extends JpaRepository<Customer, String> {
}
