package com.aml.transaction.service;

import com.aml.common.dto.request.CreateInvestigationCaseRequest;
import com.aml.common.dto.request.UpdateInvestigationCaseRequest;
import com.aml.common.dto.response.InvestigationCaseResponse;
import com.aml.common.entity.InvestigationCase;
import com.aml.common.entity.RiskAssessment;
import com.aml.common.entity.Transaction;
import com.aml.common.entity.TransactionStatus;
import com.aml.common.repository.InvestigationCaseRepository;
import com.aml.common.repository.RiskAssessmentRepository;
import com.aml.common.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class InvestigationCaseService {
    private static final Set<String> STATUSES = Set.of("OPEN", "IN_PROGRESS", "ESCALATED", "RESOLVED");
    private final InvestigationCaseRepository cases;
    private final TransactionRepository transactions;
    private final RiskAssessmentRepository assessments;
    public InvestigationCaseService(InvestigationCaseRepository c, TransactionRepository t, RiskAssessmentRepository a) { cases=c; transactions=t; assessments=a; }

    @Transactional
    public InvestigationCaseResponse create(CreateInvestigationCaseRequest request) {
        return cases.findByTransaction_TransactionId(request.getTransactionId()).map(existing -> {
            if (request.getOwner() != null && !request.getOwner().isBlank()) existing.setOwner(request.getOwner());
            return map(cases.save(existing));
        }).orElseGet(() -> {
            Transaction transaction = transactions.findByTransactionId(request.getTransactionId())
                    .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + request.getTransactionId()));
            RiskAssessment risk = assessments.findFirstByTransaction_IdOrderByCreatedAtDesc(transaction.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Transaction has no risk assessment"));
            if (!Boolean.TRUE.equals(risk.getShouldFlag())) throw new IllegalArgumentException("Only flagged transactions can become investigation cases");
            InvestigationCase item = new InvestigationCase(); item.setId(UUID.randomUUID().toString()); item.setTransaction(transaction);
            item.setStatus("OPEN"); item.setPriority(request.getPriority()==null ? priority(risk.getRiskCategory()) : request.getPriority().toUpperCase());
            item.setOwner(request.getOwner()); transaction.setStatus(TransactionStatus.UNDER_REVIEW); transactions.save(transaction);
            return map(cases.save(item));
        });
    }
    @Transactional(readOnly=true) public List<InvestigationCaseResponse> all(){return cases.findAll().stream().map(this::map).toList();}
    @Transactional
    public InvestigationCaseResponse update(String id, UpdateInvestigationCaseRequest request) {
        InvestigationCase item=cases.findById(id).orElseThrow(() -> new IllegalArgumentException("Investigation case not found: "+id));
        if(request.getOwner()!=null)item.setOwner(request.getOwner());
        if(request.getStatus()!=null){String status=request.getStatus().toUpperCase();if(!STATUSES.contains(status))throw new IllegalArgumentException("Invalid case status: "+status);item.setStatus(status);}
        if(request.getResolution()!=null)item.setResolution(request.getResolution());
        if("RESOLVED".equals(item.getStatus())) item.getTransaction().setStatus(TransactionStatus.COMPLETED);
        return map(cases.save(item));
    }
    private String priority(String risk){return "CRITICAL".equalsIgnoreCase(risk)?"CRITICAL":"HIGH";}
    private InvestigationCaseResponse map(InvestigationCase i){InvestigationCaseResponse r=new InvestigationCaseResponse();r.setId(i.getId());r.setTransactionId(i.getTransaction().getTransactionId());r.setCustomerId(i.getTransaction().getSenderAccount().getCustomer().getId());r.setStatus(i.getStatus());r.setPriority(i.getPriority());r.setOwner(i.getOwner());r.setResolution(i.getResolution());r.setCreatedAt(i.getCreatedAt());r.setUpdatedAt(i.getUpdatedAt());return r;}
}
