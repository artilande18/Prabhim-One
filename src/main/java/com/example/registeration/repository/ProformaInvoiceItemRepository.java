package com.example.registeration.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.registeration.entity.ProformaInvoiceItem;

@Repository
public interface ProformaInvoiceItemRepository
        extends JpaRepository<ProformaInvoiceItem, UUID> {

    List<ProformaInvoiceItem> findByProformaInvoiceId(UUID proformaInvoiceId);
}
