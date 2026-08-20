# Anti-Money Laundering (AML) System - UPDATED Complete Guide

**Built with:** Java 17 | Spring Boot | Microservices | Event-Driven | H2/Oracle | No Docker (for now)

---

## 🎯 UPDATED ARCHITECTURE

### **Event-Driven Flow (NEW)**

```
TRANSACTION SUBMISSION
    ↓
DETECTION ENGINE (Rules + ML + GNN)
    ↓
RISK SCORE >= 50?
    ↓
IF NO → Archive
IF YES → Publish: TransactionFlaggedEvent
    ↓
CASE MANAGEMENT (Event Listener)
    │
    ├─ Create Case object
    ├─ Assign to expert type
    ├─ Set priority
    └─ Publish: CaseCreatedEvent
        ↓
LLM/REPORT SERVICE (Event Listener)
    │
    ├─ Receive Case details
    ├─ Generate report (GPT-mini, Gemini, Template)
    ├─ Store PDF/JSON
    └─ Publish: ReportGeneratedEvent
        ↓
NOTIFICATION SERVICE (Event Listener)
    │
    ├─ Fetch Case + Report
    ├─ Format message
    └─ Send to professional:
       • Email
       • Dashboard notification
       • SMS (future)
       • Mobile push (future)
```

---

## 📦 PROJECT STRUCTURE (UPDATED)

```
aml-system/
├── pom.xml (Parent)
│
├── aml-config/                           # Shared Config
│   ├── pom.xml
│   └── src/main/java/config/
│       ├── KafkaConfig.java (In-memory broker for MVP)
│       ├── SecurityConfig.java
│       ├── JwtTokenProvider.java
│       └── H2OracleDataSourceConfig.java
│
├── aml-common/                           # DTOs, Entities, Events
│   ├── pom.xml
│   └── src/main/java/com/aml/common/
│       │
│       ├── entity/
│       │   ├── Transaction.java
│       │   ├── Account.java
│       │   ├── Customer.java
│       │   ├── FlaggedTransaction.java
│       │   ├── Case.java                 (NEW: Event-driven)
│       │   ├── GeneratedReport.java
│       │   ├── AuditLog.java
│       │   └── User.java
│       │
│       ├── dto/
│       │   ├── request/
│       │   │   ├── CreateTransactionRequest.java
│       │   │   ├── AnalysisRequest.java
│       │   │   ├── CreateCaseFromFlagRequest.java (NEW)
│       │   │   ├── UpdateCaseStatusRequest.java (NEW)
│       │   │   ├── ResolveCaseRequest.java (NEW)
│       │   │   ├── GenerateReportRequest.java
│       │   │   ├── LoginRequest.java
│       │   │   └── OAuth2Request.java
│       │   │
│       │   ├── response/
│       │   │   ├── TransactionResponse.java
│       │   │   ├── FlaggedTransactionResponse.java
│       │   │   ├── CaseResponse.java                (NEW: Full context)
│       │   │   ├── GeneratedReportResponse.java
│       │   │   ├── AuthResponse.java
│       │   │   ├── ErrorResponse.java
│       │   │   └── PagedResponse<T>.java
│       │   │
│       │   └── common/
│       │       ├── ApiResponse.java
│       │       ├── ValidationError.java
│       │       └── PageInfo.java
│       │
│       ├── event/                        (NEW: Event-driven)
│       │   ├── TransactionFlaggedEvent.java
│       │   ├── CaseCreatedEvent.java
│       │   ├── ReportGeneratedEvent.java
│       │   ├── NotificationSentEvent.java
│       │   └── AuditEvent.java
│       │
│       ├── enums/
│       │   ├── RiskLevel.java
│       │   ├── CaseStatus.java           (NEW)
│       │   ├── ExpertType.java
│       │   ├── CaseType.java             (NEW)
│       │   ├── TransactionStatus.java
│       │   └── DetectionRuleType.java
│       │
│       └── utils/
│           ├── HashUtil.java
│           └── ValidationUtil.java
│
├── aml-batch/                            # Spring Batch (CSV Import)
│   ├── pom.xml
│   └── src/main/java/com/aml/batch/
│       ├── config/BatchConfig.java
│       ├── processor/TransactionItemProcessor.java
│       ├── listener/BatchJobListener.java
│       └── generator/SyntheticDataGenerator.java
│
├── aml-detection-engine/                 # CORE: 3-Part Detection
│   ├── pom.xml
│   └── src/main/java/com/aml/detection/
│       │
│       ├── PART 1: RULE-BASED
│       │   ├── rules/
│       │   │   ├── DetectionRule.java (Interface)
│       │   │   ├── HighAmountRule.java
│       │   │   ├── StructuringRule.java
│       │   │   ├── HighRiskCountryRule.java
│       │   │   ├── BehavioralAnomalyRule.java
│       │   │   ├── VelocityRule.java
│       │   │   └── CrossBorderHighRiskRule.java
│       │   │
│       │   └── engine/
│       │       └── RuleEngine.java
│       │
│       ├── PART 2: ML CLASSIFIER
│       │   ├── ml/
│       │   │   ├── MLClassifier.java (Random Forest wrapper)
│       │   │   ├── FeatureExtractor.java
│       │   │   └── ModelLoader.java
│       │   │
│       │   └── config/
│       │       └── MLConfig.java
│       │
│       ├── PART 3: GNN (FUTURE)
│       │   ├── gnn/
│       │   │   ├── GraphBuilder.java (placeholder)
│       │   │   ├── GNNDetector.java (placeholder)
│       │   │   └── NetworkAnalyzer.java (placeholder)
│       │   │
│       │   └── graph/
│       │       └── TransactionGraph.java (placeholder)
│       │
│       └── ENSEMBLE
│           ├── EnsembleDetector.java
│           ├── RiskScoringEngine.java
│           └── DetectionResultFormatter.java
│
├── aml-transaction-service/              # Microservice 1 (Eureka)
│   ├── pom.xml
│   └── src/main/java/com/aml/transaction/
│       ├── controller/TransactionController.java
│       ├── service/
│       │   ├── TransactionService.java
│       │   └── TransactionAnalysisService.java (calls detection)
│       ├── repository/TransactionRepository.java
│       ├── event/TransactionEventPublisher.java (NEW)
│       └── TransactionServiceApplication.java
│
├── aml-case-management/                  # Microservice 2 (Case + Routing)
│   ├── pom.xml
│   └── src/main/java/com/aml/case/
│       ├── controller/CaseController.java (NEW: Full CRUD)
│       ├── service/
│       │   ├── CaseService.java (NEW: Create from event)
│       │   ├── ExpertRoutingService.java (NEW: Logic)
│       │   ├── CaseEventListener.java (NEW: Listens to TransactionFlaggedEvent)
│       │   └── NotificationService.java (NEW: Sends alerts)
│       ├── repository/
│       │   ├── CaseRepository.java (NEW)
│       │   └── ExpertAssignmentRepository.java (DEPRECATED: merged into Case)
│       ├── event/CaseEventPublisher.java (NEW)
│       └── CaseManagementApplication.java
│
├── aml-llm-service/                      # Microservice 3 (Report Generation)
│   ├── pom.xml
│   └── src/main/java/com/aml/llm/
│       ├── controller/ReportController.java
│       ├── service/
│       │   ├── ReportGenerationService.java
│       │   ├── LLMStrategy.java (Interface)
│       │   ├─ impl/
│       │   │   ├─ GeminiLLMStrategy.java (Free tier)
│       │   │   ├─ OpenAIStrategy.java (Paid)
│       │   │   ├─ ClaudeLLMStrategy.java (Paid)
│       │   │   ├─ HuggingFaceLLMStrategy.java (Free)
│       │   │   └─ TemplateBasedStrategy.java (MVP)
│       │   ├── CaseEventListener.java (NEW: Listens to CaseCreatedEvent)
│       │   └── PDFGenerator.java
│       ├── repository/GeneratedReportRepository.java
│       ├── event/ReportEventPublisher.java (NEW)
│       ├── config/LLMConfig.java (NEW)
│       └── LLMServiceApplication.java
│
├── aml-account-service/                  # Microservice 4
│   ├── pom.xml
│   └── src/main/java/com/aml/account/
│       ├── controller/AccountController.java
│       ├── service/AccountService.java
│       ├── repository/AccountRepository.java
│       └── AccountServiceApplication.java
│
├── aml-customer-service/                 # Microservice 5
│   ├── pom.xml
│   └── src/main/java/com/aml/customer/
│       ├── controller/CustomerController.java
│       ├── service/CustomerService.java
│       ├── repository/CustomerRepository.java
│       └── CustomerServiceApplication.java
│
├── aml-auth-service/                     # Microservice 6 (Auth)
│   ├── pom.xml
│   ├── src/main/java/com/aml/auth/
│   │   ├── controller/
│   │   │   ├── AuthController.java (JWT)
│   │   │   └── OAuth2Controller.java (OAuth2 alternative)
│   │   ├── service/
│   │   │   ├── JwtTokenService.java
│   │   │   ├── OAuth2Service.java
│   │   │   └── UserService.java
│   │   ├── repository/UserRepository.java
│   │   ├── filter/JwtFilter.java
│   │   └── AuthServiceApplication.java
│   └── src/main/resources/
│       ├── application.yml (JWT config)
│       └── oauth2-config.yml
│
├── aml-api-gateway/                      # API Gateway
│   ├── pom.xml
│   ├── src/main/java/com/aml/gateway/
│   │   ├── config/GatewayConfig.java
│   │   └── GatewayApplication.java
│   └── src/main/resources/application.yml
│
├── aml-eureka-server/                    # Service Discovery
│   ├── pom.xml
│   ├── src/main/java/com/aml/eureka/EurekaServerApplication.java
│   └── src/main/resources/application.yml
│
├── aml-frontend/                         # Frontend (HTML/CSS/JS)
│   ├── index.html (login)
│   ├── dashboard.html
│   ├── transactions.html
│   ├── cases.html (NEW: Full case management)
│   ├── reports.html
│   ├── css/
│   │   ├── style.css
│   │   ├── dashboard.css
│   │   └── responsive.css
│   ├── js/
│   │   ├── config.js
│   │   ├── auth.js
│   │   ├── api.js
│   │   ├── dashboard.js
│   │   ├── transactions.js
│   │   ├── cases.js (NEW)
│   │   └── utils.js
│   └── assets/
│       └── charts/
│
├── postman-collection.json (UPDATED)
├── docker-compose.yml (For later, commented out for now)
└── README.md
```

