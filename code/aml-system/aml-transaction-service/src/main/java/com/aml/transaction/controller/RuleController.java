package com.aml.transaction.controller;

import com.aml.common.dto.common.ApiResponse;
import com.aml.common.dto.request.CreateRuleRequest;
import com.aml.common.dto.request.UpdateRuleRequest;
import com.aml.common.dto.response.RuleResponse;
import com.aml.transaction.service.RuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rules")
public class RuleController {

    private final RuleService service;

    public RuleController(RuleService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<RuleResponse>> all() {
        return ApiResponse.success(service.all(), "Rules fetched successfully");
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RuleResponse>> create(@Valid @RequestBody CreateRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.create(request), "Rule created successfully"));
    }

    @PutMapping("/{id}")
    public ApiResponse<RuleResponse> update(@PathVariable String id, @RequestBody UpdateRuleRequest request) {
        return ApiResponse.success(service.update(id, request), "Rule updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
