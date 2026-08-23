package com.aml.transaction.service;

import com.aml.common.dto.request.GenerateReportRequest;
import com.aml.common.dto.response.GeneratedReportResponse;
import com.aml.common.entity.GeneratedReport;
import com.aml.common.entity.InvestigationCase;
import com.aml.common.entity.RiskAssessment;
import com.aml.common.entity.Transaction;
import com.aml.common.repository.GeneratedReportRepository;
import com.aml.common.repository.InvestigationCaseRepository;
import com.aml.common.repository.RiskAssessmentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ReportGenerationService {
    private final GeneratedReportRepository reports;
    private final InvestigationCaseRepository cases;
    private final RiskAssessmentRepository assessments;
    private final ObjectMapper objectMapper;

    public ReportGenerationService(GeneratedReportRepository reports, InvestigationCaseRepository cases,
                                   RiskAssessmentRepository assessments, ObjectMapper objectMapper) {
        this.reports = reports;
        this.cases = cases;
        this.assessments = assessments;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public GeneratedReportResponse generate(GenerateReportRequest request) {
        InvestigationCase investigationCase = cases.findById(request.getCaseId())
                .orElseThrow(() -> new IllegalArgumentException("Investigation case not found: " + request.getCaseId()));
        Transaction transaction = investigationCase.getTransaction();
        RiskAssessment risk = assessments.findFirstByTransaction_IdOrderByCreatedAtDesc(transaction.getId())
                .orElseThrow(() -> new IllegalArgumentException("Case transaction has no risk assessment"));

        String format = normalize(request.getReportFormat(), "TEXT");
        String provider = normalize(request.getLlmProvider(), "TEMPLATE");
        if (!"TEMPLATE".equals(provider)) {
            throw new IllegalArgumentException("Provider " + provider + " is not configured; use TEMPLATE");
        }
        String content = buildContent(format, request.isIncludeAnalysis(), request.getCustomInstructions(), investigationCase, transaction, risk);
        GeneratedReport report = new GeneratedReport();
        report.setId(UUID.randomUUID().toString());
        report.setCaseId(investigationCase.getId());
        report.setFormat(format);
        report.setProvider(provider);
        report.setModelName("aml-template-v1");
        report.setContent(content);
        GeneratedReport saved = reports.save(report);
        investigationCase.setReportId(saved.getId());
        cases.save(investigationCase);
        return map(saved);
    }

    @Transactional(readOnly = true)
    public GeneratedReportResponse get(String id) {
        return map(reports.findById(id).orElseThrow(() -> new IllegalArgumentException("Report not found: " + id)));
    }

    @Transactional(readOnly = true)
    public byte[] download(String id) {
        return reports.findById(id).orElseThrow(() -> new IllegalArgumentException("Report not found: " + id))
                .getContent().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private String buildContent(String format, boolean includeAnalysis, String instructions,
                                 InvestigationCase investigationCase, Transaction transaction, RiskAssessment risk) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("reportType", "Suspicious Activity / Compliance Review Report");
        values.put("caseId", investigationCase.getId());
        values.put("transactionId", transaction.getTransactionId());
        values.put("customerId", transaction.getSenderAccount().getCustomer().getId());
        values.put("amount", transaction.getAmount());
        values.put("currency", transaction.getPaymentCurrency());
        values.put("paymentType", transaction.getPaymentType());
        values.put("transactionDate", transaction.getTransactionDatetime());
        values.put("caseStatus", investigationCase.getStatus());
        values.put("priority", investigationCase.getPriority());
        values.put("expertType", investigationCase.getExpertType());
        values.put("riskScore", risk.getRiskScore());
        values.put("riskCategory", risk.getRiskCategory());
        values.put("flagged", risk.getShouldFlag());
        if (includeAnalysis) {
            values.put("explanation", risk.getOneLineExplanation());
            values.put("recommendation", risk.getRecommendation());
            values.put("model", risk.getModelName());
        }
        if (instructions != null && !instructions.isBlank()) values.put("reviewInstructions", instructions);
        if ("JSON".equals(format)) {
            try { return objectMapper.writeValueAsString(values); }
            catch (JsonProcessingException exception) { throw new IllegalStateException("Could not serialize report", exception); }
        }
        StringBuilder content = new StringBuilder();
        values.forEach((key, value) -> content.append(key).append(": ").append(value == null ? "" : value).append(System.lineSeparator()));
        content.append(System.lineSeparator()).append("Generated by aml-template-v1").append(System.lineSeparator());
        return content.toString();
    }

    private String normalize(String value, String fallback) { return value == null || value.isBlank() ? fallback : value.trim().toUpperCase(); }

    private GeneratedReportResponse map(GeneratedReport report) {
        GeneratedReportResponse response = new GeneratedReportResponse();
        response.setId(report.getId()); response.setCaseId(report.getCaseId()); response.setFormat(report.getFormat());
        response.setProvider(report.getProvider()); response.setModelName(report.getModelName()); response.setContent(report.getContent());
        response.setDownloadUrl("/api/v1/reports/" + report.getId() + "/download"); response.setCreatedAt(report.getCreatedAt());
        return response;
    }
}
