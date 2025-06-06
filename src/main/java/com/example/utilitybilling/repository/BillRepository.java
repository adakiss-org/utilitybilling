package com.example.utilitybilling.repository;

import com.example.utilitybilling.model.Bill;
import com.example.utilitybilling.model.BillStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BillRepository extends JpaRepository<Bill, UUID> {
    List<Bill> findByDueDateBetween(LocalDateTime start, LocalDateTime end);
    List<Bill> findByProviderIdAndDueDateBetween(UUID providerId, LocalDateTime start, LocalDateTime end);
    List<Bill> findByDueDateLessThanEqual(LocalDateTime dateTime);
}