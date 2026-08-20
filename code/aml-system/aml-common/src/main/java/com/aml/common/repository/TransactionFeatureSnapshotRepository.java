package com.aml.common.repository;

import com.aml.common.entity.TransactionFeatureSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionFeatureSnapshotRepository
        extends JpaRepository<TransactionFeatureSnapshot, String> {

    List<TransactionFeatureSnapshot> findByTransaction_Id(
            String transactionId
    );

    Optional<TransactionFeatureSnapshot>
    findFirstByTransaction_IdOrderByCreatedAtDesc(
            String transactionId
    );
}