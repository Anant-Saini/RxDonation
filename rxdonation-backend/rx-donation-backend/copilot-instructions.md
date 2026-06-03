# RxDonation Backend (Spring Boot) - Copilot Instructions

## Purpose
Concise developer-facing guidelines for backend implementation aligned to the RxDonation MVP vision. Focus is on secure, geospatially-aware Spring Boot APIs that connect Donors and Pharmacies through a medicine donation platform.

## Vision & Mission
**Vision**: Bridge between donors and pharmacies, ensuring unexpired, usable medications are redirected to those in need. Leverage geospatial technology for local, community-driven redistribution while maintaining strict chain of custody and security.

**Scope**: Backend API serves two distinct user types (Donors and Pharmacies) with separate workflows. Full-stack deployment targets AWS with professional-grade DevOps (Docker, RDS, S3, SES, CI/CD).

## Tech Stack (backend-focused)
- **Runtime**: Java 17 or 21, Spring Boot 3.x
- **Security**: Spring Security with JWT (stateless, no sessions), RBAC (roles: ROLE_DONOR, ROLE_PHARMACY, ROLE_ADMIN)
- **Persistence**: Spring Data JPA, Hibernate Spatial, LocationTech JTS
- **Database**: PostgreSQL with PostGIS extension (SRID 4326)
- **Cloud**: AWS S3 (image uploads), SES (email), RDS (managed Postgres)
- **DevOps**: Docker, Docker Compose, Maven, Elastic Beanstalk or EC2 deployment
- **Documentation**: Swagger / OpenAPI (auto-generated API docs)
- **Tooling**: Lombok (optional), SLF4J structured logging

## Architecture & Project Conventions

### Domain Entities
- **Identity vs Profile Pattern**: `User` entity handles authentication only; `Donor` and `Pharmacy` entities hold profile/location data (OneToOne relationship). Never expose raw entities in API responses—always map to DTOs.
- **Core Models**:
  - `User`: email, password_hash (BCrypt), role, created_at, verification_status
  - `Donor`: user_id (FK), name, location (Point, SRID 4326), phone, address
  - `Pharmacy`: user_id (FK), name, location (Point, SRID 4326), phone, address, license_number
  - `DonationOrder`: id, donor_id (FK), status (enum: PENDING, ACCEPTED, COMPLETED, CANCELLED, EXPIRED), created_at, expires_at (created_at + 10 days)
  - `MedicineItem`: id, order_id (FK), medicine_name, quantity, expiry_date, image_url (S3), description
  - `Notification`: id, user_id (FK), type, message, is_read, created_at
  - Use database-level version columns for optimistic locking on orders (prevent double-claims)

### Package Structure (suggested)
- `com.rxdonation.backend.model` — JPA entities (use org.locationtech.jts.geom.Point for locations)
- `com.rxdonation.backend.repository` — Spring Data interfaces
- `com.rxdonation.backend.service` — Business logic; annotate multi-table operations with `@Transactional`
- `com.rxdonation.backend.controller` — REST controllers (map to `/api/v1/...`)
- `com.rxdonation.backend.dto` — API payloads (separate Request/Response DTOs)
- `com.rxdonation.backend.security` — JWT filters, auth providers, role config
- `com.rxdonation.backend.exception` — Global `@RestControllerAdvice` for error handling
- `com.rxdonation.backend.config` — AWS, email, PostGIS configuration

### Configuration & Secrets
- Use `application-dev.yml` and `application-prod.yml` profiles
- All secrets (AWS keys, DB passwords, JWT secrets) injected via environment variables with `${VAR}` syntax
- Validate critical env vars on application startup; fail fast if missing
- Never commit credentials, keys, or sensitive data to Git
- Use AWS Secrets Manager or similar for production secrets

## Core Backend Features (21-Day Roadmap Alignment)

### Phase 1: Foundation & Security (Days 1–4)
- **Git Monorepo & Project Init**: Set up Maven project, .gitignore, dual-repo structure (backend/frontend)
- **PostGIS & Database Schema**: Enable PostGIS extension, create initial User/Donor/Pharmacy schema
- **Spring Security & JWT Setup**: Implement JWT provider, token generation, stateless auth filters
- **Registration & Login APIs**: Two-role registration flow; support distinct Donor and Pharmacy signup/login

### Phase 2: Donor Workflow (Days 5–8)
- **DonationOrder & MedicineItem Entities**: Build OneToMany relationship, map to DTOs
- **AWS S3 Integration**: Multipart upload service, secure file streaming, pre-signed URLs (optional)
- **Place Order API**: Complex POST endpoint accepting order header + array of items; atomic transaction (all-or-nothing)
- **API Response Structure**: Return consistent DTOs with proper HTTP status codes (201 for created, 400 for validation, 500 for server errors)

