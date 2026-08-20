# AML System Implementation Roadmap - UPDATED

**Timeline:** 8-12 weeks | Difficulty: Intermediate to Advanced | No Docker Required

---

## 🎯 KEY CHANGES FROM V1

✅ **DTOs for all endpoints** - Clean request/response contracts  
✅ **Event-Driven Architecture** - Events published, services listen  
✅ **Case as first-class object** - Not just assignment, full context  
✅ **Multiple LLM strategies** - Template MVP → Gemini/GPT later  
✅ **Detection engine fully coded** - Rules + ML + GNN framework  
✅ **No Docker** - Run services standalone for now  

---

## PHASE 1: Foundation & Setup (Weeks 1-2)

### Goals
- Create Maven multi-module structure
- Set up shared modules (config, common with DTOs)
- Implement Event Bus (in-memory)
- Create database schema

### Tasks

#### 1.1 Maven Structure

```bash
mvn archetype:generate \
  -DgroupId=com.aml \
  -DartifactId=aml-system \
  -DarchetypeArtifactId=maven-archetype-quickstart

cd aml-system
```

**Parent pom.xml** (from guide):
- Add Spring Boot 3.1.0
- Add Spring Cloud dependencies
- Define all modules

#### 1.2 Create aml-config Module

```
aml-config/src/main/java/config/
├── KafkaConfig.java (In-memory for MVP)
├── SecurityConfig.java (Spring Security)
├── JwtTokenProvider.java (JWT generation/validation)
├── H2OracleDataSourceConfig.java (Datasource switching)
└── EventBusConfig.java (NEW: Event bus initialization)
```

**Key:** Set up in-memory Kafka/Event Bus for MVP (no external broker needed)

#### 1.3 Create aml-common Module (DTOs)

```
aml-common/src/main/java/com/aml/common/
├── dto/request/ (All *Request classes)
├── dto/response/ (All *Response classes)
├── dto/common/ (ApiResponse, ValidationError, PagedResponse)
├── entity/ (JPA entities for DB mapping)
├── event/ (Event classes for publishing)
├── enums/ (RiskLevel, CaseStatus, ExpertType, etc.)
└── utils/ (HashUtil, ValidationUtil)
```

**Critical DTOs to implement:**
- `CreateTransactionRequest` / `TransactionResponse`
- `AnalysisRequest` / `FlaggedTransactionResponse`
- `CreateCaseFromFlagRequest` / `CaseResponse` (NEW)
- `GenerateReportRequest` / `GeneratedReportResponse`
- `UpdateCaseStatusRequest` / `ResolveCaseRequest` (NEW)

#### 1.4 Create Event Bus

```java
@Component
public class EventBus {
    private Map<Class<?>, List<EventListener<?>>> listeners;
    private ExecutorService executor;
    
    public <T> void subscribe(Class<T> eventType, EventListener<T> listener) { }
    public <T> void publish(T event) { }
}
```

#### 1.5 Create Database Schema

From updated guide:
- `accounts` table
- `transactions` table
- `flagged_transactions` table
- `cases` table (NEW)
- `generated_reports` table
- `users` table
- `audit_logs` table

Run via:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create
```

OR manually with H2 console at `http://localhost:8080/h2-console`

### Testing
- ✓ All DTOs compile and validate
- ✓ Event Bus can publish/subscribe
- ✓ Database schema created in H2
- ✓ JWT config loads

---

## PHASE 2: Eureka & Core Microservices (Weeks 3-4)

### Goals
- Start Eureka server
- Create 6 microservices with basic CRUD
- Connect all services to Eureka
- Test service discovery

### Tasks

#### 2.1 Create aml-eureka-server

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

Start first: `mvn spring-boot:run`

#### 2.2 Create aml-transaction-service (Port 8081)

**TransactionRepository:**
```java
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findBySenderAccountId(String senderAccountId);
    Page<Transaction> findAll(Pageable pageable);
}
```

**TransactionService:**
```java
@Service
public class TransactionService {
    public Transaction submitTransaction(CreateTransactionRequest req) { }
    public Page<Transaction> listTransactions(int page, int size) { }
    public Transaction getTransaction(String id) { }
}
```

**TransactionController:**
```java
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    @PostMapping
    public ResponseEntity<TransactionResponse> submit(@RequestBody CreateTransactionRequest req) { }
    
    @GetMapping
    public ResponseEntity<PagedResponse<TransactionResponse>> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) { }
    
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> get(@PathVariable String id) { }
}
```

