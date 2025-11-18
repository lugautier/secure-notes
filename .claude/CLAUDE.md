# Development Guide

## Project Overview

**Purpose**: REST API for encrypted notes management - Showcase Java/Spring Boot best practices, security, cloud infrastructure, and FinOps

**Role**: Backend API providing secure note storage with AES-256 encryption, JWT authentication, and full audit trails

**Stack**:
- **Java 21** + **Spring Boot 3.5.7** + **Maven 3.9+** with Toolchains
- **PostgreSQL** (RDS/Azure Database managed)
- **Docker** + **Docker Compose** (multi-stage builds)
- **Terraform** (Infrastructure as Code with multi-env: dev/prod)
- **GitHub Actions** (CI/CD with security scans)
- **AWS/Azure/GCP** (cloud provider - choose one)

## Architecture

### Core Components
- **Authentication Layer** (Spring Security + JWT RS256): Asymmetric key pair for stateless auth
- **Encryption Layer** (AES-256-GCM + PBKDF2): User-derived keys for note encryption, KMS for master key
- **Domain Layer** (User, Note, AuditLog): Clean entity design with Flyway migrations
- **Service Layer**: Business logic with encryption/decryption, audit logging
- **Repository Layer** (Spring Data JPA): Clean database access patterns
- **Controller Layer** (REST endpoints): Input validation, error handling, OpenAPI docs
- **Security Filter Chain** (Rate limiting, headers, CORS): Multi-layer protection
- **Observability** (Actuator, Prometheus, JSON logs): Production-ready monitoring

### Package Structure
```
src/main/java/com/securenotes/
├── config/          # Spring Security, DB, Cloud, Crypto configuration
├── domain/          # User, Note, AuditLog entities + enums
├── repository/      # Spring Data JPA repositories
├── service/         # Business logic (UserService, NoteService, CryptoService, AuditService)
├── controller/      # REST endpoints (@RestController)
├── dto/             # Request/Response records (Java 21)
├── security/        # JWT provider, filters, RBAC
├── exception/       # Custom exceptions + global @ControllerAdvice
└── util/            # Crypto utilities (AES-256-GCM, PBKDF2, KeyDeriv)

src/test/java/com/securenotes/
├── controller/      # @SpringBootTest controller tests
├── service/         # Unit tests + Testcontainers integration tests
└── security/        # JWT, crypto, auth security tests

src/main/resources/
├── application.yml                      # App config (dev/prod profiles)
├── application-dev.yml / -prod.yml     # Environment-specific
└── db/migration/                        # Flyway SQL migrations (V1__, V2__, ...)
```

### Runtime & Build
- **Java 21** (Eclipse Temurin LTS) - Use records, pattern matching, enhanced switch
- **Maven 3.9+** with Toolchains - Multi-version Java support configured
- **Spring Boot 3.5.6** - Latest stable, requires Java 17+ minimum

### Key Dependencies
- **spring-boot-starter-web**: REST endpoints, Tomcat, Jackson
- **spring-boot-starter-security**: Spring Security framework
- **spring-boot-starter-data-jpa**: Hibernate ORM, Spring Data repositories
- **spring-boot-starter-validation**: Jakarta Bean Validation (@Valid, @NotNull, @Pattern)
- **spring-boot-starter-actuator**: Health endpoints (/actuator/health), metrics
- **jjwt 0.12.5**: JWT RS256 (asymmetric) token handling
- **bouncy-castle 1.78.1**: AES-256-GCM encryption, PBKDF2 key derivation
- **postgresql**: PostgreSQL JDBC driver
- **flyway-core + flyway-database-postgresql**: Database schema migration
- **springdoc-openapi 2.6.0**: Swagger UI at /swagger-ui.html, OpenAPI 3.0 documentation
- **micrometer-registry-prometheus**: Prometheus metrics endpoint
- **lombok**: Reduce boilerplate (@Data, @Builder, @Slf4j, @RequiredArgsConstructor)
- **testcontainers**: PostgreSQL containers for integration tests
- **spring-security-test**: Security test support

## Development Best Practices

### Code Style
- **Use Lombok**: @Data, @Builder, @Slf4j, @RequiredArgsConstructor
- **Use Java 21 features**: Records for DTOs, pattern matching, enhanced switch
- **Use Spring conventions**: @RestController, @Service, @Configuration
- **Dependency Injection**: Constructor injection (final fields + @RequiredArgsConstructor)

### Validation
- **Input validation**: Jakarta Bean Validation (@NotNull, @Valid, @Pattern, @Email, @Size)
- **Email format**: Must be valid email for user registration
- **Password strength**: Min 12 chars, must contain upper, lower, number, special char
- **Note title**: Non-empty, max 255 chars
- **Note content**: Non-empty, max 100,000 chars (encrypted at rest)
- **Date formats**: ISO8601 only (yyyy-MM-dd, yyyy-MM-dd'T'HH:mm:ss)
- **UUIDs**: Validate all user/note IDs are valid UUIDs
- **Rate limiting**: 10 requests/min on POST /api/auth/login endpoint
- **Sanitization**: Never trust external input, validate ALL parameters before processing