### Phase 3: Pharmacy Proximity & Real-time Logic (Days 9–13)
- **Proximity Search API**: PostGIS query using `ST_DWithin(pharmacy_location, donation_location, 2000)` in meters (2km radius); return orders within radius, sorted by distance
- **Accept Order Logic**: Atomic state transition (PENDING → ACCEPTED); use version column + transactional read to prevent double-claims; return 409 Conflict if already accepted
- **Order State Management**: REST endpoints to view orders by state, filter by pharmacy location, track lifecycle
- **Response Payload**: Include order details, medicine items, donor location (masked for privacy), expiry countdown

### Phase 4: Automation & Communication (Days 14–17)
- **Notification System (DB Side)**: Notification entity with user_id, type, message, is_read; inbox endpoint to fetch unread notifications
- **Mark as Read API**: Endpoint to mark single or batch notifications as read
- **Scheduled Expiry Job**: Use `@Scheduled` cron task to expire orders after 10 days; update status to EXPIRED and create notification for both parties
- **Email Verification (AWS SES)**: Send verification emails during signup; track verification status in User entity; require verified email before placing/accepting orders

### Phase 5: Deployment & DevOps (Days 18–21)
- **Dockerization**: Provide `Dockerfile` for Spring Boot app; multi-stage build for optimized image size
- **Docker Compose**: `docker-compose.yml` with Postgres+PostGIS, app service, optional LocalStack for S3/SES mocking
- **AWS RDS & Infrastructure**: Provision managed Postgres with PostGIS, configure security groups, backup policies
- **Environment & CI/CD**: Deploy to AWS (EC2 or Elastic Beanstalk); configure environment variables, secrets manager, CI/CD pipeline
- **Swagger Documentation**: Keep OpenAPI/Swagger docs up-to-date with all endpoints, request/response examples

## Important Backend Requirements

### Geospatial Logic
- **Coordinate System**: Always use WGS 84 (SRID 4326) for all location data
- **Proximity Query**: Use `ST_DWithin(geometry1, geometry2, distance_in_meters)` to find donations within 2,000 meters (2 km)
- **Storage**: Store all donor and pharmacy locations as `org.locationtech.jts.geom.Point` with SRID 4326
- **Distance Calculation**: Return distance in response for UX sorting and transparency

### State Machine (Orders)
- **States**: Pending → Accepted → Completed (or Cancelled or Expired)
- **Valid Transitions**:
  - PENDING → ACCEPTED (pharmacy accepts)
  - PENDING → CANCELLED (donor cancels)
  - PENDING → EXPIRED (auto-expiry after 10 days)
  - ACCEPTED → COMPLETED (pharmacy confirms pickup)
  - ACCEPTED → CANCELLED (pharmacy or donor cancels)
- **Model as Enum**: Use Java enum for type safety; persist as VARCHAR in DB
- **Atomic Updates**: Use database-level transactions to ensure state changes are durable

### Concurrency & Double-Claim Prevention
- **Optimistic Locking**: Add `@Version` column to DonationOrder entity
- **Accept Logic**: Single atomic SQL update: `UPDATE DonationOrder SET status = 'ACCEPTED', version = version + 1 WHERE id = ? AND status = 'PENDING' AND version = ?`
- **Check Affected Rows**: If update returns 0 rows, order was already claimed; return 409 Conflict
- **Test Scenarios**: Simulate concurrent accept requests from multiple pharmacies

### Notifications & Long-Polling
- **DB Backing**: Notification entity stores all events (order placed, accepted, completed, expired)
- **Polling Endpoint**: GET `/api/v1/notifications` with optional `?since=timestamp` parameter; return unread notifications sorted by timestamp
- **Frontend Polling**: Frontend polls every 60 seconds (configurable); backend may implement caching to reduce DB load
- **Mark as Read**: Batch endpoint to mark multiple notifications as read in single transaction

### Auto-Expiry Scheduled Task
- **Trigger**: Daily cron job or time-based scheduler
- **Logic**: Find all PENDING orders with `created_at + 10 days < NOW()`, update status to EXPIRED
- **Notifications**: Create Notification records for donor and pharmacy
- **Idempotency**: Ensure job can run multiple times safely (idempotent design)
- **Logging**: Structured log entry for each batch (e.g., "Expired 5 orders in batch run")

### Image Handling & S3
- **Multipart Uploads**: Accept multipart/form-data POST with medicine image
- **S3 Storage**: Use AWS SDK (boto3 equiv., or AWS Java SDK) to upload to S3 bucket
- **URL Generation**: Return pre-signed URL or direct S3 URL in response (configure CORS on bucket)
- **File Naming**: Use UUID or hash-based naming to avoid collisions
- **Validation**: Validate file size (e.g., max 5MB), MIME type (JPEG, PNG only)