---

## 🗄️ UPDATED DATABASE SCHEMA

### **New: Case Table**

```sql
-- Accounts Table (unchanged)
CREATE TABLE accounts (
    id VARCHAR(36) PRIMARY KEY,
    account_hash VARCHAR(255) NOT NULL UNIQUE,
    account_type VARCHAR(50),
    bank_location VARCHAR(100),
    account_risk_score DECIMAL(5,2) DEFAULT 0,
    avg_transaction_amount DECIMAL(15,2),
    avg_transactions_per_month INT,
    account_age_days INT,
    is_flagged BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_account_hash (account_hash),
    INDEX idx_risk_score (account_risk_score)
);

-- Customers Table (unchanged)
CREATE TABLE customers (
    id VARCHAR(36) PRIMARY KEY,
    account_id VARCHAR(36) NOT NULL,
    account_holder_type VARCHAR(50),
    kyc_verification_status VARCHAR(50),
    risk_country_flag BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);

-- Transactions Table (unchanged)
CREATE TABLE transactions (
    id VARCHAR(36) PRIMARY KEY,
    sender_account_id VARCHAR(36) NOT NULL,
    receiver_account_id VARCHAR(36) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    payment_currency VARCHAR(10),
    received_currency VARCHAR(10),
    transaction_date TIMESTAMP NOT NULL,
    payment_type VARCHAR(50),
    status VARCHAR(50) DEFAULT 'PENDING',
    is_flagged BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_account_id) REFERENCES accounts(id),
    FOREIGN KEY (receiver_account_id) REFERENCES accounts(id),
    INDEX idx_sender (sender_account_id),
    INDEX idx_receiver (receiver_account_id),
    INDEX idx_flagged (is_flagged),
    INDEX idx_date (transaction_date)
);

-- Flagged Transactions Table (unchanged)
CREATE TABLE flagged_transactions (
    id VARCHAR(36) PRIMARY KEY,
    transaction_id VARCHAR(36) NOT NULL UNIQUE,
    detection_rule_triggered VARCHAR(500),
    risk_score DECIMAL(5,2) NOT NULL,
    risk_level VARCHAR(50),
    ai_explanation TEXT,
    ai_confidence DECIMAL(5,2),
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (transaction_id) REFERENCES transactions(id),
    INDEX idx_risk_level (risk_level),
    INDEX idx_status (status)
);

-- NEW: Cases Table (Event-driven)
CREATE TABLE cases (
    id VARCHAR(36) PRIMARY KEY,
    flagged_transaction_id VARCHAR(36) NOT NULL UNIQUE,
    case_type VARCHAR(100),
    risk_score DECIMAL(5,2) NOT NULL,
    assigned_to_expert_id VARCHAR(36),
    expert_type VARCHAR(100),
    priority_level INT DEFAULT 3,
    status VARCHAR(50) DEFAULT 'PENDING',
    generated_report_id VARCHAR(36),
    report_url TEXT,
    case_notes TEXT,
    resolution_notes TEXT,
    assigned_at TIMESTAMP,
    reviewed_at TIMESTAMP,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (flagged_transaction_id) REFERENCES flagged_transactions(id),
    FOREIGN KEY (generated_report_id) REFERENCES generated_reports(id),
    INDEX idx_status (status),
    INDEX idx_priority (priority_level),
    INDEX idx_expert_type (expert_type),
    INDEX idx_created (created_at)
);

-- Generated Reports Table (updated to link to Case)
CREATE TABLE generated_reports (
    id VARCHAR(36) PRIMARY KEY,
    case_id VARCHAR(36),
    flagged_transaction_id VARCHAR(36),
    report_content LONGTEXT,
    report_format VARCHAR(20),
    generated_by_llm_model VARCHAR(100),
    report_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (case_id) REFERENCES cases(id),
    FOREIGN KEY (flagged_transaction_id) REFERENCES flagged_transactions(id),
    INDEX idx_case_id (case_id)
);

-- Users Table (unchanged)
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
);

-- Audit Logs Table (unchanged)
CREATE TABLE audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    action VARCHAR(255),
    entity_type VARCHAR(100),
    entity_id VARCHAR(36),
    old_value TEXT,
    new_value TEXT,
    performed_by VARCHAR(100),
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_timestamp (performed_at)
);
```

