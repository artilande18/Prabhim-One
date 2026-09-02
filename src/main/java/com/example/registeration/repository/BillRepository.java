package com.example.registeration.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import com.example.registeration.entity.Bill;

@Repository
public interface BillRepository extends JpaRepository<Bill, UUID>, JpaSpecificationExecutor<Bill> {
}
