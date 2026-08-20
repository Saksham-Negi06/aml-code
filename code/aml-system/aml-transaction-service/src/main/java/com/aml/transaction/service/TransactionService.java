package com.aml.transaction.service;
import com.aml.transaction.client.FastApiClient;
import com.aml.common.dto.request.CreateTransactionRequest;
import com.aml.common.dto.request.FastApiRequest;
import com.aml.common.dto.response.FastApiResponse;
import com.aml.common.dto.response.RiskAssessmentResponse;
import com.aml.common.dto.response.TransactionResponse;
import com.aml.common.entity.Account;
import com.aml.common.entity.RiskAssessment;
import com.aml.common.entity.Transaction;
import com.aml.common.entity.TransactionFeatureSnapshot;
import com.aml.common.entity.TransactionStatus;
import com.aml.common.repository.AccountRepository;
import com.aml.common.repository.RiskAssessmentRepository;
import com.aml.common.repository.TransactionFeatureSnapshotRepository;
import com.aml.common.repository.TransactionRepository;
import com.aml.config.event.EventBus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aml.common.dto.response.RiskAssessmentResponse;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final EventBus eventBus;
    private final TransactionHistoryService transactionHistoryService;

    private final TransactionFeatureSnapshotRepository
            featureSnapshotRepository;

    private final RiskAssessmentRepository
            riskAssessmentRepository;

    private final FastApiClient fastApiClient;

    private final ObjectMapper objectMapper;

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            EventBus eventBus,
            TransactionHistoryService transactionHistoryService,
            TransactionFeatureSnapshotRepository
                    featureSnapshotRepository,
            RiskAssessmentRepository riskAssessmentRepository,
            FastApiClient fastApiClient,
            ObjectMapper objectMapper
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.eventBus = eventBus;
        this.transactionHistoryService = transactionHistoryService;
        this.featureSnapshotRepository = featureSnapshotRepository;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.fastApiClient = fastApiClient;
        this.objectMapper = objectMapper;
    }

    // ============================================================
    // CREATE TRANSACTION + AML PROCESSING
    // ============================================================

    @Transactional
    public TransactionResponse createTransaction(
            CreateTransactionRequest request
    ) {

        // --------------------------------------------------------
        // 1. Find sender account
        // --------------------------------------------------------

        Account sender = accountRepository
                .findByAccountHash(
                        hashAccountId(
                                request.getSenderAccountId()
                        )
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Sender account not found: "
                                        + request.getSenderAccountId()
                        )
                );

        // --------------------------------------------------------
        // 2. Find receiver account
        // --------------------------------------------------------

        Account receiver = accountRepository
                .findByAccountHash(
                        hashAccountId(
                                request.getReceiverAccountId()
                        )
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Receiver account not found: "
                                        + request.getReceiverAccountId()
                        )
                );

        // --------------------------------------------------------
        // 3. Update bank locations from request
        // --------------------------------------------------------

        sender.setBankLocation(
                request.getSenderBankLocation()
        );

        receiver.setBankLocation(
                request.getReceiverBankLocation()
        );

        accountRepository.save(sender);
        accountRepository.save(receiver);

        // --------------------------------------------------------
        // 4. Create transaction
        // --------------------------------------------------------

        Transaction transaction = new Transaction();

        transaction.setId(
                UUID.randomUUID().toString()
        );

        transaction.setTransactionId(
                request.getTransactionId()
        );

        transaction.setSenderAccount(sender);
        transaction.setReceiverAccount(receiver);

        transaction.setAmount(
                request.getAmount()
        );

        transaction.setPaymentCurrency(
                request.getPaymentCurrency()
        );

        transaction.setReceivedCurrency(
                request.getReceivedCurrency()
        );

        transaction.setPaymentType(
                request.getPaymentType()
        );

        transaction.setTransactionDatetime(
                request.getTransactionDatetime()
        );

        transaction.setStatus(
                TransactionStatus.COMPLETED
        );

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        // --------------------------------------------------------
        // 5. Calculate sender history
        // --------------------------------------------------------

        TransactionHistoryService.AccountHistory senderHistory =
                transactionHistoryService.calculateSenderHistory(
                        sender,
                        savedTransaction.getTransactionId()
                );

        // --------------------------------------------------------
        // 6. Calculate receiver history
        // --------------------------------------------------------

        TransactionHistoryService.AccountHistory receiverHistory =
                transactionHistoryService.calculateReceiverHistory(
                        receiver,
                        savedTransaction.getTransactionId()
                );

        // --------------------------------------------------------
        // 7. Save feature snapshot
        // --------------------------------------------------------

        TransactionFeatureSnapshot featureSnapshot =
                new TransactionFeatureSnapshot();

        featureSnapshot.setId(
                UUID.randomUUID().toString()
        );

        featureSnapshot.setTransaction(
                savedTransaction
        );

        featureSnapshot.setSenderTransactionCount(
                senderHistory.getTransactionCount()
        );

        featureSnapshot.setSenderAverageAmount(
                senderHistory.getAverageAmount()
        );

        featureSnapshot.setSenderAmountStddev(
                senderHistory.getAmountStddev()
        );

        featureSnapshot.setSenderMinimumAmount(
                senderHistory.getMinimumAmount()
        );

        featureSnapshot.setSenderMaximumAmount(
                senderHistory.getMaximumAmount()
        );

        featureSnapshot.setSenderUniqueCounterparties(
                senderHistory.getUniqueCounterparties()
        );

        featureSnapshot.setReceiverTransactionCount(
                receiverHistory.getTransactionCount()
        );

        featureSnapshot.setReceiverAverageAmount(
                receiverHistory.getAverageAmount()
        );

        featureSnapshot.setReceiverAmountStddev(
                receiverHistory.getAmountStddev()
        );

        featureSnapshot.setReceiverMinimumAmount(
                receiverHistory.getMinimumAmount()
        );

        featureSnapshot.setReceiverMaximumAmount(
                receiverHistory.getMaximumAmount()
        );

        featureSnapshot.setReceiverUniqueCounterparties(
                receiverHistory.getUniqueCounterparties()
        );

        featureSnapshot.setUseGemini(
                request.getUseGemini() != null
                        ? request.getUseGemini()
                        : false
        );

        TransactionFeatureSnapshot savedFeatureSnapshot =
                featureSnapshotRepository.save(
                        featureSnapshot
                );

        // --------------------------------------------------------
        // 8. Build FastAPI request
        // --------------------------------------------------------

        FastApiRequest fastApiRequest =
                new FastApiRequest();

        fastApiRequest.setTransactionId(
                savedTransaction.getTransactionId()
        );

        fastApiRequest.setSenderAccount(
                request.getSenderAccountId()
        );

        fastApiRequest.setReceiverAccount(
                request.getReceiverAccountId()
        );

        fastApiRequest.setAmount(
                savedTransaction.getAmount()
        );

        fastApiRequest.setPaymentCurrency(
                savedTransaction.getPaymentCurrency()
        );

        fastApiRequest.setReceivedCurrency(
                savedTransaction.getReceivedCurrency()
        );

        fastApiRequest.setSenderBankLocation(
                sender.getBankLocation()
        );

        fastApiRequest.setReceiverBankLocation(
                receiver.getBankLocation()
        );

        fastApiRequest.setPaymentType(
                savedTransaction.getPaymentType()
        );

        fastApiRequest.setTransactionDatetime(
                savedTransaction.getTransactionDatetime()
        );

        fastApiRequest.setSenderHistory(
                toHistoryDto(senderHistory)
        );

        fastApiRequest.setReceiverHistory(
                toHistoryDto(receiverHistory)
        );

        fastApiRequest.setUseGemini(
                request.getUseGemini() != null
                        ? request.getUseGemini()
                        : false
        );

        // --------------------------------------------------------
        // 9. CALL FASTAPI
        // --------------------------------------------------------

        FastApiResponse fastApiResponse =
                fastApiClient.predict(
                        fastApiRequest
                );

        // --------------------------------------------------------
        // 10. Save RiskAssessment
        // --------------------------------------------------------

        RiskAssessment riskAssessment =
                new RiskAssessment();

        riskAssessment.setId(
                UUID.randomUUID().toString()
        );

        riskAssessment.setTransaction(
                savedTransaction
        );

        riskAssessment.setFeatureSnapshot(
                savedFeatureSnapshot
        );

        riskAssessment.setRequestId(
                fastApiResponse.getRequestId()
        );

        riskAssessment.setRiskScore(
                fastApiResponse.getRiskScore()
        );

        riskAssessment.setRiskCategory(
                fastApiResponse.getRiskCategory()
        );

        riskAssessment.setModelConfidence(
                fastApiResponse.getModelConfidence()
        );

        riskAssessment.setShouldFlag(
                fastApiResponse.getShouldFlag()
        );

        riskAssessment.setDecisionThreshold(
                fastApiResponse.getDecisionThreshold()
        );

        riskAssessment.setEnsembleMethod(
                fastApiResponse.getEnsembleMethod()
        );

        riskAssessment.setOneLineExplanation(
                fastApiResponse.getOneLineExplanation()
        );

        riskAssessment.setRecommendation(
                fastApiResponse.getRecommendation()
        );

        riskAssessment.setHistoryContext(
                fastApiResponse.getHistoryContext()
        );

        riskAssessment.setProcessingTimeMs(
                fastApiResponse.getProcessingTimeMs()
        );

        riskAssessment.setExplanationJson(
                serializeExplanation(
                        fastApiResponse.getExplanation()
                )
        );

        RiskAssessment savedRiskAssessment =
                riskAssessmentRepository.save(
                        riskAssessment
                );

        // --------------------------------------------------------
        // 11. Publish transaction event
        // --------------------------------------------------------

        TransactionResponse response =
                mapToResponse(savedTransaction);

        eventBus.publish(response);

        return response;
    }

    // ============================================================
    // GET ALL TRANSACTIONS
    // ============================================================

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions() {

        return transactionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

   @Transactional
   public TransactionResponse updateReviewStatus(String transactionId, String status) {
       Transaction transaction = transactionRepository.findByTransactionId(transactionId)
               .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
       TransactionStatus requested;
       try { requested = TransactionStatus.valueOf(status == null ? "" : status.toUpperCase()); }
       catch (IllegalArgumentException ex) { throw new IllegalArgumentException("Invalid transaction status: " + status); }
       if (requested != TransactionStatus.COMPLETED && requested != TransactionStatus.UNDER_REVIEW && requested != TransactionStatus.BLOCKED) {
           throw new IllegalArgumentException("Review status must be COMPLETED, UNDER_REVIEW, or BLOCKED");
       }
       transaction.setStatus(requested);
       return mapToResponse(transactionRepository.save(transaction));
   }

    // ============================================================
    // GET BY INTERNAL ID
    // ============================================================

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(
            String id
    ) {

        Transaction transaction =
                transactionRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Transaction not found: "
                                                + id
                                )
                        );

        return mapToResponse(transaction);
    }

    // ============================================================
    // GET BY BUSINESS TRANSACTION ID
    // ============================================================

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByBusinessId(
            String transactionId
    ) {

        Transaction transaction =
                transactionRepository
                        .findByTransactionId(transactionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Transaction not found: "
                                                + transactionId
                                )
                        );

        return mapToResponse(transaction);
    }

    // ============================================================
    // GET TRANSACTIONS FOR ACCOUNT
    // ============================================================

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByAccount(
            String accountId
    ) {

        String accountHash =
                hashAccountId(accountId);

        Account account =
                accountRepository
                        .findByAccountHash(accountHash)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found: "
                                                + accountId
                                )
                        );

        return transactionRepository
                .findBySenderAccount_IdOrReceiverAccount_Id(
                        account.getId(),
                        account.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ============================================================
    // ENTITY -> RESPONSE
    // ============================================================

    private TransactionResponse mapToResponse(
            Transaction transaction
    ) {

        TransactionResponse response =
                new TransactionResponse();

        response.setId(
                transaction.getId()
        );

        response.setTransactionId(
                transaction.getTransactionId()
        );

        response.setSenderAccountId(
                transaction.getSenderAccount().getId()
        );

        response.setReceiverAccountId(
                transaction.getReceiverAccount().getId()
        );

        response.setAmount(
                transaction.getAmount()
        );

        response.setPaymentCurrency(
                transaction.getPaymentCurrency()
        );

        response.setReceivedCurrency(
                transaction.getReceivedCurrency()
        );

        response.setTransactionDatetime(
                transaction.getTransactionDatetime()
        );

        response.setPaymentType(
                transaction.getPaymentType()
        );

        response.setStatus(
                transaction.getStatus().name()
        );

        response.setCreatedAt(
                transaction.getCreatedAt()
        );

        return response;
    }

    // ============================================================
    // HISTORY -> FASTAPI DTO
    // ============================================================

    private FastApiRequest.History toHistoryDto(
            TransactionHistoryService.AccountHistory history
    ) {

        FastApiRequest.History dto =
                new FastApiRequest.History();

        dto.setTransactionCount(
                history.getTransactionCount()
        );

        dto.setAverageAmount(
                history.getAverageAmount()
        );

        dto.setAmountStddev(
                history.getAmountStddev()
        );

        dto.setMinimumAmount(
                history.getMinimumAmount()
        );

        dto.setMaximumAmount(
                history.getMaximumAmount()
        );

        dto.setUniqueCounterparties(
                history.getUniqueCounterparties()
        );

        return dto;
    }

    // ============================================================
    // SERIALIZE FASTAPI EXPLANATION
    // ============================================================

    private String serializeExplanation(
            FastApiResponse.Explanation explanation
    ) {

        if (explanation == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(
                    explanation
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize FastAPI explanation",
                    e
            );
        }
    }

    // ============================================================
    // HASH ACCOUNT ID
    // ============================================================

    private String hashAccountId(
            String accountId
    ) {

        return com.aml.common.utils.HashUtil
                .sha256(accountId);
    }

    //Get Risk assessment
    @Transactional(readOnly = true)
public RiskAssessmentResponse getRiskAssessment(
        String transactionId
) {

    Transaction transaction = transactionRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

    RiskAssessment assessment =
            riskAssessmentRepository
                    .findFirstByTransaction_IdOrderByCreatedAtDesc(
                            transaction.getId()
                    )
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Risk assessment not found for transaction: "
                                            + transactionId
                            )
                    );

    RiskAssessmentResponse response =
            new RiskAssessmentResponse();

    response.setTransactionId(
            assessment.getTransaction().getTransactionId()
    );

    response.setRequestId(
            assessment.getRequestId()
    );

    response.setRiskScore(
            assessment.getRiskScore()
    );

    response.setRiskCategory(
            assessment.getRiskCategory()
    );

    response.setModelConfidence(
            assessment.getModelConfidence()
    );

    response.setShouldFlag(
            assessment.getShouldFlag()
    );

    response.setDecisionThreshold(
            assessment.getDecisionThreshold()
    );

    response.setEnsembleMethod(
            assessment.getEnsembleMethod()
    );

    response.setExplanationJson(
            assessment.getExplanationJson()
    );

    response.setOneLineExplanation(
            assessment.getOneLineExplanation()
    );

    response.setRecommendation(
            assessment.getRecommendation()
    );

    response.setHistoryContext(
            assessment.getHistoryContext()
    );

    response.setProcessingTimeMs(
            assessment.getProcessingTimeMs()
    );

    response.setCreatedAt(
            assessment.getCreatedAt()
    );

    return response;
}
}