---

## 📝 DTO IMPLEMENTATIONS

### **Request DTOs**

```java
// CreateTransactionRequest.java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTransactionRequest {
    @NotBlank(message = "Sender account ID is required")
    private String senderAccountId;
    
    @NotBlank(message = "Receiver account ID is required")
    private String receiverAccountId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotBlank(message = "Payment currency is required")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    private String paymentCurrency;
    
    @NotBlank(message = "Received currency is required")
    private String receivedCurrency;
    
    @NotNull(message = "Transaction date is required")
    private LocalDateTime transactionDate;
    
    @NotBlank(message = "Payment type is required")
    private String paymentType;
}

// AnalysisRequest.java
@Data
public class AnalysisRequest {
    @NotBlank(message = "Transaction ID is required")
    private String transactionId;
}

// CreateCaseFromFlagRequest.java (NEW: Event-driven)
@Data
public class CreateCaseFromFlagRequest {
    @NotBlank(message = "Flagged transaction ID is required")
    private String flaggedTransactionId;
    
    private String assignToExpertType;
    private Integer priorityOverride;
    private String additionalContext;
}

// UpdateCaseStatusRequest.java (NEW)
@Data
public class UpdateCaseStatusRequest {
    @NotBlank(message = "Status is required")
    private String status;
    
    private String assignedToExpertId;
    private String notes;
}

// ResolveCaseRequest.java (NEW)
@Data
public class ResolveCaseRequest {
    @NotBlank(message = "Resolution is required")
    private String resolution;
    
    @NotBlank(message = "Resolution notes are required")
    private String resolutionNotes;
    
    private String recommendation;
    private String resolvedBy;
}

// GenerateReportRequest.java (UPDATED: LLM options)
@Data
public class GenerateReportRequest {
    @NotBlank(message = "Case ID is required")
    private String caseId;
    
    @NotNull
    @Builder.Default
    private String reportFormat = "PDF"; // PDF, JSON, TEXT
    
    @NotNull
    @Builder.Default
    private Boolean includeAnalysis = true;
    
    @NotNull
    @Builder.Default
    private String llmProvider = "TEMPLATE"; // TEMPLATE, GEMINI, GPT, CLAUDE, HUGGINGFACE
    
    private String customInstructions;
}

// LoginRequest.java
@Data
public class LoginRequest {
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;
}

// OAuth2Request.java
@Data
public class OAuth2Request {
    @NotBlank
    private String grantType;
    
    @NotBlank
    private String clientId;
    
    private String clientSecret;
    
    private String username;
    
    private String password;
    
    private String scope;
}
```

### **Response DTOs**

```java
// TransactionResponse.java
@Data
@Builder
public class TransactionResponse {
    private String id;
    private String senderAccountId;
    private String receiverAccountId;
    private BigDecimal amount;
    private String paymentCurrency;
    private String receivedCurrency;
    private LocalDateTime transactionDate;
    private String paymentType;
    private String status;
    private Boolean isFlagged;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// FlaggedTransactionResponse.java
@Data
@Builder
public class FlaggedTransactionResponse {
    private String id;
    private String transactionId;
    private String senderAccountId;
    private String receiverAccountId;
    private BigDecimal amount;
    private String[] detectionRuleTriggered;
    private Double riskScore;
    private String riskLevel;
    private String aiExplanation;
    private Double aiConfidence;
    private String status;
    private String assignedCaseId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// CaseResponse.java (NEW: Full context for professional)
@Data
@Builder
public class CaseResponse {
    private String id;
    private String flaggedTransactionId;
    
    // Nested full transaction context
    private FlaggedTransactionResponse flaggedTransaction;
    
    private String caseType;
    private Double riskScore;
    private String assignedToExpertId;
    private String expertType;
    private Integer priorityLevel;
    private String status;
    
    // Report info
    private String generatedReportId;
    private String reportUrl;
    private String reportSummary;
    
    private String caseNotes;
    private String resolutionNotes;
    
    private LocalDateTime assignedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// GeneratedReportResponse.java (UPDATED)
@Data
@Builder
public class GeneratedReportResponse {
    private String id;
    private String caseId;
    private String flaggedTransactionId;
    private String reportContent;
    private String reportFormat;
    private String generatedByLLM;
    private String reportUrl;
    private String[] summaryFindings;
    private LocalDateTime createdAt;
}

// AuthResponse.java
@Data
@Builder
public class AuthResponse {
    private String token;
    private String tokenType;
    private Long expiresIn;
    private String username;
    private String role;
    private String refreshToken;
}

// ErrorResponse.java
@Data
@Builder
public class ErrorResponse {
    private String code;
    private String message;
    private Map<String, String> details;
    private LocalDateTime timestamp;
    private String path;
}

// PagedResponse.java (Generic)
@Data
@Builder
public class PagedResponse<T> {
    private List<T> content;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
    private Boolean hasNext;
    private Boolean hasPrevious;
    private LocalDateTime lastModified;
}
```

---

## 🎯 EVENT-DRIVEN ARCHITECTURE

### **Event Classes**

