package com.example.registeration.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.registeration.entity.SubscriptionBillingHistory;

@Repository
public interface SubscriptionBillingHistoryRepository extends JpaRepository<SubscriptionBillingHistory, UUID> {
    List<SubscriptionBillingHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);
    long count();
}
