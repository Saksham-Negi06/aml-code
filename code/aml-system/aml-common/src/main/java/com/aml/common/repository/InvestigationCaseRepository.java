package com.aml.common.repository;
import com.aml.common.entity.InvestigationCase;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface InvestigationCaseRepository extends JpaRepository<InvestigationCase,String> {
    Optional<InvestigationCase> findByTransaction_TransactionId(String transactionId);
}