```java
// TransactionFlaggedEvent.java (Published by Detection Engine)
@Data
@AllArgsConstructor
public class TransactionFlaggedEvent {
    private String transactionId;
    private String senderAccountId;
    private String receiverAccountId;
    private BigDecimal amount;
    private Double riskScore;
    private String[] triggeredRules;
    private String aiExplanation;
    private Double aiConfidence;
    private LocalDateTime timestamp;
    private String eventId; // UUID for tracking
}

// CaseCreatedEvent.java (Published by Case Management)
@Data
@AllArgsConstructor
public class CaseCreatedEvent {
    private String caseId;
    private String flaggedTransactionId;
    private String caseType;
    private Double riskScore;
    private String expertType;
    private Integer priorityLevel;
    private String assignedToExpertId;
    private LocalDateTime timestamp;
    private String eventId;
}

// ReportGeneratedEvent.java (Published by LLM Service)
@Data
@AllArgsConstructor
public class ReportGeneratedEvent {
    private String reportId;
    private String caseId;
    private String flaggedTransactionId;
    private String reportUrl;
    private String generatedByLLM;
    private LocalDateTime timestamp;
    private String eventId;
}

// NotificationSentEvent.java (Published by Notification Service)
@Data
@AllArgsConstructor
public class NotificationSentEvent {
    private String caseId;
    private String expertId;
    private String notificationChannel;
    private String status; // SENT, FAILED, PENDING
    private LocalDateTime timestamp;
    private String eventId;
}
```

### **Event Bus (In-Memory for MVP)**

```java
// EventBus.java (Simple pub-sub)
@Component
public class EventBus {
    private final Map<Class<?>, List<EventListener<?>>> listeners = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    
    @FunctionalInterface
    public interface EventListener<T> {
        void onEvent(T event);
    }
    
    public <T> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
            .add(listener);
    }
    
    public <T> void publish(T event) {
        List<EventListener<?>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            eventListeners.forEach(listener -> {
                executor.submit(() -> {
                    try {
                        ((EventListener<T>) listener).onEvent(event);
                    } catch (Exception e) {
                        System.err.println("Error processing event: " + e.getMessage());
                    }
                });
            });
        }
    }
}
```

### **Event Flow Example**

```java
// TransactionAnalysisService.java (Detection Engine)
@Service
public class TransactionAnalysisService {
    @Autowired
    private EnsembleDetector ensembleDetector;
    
    @Autowired
    private EventBus eventBus;
    
    @Autowired
    private FlaggedTransactionRepository flaggedRepo;
    
    public void analyzeTransaction(String transactionId) {
        Transaction txn = transactionRepository.findById(transactionId).orElseThrow();
        FlaggedTransactionResult result = ensembleDetector.detectAnomaly(txn);
        
        if (result.isFlagged()) {
            // Save flagged transaction
            FlaggedTransaction flagged = saveFlaggedTransaction(txn, result);
            
            // Publish event (This triggers Case Management service)
            TransactionFlaggedEvent event = new TransactionFlaggedEvent(
                txn.getId(),
                txn.getSenderAccountId(),
                txn.getReceiverAccountId(),
                txn.getAmount(),
                result.getRiskScore(),
                result.getTriggeredRules().split(","),
                result.getAiExplanation(),
                result.getAiConfidence(),
                LocalDateTime.now(),
                UUID.randomUUID().toString()
            );
            
            eventBus.publish(event);
        }
    }
}

// CaseManagementService.java (Listens to TransactionFlaggedEvent)
@Service
public class CaseManagementService {
    @Autowired
    private EventBus eventBus;
    
    @Autowired
    private CaseRepository caseRepository;
    
    @PostConstruct
    public void subscribeToEvents() {
        // Listen for flagged transactions
        eventBus.subscribe(TransactionFlaggedEvent.class, this::handleTransactionFlagged);
    }
    
    public void handleTransactionFlagged(TransactionFlaggedEvent event) {
        // Create Case object
        Case caseObj = new Case();
        caseObj.setId(UUID.randomUUID().toString());
        caseObj.setFlaggedTransactionId(event.getTransactionId());
        caseObj.setCaseType(categorizeCaseType(event.getTriggeredRules()));
        caseObj.setRiskScore(event.getRiskScore());
        
        // Route to expert
        ExpertRouting routing = routeToExpert(event.getRiskScore(), caseObj.getCaseType());
        caseObj.setExpertType(routing.getExpertType());
        caseObj.setPriorityLevel(routing.getPriorityLevel());
        caseObj.setStatus("PENDING");
        caseObj.setAssignedAt(LocalDateTime.now());
        
        caseRepository.save(caseObj);
        
        // Publish event (This triggers LLM Service)
        CaseCreatedEvent caseEvent = new CaseCreatedEvent(
            caseObj.getId(),
            caseObj.getFlaggedTransactionId(),
            caseObj.getCaseType(),
            caseObj.getRiskScore(),
            caseObj.getExpertType(),
            caseObj.getPriorityLevel(),
            caseObj.getAssignedToExpertId(),
            LocalDateTime.now(),
            UUID.randomUUID().toString()
        );
        
        eventBus.publish(caseEvent);
    }
}

// ReportGenerationService.java (Listens to CaseCreatedEvent)
@Service
public class ReportGenerationService {
    @Autowired
    private EventBus eventBus;
    
    @Autowired
    private GeneratedReportRepository reportRepo;
    
    @Autowired
    private LLMStrategy llmStrategy;
    
    @PostConstruct
    public void subscribeToEvents() {
        eventBus.subscribe(CaseCreatedEvent.class, this::handleCaseCreated);
    }
    
    public void handleCaseCreated(CaseCreatedEvent event) {
        Case caseObj = caseRepository.findById(event.getCaseId()).orElseThrow();
        
        // Generate report
        String reportContent = llmStrategy.generateReport(caseObj);
        
        GeneratedReport report = new GeneratedReport();
        report.setId(UUID.randomUUID().toString());
        report.setCaseId(caseObj.getId());
        report.setFlaggedTransactionId(caseObj.getFlaggedTransactionId());
        report.setReportContent(reportContent);
        report.setReportFormat("PDF");
        report.setGeneratedByLLMModel(llmStrategy.getName());
        report.setReportUrl("/api/v1/reports/" + report.getId() + "/download");
        
        reportRepo.save(report);
        caseObj.setGeneratedReportId(report.getId());
        caseObj.setReportUrl(report.getReportUrl());
        caseRepository.save(caseObj);
        
        // Publish event (This triggers Notification Service)
        ReportGeneratedEvent reportEvent = new ReportGeneratedEvent(
            report.getId(),
            caseObj.getId(),
            event.getFlaggedTransactionId(),
            report.getReportUrl(),
            llmStrategy.getName(),
            LocalDateTime.now(),
            UUID.randomUUID().toString()
        );
        
        eventBus.publish(reportEvent);
    }
}

// NotificationService.java (Listens to ReportGeneratedEvent)
@Service
public class NotificationService {
    @Autowired
    private EventBus eventBus;
    
    @PostConstruct
    public void subscribeToEvents() {
        eventBus.subscribe(ReportGeneratedEvent.class, this::handleReportGenerated);
    }
    
    public void handleReportGenerated(ReportGeneratedEvent event) {
        Case caseObj = caseRepository.findById(event.getCaseId()).orElseThrow();
        
        // Format message for professional
        String message = formatCaseNotification(caseObj);
        
        // Send notifications (can be multiple channels)
        sendEmailNotification(caseObj.getAssignedToExpertId(), message);
        sendDashboardNotification(caseObj.getAssignedToExpertId(), caseObj);
        
        // Log event
        eventBus.publish(new NotificationSentEvent(
            event.getCaseId(),
            caseObj.getAssignedToExpertId(),
            "EMAIL",
            "SENT",
            LocalDateTime.now(),
            UUID.randomUUID().toString()
        ));
    }
}
```