#### 2.3 Create Other Services (Same Pattern)

**aml-account-service (8082)**
```
AccountRepository → AccountService → AccountController
```

**aml-customer-service (8083)**
```
CustomerRepository → CustomerService → CustomerController
```

**aml-auth-service (8086)**
```
UserRepository → UserService → AuthController (JWT + OAuth2)
```

#### 2.4 Register with Eureka

Each service **application.yml:**
```yaml
spring:
  application:
    name: transaction-service

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
  instance:
    preferIpAddress: true
```

### Testing
```bash
# Terminal 1: Eureka
cd aml-eureka-server && mvn spring-boot:run

# Terminal 2-7: Services
cd aml-transaction-service && mvn spring-boot:run
cd aml-account-service && mvn spring-boot:run
# ... etc

# Browser: http://localhost:8761
# Should show all 6 services registered
```

---

## PHASE 3: Detection Engine (Weeks 5-6)

### Goals
- Implement all 6 rule-based detection rules
- Add ML classifier wrapper (mock for MVP)
- Create ensemble detector
- Test with sample transactions

### Tasks

#### 3.1 Create aml-detection-engine Module

**Structure:**
```
src/main/java/com/aml/detection/
├── rules/
│   ├── DetectionRule.java (Interface)
│   ├── HighAmountRule.java
│   ├── StructuringRule.java
│   ├── HighRiskCountryRule.java
│   ├── BehavioralAnomalyRule.java
│   ├── VelocityRule.java
│   └── CrossBorderHighRiskRule.java
├── engine/
│   ├── RuleEngine.java
│   └── RiskScoringEngine.java
├── ml/
│   ├── FeatureExtractor.java
│   ├── MLClassifier.java
│   ├── RandomForestModel.java (Interface)
│   └── MockRandomForestModel.java
├── ensemble/
│   ├── EnsembleDetector.java
│   └── DetectionResultFormatter.java
└── gnn/ (Placeholder for future)
    ├── GraphBuilder.java
    └── GNNDetector.java
```

#### 3.2 Implement Rule Engine

From updated guide - copy all rule implementations:
- `HighAmountRule` - detects > $10k
- `StructuringRule` - detects 5+ small txns in 24h
- `HighRiskCountryRule` - checks OFAC list
- `BehavioralAnomalyRule` - detects deviation from avg
- `VelocityRule` - detects rapid transactions
- `CrossBorderHighRiskRule` - cross-border + high-risk combo

#### 3.3 Implement ML Classifier

```java
@Component
public class MLClassifier {
    @Autowired private FeatureExtractor featureExtractor;
    
    @PostConstruct
    public void loadModel() {
        try {
            model = ModelLoader.load("models/rf.pkl");
        } catch (Exception e) {
            model = new MockRandomForestModel();
        }
    }
    
    public double predictSuspicion(Transaction txn) { }
}
```

#### 3.4 Create Ensemble Detector

```java
@Service
public class EnsembleDetector {
    @Autowired private RuleEngine ruleEngine;
    @Autowired private MLClassifier mlClassifier;
    
    public FlaggedTransactionResult detectAnomaly(Transaction txn) {
        RuleResult rules = ruleEngine.analyze(txn);
        double ml = mlClassifier.predict(txn);
        double gnn = 0; // Placeholder
        
        double ensemble = (rules + ml + gnn) / 3;
        return formatResult(ensemble, ...);
    }
}
```

### Testing
```java
@Test
public void testHighAmountRule() {
    Transaction txn = new Transaction();
    txn.setAmount(new BigDecimal("50000"));
    assertTrue(highAmountRule.evaluate(txn));
    assertTrue(highAmountRule.calculateRiskScore(txn) >= 40);
}

@Test
public void testEnsembleDetection() {
    Transaction txn = createSampleTransaction();
    FlaggedTransactionResult result = ensembleDetector.detectAnomaly(txn);
    assertTrue(result.isFlagged());
    assertTrue(result.getRiskScore() >= 50);
}
```

---

## PHASE 4: Event-Driven Architecture (Weeks 7)

### Goals
- Create event classes
- Add event publishing to detection engine
- Add event listeners to case management
- Test event flow

### Tasks

#### 4.1 Create Event Classes

```java
// In aml-common/event/
public class TransactionFlaggedEvent { }
public class CaseCreatedEvent { }
public class ReportGeneratedEvent { }
public class NotificationSentEvent { }
```