### Email Verification (AWS SES)
- **Signup Flow**: After registration, send verification email via SES with token in link
- **Verification Endpoint**: GET `/api/v1/auth/verify-email?token=...` to confirm email
- **User Status**: Track `email_verified` boolean in User entity; block certain actions (place order, accept order) until verified
- **Resend Logic**: Provide endpoint to resend verification email
- **SES Configuration**: Set up sender email, template (optional), handle bounce/complaint feedback

### Security & RBAC
- **JWT Token**: Include claims (user_id, role, email) in JWT payload; set expiration (e.g., 24 hours)
- **Stateless Auth**: No session storage; validate JWT on every request
- **Role-Based Access**: Use `@PreAuthorize("hasRole('DONOR')")` or `@PreAuthorize("hasRole('PHARMACY')")` on controller methods
- **CORS Configuration**: Allow only frontend origin (`http://localhost:4200` for dev, production URL for prod)
- **Security Headers**: Include HSTS, X-Content-Type-Options, X-Frame-Options headers
- **Rate Limiting**: Optionally add rate-limit headers on auth endpoints (prevent brute-force)
- **MFA (Future)**: Consider adding MFA on AWS account and Git

### Cost Control & Monitoring
- **AWS Budget Alert**: Set alert at $1.00/month to prevent runaway charges
- **S3 Lifecycle**: Configure S3 lifecycle policies to delete old images after X days
- **SES Quotas**: Monitor SES sending quota; test in sandbox mode first
- **CloudWatch Logs**: Enable logging for RDS and API Gateway (optional)
- **Use Free Tier**: Maximize AWS free tier services during development

## Coding & API Standards

### Endpoints
- **Versioning**: All endpoints prefixed with `/api/v1/`
- **Naming**: Use kebab-case (e.g., `/api/v1/donation-orders`, `/api/v1/notification-inbox`)
- **HTTP Methods**: POST for create, PUT/PATCH for update, GET for read, DELETE for delete
- **Path Parameters**: `/api/v1/donation-orders/{id}`

### DTOs & Validation
- **Separate Request/Response**: Create distinct DTO classes (e.g., `CreateOrderRequest`, `OrderResponse`)
- **Validation Annotations**: Use Jakarta Validation (`@NotNull`, `@NotEmpty`, `@Min`, `@Max`, `@Email`)
- **Custom Validators**: Implement `ConstraintValidator` for complex rules (e.g., expiry_date must be in future)
- **Error Responses**: Return consistent error DTOs with error code, message, field-level errors

### Dates & Timestamps
- **Format**: ISO-8601 with UTC timezone (e.g., `2026-06-04T01:52:38Z`)
- **Storage**: Use `java.time.LocalDateTime` or `java.time.OffsetDateTime` in Java; store as TIMESTAMP in DB
- **Serialization**: Jackson DateTimeFormatter for ISO-8601 JSON serialization

### Responses
- **Success Response**: Consistent wrapper (optional) or clear DTO; HTTP 200 for GET/PATCH, 201 for POST, 204 for DELETE
- **Error Response**: `{ "error_code": "CONFLICT", "message": "Order already accepted", "details": {...} }`
- **HTTP Status Codes**:
  - 200 OK (successful GET/PATCH)
  - 201 Created (successful POST)
  - 204 No Content (successful DELETE)
  - 400 Bad Request (validation error)
  - 401 Unauthorized (missing/invalid JWT)
  - 403 Forbidden (insufficient role)
  - 404 Not Found (resource missing)
  - 409 Conflict (double-claim on order)
  - 500 Internal Server Error (server error)

### Logging
- **Structured Logging**: Use SLF4J with JSON output (e.g., logback JSON encoder)
- **Log Levels**: INFO for business events (order placed, accepted), DEBUG for internal flow, WARN for retries, ERROR for exceptions
- **Never Log**: Passwords, JWT tokens, API keys, PII (emails, phone numbers) unless absolutely necessary for debugging
- **Key Events**: "Order placed by {donor_id}", "Order accepted by {pharmacy_id}", "Expiry job completed: {count} orders expired"

### Transactional Integrity
- **`@Transactional` Annotation**: Use on service methods that modify multiple entities
- **Isolation Level**: Default (READ_COMMITTED) usually sufficient; consider SERIALIZABLE for critical sections
- **Rollback on Exception**: Annotate with `rollbackFor` if catching checked exceptions
- **Example**: Place Order API must create Order + Items atomically

## Testing, Local Dev & Deployment

### Unit Testing
- Mock external services (S3, SES, PostGIS) using Mockito
- Test business logic in isolation (service layer)
- Aim for 70%+ code coverage on critical paths (auth, order state, concurrency)