---

## 🔍 DETECTION ENGINE: ALL 3 APPROACHES (COMPLETE)

### **PART 1: RULE-BASED DETECTION (Fast, Explainable)**

```java
// DetectionRule.java (Interface)
public interface DetectionRule {
    boolean evaluate(Transaction transaction);
    double calculateRiskScore(Transaction transaction);
    String getRuleName();
    String getExplanation(Transaction transaction);
}

// HighAmountRule.java
@Component
public class HighAmountRule implements DetectionRule {
    private static final BigDecimal THRESHOLD = new BigDecimal("10000");
    
    @Override
    public boolean evaluate(Transaction txn) {
        return txn.getAmount().compareTo(THRESHOLD) > 0;
    }
    
    @Override
    public double calculateRiskScore(Transaction txn) {
        if (!evaluate(txn)) return 0;
        
        double ratio = txn.getAmount().doubleValue() / THRESHOLD.doubleValue();
        // Base 30 points + (ratio - 1) * 20
        // $10k = 30, $15k = 40, $20k = 50, capped at 100
        return Math.min(100, 30 + ((ratio - 1) * 20));
    }
    
    @Override
    public String getRuleName() {
        return "HIGH_AMOUNT";
    }
    
    @Override
    public String getExplanation(Transaction txn) {
        return String.format(
            "Amount $%.2f exceeds threshold $%.2f by %.1fx",
            txn.getAmount(),
            THRESHOLD,
            txn.getAmount().doubleValue() / THRESHOLD.doubleValue()
        );
    }
}

// StructuringRule.java
@Component
public class StructuringRule implements DetectionRule {
    @Autowired
    private TransactionRepository transactionRepository;
    
    private static final int THRESHOLD_COUNT = 5;
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("10000");
    private static final int WINDOW_HOURS = 24;
    
    @Override
    public boolean evaluate(Transaction txn) {
        LocalDateTime dayAgo = txn.getTransactionDate().minusHours(WINDOW_HOURS);
        
        long count = transactionRepository
            .findBySenderAccountIdAndTransactionDateAfter(
                txn.getSenderAccountId(),
                dayAgo
            )
            .stream()
            .filter(t -> !t.getId().equals(txn.getId()))
            .filter(t -> t.getAmount().compareTo(MAX_AMOUNT) <= 0)
            .count();
        
        return count >= THRESHOLD_COUNT;
    }
    
    @Override
    public double calculateRiskScore(Transaction txn) {
        if (!evaluate(txn)) return 0;
        
        // More transactions = higher risk
        long count = countRecentSmallTxns(txn);
        return Math.min(100, 40 + (count * 5)); // Base 40 + 5 per txn
    }
    
    @Override
    public String getRuleName() {
        return "STRUCTURING";
    }
    
    @Override
    public String getExplanation(Transaction txn) {
        long count = countRecentSmallTxns(txn);
        return String.format(
            "%d transactions < $%.2f from this account in 24 hours (threshold: %d)",
            count, MAX_AMOUNT, THRESHOLD_COUNT
        );
    }
}

// HighRiskCountryRule.java
@Component
public class HighRiskCountryRule implements DetectionRule {
    private static final Set<String> HIGH_RISK_COUNTRIES = Set.of(
        "IRAN", "NORTH_KOREA", "SYRIA", "CRIMEA"
    );
    
    private static final Set<String> MODERATE_RISK = Set.of(
        "UAE", "PAKISTAN", "HONG_KONG"
    );
    
    @Override
    public boolean evaluate(Transaction txn) {
        String receiver = txn.getReceiverBankLocation().toUpperCase();
        return HIGH_RISK_COUNTRIES.contains(receiver) || MODERATE_RISK.contains(receiver);
    }
    
    @Override
    public double calculateRiskScore(Transaction txn) {
        if (!evaluate(txn)) return 0;
        
        String receiver = txn.getReceiverBankLocation().toUpperCase();
        if (HIGH_RISK_COUNTRIES.contains(receiver)) {
            return 75.0; // Critical risk country
        } else if (MODERATE_RISK.contains(receiver)) {
            return 50.0; // Moderate risk country
        }
        return 0;
    }
    
    @Override
    public String getRuleName() {
        return "HIGH_RISK_COUNTRY";
    }
    
    @Override
    public String getExplanation(Transaction txn) {
        String receiver = txn.getReceiverBankLocation().toUpperCase();
        if (HIGH_RISK_COUNTRIES.contains(receiver)) {
            return "Receiver located in OFAC sanctions list country: " + receiver;
        }
        return "Receiver located in moderate-risk jurisdiction: " + receiver;
    }
}

// BehavioralAnomalyRule.java
@Component
public class BehavioralAnomalyRule implements DetectionRule {
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Override
    public boolean evaluate(Transaction txn) {
        Account account = accountRepository.findById(txn.getSenderAccountId())
            .orElseThrow();
        
        BigDecimal avgAmount = account.getAvgTransactionAmount();
        if (avgAmount == null || avgAmount.equals(BigDecimal.ZERO)) {
            return false; // No history
        }
        
        // 3 standard deviations from mean
        // For MVP: assume std dev = 0.5 * mean
        BigDecimal stdDev = avgAmount.multiply(new BigDecimal("0.5"));
        BigDecimal threshold = avgAmount.add(stdDev.multiply(new BigDecimal("3")));
        
        return txn.getAmount().compareTo(threshold) > 0;
    }
    
    @Override
    public double calculateRiskScore(Transaction txn) {
        if (!evaluate(txn)) return 0;
        
        Account account = accountRepository.findById(txn.getSenderAccountId())
            .orElseThrow();
        
        BigDecimal avgAmount = account.getAvgTransactionAmount();
        double deviation = txn.getAmount().doubleValue() / avgAmount.doubleValue();
        
        // deviation = 1.0 (same as avg) = no risk
        // deviation = 5.0 (5x average) = 50 points
        // deviation = 10.0 (10x average) = 75 points
        return Math.min(100, 20 + ((deviation - 1) * 5));
    }
    
    @Override
    public String getRuleName() {
        return "BEHAVIORAL_ANOMALY";
    }
    
    @Override
    public String getExplanation(Transaction txn) {
        Account account = accountRepository.findById(txn.getSenderAccountId())
            .orElseThrow();
        
        double ratio = txn.getAmount().doubleValue() /
                      account.getAvgTransactionAmount().doubleValue();
        
        return String.format(
            "Transaction amount is %.1fx higher than account average ($ %.2f)",
            ratio, account.getAvgTransactionAmount()
        );
    }
}

// VelocityRule.java
@Component
public class VelocityRule implements DetectionRule {
    @Autowired
    private TransactionRepository transactionRepository;
    
    private static final int THRESHOLD_COUNT = 20;
    private static final int WINDOW_MINUTES = 60;
    
    @Override
    public boolean evaluate(Transaction txn) {
        LocalDateTime oneHourAgo = txn.getTransactionDate().minusMinutes(WINDOW_MINUTES);
        
        long count = transactionRepository
            .findBySenderAccountIdAndTransactionDateAfter(
                txn.getSenderAccountId(),
                oneHourAgo
            )
            .stream()
            .filter(t -> !t.getId().equals(txn.getId()))
            .count();
        
        return count >= THRESHOLD_COUNT;
    }
    
    @Override
    public double calculateRiskScore(Transaction txn) {
        if (!evaluate(txn)) return 0;
        
        long count = countRecentTransactions(txn);
        // Base 40 + 2 per transaction over threshold
        return Math.min(100, 40 + ((count - THRESHOLD_COUNT) * 2));
    }
    
    @Override
    public String getRuleName() {
        return "VELOCITY";
    }
    
    @Override
    public String getExplanation(Transaction txn) {
        long count = countRecentTransactions(txn);
        return String.format(
            "%d transactions from this account in %d minutes (threshold: %d)",
            count, WINDOW_MINUTES, THRESHOLD_COUNT
        );
    }
}

// RuleEngine.java
@Service
public class RuleEngine {
    @Autowired
    private List<DetectionRule> rules;
    
    public RuleResult analyzeTransaction(Transaction txn) {
        List<String> triggeredRules = new ArrayList<>();
        double totalScore = 0;
        
        for (DetectionRule rule : rules) {
            if (rule.evaluate(txn)) {
                triggeredRules.add(rule.getRuleName());
                totalScore += rule.calculateRiskScore(txn);
            }
        }
        
        double avgScore = triggeredRules.isEmpty() ? 0 : (totalScore / triggeredRules.size());
        
        return RuleResult.builder()
            .riskScore(avgScore)
            .triggeredRules(triggeredRules)
            .explanation(buildExplanation(triggeredRules, txn))
            .build();
    }
    
    private String buildExplanation(List<String> rules, Transaction txn) {
        if (rules.isEmpty()) return "No suspicious patterns detected";
        
        StringBuilder sb = new StringBuilder();
        for (String ruleName : rules) {
            DetectionRule rule = findRuleByName(ruleName);
            if (rule != null) {
                sb.append("• ").append(rule.getExplanation(txn)).append("\n");
            }
        }
        return sb.toString();
    }
}
```

