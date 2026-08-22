package com.example.registeration.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.registeration.entity.EstimateItem;

@Repository
public interface EstimateItemRepository
        extends JpaRepository<EstimateItem, UUID> {

    List<EstimateItem> findByEstimateId(UUID estimateId);
}