### Integration Testing
- Use Testcontainers for Postgres+PostGIS (real DB, isolated per test)
- Test repository queries with actual spatial functions
- Test controller endpoints with MockMvc or RestAssured
- Example: Test proximity search returns only orders within 2km

### Local Development
- **Docker Compose Stack**: `docker-compose.yml` with Postgres+PostGIS service
- **Database Init**: SQL scripts to create schema, enable PostGIS, seed test data
- **Hot Reload**: Use Spring DevTools for live code reloading during dev
- **Postman/Insomnia**: Collection for testing all endpoints locally
- **Application Properties**: `application-dev.yml` with local Postgres connection, mock S3/SES (LocalStack optional)

### Docker & Containerization
- **Dockerfile**: Multi-stage build (compile, runtime); use `openjdk:21-jdk-slim` or `eclipse-temurin:21-jre-alpine` base image
- **Docker Compose**: Include Postgres+PostGIS service, app service, optional volumes for persistence
- **Image Size**: Target < 300MB (use Alpine base + minimal dependencies)
- **Healthchecks**: Add health endpoint (`/actuator/health`) for container orchestration

### Deployment to AWS
- **RDS Setup**: Create managed PostgreSQL instance, enable PostGIS extension, configure backup/retention
- **EC2/Elastic Beanstalk**: Deploy Spring Boot JAR or Docker container
- **Environment Variables**: Inject DB URL, AWS keys, JWT secret via CI/CD pipeline
- **CI/CD Pipeline**: GitHub Actions or AWS CodePipeline to build, test, and deploy on push to main branch
- **Domain & HTTPS**: Use Route53 for DNS, ACM for SSL certificate, ALB for load balancing (if scaling)

## Project Deliverables
- [x] Fully functional Spring Boot API with Swagger/OpenAPI documentation
- [x] Clean GitHub repository with professional README (installation, local dev, deployment steps)
- [x] Live AWS URL for portfolio review (EC2/Elastic Beanstalk domain)
- [x] Responsive Angular Frontend (frontend repo)
- [x] Docker Compose for local development (reproducible local environment)
- [x] Production-ready code (secrets, error handling, logging, validation)

## Security & Safety Guardrails
- **No Hardcoded Secrets**: All configuration via environment variables or AWS Secrets Manager
- **AWS Budget Alert**: $1.00/month to catch unexpected charges
- **CORS**: Strict whitelist (frontend origin only)
- **JWT Secret**: Use strong, random secret (min. 256 bits); rotate periodically
- **DB Connection**: Use SSL/TLS in production; restrict security groups
- **Input Validation**: Validate all incoming data; sanitize for SQL injection (JPA protects via parameterized queries)
- **API Rate Limiting**: Consider adding rate limits on auth endpoints (brute-force prevention)
- **MFA & Access Control**: MFA on AWS account and GitHub; limit prod access to authorized personnel

## Quick Developer Checklist
- [ ] Java 17/21 + Spring Boot 3.x project skeleton with Maven
- [ ] Postgres + PostGIS connection; test Point/geometry mapping (SRID 4326)
- [ ] JWT provider and stateless auth filter; test token generation/validation
- [ ] User/Donor/Pharmacy entities with OneToOne/OneToMany relationships; test schema
- [ ] Registration & Login APIs (two user types); test with Postman
- [ ] DonationOrder and MedicineItem entities with state enum (PENDING, ACCEPTED, COMPLETED, CANCELLED, EXPIRED)
- [ ] S3 upload service and SES email sender (interfaces + local mocks for dev)
- [ ] Place Order API (atomic transaction); test with sample data
- [ ] Proximity search endpoint using ST_DWithin (2km); test with multiple locations
- [ ] Accept Order API with concurrency-safe state transition (version-based locking); test double-claim scenarios
- [ ] Notification entity and inbox endpoints; polling endpoint
- [ ] @Scheduled auto-expiry task (10 days); test with shortened intervals in dev
- [ ] Email verification flow (signup, verification link, resend); test with AWS SES
- [ ] Swagger/OpenAPI documentation with examples for all endpoints
- [ ] Dockerfile and docker-compose.yml; test local stack startup
- [ ] Environment-based configuration (dev vs. prod profiles)
- [ ] Unit + integration test coverage (70%+ on critical paths)
- [ ] Error handling (@RestControllerAdvice with consistent error responses)
- [ ] Structured logging (SLF4J with JSON output for production)

---

**Last Updated**: 2026-06-04  
**Status**: Backend roadmap phases 1–5 defined; ready for incremental implementation.

If additional backend requirements or architectural decisions arise, update this file with the specific section (e.g., "State Machine", "Notifications", "Deployment") and concise guidance.