#### 4.2 Add Event Publishing

**TransactionAnalysisService:**
```java
@Service
public class TransactionAnalysisService {
    @Autowired private EnsembleDetector detector;
    @Autowired private EventBus eventBus;
    @Autowired private FlaggedTransactionRepository flagRepo;
    
    public void analyzeTransaction(String txnId) {
        Transaction txn = txnRepo.findById(txnId).orElseThrow();
        FlaggedTransactionResult result = detector.detectAnomaly(txn);
        
        if (result.isFlagged()) {
            FlaggedTransaction flagged = saveFlagged(txn, result);
            
            // PUBLISH EVENT
            TransactionFlaggedEvent event = new TransactionFlaggedEvent(
                txn.getId(),
                txn.getSenderAccountId(),
                txn.getReceiverAccountId(),
                txn.getAmount(),
                result.getRiskScore(),
                result.getTriggeredRules(),
                result.getAiExplanation(),
                result.getAiConfidence(),
                LocalDateTime.now(),
                UUID.randomUUID().toString()
            );
            
            eventBus.publish(event);
        }
    }
}
```

#### 4.3 Add Event Listener (Case Management)

**CaseManagementService:**
```java
@Service
public class CaseManagementService {
    @Autowired private EventBus eventBus;
    @Autowired private CaseRepository caseRepo;
    
    @PostConstruct
    public void subscribeToEvents() {
        eventBus.subscribe(TransactionFlaggedEvent.class, 
            this::handleTransactionFlagged);
    }
    
    public void handleTransactionFlagged(TransactionFlaggedEvent event) {
        // Create Case object
        Case caseObj = new Case();
        caseObj.setId(UUID.randomUUID().toString());
        caseObj.setFlaggedTransactionId(event.getTransactionId());
        caseObj.setCaseType(categorizeCase(event.getTriggeredRules()));
        caseObj.setRiskScore(event.getRiskScore());
        
        // Route to expert
        ExpertRouting routing = routeToExpert(event.getRiskScore(), 
                                               caseObj.getCaseType());
        caseObj.setExpertType(routing.getExpertType());
        caseObj.setPriorityLevel(routing.getPriorityLevel());
        caseObj.setStatus("PENDING");
        caseObj.setAssignedAt(LocalDateTime.now());
        
        caseRepo.save(caseObj);
        
        // PUBLISH CASE CREATED EVENT
        eventBus.publish(new CaseCreatedEvent(
            caseObj.getId(),
            caseObj.getFlaggedTransactionId(),
            caseObj.getCaseType(),
            caseObj.getRiskScore(),
            caseObj.getExpertType(),
            caseObj.getPriorityLevel(),
            caseObj.getAssignedToExpertId(),
            LocalDateTime.now(),
            UUID.randomUUID().toString()
        ));
    }
}
```

### Testing
```bash
# Submit transaction via API
POST /api/v1/transactions

# Check if flagged transaction created
GET /api/v1/flagged-transactions

# Check if case auto-created
GET /api/v1/cases
# Should see Case with full transaction details
```

---

## PHASE 5: LLM Report Generation (Week 8)

### Goals
- Implement LLMStrategy interface
- Create Template-based strategy (MVP)
- Add Gemini/OpenAI strategies (optional)
- Test report generation

### Tasks

#### 5.1 Create LLM Strategies

```
aml-llm-service/src/main/java/com/aml/llm/service/
├── LLMStrategy.java (Interface)
├── impl/
│   ├── TemplateBasedStrategy.java (MVP)
│   ├── GeminiLLMStrategy.java (Optional)
│   ├── OpenAIStrategy.java (Optional)
│   ├── ClaudeLLMStrategy.java (Optional)
│   └── HuggingFaceLLMStrategy.java (Optional)
```

#### 5.2 MVP: Template-Based Strategy

```java
@Component
public class TemplateBasedStrategy implements LLMStrategy {
    @Override
    public String generateReport(Case caseObj) {
        return String.format("""
            SUSPICIOUS ACTIVITY REPORT
            ============================
            Case ID: %s
            Risk Score: %.1f/100
            Case Type: %s
            Assigned To: %s
            
            Detected Patterns:
            %s
            
            Recommendation: MANUAL_REVIEW
            """,
            caseObj.getId(),
            caseObj.getRiskScore(),
            caseObj.getCaseType(),
            caseObj.getExpertType(),
            caseObj.getCaseNotes()
        );
    }
    
    @Override
    public String getName() {
        return "TEMPLATE_BASED";
    }
}
```

