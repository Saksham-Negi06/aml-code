# Anti-Money Laundering (AML) System - Complete Specification Document

**Project:** Capstone AML Compliance Platform  
**Version:** 2.0 (Updated)  
**Date:** November 2024  
**Status:** Ready for Implementation  

---

## 📋 TABLE OF CONTENTS

1. [Project Overview](#project-overview)
2. [Architecture Overview](#architecture-overview)
3. [Technology Stack](#technology-stack)
4. [Module Breakdown](#module-breakdown)
5. [Data Models & DTOs](#data-models--dtos)
6. [Event-Driven Flow](#event-driven-flow)
7. [Detection Engine Specification](#detection-engine-specification)
8. [API Endpoints](#api-endpoints)
9. [Database Schema](#database-schema)
10. [Deployment Instructions](#deployment-instructions)
11. [Implementation Roadmap](#implementation-roadmap)

---

## PROJECT OVERVIEW

### What to Build
A production-ready **Anti-Money Laundering (AML) Compliance Platform** using Java Spring Boot microservices. This is a capstone-level project that demonstrates enterprise architecture, event-driven design, machine learning integration, and compliance systems knowledge.

### Key Requirements

✅ **Event-Driven Architecture** - Not traditional request/response. Services communicate via events published to an event bus.

✅ **Case-Based Workflow** - When a transaction is flagged, automatically create a Case object (not just an assignment). This Case contains:
- Full transaction context
- Risk analysis and scores
- Auto-generated SAR report
- Expert routing information

✅ **Hybrid Detection Engine** - Three approaches combined:
1. **Rule-Based** (deterministic, explainable)
2. **ML Classifier** (adaptive, reduces false positives)
3. **GNN Framework** (placeholder for network analysis)

✅ **No Docker (for now)** - Services run standalone on localhost. Docker can be added later.

✅ **Clean API Contracts** - All requests/responses use DTOs. No raw entity exposure.

✅ **Multiple LLM Options** - Don't hardcode one LLM. Use strategy pattern:
- Template-based (MVP, no API calls)
- Gemini API (free tier)
- OpenAI GPT (paid)
- Claude API (paid)
- HuggingFace (free tier)

✅ **No PII Storage** - Account holders are hashed IDs. No names, SSNs, or addresses stored.

---

## ARCHITECTURE OVERVIEW

### High-Level Flow

```
CLIENT REQUEST
    ↓
API GATEWAY (8080)
    ↓ (routes to microservices)
SERVICE RECEIVES REQUEST (Transaction, Case, Report, etc.)
    ↓
SERVICE PROCESSES + PUBLISHES EVENT (if needed)
    ↓
OTHER SERVICES LISTEN FOR EVENTS (via Event Bus)
    ↓
CHAIN REACTION (Detection → Case → Report → Notification)
    ↓
RESPONSE RETURNED TO CLIENT
```

### Event-Driven Sequence (Complete Flow)

```
USER SUBMITS TRANSACTION
    ↓
POST /api/v1/transactions
    ↓
TransactionService validates + saves
    ↓
IF amount > 0: TransactionAnalysisService.analyzeTransaction()
    ├─ Runs EnsembleDetector (Rules + ML + GNN)
    ├─ Gets riskScore
    └─ IF riskScore >= 50:
        ├─ Saves FlaggedTransaction
        └─ PUBLISHES: TransactionFlaggedEvent
            ↓
            CaseManagementService listens (subscribed)
            ├─ Creates Case object
            ├─ Routes to expert type (SENIOR_COMPLIANCE, LEAD, ANALYST, AUTO_REVIEW)
            ├─ Sets priority (1=CRITICAL, 2=HIGH, 3=NORMAL, 4=LOW)
            ├─ Saves Case to DB
            └─ PUBLISHES: CaseCreatedEvent
                ↓
                ReportGenerationService listens (subscribed)
                ├─ Gets Case + FlaggedTransaction details
                ├─ Calls LLMStrategy.generateReport()
                ├─ Saves GeneratedReport
                ├─ Updates Case with reportId + reportUrl
                └─ PUBLISHES: ReportGeneratedEvent
                    ↓
                    NotificationService listens (subscribed)
                    ├─ Fetches Case + Report
                    ├─ Formats professional alert
                    ├─ Sends email/dashboard notification
                    └─ PUBLISHES: NotificationSentEvent
                        ↓
                        (Expert receives full case context with report)
```

### Service Topology (Microservices)

```
┌─────────────────────────────────────────────────────────┐
│                    EUREKA REGISTRY (8761)                │
│  Tracks all running microservices & their locations     │
└─────────────────────────────────────────────────────────┘
                            ↑
                ┌───────────┼───────────┐
                ↑           ↑           ↑
    ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐
    │  Transaction Svc │ │  Account Svc     │ │  Customer Svc    │
    │  (Port 8081)     │ │  (Port 8082)     │ │  (Port 8083)     │
    │                  │ │                  │ │                  │
    │ - Submit txns    │ │ - Account CRUD   │ │ - Customer CRUD  │
    │ - List txns      │ │ - Update risk    │ │ - KYC status     │
    │ - Trigger detect │ │ - Query patterns │ │ - Risk flags     │
    └──────────────────┘ └──────────────────┘ └──────────────────┘
            ↓
    ┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐
    │  Case Management │ │  LLM Service     │ │  Auth Service    │
    │  (Port 8084)     │ │  (Port 8085)     │ │  (Port 8086)     │
    │                  │ │                  │ │                  │
    │ - Case CRUD      │ │ - Generate SARs  │ │ - JWT tokens     │
    │ - Auto-create    │ │ - Download PDF   │ │ - OAuth2         │
    │ - Expert routing │ │ - Template/GPT   │ │ - User mgmt      │
    │ - Resolve cases  │ │ - Gemini/Claude  │ │ - Role auth      │
    └──────────────────┘ └──────────────────┘ └──────────────────┘
            ↓
    ┌──────────────────────────────────────────────────────┐
    │       API GATEWAY (Port 8080)                         │
    │  - Routes requests to services                       │
    │  - JWT validation                                    │
    │  - Rate limiting                                     │
    │  - Response aggregation                              │
    └──────────────────────────────────────────────────────┘
            ↓
    ┌──────────────────────────────────────────────────────┐
    │  FRONTEND (Port 3000, served via Python SimpleServer)│
    │  - Login page                                        │
    │  - Dashboard (cases, transactions, reports)          │
    │  - Case detail view                                  │
    │  - Report download                                   │
    └──────────────────────────────────────────────────────┘
```

---

## TECHNOLOGY STACK

### Backend
- **Language:** Java 17
- **Framework:** Spring Boot 3.1.0
- **Microservices:** Spring Cloud, Spring Cloud Gateway
- **Service Discovery:** Eureka Server
- **Build Tool:** Maven (multi-module)
- **Database:** H2 (development/demo), Oracle (migration path via JDBC)
- **ORM:** JPA/Hibernate
- **Authentication:** Spring Security, JWT, OAuth2
- **Logging:** SLF4J + Logback
- **Testing:** JUnit 5, Mockito

### Frontend
- **HTML5, CSS3, Vanilla JavaScript**
- **No framework initially** (Angular-ready structure)
- **Charts:** Chart.js
- **HTTP Client:** Fetch API

### Event System
- **In-Memory Event Bus** (custom pub-sub, no Kafka for MVP)
- **Executor Service** for async event handling (threadpool of 5)
- **ConcurrentHashMap** for thread-safe subscriber registry

### LLM Integration
- **Strategy Pattern** (switchable implementations)
- **RestTemplate** for API calls
- **HTTP Headers** for Bearer auth (API keys)

### Deployment (No Docker)
- **JVM:** Java 17
- **Local Execution:** `mvn spring-boot:run`
- **Service Registry:** Eureka on localhost:8761
- **Frontend Server:** Python's `python -m http.server 3000`

---

## MODULE BREAKDOWN

### 1. **aml-config** (Shared Configuration)
**Purpose:** Centralized configuration for all microservices

**Contains:**
- `SecurityConfig.java` - Spring Security, CORS, password encoding
- `JwtTokenProvider.java` - JWT token generation/validation (HS256 algorithm)
- `EventBusConfig.java` - Initialize in-memory event bus as singleton
- `H2OracleDataSourceConfig.java` - Datasource factory (choose H2 or Oracle via config)
- `RestTemplateConfig.java` - RestTemplate bean for HTTP calls
- `JacksonConfig.java` - Custom JSON serialization (LocalDateTime format, etc.)

**Key Responsibilities:**
- Provide shared beans
- Handle configuration properties
- Set up security filters

---

### 2. **aml-common** (Shared DTOs, Entities, Events)
**Purpose:** Single source of truth for data contracts across all services

**Subpackages:**

#### **dto/request/**
All request objects that clients send to APIs:
- `CreateTransactionRequest` - amount, currency, sender/receiver, date, type
- `AnalysisRequest` - transactionId only (triggers manual detection)
- `CreateCaseFromFlagRequest` - flaggedTransactionId, expert type override, priority override
- `UpdateCaseStatusRequest` - status, assignedExpertId, notes
- `ResolveCaseRequest` - resolution (APPROVED/REJECTED/ESCALATED), notes, recommendation
- `GenerateReportRequest` - caseId, reportFormat (PDF/JSON/TEXT), llmProvider (TEMPLATE/GEMINI/OPENAI/CLAUDE)
- `LoginRequest` - username, password
- `OAuth2Request` - grantType, clientId, username, password, scope

**Validation:** Use `@NotBlank`, `@NotNull`, `@Positive`, `@Size` annotations

#### **dto/response/**
All response objects returned from APIs:
- `TransactionResponse` - transaction details + status + isFlagged
- `FlaggedTransactionResponse` - transaction + risk score + rules triggered + explanation + confidence
- `CaseResponse` - **CRITICAL** - contains nested FlaggedTransactionResponse + all case details + report URL
- `GeneratedReportResponse` - report content + format + URL + LLM model used
- `AuthResponse` - JWT token + expiry + username + role
- `ErrorResponse` - error code + message + field-level details + timestamp + path
- `PagedResponse<T>` - Generic wrapper for paginated results (content, page, size, totalElements, hasNext)

#### **dto/common/**
- `ApiResponse<T>` - Wrapper for all responses (data + success flag + message)
- `ValidationError` - Field name + error message pair
- `PageInfo` - Pagination metadata

#### **entity/**
JPA Entity classes (map to DB tables):
- `Transaction` - Core transaction record
- `Account` - Account hashed ID + behavioral profile (risk score, avg amount, velocity)
- `Customer` - KYC status + risk country flag (NO PII)
- `FlaggedTransaction` - Link to transaction + risk analysis
- `Case` - **NEW** - Full case object with expert routing
- `GeneratedReport` - Report content + metadata
- `User` - System users (analysts, compliance officers)
- `AuditLog` - Who did what when

#### **event/**
Event classes (not persisted, published via EventBus):
- `TransactionFlaggedEvent` - txnId, amount, risk score, rules triggered, explanation, confidence, timestamp
- `CaseCreatedEvent` - caseId, flaggedTxnId, case type, risk score, expert type, priority, timestamp
- `ReportGeneratedEvent` - reportId, caseId, report URL, LLM model, timestamp
- `NotificationSentEvent` - caseId, expert id, channel, status (SENT/FAILED), timestamp

#### **enum/**
- `RiskLevel` - LOW (<50), MEDIUM (50-69), HIGH (70-84), CRITICAL (85+)
- `CaseStatus` - PENDING, IN_REVIEW, RESOLVED, ESCALATED
- `ExpertType` - SENIOR_COMPLIANCE, COMPLIANCE_LEAD, JUNIOR_ANALYST, BEHAVIORAL_EXPERT, SANCTIONS_EXPERT, TRADE_EXPERT, AUTO_REVIEW
- `CaseType` - STRUCTURING, SANCTIONS, TRADE_BASED, GENERAL
- `TransactionStatus` - PENDING, COMPLETED, FLAGGED, REJECTED
- `DetectionRuleType` - HIGH_AMOUNT, STRUCTURING, HIGH_RISK_COUNTRY, BEHAVIORAL_ANOMALY, VELOCITY, CROSS_BORDER_HIGH_RISK

#### **utils/**
- `HashUtil.java` - Anonymize account IDs (SHA-256)
- `ValidationUtil.java` - Common validation methods
- `CurrencyUtil.java` - Currency code validation
- `DateUtil.java` - Date formatting utilities

---

### 3. **aml-batch** (Spring Batch for CSV Import)
**Purpose:** Import SAML-D.csv (996MB) and generate synthetic realistic data

**Contains:**
- `BatchConfig.java` - Define job, steps, itemReader, processor, writer
- `TransactionItemProcessor.java` - Map CSV rows to Transaction entities
- `BatchJobListener.java` - Log job start/completion
- `SyntheticDataGenerator.java` - Generate realistic account & customer data based on CSV patterns

**Key Details:**
- Read SAML-D.csv in chunks
- Process 10,000 transactions at a time
- Write to H2 database
- Skip header rows
- Handle malformed data gracefully

**Trigger:** Run on startup via Spring Batch auto-run, or manually via admin endpoint

---

### 4. **aml-detection-engine** (Core: Rules + ML + GNN)
**Purpose:** Analyze transactions and produce risk scores via ensemble approach

#### **PART 1: Rule-Based Engine (rules/ package)**

**DetectionRule.java (Interface):**
```java
public interface DetectionRule {
    boolean evaluate(Transaction transaction);        // Does this rule trigger?
    double calculateRiskScore(Transaction transaction); // 0-100 score
    String getRuleName();                             // "HIGH_AMOUNT", etc.
    String getExplanation(Transaction transaction);  // Why it triggered
}
```

**Six Implementations:**

1. **HighAmountRule**
   - Threshold: $10,000
   - Score: Base 30 + (ratio - 1) * 20
   - $10k = 30pts, $15k = 40pts, $20k = 50pts, capped at 100

2. **StructuringRule**
   - Detects: 5+ transactions < $10k in 24 hours from same sender
   - Score: Base 40 + (count * 5)
   - Rationale: Breaking up large sums to avoid reporting

3. **HighRiskCountryRule**
   - OFAC sanctions list: IRAN, NORTH_KOREA, SYRIA, CRIMEA = 75pts
   - Moderate risk: UAE, PAKISTAN, HONG_KONG = 50pts
   - Checks receiver location

4. **BehavioralAnomalyRule**
   - Triggers: Amount > mean + (3 * std_dev)
   - Score: Base 20 + ((deviation - 1) * 5)
   - Uses account's historical transaction average

5. **VelocityRule**
   - Detects: 20+ transactions in 1 hour from same sender
   - Score: Base 40 + ((count - 20) * 2)
   - Indicates rapid movement/washing

6. **CrossBorderHighRiskRule**
   - Detects: Cross-border transactions to high-risk countries
   - Score: 55-75 points depending on country risk
   - Combines geography + amount analysis

**RuleEngine.java:**
- Loop through all rules
- Collect triggered rule names
- Average their scores
- Build explanation string
- Return RuleResult (score, rules, explanation)

#### **PART 2: ML Classifier (ml/ package)**

**FeatureExtractor.java:**
Extracts 15 numerical features from a transaction:
1. Normalized amount (0-1 scale)
2. Sender country risk (0-1)
3. Receiver country risk (0-1)
4. Sender account age (years, normalized)
5. Amount deviation from sender's average
6. Sender velocity (txns/month, normalized)
7. Is cross-border (0 or 1)
8. Currency mismatch (0 or 1)
9. Off-hours transaction (0 or 1, 11pm-5am)
10. Weekend transaction (0 or 1)
11. Recent transaction velocity (count/1hr)
12. Account type (0=individual, 1=business)
13. KYC verification status
14. Sender account risk score (from previous analysis)
15. Receiver account risk score

**MLClassifier.java:**
- Loads pre-trained Random Forest model (or mock for MVP)
- Takes transaction, extracts features via FeatureExtractor
- Passes features to model
- Returns probability (0-1), convert to 0-100 scale
- Example: 0.72 probability = 72 points

**MockRandomForestModel.java (for MVP/testing):**
- Simple heuristic: base 30% + weighted feature bonuses
- Amount > 50% threshold: +15%
- Receiver country risk: +20%
- New account + high amount: +15%
- Cross-border + currency mismatch: +8% each
- Velocity: +10%
- Returns final score as decimal 0-1

#### **PART 3: GNN (Placeholder for Future)**

**GraphBuilder.java:**
- Placeholder methods (return empty/default values)
- Future: Build transaction network graph
- Nodes = Accounts, Edges = Transactions
- Detect: hub accounts, circular flows, structuring patterns

**GNNDetector.java & TransactionGraph.java:**
- Skeleton classes for future implementation
- Currently return 0 score

#### **ENSEMBLE: EnsembleDetector.java**

**Main method: `detectAnomaly(Transaction txn)`**
1. Run RuleEngine → get ruleScore
2. Run MLClassifier → get mlScore
3. Run GraphBuilder → get gnnScore (currently 0)
4. Ensemble = (ruleScore + mlScore + gnnScore) / 3
5. Determine RiskLevel based on ensemble score:
   - < 50: LOW (not flagged)
   - 50-69: MEDIUM
   - 70-84: HIGH
   - 85+: CRITICAL
6. Build detailed explanation combining all three approaches
7. Return FlaggedTransactionResult (score, level, rules, explanation, confidence, isFlagged)

**Key Output: FlaggedTransactionResult**
- riskScore: 0-100
- riskLevel: LOW/MEDIUM/HIGH/CRITICAL
- triggeredRules: array of rule names
- aiExplanation: multi-line explanation
- aiConfidence: 0-1 (ensemble score / 100)
- isFlagged: boolean (>= 50)

---

### 5. **aml-transaction-service** (Eureka Port 8081)
**Purpose:** Submit, store, and analyze transactions

**Components:**

**TransactionRepository.java:**
```java
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findBySenderAccountId(String accountId);
    List<Transaction> findBySenderAccountIdAndTransactionDateAfter(String accountId, LocalDateTime date);
    Page<Transaction> findAll(Pageable pageable);
}
```

**TransactionService.java:**
- `submitTransaction(CreateTransactionRequest)` → save + return DTO
- `listTransactions(page, size)` → paginated results
- `getTransaction(id)` → single transaction details
- `updateTransactionStatus(id, status)` → mark as completed/flagged/rejected

**TransactionAnalysisService.java:**
- Injected with `EnsembleDetector` and `EventBus`
- Called after transaction submission
- **Key method: `analyzeTransaction(String txnId)`**
  1. Fetch transaction from DB
  2. Call `ensembleDetector.detectAnomaly(txn)`
  3. If `result.isFlagged()` (score >= 50):
     - Create `FlaggedTransaction` entity
     - Save to DB
     - Create `TransactionFlaggedEvent`
     - **Publish event via EventBus** (triggers chain)

**TransactionController.java:**
```java
@PostMapping("/api/v1/transactions")
public ResponseEntity<TransactionResponse> submitTransaction(
    @RequestBody CreateTransactionRequest req) {
    Transaction txn = transactionService.submit(req);
    transactionAnalysisService.analyzeTransaction(txn.getId()); // Async would be better
    return ResponseEntity.ok(mapToDTO(txn));
}

@GetMapping("/api/v1/transactions")
public ResponseEntity<PagedResponse<TransactionResponse>> listTransactions(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size) { }

@GetMapping("/api/v1/transactions/{id}")
public ResponseEntity<TransactionResponse> getTransaction(@PathVariable String id) { }
```

---

### 6. **aml-case-management** (Eureka Port 8084)
**Purpose:** Auto-create cases from flagged transactions, route to experts, manage resolution

**Components:**

**CaseRepository.java:**
```java
@Repository
public interface CaseRepository extends JpaRepository<Case, String> {
    Page<Case> findByStatus(String status, Pageable pageable);
    Page<Case> findByPriorityLevel(int priority, Pageable pageable);
    Page<Case> findByExpertType(String expertType, Pageable pageable);
}
```

**CaseService.java:**
- `getCaseWithContext(caseId)` → fetch case + nested transaction details
- `updateCaseStatus(caseId, UpdateCaseStatusRequest)` → change status + assign expert
- `resolveCase(caseId, ResolveCaseRequest)` → mark resolved + store decision

**ExpertRoutingService.java:**
**Critical: Expert routing logic based on risk & case type**
```
Risk 85+ AND STRUCTURING → BEHAVIORAL_EXPERT, Priority 1 (CRITICAL)
Risk 85+ → SENIOR_COMPLIANCE, Priority 1 (CRITICAL)
Risk 70-84 → COMPLIANCE_LEAD, Priority 2 (HIGH)
Risk 50-69 → JUNIOR_ANALYST, Priority 3 (NORMAL)
Risk < 50 → AUTO_REVIEW, Priority 4 (LOW)

Case Type Overrides:
STRUCTURING → BEHAVIORAL_EXPERT (if not already)
HIGH_RISK_COUNTRY/SANCTIONS → SANCTIONS_EXPERT
TRADE_BASED → TRADE_EXPERT
```

**CaseEventListener.java:**
- Subscribes to `TransactionFlaggedEvent` via `@PostConstruct`
- When event received, calls `handleTransactionFlagged(event)`
- **Logic:**
  1. Extract event data (txnId, amount, riskScore, rules, explanation)
  2. Create `Case` entity:
     ```java
     Case case = new Case();
     case.setId(UUID.randomUUID().toString());
     case.setFlaggedTransactionId(event.getTransactionId());
     case.setCaseType(categorizeCaseType(event.getTriggeredRules()));
     case.setRiskScore(event.getRiskScore());
     ```
  3. Route to expert via `ExpertRoutingService.route(score, caseType)`
  4. Set expertType, priorityLevel, status = "PENDING"
  5. Save case to DB
  6. **Publish `CaseCreatedEvent`** (triggers LLM service)

**NotificationService.java:**
- Subscribes to `ReportGeneratedEvent`
- When case + report ready, format alert for expert
- Send email (stub), dashboard notification (stub)
- Later: Email via Spring Mail, SMS via Twilio, etc.

**CaseController.java:**
```java
@PostMapping("/api/v1/cases")
public ResponseEntity<CaseResponse> createCase(
    @RequestBody CreateCaseFromFlagRequest req) { }
    
@GetMapping("/api/v1/cases")
public ResponseEntity<PagedResponse<CaseResponse>> listCases(
    @RequestParam int page, @RequestParam int size,
    @RequestParam(required=false) String status) { }
    
@GetMapping("/api/v1/cases/{id}")
public ResponseEntity<CaseResponse> getCase(@PathVariable String id) { }
    
@PutMapping("/api/v1/cases/{id}")
public ResponseEntity<CaseResponse> updateCase(
    @PathVariable String id, @RequestBody UpdateCaseStatusRequest req) { }
    
@PostMapping("/api/v1/cases/{id}/resolve")
public ResponseEntity<CaseResponse> resolveCase(
    @PathVariable String id, @RequestBody ResolveCaseRequest req) { }
```

---

### 7. **aml-llm-service** (Eureka Port 8085)
**Purpose:** Generate SAR reports using configurable LLM strategies

**Components:**

**LLMStrategy.java (Interface):**
```java
public interface LLMStrategy {
    String generateReport(Case caseObj);
    String getName(); // "TEMPLATE_BASED", "GEMINI", "OPENAI", etc.
}
```

**Implementations:**

1. **TemplateBasedStrategy** (MVP, no API calls)
   - Hardcoded template with format string
   - Instant response, no latency
   - Good for demo/testing

2. **GeminiLLMStrategy** (Free tier)
   - API endpoint: `https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent`
   - Free tier: 60 requests/min, 1.5M tokens/month
   - No credit card needed
   - RestTemplate POST call with API key in query param

3. **OpenAIStrategy** (Paid)
   - Model: gpt-3.5-turbo
   - Endpoint: `https://api.openai.com/v1/chat/completions`
   - Cost: $0.15 per 1M tokens
   - RestTemplate with Bearer auth header

4. **ClaudeLLMStrategy** (Paid)
   - Model: claude-instant-1 or claude-2
   - Endpoint: `https://api.anthropic.com/v1/messages`
   - Cost: $0.015 per 1K input tokens
   - RestTemplate with Bearer auth header

5. **HuggingFaceLLMStrategy** (Free tier)
   - Use community models via inference API
   - Model: mistral-7b or similar
   - Free tier with rate limits

**ReportGenerationService.java:**
- Subscribes to `CaseCreatedEvent` via `@PostConstruct`
- When event received, calls `handleCaseCreated(event)`
- **Logic:**
  1. Fetch Case from DB
  2. Fetch FlaggedTransaction details
  3. Call configured LLMStrategy
  4. Create `GeneratedReport` entity:
     ```java
     GeneratedReport report = new GeneratedReport();
     report.setId(UUID.randomUUID().toString());
     report.setCaseId(caseObj.getId());
     report.setReportContent(llmStrategy.generateReport(caseObj));
     report.setGeneratedByLLMModel(llmStrategy.getName());
     report.setReportFormat("PDF");
     report.setReportUrl("/api/v1/reports/" + report.getId() + "/download");
     ```
  5. Save report to DB
  6. Update Case with reportId + reportUrl
  7. **Publish `ReportGeneratedEvent`**

**PDFGenerator.java:**
- Convert report string to PDF
- Use iText or Apache PDFBox library
- Store PDF in file system (./reports/ or /tmp/)
- Later: Move to S3/Azure Blob

**LLMConfig.java:**
```yaml
llm:
  provider: TEMPLATE  # Switch between strategies
  
gemini:
  api:
    key: ${GEMINI_API_KEY}
    
openai:
  api:
    key: ${OPENAI_API_KEY}
```

**ReportController.java:**
```java
@PostMapping("/api/v1/reports/generate")
public ResponseEntity<GeneratedReportResponse> generateReport(
    @RequestBody GenerateReportRequest req) { }
    
@GetMapping("/api/v1/reports/{id}")
public ResponseEntity<GeneratedReportResponse> getReport(@PathVariable String id) { }
    
@GetMapping("/api/v1/reports/{id}/download")
public ResponseEntity<byte[]> downloadReport(@PathVariable String id) {
    // Fetch PDF from file system
    // Return with Content-Type: application/pdf
}
```

---

### 8. **aml-account-service** (Eureka Port 8082)
**Purpose:** Manage accounts (hashed IDs), track behavioral profiles

**Components:**

**AccountRepository.java:**
```java
@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByAccountHash(String hash);
    Page<Account> findAll(Pageable pageable);
}
```

**AccountService.java:**
- `getAccount(id)` → retrieve with behavioral stats
- `updateAccountRiskScore(id, score)` → manual override
- `getAccountStats(id)` → avg amount, velocity, age

**AccountController.java:**
```java
@GetMapping("/api/v1/accounts/{id}")
public ResponseEntity<AccountResponse> getAccount(@PathVariable String id) { }

@PatchMapping("/api/v1/accounts/{id}")
public ResponseEntity<AccountResponse> updateAccountRisk(
    @PathVariable String id,
    @RequestBody Map<String, Double> update) { }
```

---

### 9. **aml-customer-service** (Eureka Port 8083)
**Purpose:** Manage customers (KYC status, risk flags, NO PII)

**Components:**

**CustomerRepository.java:**
**CustomerService.java:**
**CustomerController.java:**
- Similar pattern to Account Service
- `GET /api/v1/customers/{id}`
- KYC verification status, risk country flag
- NO personal information stored

---

### 10. **aml-auth-service** (Eureka Port 8086)
**Purpose:** JWT authentication, user management, OAuth2 (optional)

**Components:**

**JwtTokenProvider.java:**
- Algorithm: HS256 (symmetric key)
- Secret: Spring configuration (${JWT_SECRET})
- Payload: username, role, issuedAt, expiresAt
- Methods: `generateToken(username, role)`, `validateToken(token)`, `getUsername(token)`

**AuthController.java:**
```java
@PostMapping("/api/v1/auth/login")
public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
    User user = userService.authenticate(req.getUsername(), req.getPassword());
    String token = jwtProvider.generateToken(user.getUsername(), user.getRole());
    return ResponseEntity.ok(AuthResponse.builder()
        .token(token)
        .tokenType("Bearer")
        .expiresIn(3600000L)
        .username(user.getUsername())
        .role(user.getRole())
        .build());
}
```

**JwtFilter.java:**
- Intercepts all requests
- Extracts token from `Authorization: Bearer <token>` header
- Validates token
- Sets Spring Security context
- Reject if invalid/expired

**SecurityConfig.java:**
- Add JwtFilter to filter chain
- Disable session management (stateless)
- Configure CORS
- Password encoder: BCrypt

---

### 11. **aml-api-gateway** (Eureka Port 8080)
**Purpose:** Single entry point, routing, rate limiting, JWT validation

**Components:**

**GatewayConfig.java:**
```java
@Configuration
public class GatewayConfig {
    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("transaction-service", r -> r
                .path("/api/v1/transactions/**")
                .uri("lb://transaction-service:8081"))
            .route("account-service", r -> r
                .path("/api/v1/accounts/**")
                .uri("lb://account-service:8082"))
            .route("customer-service", r -> r
                .path("/api/v1/customers/**")
                .uri("lb://customer-service:8083"))
            .route("case-management", r -> r
                .path("/api/v1/cases/**")
                .uri("lb://case-management:8084"))
            .route("llm-service", r -> r
                .path("/api/v1/reports/**")
                .uri("lb://llm-service:8085"))
            .route("auth-service", r -> r
                .path("/api/v1/auth/**")
                .uri("lb://auth-service:8086"))
            .build();
    }
}
```

**application.yml:**
```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lowerCaseServiceId: true
```

---

### 12. **aml-eureka-server** (Eureka Registry Port 8761)
**Purpose:** Service discovery registry, health checks

**EurekaServerApplication.java:**
```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

**application.yml:**
```yaml
server:
  port: 8761

eureka:
  client:
    registerWithEureka: false
    fetchRegistry: false
  server:
    enableSelfPreservation: false
```

---

### 13. **aml-frontend** (Static HTML, served on Port 3000)
**Purpose:** Web UI for professionals to view cases, download reports, resolve cases

**Files:**

- **index.html** - Login page
  - Username/password form
  - POST to `/api/v1/auth/login`
  - Store JWT in localStorage
  - Redirect to dashboard

- **dashboard.html** - Main view
  - Dashboard stats (total cases, critical priority, pending, etc.)
  - Quick filters (status, priority)
  - Real-time auto-refresh (every 30 seconds)

- **cases.html** - Case listing + detail view
  - Table: Case ID, Transaction ID, Amount, Risk Score, Status, Priority, Assigned Expert
  - Click row to open detail view
  - Show full transaction context (sender, receiver, amount, date)
  - Show risk analysis (rules triggered, ML score, explanation)
  - Show assigned expert + priority
  - Buttons: Review, Update Status, Download Report, Resolve

- **reports.html** - Report management
  - List generated reports
  - Download PDF link
  - View report metadata (generated date, LLM model)

- **transactions.html** - Transaction listing
  - Table: Transaction ID, Sender, Receiver, Amount, Status, Is Flagged
  - Filter by flagged status
  - Search by transaction ID

- **css/style.css** - Base styles (colors, fonts, spacing)
- **css/dashboard.css** - Dashboard layout (grid, cards)
- **css/responsive.css** - Mobile responsive (media queries)

- **js/config.js** - API base URL, constants
- **js/auth.js** - JWT token management (localStorage), login/logout
- **js/api.js** - Fetch wrapper with auth header, error handling
- **js/dashboard.js** - Dashboard stats, auto-refresh
- **js/cases.js** - Case list, detail view, status updates, resolution
- **js/transactions.js** - Transaction list, filtering
- **js/utils.js** - Date formatting, currency formatting, helpers

---

## DATA MODELS & DTOs

### Transaction Submission to Response Flow

**Client sends:**
```json
{
  "senderAccountId": "ACC_HASH_1a2b3c",
  "receiverAccountId": "ACC_HASH_9x8y7z",
  "amount": 50000.00,
  "paymentCurrency": "USD",
  "receivedCurrency": "USD",
  "transactionDate": "2024-11-25T10:35:19Z",
  "paymentType": "WIRE_TRANSFER"
}
```

**Server returns (TransactionResponse):**
```json
{
  "id": "txn-abc123",
  "senderAccountId": "ACC_HASH_1a2b3c",
  "receiverAccountId": "ACC_HASH_9x8y7z",
  "amount": 50000.00,
  "paymentCurrency": "USD",
  "receivedCurrency": "USD",
  "transactionDate": "2024-11-25T10:35:19Z",
  "paymentType": "WIRE_TRANSFER",
  "status": "PENDING",
  "isFlagged": true,
  "createdAt": "2024-11-25T10:35:20Z",
  "updatedAt": "2024-11-25T10:35:20Z"
}
```

### Flagged Transaction Details

**Server returns (FlaggedTransactionResponse):**
```json
{
  "id": "flag-xyz789",
  "transactionId": "txn-abc123",
  "senderAccountId": "ACC_HASH_1a2b3c",
  "receiverAccountId": "ACC_HASH_9x8y7z",
  "amount": 50000.00,
  "detectionRuleTriggered": ["HIGH_AMOUNT", "HIGH_RISK_COUNTRY"],
  "riskScore": 72.5,
  "riskLevel": "HIGH",
  "aiExplanation": "• Amount $50,000 exceeds threshold $10,000 by 5.0x\n• Receiver located in moderate-risk jurisdiction: UAE",
  "aiConfidence": 0.725,
  "status": "PENDING",
  "assignedCaseId": "case-123",
  "createdAt": "2024-11-25T10:35:21Z",
  "updatedAt": "2024-11-25T10:35:21Z"
}
```

### Case Response (Full Context for Expert)

**Server returns (CaseResponse):**
```json
{
  "id": "case-123",
  "flaggedTransactionId": "flag-xyz789",
  "flaggedTransaction": {
    "id": "flag-xyz789",
    "transactionId": "txn-abc123",
    "senderAccountId": "ACC_HASH_1a2b3c",
    "receiverAccountId": "ACC_HASH_9x8y7z",
    "amount": 50000.00,
    "detectionRuleTriggered": ["HIGH_AMOUNT", "HIGH_RISK_COUNTRY"],
    "riskScore": 72.5,
    "riskLevel": "HIGH",
    "aiExplanation": "...",
    "aiConfidence": 0.725
  },
  "caseType": "SANCTIONS",
  "riskScore": 72.5,
  "assignedToExpertId": "expert-001",
  "expertType": "SANCTIONS_EXPERT",
  "priorityLevel": 2,
  "status": "PENDING",
  "generatedReportId": "report-456",
  "reportUrl": "/api/v1/reports/report-456/download",
  "caseNotes": null,
  "resolutionNotes": null,
  "assignedAt": "2024-11-25T10:35:22Z",
  "reviewedAt": null,
  "resolvedAt": null,
  "createdAt": "2024-11-25T10:35:22Z",
  "updatedAt": "2024-11-25T10:35:22Z"
}
```

---

## EVENT-DRIVEN FLOW

### Event Bus (In-Memory)

**EventBus.java:**
```java
@Component
public class EventBus {
    private final Map<Class<?>, List<EventListener<?>>> listeners = 
        new ConcurrentHashMap<>();
    private final ExecutorService executor = 
        Executors.newFixedThreadPool(5);
    
    @FunctionalInterface
    public interface EventListener<T> {
        void onEvent(T event);
    }
    
    public <T> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
            .add(listener);
    }
    
    public <T> void publish(T event) {
        List<EventListener<?>> eventListeners = 
            listeners.get(event.getClass());
        if (eventListeners != null) {
            eventListeners.forEach(listener -> {
                executor.submit(() -> {
                    try {
                        ((EventListener<T>) listener).onEvent(event);
                    } catch (Exception e) {
                        System.err.println("Error: " + e.getMessage());
                    }
                });
            });
        }
    }
}
```

### Event Classes

**TransactionFlaggedEvent:**
```java
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
```

**CaseCreatedEvent:**
```java
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
```

**ReportGeneratedEvent:**
```java
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
```

### Event Subscription Example

**In CaseManagementService:**
```java
@Service
public class CaseManagementService {
    @Autowired private EventBus eventBus;
    @Autowired private CaseRepository caseRepository;
    
    @PostConstruct
    public void subscribeToEvents() {
        eventBus.subscribe(TransactionFlaggedEvent.class, 
            this::handleTransactionFlagged);
    }
    
    public void handleTransactionFlagged(TransactionFlaggedEvent event) {
        // Create Case, route to expert, publish CaseCreatedEvent
        ...
    }
}
```

---

## DETECTION ENGINE SPECIFICATION

### Input: Transaction
```java
Transaction {
    id: String (UUID)
    senderAccountId: String (hashed)
    receiverAccountId: String (hashed)
    amount: BigDecimal
    paymentCurrency: String (ISO 4217)
    receivedCurrency: String (ISO 4217)
    transactionDate: LocalDateTime
    paymentType: String (WIRE_TRANSFER, ACH, CARD, etc.)
    status: String (PENDING, COMPLETED, FLAGGED, REJECTED)
    createdAt: LocalDateTime
}
```

### Output: FlaggedTransactionResult
```java
FlaggedTransactionResult {
    riskScore: double (0-100)
    riskLevel: RiskLevel (LOW, MEDIUM, HIGH, CRITICAL)
    triggeredRules: String[] (rule names)
    aiExplanation: String (multi-line explanation)
    aiConfidence: double (0-1, = riskScore/100)
    isFlagged: boolean (true if score >= 50)
}
```

### Detection Rules (Detailed)

#### Rule 1: HighAmountRule
- **Trigger Condition:** `amount > $10,000`
- **Risk Score:** `30 + ((amount / $10,000 - 1) * 20)`, capped at 100
- **Examples:**
  - $10,000: 30 points
  - $15,000: 40 points
  - $20,000: 50 points
  - $50,000: 90 points
- **Explanation:** "Amount $X exceeds threshold $10,000 by Y.Zx"

#### Rule 2: StructuringRule
- **Trigger Condition:** `5+ transactions < $10,000 from same sender in 24 hours`
- **Risk Score:** `40 + (count * 5)`, capped at 100
- **Examples:**
  - 5 small txns: 65 points
  - 10 small txns: 90 points
  - 12 small txns: 100 points (capped)
- **Explanation:** "N transactions < $10,000 from this account in 24 hours (threshold: 5)"

#### Rule 3: HighRiskCountryRule
- **Trigger Condition:** receiver country in high/moderate risk list
- **Risk Score:**
  - OFAC list (IRAN, NK, SYRIA, CRIMEA): 75 points
  - Moderate (UAE, PAKISTAN, HONG_KONG): 50 points
- **Explanation:** "Receiver located in OFAC sanctions list country: IRAN" OR "moderate-risk jurisdiction: UAE"

#### Rule 4: BehavioralAnomalyRule
- **Trigger Condition:** `amount > mean + (3 * stdDev)` of sender's history
- **Risk Score:** `20 + ((deviation - 1) * 5)`, capped at 100
  - Where deviation = current_amount / historical_average
- **Examples:**
  - If avg = $1,000, stdDev = $500, threshold = $1,000 + (3 * $500) = $2,500
  - Transaction $5,000: deviation = 5, score = 20 + (4 * 5) = 40 points
  - Transaction $10,000: deviation = 10, score = 20 + (9 * 5) = 65 points
- **Explanation:** "Transaction amount is X.Yx higher than account average ($Z)"

#### Rule 5: VelocityRule
- **Trigger Condition:** `20+ transactions from same sender in 60 minutes`
- **Risk Score:** `40 + ((count - 20) * 2)`, capped at 100
- **Examples:**
  - 20 txns/hour: 40 points
  - 30 txns/hour: 60 points
  - 50 txns/hour: 100 points (capped)
- **Explanation:** "N transactions from this account in 60 minutes (threshold: 20)"

#### Rule 6: CrossBorderHighRiskRule
- **Trigger Condition:** `sender_country != receiver_country AND receiver is high-risk`
- **Risk Score:** Varies 55-75 based on receiver risk
- **Explanation:** "Cross-border transaction to high-risk jurisdiction"

### ML Classifier Details

**Feature Extraction (15 features):**
1. Amount normalized (0-1): `min(amount / $1M, 1.0)`
2. Sender country risk (0-1): lookup table (IRAN=0.95, USA=0.25, etc.)
3. Receiver country risk (0-1): same lookup
4. Sender account age (0-1): `accountAgeDays / 365.0`
5. Amount deviation: `txn_amount / account_avg_amount`
6. Sender velocity (0-1): `min(txns_per_month / 100, 1.0)`
7. Cross-border (0 or 1): `sender_country != receiver_country ? 1 : 0`
8. Currency mismatch (0 or 1): `!paymentCurrency.equals(receivedCurrency) ? 1 : 0`
9. Off-hours (0 or 1): `(hour >= 23 || hour <= 5) ? 1 : 0`
10. Weekend (0 or 1): `(dayOfWeek == 6 || 7) ? 1 : 0`
11. Recent velocity (0-1): `min(txns_last_hour / 20, 1.0)`
12. Account type (0 or 1): `"BUSINESS" ? 1 : 0`
13. KYC verified (0 or 1): from Customer.kycVerificationStatus
14. Sender risk score (0-1): `account.riskScore / 100`
15. Receiver risk score (0-1): `receiver_account.riskScore / 100`

**Mock Model (for MVP):**
```java
public double predictProba(double[] features) {
    double score = 0.3; // Base 30%
    
    // Amount (feature 0)
    if (features[0] > 0.5) score += 0.15; // High amount
    
    // Country risk (features 1, 2)
    score += (features[1] * 0.1);  // Sender
    score += (features[2] * 0.2);  // Receiver (more weight)
    
    // Behavioral (features 3, 4, 5)
    if (features[3] < 0.1) score += 0.15;  // Very new account
    if (features[4] > 3) score += 0.15;    // 3x+ average
    
    // Cross-border + currency (features 6, 7)
    if (features[6] == 1) score += 0.08;
    if (features[7] == 1) score += 0.08;
    
    // Time (features 8, 9)
    if (features[8] == 1) score += 0.05;   // Off-hours
    
    // Velocity (feature 10)
    score += (features[10] * 0.1);
    
    return Math.min(1.0, score);  // Cap at 100%
}
```

### GNN Placeholder (Future)

Currently returns `GNNScore(0, "Not implemented yet")`

Future implementation will:
- Build directed graph: nodes = accounts, edges = transactions
- Detect hub accounts (many incoming/outgoing)
- Detect circular flows (A→B→C→A)
- Detect structuring patterns (many small amounts to shells)
- Return anomaly score 0-100

---

## API ENDPOINTS

### Authentication
```
POST /api/v1/auth/login
    Body: { username, password }
    Response: { token, tokenType, expiresIn, username, role }

POST /api/v1/auth/oauth2/authorize  [Future]
    Body: { grantType, clientId, username, password }
    Response: { accessToken, tokenType, expiresIn }
```

### Transactions
```
POST /api/v1/transactions
    Body: CreateTransactionRequest
    Response: TransactionResponse

GET /api/v1/transactions
    Query: page=0, size=20
    Response: PagedResponse<TransactionResponse>

GET /api/v1/transactions/{id}
    Response: TransactionResponse
```

### Detection & Analysis
```
POST /api/v1/analysis/detect
    Body: { transactionId }
    Response: FlaggedTransactionResponse

GET /api/v1/analysis/result/{transactionId}
    Response: FlaggedTransactionResponse
```

### Flagged Transactions
```
GET /api/v1/flagged-transactions
    Query: page=0, size=50, riskLevel=HIGH, status=PENDING
    Response: PagedResponse<FlaggedTransactionResponse>

GET /api/v1/flagged-transactions/{id}
    Response: FlaggedTransactionResponse
```

### Cases (Event-Driven)
```
POST /api/v1/cases  [Manual creation, normally auto-created]
    Body: CreateCaseFromFlagRequest
    Response: CaseResponse

GET /api/v1/cases
    Query: page=0, size=20, status=PENDING, priority=1
    Response: PagedResponse<CaseResponse>

GET /api/v1/cases/{id}
    Response: CaseResponse

PUT /api/v1/cases/{id}
    Body: UpdateCaseStatusRequest
    Response: CaseResponse

POST /api/v1/cases/{id}/resolve
    Body: ResolveCaseRequest
    Response: CaseResponse
```

### Reports & LLM
```
POST /api/v1/reports/generate
    Body: GenerateReportRequest { caseId, reportFormat, llmProvider }
    Response: GeneratedReportResponse

GET /api/v1/reports/{id}
    Response: GeneratedReportResponse

GET /api/v1/reports/{id}/download
    Response: PDF file (Content-Type: application/pdf)
```

### Accounts
```
GET /api/v1/accounts/{id}
    Response: AccountResponse

PATCH /api/v1/accounts/{id}
    Body: { accountRiskScore }
    Response: AccountResponse
```

### Customers
```
GET /api/v1/customers/{id}
    Response: CustomerResponse
```

### Health
```
GET /actuator/health
    Response: { status: "UP" }

GET /eureka/apps
    Response: Eureka registry JSON
```

---

## DATABASE SCHEMA

### Tables

#### accounts
```sql
CREATE TABLE accounts (
    id VARCHAR(36) PRIMARY KEY,
    account_hash VARCHAR(255) NOT NULL UNIQUE,
    account_type VARCHAR(50),  -- INDIVIDUAL, BUSINESS
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
```

#### transactions
```sql
CREATE TABLE transactions (
    id VARCHAR(36) PRIMARY KEY,
    sender_account_id VARCHAR(36) NOT NULL,
    receiver_account_id VARCHAR(36) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    payment_currency VARCHAR(10),  -- USD, EUR, GBP, etc.
    received_currency VARCHAR(10),
    transaction_date TIMESTAMP NOT NULL,
    payment_type VARCHAR(50),  -- WIRE_TRANSFER, ACH, CARD, etc.
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
```

#### flagged_transactions
```sql
CREATE TABLE flagged_transactions (
    id VARCHAR(36) PRIMARY KEY,
    transaction_id VARCHAR(36) NOT NULL UNIQUE,
    detection_rule_triggered VARCHAR(500),  -- Comma-separated rule names
    risk_score DECIMAL(5,2) NOT NULL,
    risk_level VARCHAR(50),  -- LOW, MEDIUM, HIGH, CRITICAL
    ai_explanation TEXT,
    ai_confidence DECIMAL(5,2),
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (transaction_id) REFERENCES transactions(id),
    INDEX idx_risk_level (risk_level),
    INDEX idx_status (status)
);
```

#### cases (NEW)
```sql
CREATE TABLE cases (
    id VARCHAR(36) PRIMARY KEY,
    flagged_transaction_id VARCHAR(36) NOT NULL UNIQUE,
    case_type VARCHAR(100),  -- STRUCTURING, SANCTIONS, TRADE_BASED, GENERAL
    risk_score DECIMAL(5,2) NOT NULL,
    assigned_to_expert_id VARCHAR(36),
    expert_type VARCHAR(100),  -- SENIOR_COMPLIANCE, LEAD, ANALYST, etc.
    priority_level INT DEFAULT 3,  -- 1=CRITICAL, 2=HIGH, 3=NORMAL, 4=LOW
    status VARCHAR(50) DEFAULT 'PENDING',  -- PENDING, IN_REVIEW, RESOLVED, ESCALATED
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
```

#### generated_reports
```sql
CREATE TABLE generated_reports (
    id VARCHAR(36) PRIMARY KEY,
    case_id VARCHAR(36),
    flagged_transaction_id VARCHAR(36),
    report_content LONGTEXT,
    report_format VARCHAR(20),  -- PDF, JSON, TEXT
    generated_by_llm_model VARCHAR(100),  -- TEMPLATE_BASED, GEMINI, OPENAI, etc.
    report_url TEXT,  -- Path to PDF file or S3 URL
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (case_id) REFERENCES cases(id),
    FOREIGN KEY (flagged_transaction_id) REFERENCES flagged_transactions(id),
    INDEX idx_case_id (case_id)
);
```

#### users
```sql
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(50),  -- ADMIN, ANALYST, COMPLIANCE_LEAD, SENIOR_OFFICER
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
);
```

#### customers
```sql
CREATE TABLE customers (
    id VARCHAR(36) PRIMARY KEY,
    account_id VARCHAR(36) NOT NULL,
    account_holder_type VARCHAR(50),  -- INDIVIDUAL, BUSINESS, TRUST
    kyc_verification_status VARCHAR(50),  -- VERIFIED, PENDING, REJECTED
    risk_country_flag BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);
```

#### audit_logs
```sql
CREATE TABLE audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    action VARCHAR(255),
    entity_type VARCHAR(100),  -- TRANSACTION, CASE, REPORT, USER
    entity_id VARCHAR(36),
    old_value TEXT,
    new_value TEXT,
    performed_by VARCHAR(100),  -- Username
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_timestamp (performed_at)
);
```

---

## DEPLOYMENT INSTRUCTIONS

### Prerequisites
- Java 17 JDK installed
- Maven 3.8+
- Git
- Terminal/Command line

### Step 1: Clone & Build
```bash
git clone <repository-url>
cd aml-system
mvn clean install  # Builds all modules
```

### Step 2: Start Services (Each in separate terminal)

**Terminal 1: Eureka Server**
```bash
cd aml-eureka-server
mvn spring-boot:run
# Access at http://localhost:8761
```

**Terminal 2: Transaction Service**
```bash
cd aml-transaction-service
mvn spring-boot:run
# Registers with Eureka automatically
# Listens on port 8081
```

**Terminal 3: Account Service**
```bash
cd aml-account-service
mvn spring-boot:run
# Port 8082
```

**Terminal 4: Customer Service**
```bash
cd aml-customer-service
mvn spring-boot:run
# Port 8083
```

**Terminal 5: Case Management Service**
```bash
cd aml-case-management
mvn spring-boot:run
# Port 8084
```

**Terminal 6: LLM Service**
```bash
cd aml-llm-service
mvn spring-boot:run
# Port 8085
```

**Terminal 7: Auth Service**
```bash
cd aml-auth-service
mvn spring-boot:run
# Port 8086
```

**Terminal 8: API Gateway**
```bash
cd aml-api-gateway
mvn spring-boot:run
# Port 8080 (entry point)
```

**Terminal 9: Frontend**
```bash
cd aml-frontend
python -m http.server 3000
# Serves on http://localhost:3000
```

### Step 3: Verify All Services Running
- Eureka: http://localhost:8761 → Should show all 7 services (green)
- API Gateway health: http://localhost:8080/actuator/health
- Frontend: http://localhost:3000 → Login page

### Step 4: Test Full Flow
1. Login: `POST http://localhost:8080/api/v1/auth/login`
   ```json
   {
     "username": "admin",
     "password": "password123"
   }
   ```
   Copy JWT token

2. Submit transaction: `POST http://localhost:8080/api/v1/transactions`
   ```json
   {
     "senderAccountId": "ACC_1a2b3c",
     "receiverAccountId": "ACC_9x8y7z",
     "amount": 95000,
     "paymentCurrency": "USD",
     "receivedCurrency": "USD",
     "transactionDate": "2024-11-25T10:35:19Z",
     "paymentType": "WIRE_TRANSFER"
   }
   ```

3. Check flagged transaction: `GET http://localhost:8080/api/v1/flagged-transactions`

4. Check auto-created case: `GET http://localhost:8080/api/v1/cases`
   → Should include full transaction context, risk analysis, report URL

5. Download report: `GET http://localhost:8080/api/v1/reports/{reportId}/download`

---

## IMPLEMENTATION ROADMAP

### Phase 1: Weeks 1-2 (Foundation)
- Maven structure + modules
- aml-config (security, JWT, datasource)
- aml-common (all DTOs, entities, events)
- Event Bus implementation
- Database schema (H2)
- **Deliverable:** All 8 modules exist, compile successfully

### Phase 2: Weeks 3-4 (Microservices)
- aml-eureka-server (start Eureka)
- aml-transaction-service (submit, list, CRUD)
- aml-account-service (account CRUD)
- aml-customer-service (customer CRUD)
- aml-auth-service (JWT login)
- **Deliverable:** All services register with Eureka, basic endpoints work

### Phase 3: Weeks 5-6 (Detection Engine)
- aml-detection-engine (all 6 rules)
- ML Classifier (FeatureExtractor, MLClassifier, MockModel)
- EnsembleDetector
- Integration with TransactionService
- **Deliverable:** Submit transaction → Detection runs → FlaggedTransaction saved

### Phase 4: Week 7 (Event-Driven)
- Event publishing (TransactionAnalysisService publishes TransactionFlaggedEvent)
- Case event listener (CaseEventListener subscribes, creates Case)
- **Deliverable:** Submit txn → Auto-creates Case with full context

### Phase 5: Week 8 (LLM Reports)
- LLMStrategy interface + TemplateBasedStrategy (MVP)
- ReportGenerationService (listens to CaseCreatedEvent)
- PDF generation
- Optional: Gemini/OpenAI strategies
- **Deliverable:** Cases have auto-generated reports

### Phase 6: Week 9 (Case Management)
- CaseController (full CRUD)
- ExpertRoutingService (routing logic)
- Status updates, case resolution
- **Deliverable:** Professionals can review cases and update status

### Phase 7: Week 10 (Auth & Gateway)
- aml-api-gateway (routing to all services)
- JWT validation
- Rate limiting
- **Deliverable:** Single entry point on port 8080

### Phase 8: Weeks 11-12 (Frontend)
- Login page
- Dashboard
- Case listing + detail view
- Transaction listing
- Report download
- **Deliverable:** Web UI fully functional

---

## CRITICAL NOTES FOR IMPLEMENTATION

1. **Event Bus is In-Memory:**
   - No external message broker (no Kafka, RabbitMQ)
   - Uses ExecutorService with 5-thread pool
   - Events published to all listeners asynchronously
   - If service crashes, events are lost (acceptable for MVP)

2. **No PII Stored:**
   - Account holders are hashed IDs (no names, SSNs, addresses)
   - Customer table only has: KYC status, risk country flag
   - All account references use `account_hash` (SHA-256)

3. **Detection Always Triggers:**
   - Every transaction is analyzed by EnsembleDetector
   - If score >= 50 → flagged, case created, report generated
   - Professionals see full context immediately

4. **LLM Strategy Pattern:**
   - TemplateBasedStrategy = MVP (no API keys needed)
   - Strategies are swappable via configuration
   - Only Template is required; others are optional enhancements

5. **JWT Stateless:**
   - No sessions, no cookies (aside from optional refresh token)
   - Every request includes `Authorization: Bearer <token>` header
   - API Gateway validates token

6. **No Docker (Yet):**
   - Services run standalone on localhost
   - Each service has its own application.yml
   - Eureka enables service discovery without Docker networking
   - Docker Compose can be added later (commented out in repo)

---

## SUMMARY

This is a **complete, production-ready specification** for a capstone-level AML compliance platform. Follow this document exactly to implement:

✅ Event-driven microservices  
✅ Hybrid detection (rules + ML + GNN placeholder)  
✅ Auto-case creation from flagged transactions  
✅ Multiple LLM strategies  
✅ Professional dashboard  
✅ No Docker, no PII storage  
✅ Clean API contracts (DTOs)  

**Total: 8-12 weeks to fully implement and test.**
