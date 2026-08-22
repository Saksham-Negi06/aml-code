package com.aml.transaction.service;

import com.aml.common.dto.request.CreateRuleRequest;
import com.aml.common.dto.request.UpdateRuleRequest;
import com.aml.common.dto.response.RuleResponse;
import com.aml.common.entity.Rule;
import com.aml.common.repository.RuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class RuleService {
    private static final Set<String> SEVERITIES = Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL");
    private static final Set<String> STATUSES = Set.of("ACTIVE", "INACTIVE");

    private final RuleRepository rules;

    public RuleService(RuleRepository rules) {
        this.rules = rules;
    }

    @Transactional
    public RuleResponse create(CreateRuleRequest request) {
        String severity = normalizeSeverity(request.getSeverity());
        Rule rule = new Rule();
        rule.setId(UUID.randomUUID().toString());
        rule.setName(request.getName());
        rule.setAmountThreshold(request.getAmountThreshold());
        rule.setSeverity(severity);
        rule.setStatus("ACTIVE");
        return map(rules.save(rule));
    }

    @Transactional(readOnly = true)
    public List<RuleResponse> all() {
        return rules.findAll().stream().map(this::map).toList();
    }

    @Transactional
    public RuleResponse update(String id, UpdateRuleRequest request) {
        Rule rule = rules.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            rule.setName(request.getName());
        }
        if (request.getAmountThreshold() != null) {
            rule.setAmountThreshold(request.getAmountThreshold());
        }
        if (request.getSeverity() != null) {
            rule.setSeverity(normalizeSeverity(request.getSeverity()));
        }
        if (request.getStatus() != null) {
            String status = request.getStatus().toUpperCase();
            if (!STATUSES.contains(status)) {
                throw new IllegalArgumentException("Invalid rule status: " + request.getStatus());
            }
            rule.setStatus(status);
        }
        return map(rules.save(rule));
    }

    @Transactional
    public void delete(String id) {
        if (!rules.existsById(id)) {
            throw new IllegalArgumentException("Rule not found: " + id);
        }
        rules.deleteById(id);
    }

    private String normalizeSeverity(String severity) {
        String normalized = severity == null ? "" : severity.toUpperCase();
        if (!SEVERITIES.contains(normalized)) {
            throw new IllegalArgumentException("Invalid severity: " + severity);
        }
        return normalized;
    }

    private RuleResponse map(Rule rule) {
        RuleResponse response = new RuleResponse();
        response.setId(rule.getId());
        response.setName(rule.getName());
        response.setAmountThreshold(rule.getAmountThreshold());
        response.setSeverity(rule.getSeverity());
        response.setStatus(rule.getStatus());
        response.setCreatedAt(rule.getCreatedAt());
        response.setUpdatedAt(rule.getUpdatedAt());
        return response;
    }
}
