package com.aml.transaction.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aml.common.dto.common.ApiResponse;
import com.aml.common.dto.request.CreateTransactionRequest;
import com.aml.common.dto.response.RiskAssessmentResponse;
import com.aml.common.dto.response.TransactionResponse;
import com.aml.transaction.service.TransactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // ------------------------------------------------------------
    // Create transaction
    // ------------------------------------------------------------
    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>>
    createTransaction(
            @Valid @RequestBody CreateTransactionRequest request
    ) {

        TransactionResponse response =
                transactionService.createTransaction(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                response,
                                "Transaction created successfully"
                        )
                );
    }

    // ------------------------------------------------------------
    // Get all transactions
    // ------------------------------------------------------------
    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>>
    getAllTransactions() {

        List<TransactionResponse> transactions =
                transactionService.getAllTransactions();

        return ResponseEntity.ok(
                ApiResponse.success(
                        transactions,
                        "Transactions fetched successfully"
                )
        );
    }

    // ------------------------------------------------------------
    // Get transaction by internal database ID
    // ------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>>
    getTransactionById(@PathVariable String id) {

        TransactionResponse response =
                transactionService.getTransactionById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Transaction fetched successfully"
                )
        );
    }

    // ------------------------------------------------------------
    // Get transaction by business transaction ID
    // ------------------------------------------------------------
    @GetMapping("/business/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>>
    getTransactionByBusinessId(
            @PathVariable String transactionId
    ) {

        TransactionResponse response =
                transactionService.getTransactionByBusinessId(
                        transactionId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        response,
                        "Transaction fetched successfully"
                )
        );
    }

    @org.springframework.web.bind.annotation.PutMapping("/business/{transactionId}/review")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateReviewStatus(
            @PathVariable String transactionId, @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(ApiResponse.success(
                transactionService.updateReviewStatus(transactionId, request.get("status")),
                "Transaction review status updated"));
    }

    // ------------------------------------------------------------
    // Get all transactions involving an account
    // ------------------------------------------------------------
    @GetMapping("/account/{accountId}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>>
    getTransactionsByAccount(
            @PathVariable String accountId
    ) {

        List<TransactionResponse> transactions =
                transactionService.getTransactionsByAccount(
                        accountId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        transactions,
                        "Account transactions fetched successfully"
                )
        );
    }

//Get risk assessment

    @GetMapping("/{transactionId}/risk")
public ResponseEntity<ApiResponse<RiskAssessmentResponse>>
getRiskAssessment(
        @PathVariable String transactionId
) {

    RiskAssessmentResponse response =
            transactionService.getRiskAssessment(
                    transactionId
            );

    return ResponseEntity.ok(
            ApiResponse.success(
                    response,
                    "Risk assessment fetched successfully"
            )
    );
}
}
