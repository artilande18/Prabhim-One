package com.example.registeration.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.registeration.entity.Warehouse;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
}
