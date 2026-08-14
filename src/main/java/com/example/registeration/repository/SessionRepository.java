package com.example.registeration.repository;

import com.example.registeration.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    void deleteByRefreshToken(String refreshToken);
    List<Session> findByUserId(UUID userId);
    void deleteByUserId(UUID userId);
}