#### 5.3 Add Event Listener (LLM Service)

```java
@Service
public class ReportGenerationService {
    @Autowired private EventBus eventBus;
    @Autowired private LLMStrategy llmStrategy;
    @Autowired private ReportRepository reportRepo;
    
    @PostConstruct
    public void subscribeToEvents() {
        eventBus.subscribe(CaseCreatedEvent.class, 
            this::handleCaseCreated);
    }
    
    public void handleCaseCreated(CaseCreatedEvent event) {
        Case caseObj = caseRepo.findById(event.getCaseId()).orElseThrow();
        
        // Generate report
        String reportContent = llmStrategy.generateReport(caseObj);
        
        // Save report
        GeneratedReport report = new GeneratedReport();
        report.setId(UUID.randomUUID().toString());
        report.setCaseId(caseObj.getId());
        report.setReportContent(reportContent);
        report.setGeneratedByLLMModel(llmStrategy.getName());
        report.setReportUrl("/api/v1/reports/" + report.getId() + "/download");
        
        reportRepo.save(report);
        caseObj.setGeneratedReportId(report.getId());
        caseRepository.save(caseObj);
        
        // PUBLISH REPORT GENERATED EVENT
        eventBus.publish(new ReportGeneratedEvent(...));
    }
}
```

### Testing
```bash
# In Phase 4, case should auto-generate report
# Check case response:
GET /api/v1/cases/case-123

# Response should include:
{
  "id": "case-123",
  "generatedReportId": "report-456",
  "reportUrl": "/api/v1/reports/report-456/download"
}

# Download report:
GET /api/v1/reports/report-456/download
```

---

## PHASE 6: Case Management Controller (Week 9)

### Goals
- Implement CaseController with full CRUD
- Add case routing logic
- Implement status update endpoints
- Add case resolution logic

### Tasks

#### 6.1 Case Management Service

```java
@Service
public class CaseService {
    @Autowired private CaseRepository caseRepo;
    @Autowired private ExpertRoutingService routingService;
    
    public CaseResponse getCaseWithContext(String caseId) {
        Case caseObj = caseRepo.findById(caseId).orElseThrow();
        
        // Fetch full transaction context
        FlaggedTransaction flagged = flagRepo.findById(
            caseObj.getFlaggedTransactionId()).orElseThrow();
        Transaction txn = txnRepo.findById(flagged.getTransactionId())
            .orElseThrow();
        
        // Build response with all details
        return CaseResponse.builder()
            .id(caseObj.getId())
            .flaggedTransactionId(caseObj.getFlaggedTransactionId())
            .flaggedTransaction(mapToDTO(flagged))
            .caseType(caseObj.getCaseType())
            .riskScore(caseObj.getRiskScore())
            .assignedToExpertId(caseObj.getAssignedToExpertId())
            .expertType(caseObj.getExpertType())
            .priorityLevel(caseObj.getPriorityLevel())
            .status(caseObj.getStatus())
            .generatedReportId(caseObj.getGeneratedReportId())
            .reportUrl(caseObj.getReportUrl())
            .build();
    }
    
    public CaseResponse updateCaseStatus(String caseId, 
                                        UpdateCaseStatusRequest req) {
        Case caseObj = caseRepo.findById(caseId).orElseThrow();
        caseObj.setStatus(req.getStatus());
        caseObj.setAssignedToExpertId(req.getAssignedToExpertId());
        caseObj.setCaseNotes(req.getNotes());
        caseObj.setUpdatedAt(LocalDateTime.now());
        
        if ("IN_REVIEW".equals(req.getStatus())) {
            caseObj.setReviewedAt(LocalDateTime.now());
        }
        
        caseRepo.save(caseObj);
        return getCaseWithContext(caseId);
    }
    
    public CaseResponse resolveCase(String caseId, 
                                    ResolveCaseRequest req) {
        Case caseObj = caseRepo.findById(caseId).orElseThrow();
        caseObj.setStatus("RESOLVED");
        caseObj.setResolutionNotes(req.getResolutionNotes());
        caseObj.setResolvedAt(LocalDateTime.now());
        
        caseRepo.save(caseObj);
        return getCaseWithContext(caseId);
    }
}
```

#### 6.2 Case Controller

