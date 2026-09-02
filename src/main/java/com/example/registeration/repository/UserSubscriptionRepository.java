package com.example.registeration.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.registeration.entity.UserSubscription;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, UUID> {
    Optional<UserSubscription> findTopByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<UserSubscription> findByUserIdAndStatus(UUID userId, String status);
    List<UserSubscription> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
