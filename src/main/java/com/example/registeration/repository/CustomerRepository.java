package com.example.registeration.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.registeration.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findFirstByGstNumberIgnoreCaseAndIsDeletedFalse(String gstNumber);
    List<Customer> findByIsDeletedFalse();
}