```java
@RestController
@RequestMapping("/api/v1/cases")
public class CaseController {
    @Autowired private CaseService caseService;
    
    @PostMapping
    public ResponseEntity<CaseResponse> createCase(
        @RequestBody CreateCaseFromFlagRequest req) {
        // For manual case creation (if not auto-created)
    }
    
    @GetMapping
    public ResponseEntity<PagedResponse<CaseResponse>> listCases(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String status) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Case> cases = status != null ? 
            caseRepo.findByStatus(status, pageable) :
            caseRepo.findAll(pageable);
        
        return ResponseEntity.ok(PagedResponse.builder()
            .content(cases.getContent().stream()
                .map(caseService::getCaseWithContext)
                .toList())
            .page(page)
            .size(size)
            .totalElements(cases.getTotalElements())
            .totalPages(cases.getTotalPages())
            .build());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CaseResponse> getCase(@PathVariable String id) {
        return ResponseEntity.ok(caseService.getCaseWithContext(id));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CaseResponse> updateCase(
        @PathVariable String id,
        @RequestBody UpdateCaseStatusRequest req) {
        return ResponseEntity.ok(caseService.updateCaseStatus(id, req));
    }
    
    @PostMapping("/{id}/resolve")
    public ResponseEntity<CaseResponse> resolveCase(
        @PathVariable String id,
        @RequestBody ResolveCaseRequest req) {
        return ResponseEntity.ok(caseService.resolveCase(id, req));
    }
}
```

### Testing
```bash
# List cases (from previous phase)
GET /api/v1/cases

# Get case details
GET /api/v1/cases/case-123

# Update status
PUT /api/v1/cases/case-123
{
  "status": "IN_REVIEW",
  "assignedToExpertId": "expert-001",
  "notes": "Assigned to John"
}

# Resolve case
POST /api/v1/cases/case-123/resolve
{
  "resolution": "APPROVED",
  "resolutionNotes": "Verified legitimate transaction",
  "recommendation": "FALSE_POSITIVE"
}
```

---

## PHASE 7: Authentication & API Gateway (Week 10)

### Goals
- Implement JWT authentication
- Create API Gateway with routing
- Add rate limiting
- Test secured endpoints

### Tasks

#### 7.1 JWT Authentication

From updated guide - implement in aml-auth-service:
- `JwtTokenProvider` - generate/validate tokens
- `JwtFilter` - filter for all requests
- `SecurityConfig` - Spring Security setup
- `AuthController` - login endpoint

**AuthController:**
```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired private JwtTokenProvider jwtProvider;
    @Autowired private UserService userService;
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @RequestBody LoginRequest req) {
        
        User user = userService.authenticate(req.getUsername(), 
                                             req.getPassword());
        String token = jwtProvider.generateToken(req.getUsername(), 
                                                 user.getRole());
        
        return ResponseEntity.ok(AuthResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .expiresIn(3600000L)
            .username(user.getUsername())
            .role(user.getRole())
            .build());
    }
}
```

#### 7.2 API Gateway

**GatewayConfig:**
```java
@Configuration
public class GatewayConfig {
    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("transaction-service", r -> r
                .path("/api/v1/transactions/**")
                .uri("lb://transaction-service"))
            .route("account-service", r -> r
                .path("/api/v1/accounts/**")
                .uri("lb://account-service"))
            .route("case-management", r -> r
                .path("/api/v1/cases/**")
                .uri("lb://case-management"))
            .route("llm-service", r -> r
                .path("/api/v1/reports/**")
                .uri("lb://llm-service"))
            .build();
    }
}
```

### Testing
```bash
# Login
POST /api/v1/auth/login
{
  "username": "admin",
  "password": "password123"
}

# Use token in subsequent requests
GET /api/v1/cases
Authorization: Bearer <token>
```

---

## PHASE 8: Frontend Dashboard (Weeks 11-12)

### Goals
- Create login page
- Build case management dashboard
- Add transaction list
- Implement real-time updates

### Tasks

#### 8.1 Login Page

```html
<!-- login.html -->
<form id="loginForm">
  <input type="text" id="username" placeholder="Username" required>
  <input type="password" id="password" placeholder="Password" required>
  <button type="submit">Login</button>
</form>

<script>
document.getElementById('loginForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const response = await fetch('/api/v1/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: document.getElementById('username').value,
      password: document.getElementById('password').value
    })
  });
  
  const data = await response.json();
  localStorage.setItem('jwt_token', data.token);
  window.location.href = 'dashboard.html';
});
</script>
```

#### 8.2 Case Dashboard