### **PART 2: ML CLASSIFIER (Adaptive, Reduces False Positives)**

```java
// FeatureExtractor.java
@Component
public class FeatureExtractor {
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    public double[] extractFeatures(Transaction txn) {
        double[] features = new double[15];
        
        // Feature 1: Transaction Amount (0-1 normalized)
        features[0] = normalizeAmount(txn.getAmount());
        
        // Feature 2: Sender Country Risk Score
        features[1] = getCountryRiskScore(txn.getSenderBankLocation());
        
        // Feature 3: Receiver Country Risk Score
        features[2] = getCountryRiskScore(txn.getReceiverBankLocation());
        
        // Feature 4: Sender Account Age (days, normalized)
        Account senderAccount = accountRepository.findById(txn.getSenderAccountId())
            .orElseThrow();
        features[3] = senderAccount.getAccountAgeDays() / 365.0; // Normalize to years
        
        // Feature 5: Sender Average Transaction Amount
        BigDecimal avgAmount = senderAccount.getAvgTransactionAmount();
        features[4] = avgAmount != null ?
            (txn.getAmount().doubleValue() / avgAmount.doubleValue()) : 1.0;
        
        // Feature 6: Sender Transaction Velocity (txns per month)
        int velocity = senderAccount.getAvgTransactionsPerMonth();
        features[5] = Math.min(velocity / 100.0, 1.0); // Normalized
        
        // Feature 7: Is cross-border?
        features[6] = txn.getSenderBankLocation().equals(txn.getReceiverBankLocation()) ? 0 : 1;
        
        // Feature 8: Currency exchange (different currencies = risky)
        features[7] = txn.getPaymentCurrency().equals(txn.getReceivedCurrency()) ? 0 : 1;
        
        // Feature 9: Time of day (0-23 hours)
        int hour = txn.getTransactionDate().getHour();
        features[8] = (hour >= 23 || hour <= 5) ? 1 : 0; // Off-hour transactions
        
        // Feature 10: Day of week (weekday vs weekend)
        int dayOfWeek = txn.getTransactionDate().getDayOfWeek().getValue();
        features[9] = (dayOfWeek == 6 || dayOfWeek == 7) ? 1 : 0; // Weekend
        
        // Feature 11: Recent transaction count (velocity indicator)
        LocalDateTime lastHour = txn.getTransactionDate().minusHours(1);
        long recentCount = transactionRepository
            .findBySenderAccountIdAndTransactionDateAfter(
                txn.getSenderAccountId(),
                lastHour
            )
            .size();
        features[10] = Math.min(recentCount / 20.0, 1.0);
        
        // Feature 12: Account type (individual = 0, business = 1)
        features[11] = senderAccount.getAccountType().equals("BUSINESS") ? 1 : 0;
        
        // Feature 13: KYC verification status
        // (Would need to fetch from Customer table)
        features[12] = 1.0; // Assume verified for now
        
        // Feature 14: Sender account risk score (from previous analysis)
        features[13] = senderAccount.getAccountRiskScore() / 100.0;
        
        // Feature 15: Receiver account risk score
        Account receiverAccount = accountRepository.findById(txn.getReceiverAccountId())
            .orElseThrow();
        features[14] = receiverAccount.getAccountRiskScore() / 100.0;
        
        return features;
    }
    
    private double normalizeAmount(BigDecimal amount) {
        // Assume max transaction is $1M
        double normalized = amount.doubleValue() / 1_000_000.0;
        return Math.min(normalized, 1.0);
    }
    
    private double getCountryRiskScore(String country) {
        Map<String, Double> riskScores = new HashMap<>();
        riskScores.put("IRAN", 0.95);
        riskScores.put("NORTH_KOREA", 0.95);
        riskScores.put("SYRIA", 0.95);
        riskScores.put("CRIMEA", 0.95);
        riskScores.put("UAE", 0.65);
        riskScores.put("PAKISTAN", 0.60);
        riskScores.put("HONG_KONG", 0.55);
        riskScores.put("UK", 0.20);
        riskScores.put("USA", 0.25);
        riskScores.put("SINGAPORE", 0.30);
        
        return riskScores.getOrDefault(country.toUpperCase(), 0.40);
    }
}

// MLClassifier.java
@Service
public class MLClassifier {
    private RandomForestModel model;
    
    @Autowired
    private FeatureExtractor featureExtractor;
    
    @PostConstruct
    public void loadModel() {
        // For MVP: Load pre-trained model or use mock
        try {
            this.model = ModelLoader.loadRandomForest("models/aml_rf_model.pkl");
        } catch (Exception e) {
            System.out.println("Model not found, using mock model");
            this.model = new MockRandomForestModel();
        }
    }
    
    public double predictSuspicion(Transaction txn) {
        double[] features = featureExtractor.extractFeatures(txn);
        double probability = model.predictProba(features);
        return probability * 100; // Convert to 0-100 scale
    }
}

// MockRandomForestModel.java (For MVP/testing)
public class MockRandomForestModel implements RandomForestModel {
    @Override
    public double predictProba(double[] features) {
        // Simple heuristic for demo
        double score = 0.3; // Base 30%
        
        // Amount (feature 0)
        if (features[0] > 0.5) score += 0.15; // High amount
        
        // Country risk (features 1, 2)
        score += (features[1] * 0.1); // Sender country
        score += (features[2] * 0.2); // Receiver country (more weight)
        
        // Behavioral (features 3, 4, 5)
        if (features[3] < 0.1) score += 0.15; // Very new account
        if (features[4] > 3) score += 0.15; // 3x average amount
        
        // Cross-border + exchange (features 6, 7)
        if (features[6] == 1) score += 0.08;
        if (features[7] == 1) score += 0.08;
        
        // Time anomaly (features 8, 9)
        if (features[8] == 1) score += 0.05; // Off-hours
        
        // Velocity (feature 10)
        score += (features[10] * 0.1);
        
        return Math.min(1.0, score);
    }
}
```

