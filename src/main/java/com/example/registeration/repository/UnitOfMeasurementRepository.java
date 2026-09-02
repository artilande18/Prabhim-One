package com.example.registeration.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.registeration.entity.UnitOfMeasurement;

@Repository
public interface UnitOfMeasurementRepository extends JpaRepository<UnitOfMeasurement, UUID> {
}