### Error Handling
- **Custom exceptions**: AuthenticationException, EncryptionException, NoteNotFoundException, ValidationException
- **Global handler**: @ControllerAdvice for consistent error responses (RFC 7807 Problem Detail format)
- **HTTP Status codes**:
  - 400 Bad Request (validation failures)
  - 401 Unauthorized (invalid/expired JWT)
  - 403 Forbidden (insufficient permissions)
  - 404 Not Found (note doesn't exist or user lacks access)
  - 409 Conflict (duplicate email on registration)
  - 429 Too Many Requests (rate limit exceeded)
  - 500 Internal Server Error (crypto failures, database errors)
- **Logging**: Use @Slf4j, log errors with context (request ID, user email, endpoint, operation)
- **Audit logging**: All auth attempts (success/failure) and note CRUD operations logged to DB

### Testing
- **Unit tests**: JUnit 5 + Mockito for business logic (UserService, NoteService, CryptoService)
- **Integration tests**: @SpringBootTest + Testcontainers PostgreSQL for full flow testing
- **Security tests**: JWT token validation, encryption/decryption, permission checks
- **Coverage target**: >50% for business logic (verified with JaCoCo)
- **Test naming**: `testXxx_WhenYyy_ThenZzz` format for clarity
- **Test isolation**: Each test independent, no shared state, use @DirtiesContext if needed

## Maven & Build

### Maven Wrapper (IMPORTANT)
- **Always use `./mvnw` instead of `mvn`** - The project includes Maven Wrapper to ensure consistent Maven version
- **Maven version**: 3.9.11 (specified in `.mvn/wrapper/maven-wrapper.properties`)
- **Why**: System Maven may be outdated; Maven Wrapper ensures Maven 3.9.11 + Java 21 compatibility
- **Usage**: Replace all `mvn` commands with `./mvnw`:
  - ✅ `./mvnw clean package -DskipTests`
  - ✅ `./mvnw clean install`
  - ❌ `mvn clean package` (won't work with Java 21)

### Multi-Version Java Setup
- **Toolchains configured**: Project uses Java 21 via `maven-toolchains-plugin`
- **Global registry**: `~/.m2/toolchains.xml` declares available JDKs
- **Per-project requirement**: `pom.xml` specifies Java 21
- **Benefit**: Work on multiple projects with different Java versions without PATH changes

## Documentation

## General guidelines

When working on this project:

1. **Always check TODO.md** for detailed specifications and current progress at the start of any session
2. **Infrastructure-First approach**: Docker, Terraform, and CI/CD are setup BEFORE business logic implementation
3. **Use Java 21 features**: Records for DTOs, pattern matching, switch expressions, sealed classes
4. **Follow Spring Boot conventions**: Annotations, dependency injection, profiles (dev/prod)
5. **Validate all inputs**: Never trust external data, validate emails, passwords, note content
6. **Encryption is mandatory**: ALL notes encrypted with AES-256-GCM at rest, PBKDF2-derived per-user keys
7. **Use Lombok**: Reduce boilerplate with @Data, @Builder, @Slf4j, @RequiredArgsConstructor
8. **Log with context**: Include request ID, user email, endpoint, operation timing in logs
9. **Keep secrets safe**: Never hardcode credentials, use environment variables + cloud Secrets Manager
10. **Audit everything**: All authentication attempts and note CRUD operations must be audited
11. **Explain all Java/Spring concepts in French before implementing**: Consider new to Java/Spring Boot
12. **No comments that are not production-ready**: Only write necessary, business-logic comments
13. **Security-first mindset**: Think about attack vectors, use OWASP best practices
14. **REST conventions**: Use proper HTTP verbs (GET/POST/PUT/DELETE), status codes, content negotiation

### Test-Driven Development (TDD) Workflow

**When the user requests TDD implementation (RED → GREEN → REFACTOR), follow this strict workflow:**

#### 1. TodoWrite Tool Usage in TDD

**ALWAYS use TodoWrite to track TDD progress:**
- Create a todo list at the start of TDD session with planned test cycles
- Mark each test as `in_progress` when starting RED phase
- Mark as `completed` when that cycle's REFACTOR phase is done
- Update the list as new tests are discovered during implementation

**Example todo list for TDD:**
```
1. [in_progress] Test 1: POST /mcp endpoint accepts JSON (RED→GREEN→REFACTOR)
2. [pending] Test 2: Response has JSON-RPC structure (RED→GREEN→REFACTOR)
3. [pending] Test 3: Response echoes request ID (RED→GREEN→REFACTOR)
4. [pending] Test 4: tools/list returns 4 tools (RED→GREEN→REFACTOR)
```

#### 2. Commit Points in TDD

**Notify me when we're at a good commit point**

**Good commit points:**
- After 1-3 related TDD cycles are complete (all GREEN + REFACTORED)
- After a complete feature slice works end-to-end
- Before switching to a different component or layer

**Example commit flow:**
1. Complete Test 1-3 (basic endpoint + response structure) → **COMMIT 1**
2. Complete Test 4-6 (DTOs with serialization) → **COMMIT 2**
3. Complete Test 7-9 (service layer with tools/list) → **COMMIT 3**

#### 3. TDD Session Flow Example

**User says:** "Let's implement the MCP protocol layer using TDD"

**Claude's workflow:**
1. **Create todo list** with planned TDD cycles (TodoWrite)
2. **Test 1 - RED**: Write failing test for POST /mcp endpoint
3. **Test 1 - GREEN**: Create minimal MCPController to pass test
4. **Test 1 - REFACTOR**: Clean up code (if needed)
5. **Test 2 - RED**: Write failing test for JSON-RPC response structure
6. **Test 2 - GREEN**: Add MCPResponse DTO, make test pass
7. **Test 2 - REFACTOR**: Apply Lombok, improve naming
8. **Notify user**: "Tests 1-2 complete. Good commit point."
9. **User stages and commits**: `git add ...` then `/commit-tdd`
10. **Repeat** for next TDD cycles

Don't forget to explain all Java concepts in French before implementing.

### Git Workflow Rules

**CRITICAL: Never manage git staging or commits autonomously**

- **NEVER** run `git add` to stage files yourself
- **NEVER** run `git commit` unless the user explicitly types a commit command (`/commit`, `/commit-short`, `/commit-tdd`)
- **ALWAYS** wait for the user to decide when and what to commit
- **ONLY** create commits when the user invokes a commit slash command

The user controls the git workflow entirely. Your role is to write code and tests, not to manage version control.

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user (email, password)
- `POST /api/auth/login` - Login (email, password) → returns JWT token (Bearer)
- `POST /api/auth/logout` - Logout (invalidates token)
- `GET /api/auth/profile` - Get current user profile (requires JWT)

### Notes
- `POST /api/notes` - Create note (title, content) → encrypted at rest
- `GET /api/notes` - List user's notes (paginated)
- `GET /api/notes/{noteId}` - Get note detail (auto-decrypted)
- `PUT /api/notes/{noteId}` - Update note (title, content)
- `DELETE /api/notes/{noteId}` - Soft-delete note
- `GET /api/notes/{noteId}/history` - Get note edit history (audit trail)

### System
- `GET /actuator/health` - Health check (liveness/readiness)
- `GET /actuator/prometheus` - Prometheus metrics
- `GET /swagger-ui.html` - Swagger API documentation

## Security Best Practices

### Authentication & Authorization
- JWT RS256: Public key for verification, private key in secrets manager
- All endpoints except /register, /login require Bearer token
- Token expiration: 24 hours (refresh token not implemented in MVP)
- RBAC: USER role (all authenticated users same permissions)

### Encryption
- **Master key**: From cloud KMS (AWS Secrets Manager / Azure Key Vault)
- **Per-user keys**: Derived from master key + user salt using PBKDF2 (100,000 iterations)
- **Algorithm**: AES-256-GCM (authenticated encryption)
- **IV**: Random per encryption, stored with ciphertext

### Protection Headers
- `Strict-Transport-Security`: HSTS enabled (production only)
- `X-Frame-Options`: DENY (prevent clickjacking)
- `X-Content-Type-Options`: nosniff
- `Content-Security-Policy`: Restrictive (no external resources)
- `Cache-Control`: no-cache, no-store (sensitive data)

### Rate Limiting
- 10 requests/min on POST /api/auth/login
- 100 requests/min for authenticated users (other endpoints)
- HTTP 429 Too Many Requests when exceeded

### SQL Injection Prevention
- Spring Data JPA parameterized queries (automatic)
- No String concatenation in JPQL/SQL
- Input validation before database queries

## Cloud Deployment

### Environment Variables (Secrets)
- `DB_URL`, `DB_USER`, `DB_PASSWORD`: PostgreSQL connection
- `JWT_PRIVATE_KEY`, `JWT_PUBLIC_KEY`: RSA key pair (base64 encoded)
- `KMS_KEY_ARN` / `KEYVAULT_URI`: Cloud KMS for master encryption key
- `ENVIRONMENT`: dev or prod (controls profiles)

### Docker Build
```bash
./mvnw clean package -DskipTests
docker build -t secure-notes-api:latest .
docker run -e ENVIRONMENT=dev -e DB_URL=jdbc:postgresql://db:5432/securenotes ...
```

### Terraform Deployment
```bash
cd terraform/environments/dev (or prod)
terraform init -backend-config="..."
terraform plan
terraform apply
```