### **PART 3: GNN FRAMEWORK (Future Enhancement - Placeholder)**

```java
// GraphBuilder.java (Placeholder for now)
@Component
public class GraphBuilder {
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    /**
     * Future: Build transaction graph
     * Nodes = Accounts
     * Edges = Transactions
     * Weights = Amount
     */
    public void buildTransactionGraph(LocalDateTime startDate, LocalDateTime endDate) {
        // TODO: Implement in Phase 2
        // 1. Query all accounts
        // 2. Query all transactions in date range
        // 3. Build directed graph
        // 4. Store in Neo4j or similar
    }
    
    /**
     * Future: Detect suspicious network patterns
     * - Hub accounts (many connections)
     * - Circular flows (money washing)
     * - Structuring patterns (small amounts to shell companies)
     */
    public GNNScore analyzeNetworkAnomalies(String accountId) {
        // TODO: Implement in Phase 2
        return new GNNScore(0, "Not implemented yet");
    }
}

// GNNScore.java
@Data
@AllArgsConstructor
public class GNNScore {
    private double riskScore; // 0-100
    private String explanation; // Why this account is risky
}

// TransactionGraph.java (Placeholder)
@Data
public class TransactionGraph {
    private Map<String, AccountNode> nodes;
    private List<TransactionEdge> edges;
    
    @Data
    public static class AccountNode {
        private String accountId;
        private int inDegree; // How many accounts send to this
        private int outDegree; // How many accounts receive from this
        private BigDecimal totalInAmount;
        private BigDecimal totalOutAmount;
    }
    
    @Data
    public static class TransactionEdge {
        private String fromAccount;
        private String toAccount;
        private BigDecimal amount;
        private LocalDateTime timestamp;
    }
}
```

### **ENSEMBLE: Combining All 3**

```java
// EnsembleDetector.java
@Service
public class EnsembleDetector {
    @Autowired
    private RuleEngine ruleEngine;
    
    @Autowired
    private MLClassifier mlClassifier;
    
    @Autowired
    private GraphBuilder graphBuilder; // GNN (placeholder)
    
    @Autowired
    private RiskScoringEngine riskScoringEngine;
    
    public FlaggedTransactionResult detectAnomaly(Transaction txn) {
        // Step 1: Run all 3 detectors in parallel
        RuleResult ruleResult = ruleEngine.analyzeTransaction(txn);
        double mlScore = mlClassifier.predictSuspicion(txn);
        // GNNScore gnnScore = graphBuilder.analyzeNetworkAnomalies(txn.getSenderAccountId());
        // (For now, GNN is placeholder)
        double gnnScore = 0; // Placeholder
        
        // Step 2: Ensemble scoring
        double ensembleScore = (ruleResult.getRiskScore() + mlScore + gnnScore) / 3.0;
        
        // Step 3: Determine risk level
        RiskLevel riskLevel = determineRiskLevel(ensembleScore);
        
        // Step 4: Build explanation
        String explanation = buildDetailedExplanation(
            ruleResult.getTriggeredRules(),
            mlScore,
            gnnScore,
            txn
        );
        
        // Step 5: Return result
        return FlaggedTransactionResult.builder()
            .riskScore(ensembleScore)
            .riskLevel(riskLevel)
            .triggeredRules(ruleResult.getTriggeredRules().stream()
                .toArray(String[]::new))
            .aiExplanation(explanation)
            .aiConfidence(ensembleScore / 100.0)
            .isFlagged(ensembleScore >= 50)
            .build();
    }
    
    private RiskLevel determineRiskLevel(double score) {
        if (score >= 85) return RiskLevel.CRITICAL;
        if (score >= 70) return RiskLevel.HIGH;
        if (score >= 50) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }
    
    private String buildDetailedExplanation(List<String> rules, double mlScore,
                                          double gnnScore, Transaction txn) {
        StringBuilder sb = new StringBuilder();
        
        if (!rules.isEmpty()) {
            sb.append("Rule-based signals:\n");
            for (String rule : rules) {
                sb.append("  • ").append(rule).append("\n");
            }
        }
        
        sb.append("\nMachine Learning Analysis:\n");
        sb.append("  • Probability of fraud: ").append(String.format("%.1f%%", mlScore))
            .append("\n");
        
        if (gnnScore > 0) {
            sb.append("\nNetwork Analysis (GNN):\n");
            sb.append("  • Network risk score: ").append(String.format("%.1f", gnnScore))
                .append("\n");
        }
        
        return sb.toString();
    }
}

// FlaggedTransactionResult.java
@Data
@Builder
public class FlaggedTransactionResult {
    private double riskScore; // 0-100
    private RiskLevel riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
    private String[] triggeredRules;
    private String aiExplanation;
    private double aiConfidence; // 0-1
    private boolean isFlagged;
}
```

