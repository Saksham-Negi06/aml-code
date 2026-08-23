package com.aml.transaction.controller;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aml.common.dto.common.ApiResponse;
import com.aml.common.dto.request.CreateInvestigationCaseRequest;
import com.aml.common.dto.request.UpdateInvestigationCaseRequest;
import com.aml.common.dto.request.ResolveInvestigationCaseRequest;
import com.aml.common.dto.response.InvestigationCaseResponse;
import com.aml.transaction.service.InvestigationCaseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/cases")
public class InvestigationCaseController {
    
    private final InvestigationCaseService service; 
    
    
    public InvestigationCaseController(InvestigationCaseService s)
    {
        service=s;
    }
    @GetMapping 
    public ApiResponse<List<InvestigationCaseResponse>> all()
    {
        return ApiResponse.success(service.all(),"Cases fetched successfully");
    }
    @GetMapping("/{id}") public ApiResponse<InvestigationCaseResponse> get(@PathVariable String id){return ApiResponse.success(service.get(id),"Case details fetched successfully");}
    @PostMapping
     public ResponseEntity<ApiResponse<InvestigationCaseResponse>> create(@Valid @RequestBody CreateInvestigationCaseRequest r){
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(r),"Investigation case created"));
    }
    @PutMapping("/{id}") public ApiResponse<InvestigationCaseResponse> update(@PathVariable String id,@RequestBody UpdateInvestigationCaseRequest r){
        return ApiResponse.success(service.update(id,r),"Investigation case updated");
    }
    @PostMapping("/{id}/resolve") public ApiResponse<InvestigationCaseResponse> resolve(@PathVariable String id,@Valid @RequestBody ResolveInvestigationCaseRequest r){return ApiResponse.success(service.resolve(id,r),"Investigation case resolved");}
}
