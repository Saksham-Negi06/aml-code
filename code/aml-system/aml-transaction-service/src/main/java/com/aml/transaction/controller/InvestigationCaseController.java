package com.aml.transaction.controller;
import com.aml.common.dto.common.ApiResponse;
import com.aml.common.dto.request.CreateInvestigationCaseRequest;
import com.aml.common.dto.request.UpdateInvestigationCaseRequest;
import com.aml.common.dto.response.InvestigationCaseResponse;
import com.aml.transaction.service.InvestigationCaseService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/v1/cases")
public class InvestigationCaseController {
    private final InvestigationCaseService service; public InvestigationCaseController(InvestigationCaseService s){service=s;}
    @GetMapping public ApiResponse<List<InvestigationCaseResponse>> all(){return ApiResponse.success(service.all(),"Cases fetched successfully");}
    @PostMapping public ResponseEntity<ApiResponse<InvestigationCaseResponse>> create(@Valid @RequestBody CreateInvestigationCaseRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(r),"Investigation case created"));}
    @PutMapping("/{id}") public ApiResponse<InvestigationCaseResponse> update(@PathVariable String id,@RequestBody UpdateInvestigationCaseRequest r){return ApiResponse.success(service.update(id,r),"Investigation case updated");}
}
