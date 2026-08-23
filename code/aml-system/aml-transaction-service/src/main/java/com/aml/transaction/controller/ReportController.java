package com.aml.transaction.controller;

import com.aml.common.dto.common.ApiResponse;
import com.aml.common.dto.request.GenerateReportRequest;
import com.aml.common.dto.response.GeneratedReportResponse;
import com.aml.transaction.service.ReportGenerationService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {
    private final ReportGenerationService service;
    public ReportController(ReportGenerationService service) { this.service = service; }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<GeneratedReportResponse>> generate(@Valid @RequestBody GenerateReportRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.generate(request), "Compliance report generated"));
    }

    @GetMapping("/{id}")
    public ApiResponse<GeneratedReportResponse> get(@PathVariable String id) {
        return ApiResponse.success(service.get(id), "Report fetched successfully");
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable String id) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDisposition(ContentDisposition.attachment().filename("compliance-report-" + id + ".txt").build());
        return ResponseEntity.ok().headers(headers).body(service.download(id));
    }
}