---

## 🎤 LLM STRATEGIES (Multiple Options)

### **LLMStrategy Interface**

```java
public interface LLMStrategy {
    String generateReport(Case caseObj);
    String getName();
}

// TemplateBasedStrategy.java (MVP - No API calls)
@Component
public class TemplateBasedStrategy implements LLMStrategy {
    
    @Override
    public String generateReport(Case caseObj) {
        return String.format("""
            SUSPICIOUS ACTIVITY REPORT (SAR)
            ====================================
            
            CASE ID: %s
            Flagged Transaction ID: %s
            Report Generated: %s
            
            TRANSACTION DETAILS
            -------------------
            Amount: $%.2f
            Sender: %s
            Receiver: %s
            Risk Score: %.1f/100
            Risk Level: %s
            
            DETECTED PATTERNS
            -----------------
            %s
            
            CASE CLASSIFICATION
            -------------------
            Case Type: %s
            Priority: %d
            Assigned To: %s
            
            RECOMMENDATIONS
            ----------------
            Based on the detected risk patterns, recommend:
            - MANUAL REVIEW by compliance officer
            - ADDITIONAL DUE DILIGENCE required
            - Further investigation into account history
            
            AI CONFIDENCE LEVEL: HIGH
            
            This report was auto-generated using rule-based detection.
            """,
            caseObj.getId(),
            caseObj.getFlaggedTransactionId(),
            LocalDateTime.now(),
            caseObj.getRiskScore(), // Mock amount
            "Account Hash (Anonymized)",
            "Account Hash (Anonymized)",
            caseObj.getRiskScore(),
            caseObj.getRiskScore() >= 70 ? "HIGH" : "MEDIUM",
            caseObj.getCaseNotes() != null ? caseObj.getCaseNotes() : "N/A",
            caseObj.getCaseType(),
            caseObj.getPriorityLevel(),
            caseObj.getExpertType()
        );
    }
    
    @Override
    public String getName() {
        return "TEMPLATE_BASED";
    }
}

// GeminiLLMStrategy.java (Free tier - generous limits)
@Component
@ConditionalOnProperty(name = "llm.provider", havingValue = "GEMINI")
public class GeminiLLMStrategy implements LLMStrategy {
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Override
    public String generateReport(Case caseObj) {
        String prompt = buildPrompt(caseObj);
        
        // Call Gemini API
        Map<String, Object> request = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt)
                ))
            )
        );
        
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + apiKey;
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            
            // Extract text from response
            List<Map> candidates = (List<Map>) response.getBody().get("candidates");
            Map content = (Map) candidates.get(0).get("content");
            List<Map> parts = (List<Map>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            return "Error generating report: " + e.getMessage();
        }
    }
    
    private String buildPrompt(Case caseObj) {
        return String.format("""
            Generate a professional SAR (Suspicious Activity Report) for this AML case:
            
            Case ID: %s
            Risk Score: %.1f/100
            Case Type: %s
            Priority: %d
            
            Key findings: %s
            
            Format as a formal compliance report with sections:
            1. Executive Summary
            2. Transaction Details
            3. Risk Factors Identified
            4. Recommended Actions
            5. Compliance Notes
            """,
            caseObj.getId(),
            caseObj.getRiskScore(),
            caseObj.getCaseType(),
            caseObj.getPriorityLevel(),
            caseObj.getCaseNotes()
        );
    }
    
    @Override
    public String getName() {
        return "GEMINI";
    }
}

// OpenAIStrategy.java (GPT-3.5-turbo)
@Component
@ConditionalOnProperty(name = "llm.provider", havingValue = "OPENAI")
public class OpenAIStrategy implements LLMStrategy {
    
    @Value("${openai.api.key}")
    private String apiKey;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Override
    public String generateReport(Case caseObj) {
        String prompt = buildPrompt(caseObj);
        
        Map<String, Object> request = Map.of(
            "model", "gpt-3.5-turbo",
            "messages", List.of(
                Map.of("role", "system", "content", "You are a compliance expert writing SAR reports."),
                Map.of("role", "user", "content", prompt)
            ),
            "temperature", 0.7,
            "max_tokens", 1000
        );
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            HttpEntity<Map> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.openai.com/v1/chat/completions",
                HttpMethod.POST,
                entity,
                Map.class
            );
            
            List<Map> choices = (List<Map>) response.getBody().get("choices");
            Map message = (Map) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            return "Error generating report: " + e.getMessage();
        }
    }
    
    private String buildPrompt(Case caseObj) {
        return String.format("""
            Generate a professional SAR report for this case:
            Case ID: %s
            Risk Score: %.1f
            Type: %s
            """,
            caseObj.getId(),
            caseObj.getRiskScore(),
            caseObj.getCaseType()
        );
    }
    
    @Override
    public String getName() {
        return "OPENAI";
    }
}

// ClaudeLLMStrategy.java (Claude API)
@Component
@ConditionalOnProperty(name = "llm.provider", havingValue = "CLAUDE")
public class ClaudeLLMStrategy implements LLMStrategy {
    
    @Value("${claude.api.key}")
    private String apiKey;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Override
    public String generateReport(Case caseObj) {
        // Implementation similar to OpenAI
        // Use Claude API endpoint
        return "Report from Claude";
    }
    
    @Override
    public String getName() {
        return "CLAUDE";
    }
}
```

### **LLM Configuration**

```yaml
# application.yml
llm:
  provider: TEMPLATE  # Options: TEMPLATE, GEMINI, OPENAI, CLAUDE, HUGGINGFACE

gemini:
  api:
    key: ${GEMINI_API_KEY}

openai:
  api:
    key: ${OPENAI_API_KEY}

claude:
  api:
    key: ${CLAUDE_API_KEY}
```

---

## 📊 SUMMARY OF CHANGES

| Component | Change | Impact |
|-----------|--------|--------|
| **DTOs** | Added Request/Response for all endpoints | Clean API contracts |
| **Event-Driven** | Case created as event, not just assignment | Full context to expert |
| **Case Model** | New Case table with full transaction context | Professional gets all info needed |
| **LLM** | 5 strategy implementations (Template MVP → Gemini/GPT) | No Ollama needed |
| **Detection Engine** | All 3 approaches documented with code | Ensemble detection ready |
| **No Docker** | All services run standalone | Deploy when ready |

---

## 🚀 NEXT STEPS

1. **Create Maven structure** from updated guide
2. **Implement all DTOs** in aml-common
3. **Set up Event Bus** for event-driven architecture
4. **Implement Case Management** service with listeners
5. **Add LLM Strategy pattern** with Template (MVP first)
6. **Test with Postman** collection (updated for new endpoints)
7. **Build frontend** for case management UI
8. **Deploy microservices** (no Docker for now)

All code is ready to implement!
