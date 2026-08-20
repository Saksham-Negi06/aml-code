package com.aml.common.repository;

import com.aml.common.entity.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RiskAssessmentRepository
        extends JpaRepository<RiskAssessment, String> {

    List<RiskAssessment> findByTransaction_Id(
            String transactionId
    );

    Optional<RiskAssessment> findByRequestId(
            String requestId
    );

    Optional<RiskAssessment>
    findFirstByTransaction_IdOrderByCreatedAtDesc(
            String transactionId
    );
}