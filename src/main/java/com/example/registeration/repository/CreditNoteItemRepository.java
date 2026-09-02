package com.example.registeration.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.registeration.entity.CreditNoteItem;

@Repository
public interface CreditNoteItemRepository extends JpaRepository<CreditNoteItem, UUID> {
    List<CreditNoteItem> findByCreditNoteId(UUID creditNoteId);
    void deleteByCreditNoteId(UUID creditNoteId);
}
