package com.example.registeration.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.registeration.entity.OrganizationSetting;

@Repository
public interface OrganizationSettingRepository extends JpaRepository<OrganizationSetting, UUID> {
    Optional<OrganizationSetting> findByUserId(UUID userId);
    Optional<OrganizationSetting> findByOrganizationId(UUID organizationId);
}
