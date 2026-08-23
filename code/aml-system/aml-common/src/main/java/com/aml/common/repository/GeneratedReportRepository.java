package com.aml.common.repository;

import com.aml.common.entity.GeneratedReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GeneratedReportRepository extends JpaRepository<GeneratedReport, String> {
    Optional<GeneratedReport> findFirstByCaseIdOrderByCreatedAtDesc(String caseId);
}
