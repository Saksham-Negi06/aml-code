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
    private static final Set<String> STATUSES = Set.of("PENDING", "OPEN", "IN_REVIEW", "IN_PROGRESS", "ESCALATED", "RESOLVED");
    private static final Set<String> PRIORITIES = Set.of("CRITICAL", "HIGH", "NORMAL", "LOW");
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
            item.setStatus("PENDING"); item.setPriority(request.getPriority()==null ? priority(risk.getRiskCategory()) : normalizePriority(request.getPriority()));
            item.setCaseType(caseType(risk.getRiskCategory()));
            item.setExpertType(expertType(risk));
            item.setOwner(request.getOwner()); transaction.setStatus(TransactionStatus.UNDER_REVIEW); transactions.save(transaction);
            return map(cases.save(item));
        });
    }
    @Transactional(readOnly=true) public List<InvestigationCaseResponse> all(){return cases.findAll().stream().map(this::map).toList();}
    @Transactional(readOnly=true) public InvestigationCaseResponse get(String id){return map(cases.findById(id).orElseThrow(() -> new IllegalArgumentException("Investigation case not found: "+id)));}
    @Transactional
    public InvestigationCaseResponse update(String id, UpdateInvestigationCaseRequest request) {
        InvestigationCase item=cases.findById(id).orElseThrow(() -> new IllegalArgumentException("Investigation case not found: "+id));
        if(request.getOwner()!=null)item.setOwner(request.getOwner());
        if(request.getExpertId()!=null)item.setExpertId(request.getExpertId());
        if(request.getNotes()!=null)item.setNotes(request.getNotes());
        if(request.getStatus()!=null){String status=request.getStatus().toUpperCase();if(!STATUSES.contains(status))throw new IllegalArgumentException("Invalid case status: "+status);item.setStatus(status);}
        if(request.getResolution()!=null)item.setResolution(request.getResolution());
        if("RESOLVED".equals(item.getStatus())) item.getTransaction().setStatus(TransactionStatus.COMPLETED);
        return map(cases.save(item));
    }
    @Transactional
    public InvestigationCaseResponse resolve(String id, com.aml.common.dto.request.ResolveInvestigationCaseRequest request) {
        InvestigationCase item=cases.findById(id).orElseThrow(() -> new IllegalArgumentException("Investigation case not found: "+id));
        String resolution=request.getResolution().trim().toUpperCase();
        if (!Set.of("APPROVED","REJECTED","ESCALATED","MANUAL_REVIEW").contains(resolution)) throw new IllegalArgumentException("Invalid resolution: "+resolution);
        item.setResolution(resolution + (request.getResolutionNotes()==null || request.getResolutionNotes().isBlank() ? "" : ": " + request.getResolutionNotes()));
        item.setRecommendation(request.getRecommendation()); item.setExpertId(request.getResolvedBy());
        item.setStatus("ESCALATED".equals(resolution) ? "ESCALATED" : "RESOLVED");
        if ("RESOLVED".equals(item.getStatus())) item.getTransaction().setStatus(TransactionStatus.COMPLETED);
        return map(cases.save(item));
    }
    private String priority(String risk){return "CRITICAL".equalsIgnoreCase(risk)?"CRITICAL":"HIGH";}
    private String normalizePriority(String value){String normalized=value.trim().toUpperCase();if(!PRIORITIES.contains(normalized))throw new IllegalArgumentException("Invalid case priority: "+normalized);return normalized;}
    private String caseType(String risk){if(risk==null)return "GENERAL";String normalized=risk.toUpperCase();if(normalized.contains("SANCTION")||normalized.contains("COUNTRY"))return "SANCTIONS";if(normalized.contains("STRUCTUR"))return "STRUCTURING";if(normalized.contains("TRADE"))return "TRADE_BASED";return "GENERAL";}
    private String expertType(RiskAssessment risk){String type=caseType(risk.getRiskCategory());if("SANCTIONS".equals(type))return "SANCTIONS_EXPERT";if("STRUCTURING".equals(type))return "BEHAVIORAL_EXPERT";if("CRITICAL".equals(priority(risk.getRiskCategory())))return "SENIOR_COMPLIANCE";return "COMPLIANCE_LEAD";}
    private InvestigationCaseResponse map(InvestigationCase i){InvestigationCaseResponse r=new InvestigationCaseResponse();r.setId(i.getId());r.setTransactionId(i.getTransaction().getTransactionId());r.setCustomerId(i.getTransaction().getSenderAccount().getCustomer().getId());r.setStatus(i.getStatus());r.setPriority(i.getPriority());r.setCaseType(i.getCaseType());r.setExpertType(i.getExpertType());r.setExpertId(i.getExpertId());r.setOwner(i.getOwner());r.setNotes(i.getNotes());r.setResolution(i.getResolution());r.setRecommendation(i.getRecommendation());r.setReportId(i.getReportId());RiskAssessment risk=assessments.findFirstByTransaction_IdOrderByCreatedAtDesc(i.getTransaction().getId()).orElse(null);if(risk!=null){r.setRiskScore(risk.getRiskScore());r.setRiskCategory(risk.getRiskCategory());r.setRiskExplanation(risk.getOneLineExplanation());}r.setCreatedAt(i.getCreatedAt());r.setUpdatedAt(i.getUpdatedAt());return r;}
}