```html
<!-- cases.html -->
<div class="dashboard">
  <h1>Case Management</h1>
  
  <table id="casesTable">
    <thead>
      <tr>
        <th>Case ID</th>
        <th>Transaction</th>
        <th>Amount</th>
        <th>Risk Score</th>
        <th>Status</th>
        <th>Priority</th>
        <th>Assigned To</th>
        <th>Actions</th>
      </tr>
    </thead>
    <tbody id="casesBody"></tbody>
  </table>
</div>

<script>
const token = localStorage.getItem('jwt_token');

async function loadCases() {
  const res = await fetch('/api/v1/cases?page=0&size=20', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  const data = await res.json();
  populateTable(data.content);
}

function populateTable(cases) {
  const tbody = document.getElementById('casesBody');
  tbody.innerHTML = cases.map(c => `
    <tr onclick="viewCaseDetails('${c.id}')">
      <td>${c.id}</td>
      <td>${c.flaggedTransactionId}</td>
      <td>$${c.flaggedTransaction.amount}</td>
      <td>${c.riskScore.toFixed(1)}</td>
      <td>${c.status}</td>
      <td>${c.priorityLevel}</td>
      <td>${c.expertType}</td>
      <td>
        <button onclick="openCase('${c.id}')">Review</button>
        <button onclick="downloadReport('${c.generatedReportId}')">
          Report
        </button>
      </td>
    </tr>
  `).join('');
}

loadCases();
setInterval(loadCases, 30000); // Refresh every 30s
</script>
```

---

## 📋 TESTING CHECKLIST

- [ ] All DTOs validate and serialize
- [ ] Eureka server starts, all services register
- [ ] Detection rules trigger correctly
- [ ] Ensemble scoring works (rules + ML)
- [ ] Events publish and listeners receive them
- [ ] Cases auto-create from TransactionFlaggedEvent
- [ ] Reports auto-generate from CaseCreatedEvent
- [ ] JWT login works, tokens are valid
- [ ] API Gateway routes all requests correctly
- [ ] Case CRUD operations work
- [ ] Dashboard loads cases and updates in real-time
- [ ] Rate limiting active on gateway
- [ ] Audit logs recorded for all actions

---

## 🚀 DEPLOYMENT (No Docker)

Each service runs standalone:

```bash
# Terminal 1: Eureka
cd aml-eureka-server
mvn spring-boot:run

# Terminal 2: Transaction Service
cd aml-transaction-service
mvn spring-boot:run

# Terminal 3: Account Service
cd aml-account-service
mvn spring-boot:run

# Terminal 4: Customer Service
cd aml-customer-service
mvn spring-boot:run

# Terminal 5: Case Management
cd aml-case-management
mvn spring-boot:run

# Terminal 6: LLM Service
cd aml-llm-service
mvn spring-boot:run

# Terminal 7: Auth Service
cd aml-auth-service
mvn spring-boot:run

# Terminal 8: API Gateway
cd aml-api-gateway
mvn spring-boot:run

# Terminal 9: Frontend (optional static server)
cd aml-frontend
python -m http.server 3000
```

Then access:
- Dashboard: `http://localhost:3000`
- API: `http://localhost:8080`
- Eureka: `http://localhost:8761`

---

## 📊 TIMELINE SUMMARY

| Phase | Weeks | Focus | Outcome |
|-------|-------|-------|---------|
| 1 | 1-2 | Foundation | DTOs + Events + DB Schema |
| 2 | 3-4 | Services | 6 microservices + Eureka |
| 3 | 5-6 | Detection | Rules + ML + Ensemble |
| 4 | 7 | Events | Event-driven flow |
| 5 | 8 | LLM | Report generation |
| 6 | 9 | Cases | Full CRUD + routing |
| 7 | 10 | Auth | JWT + Gateway |
| 8 | 11-12 | Frontend | Dashboard + UI |

**Total: 12 weeks (can compress to 8-10 with parallel work)**

---

## ✨ WHAT YOU'LL HAVE AT THE END

✅ Production-ready microservices architecture  
✅ Event-driven data flow (TransactionFlaggedEvent → CaseCreatedEvent → ReportGeneratedEvent)  
✅ Ensemble detection (Rules + ML + GNN placeholder)  
✅ Full case management with expert routing  
✅ Auto-generated reports (Template → Gemini/GPT optionally)  
✅ JWT authentication + API Gateway  
✅ Analytics dashboard  
✅ Scalable, cloud-ready design  

**Perfect for a capstone project or portfolio piece!**
