---
project: saas-mini-shop-service
description: |
  Backend scaffold for a production Product Order SaaS platform.
  Java 17 + Spring Boot, DDD, hexagonal architecture, Flyway migrations,
  Resilience4j, mandatory 2FA, multi-tenant, SOLID/Clean Code, event-driven with Kafka,
  PostgreSQL with replicas, MongoDB for notifications/logs/analytics.
---

# PRODUCT CONTEXT
Backend for multi-tenant SaaS Product Order system:
- Customer signup with mandatory 2FA (TOTP)
- Customers place orders with OrderItems
- Checkout and payment orchestration
- Promotions, coupons, and discounts
- Notifications stored in MongoDB and pushed async (email/push/webhook)
- Admin flows: promotions, refunds, order management
- Observability, metrics, and resilience

# ARCHITECTURE PRINCIPLES
- DDD with clear bounded contexts
- Hexagonal/Clean Architecture: domain, application, adapters, presentation
- Single Responsibility & SOLID
- Flyway for DB migrations
- Kafka for domain events
- Resilience4j for retry, circuit breaker, bulkhead
- Mandatory 2FA
- Idempotency for side-effecting commands

# MODULES
1. `platform-shared` — value objects, exceptions, DTOs, common events
2. `identity` — auth, mandatory 2FA (TOTP), RBAC, tenant management
3. `customers` — Customer aggregate, KYC, tenant context
4. `products` — Product definitions, pricing, inventory
5. `orders` — Order aggregate, OrderItem VO, order lifecycle
6. `payments` — Wallet & payment orchestration, ledger
7. `promotions` — Coupons, discounts, rules engine
8. `notifications` — Async notifications, MongoDB storage
9. `events` — Kafka producers/consumers, schemas
10. `infrastructure` — PostgreSQL config (replicas), MongoDB config, Redis, Kafka, Flyway, metrics, resilience
11. `api-gateway` — routing, JWT validation, rate limiting
12. `integration-tests` — E2E test harness

# DATABASE STRATEGY
- PostgreSQL: transactional data (customers, orders, order_items, payments, promotions) with read replicas
- MongoDB: notifications, audit logs, analytics, historical events
- Redis: ephemeral cart storage, rate-limiting, caching

# DOMAIN MODEL

## Customer
- Fields: id, tenantId, name, email, encrypted 2FA secret, phone, KYC status
- Commands: `RegisterCustomer`, `VerifyCustomer2FA`, `Login`
- 2FA mandatory for login

## Order
- Aggregate: id, tenantId, customerId, status, createdAt, idempotencyKey, total
- OrderItem: productId, sku, quantity, unitPrice, taxes, discountsApplied
- Commands: `PlaceOrder`, `CancelOrder`, `SettleOrder`
- Idempotent creation using `idempotencyKey`
- Validation: pricing, inventory, promotions, funds held

## Payment/Wallet
- Ledger entries (hold, capture, refund)
- Commands: `HoldFunds`, `CaptureFunds`, `RefundOrder`

## Promotions
- Coupons/discounts, eligibility rules
- Commands: `ApplyCoupon`, `AdminCreatePromotion`

## Notifications
- Async delivery: email/push/webhook
- Stored in MongoDB for auditing, retries, analytics
- Event-driven processing using Kafka

# USE-CASES (Commands / Queries)
- RegisterCustomer(email, password, tenantId) → provision 2FA
- VerifyCustomer2FA(customerId, totpCode)
- Login(email, password, totpCode) → JWT + refresh token
- CreateOrder(customerId, OrderItems, idempotencyKey)
- ApplyCoupon(orderId, couponCode)
- ProcessPayment(orderId, paymentMethod)
- SettleOrder(orderId)
- CancelOrder(orderId, reason)
- AdminCreatePromotion(...)

# TRANSACTIONS & SAGA
- Local DB transactions for orders/payments
- Side-effects (payments, notifications) via event-driven saga with Kafka
- Kafka events: `orders.placed`, `orders.cancelled`, `orders.settled`, `payments.captured`, `promotions.redeemed`, `notifications.sent`

# SECURITY
Provide enterprise-grade authentication and authorization for a multi-tenant SaaS:
- Secure user registration and login
- Mandatory 2FA using TOTP
- JWT access tokens + refresh tokens
- RBAC / permissions per tenant
- Audit trail for sensitive actions
- Idempotency enforcement for side-effect commands
- Secure handling of secrets (hashed/encrypted)

# BOUNDED CONTEXT
- Identity: users, authentication, 2FA, permissions, refresh tokens
- Tenant: multi-tenant awareness for RBAC and data isolation
- Audit: events for login, logout, 2FA setup, token refresh

# DOMAIN MODEL

## User
- id, tenantId, email, encrypted 2FA secret, roles, totpEnabled
- Invariants:
    - 2FA must be verified before any login
    - Roles and permissions scoped to tenant
- Commands: `RegisterUser`, `VerifyUser2FA`, `Login`

## Role / Permission
- Role examples: tenant-admin, operator, customer
- Permissions define allowed actions: PLACE_ORDER, APPLY_COUPON, CANCEL_ORDER, SETTLE_ORDER
- Enforced at application layer per command

## RefreshToken
- Managed as value object
- Includes userId, tenantId, token string, expiry, revoked status
- Rotation and revocation rules enforced

## AuditEvent
- Captures sensitive actions: LOGIN_SUCCESS, LOGIN_FAILED, LOGOUT, TOKEN_REFRESH, 2FA_VERIFIED, PASSWORD_CHANGE
- Stores userId, tenantId, timestamp, metadata
- Used for compliance, monitoring, and alerts

# USE-CASES (Commands / Queries)
1. `RegisterUser(email, password, tenantId)` → provision 2FA
2. `VerifyUser2FA(userId, totpCode)` → mandatory
3. `Login(email, password, totpCode)` → returns JWT + refresh token
4. `RefreshAccessToken(refreshToken)` → validate, rotate if needed
5. `Logout(userId, refreshToken)` → revoke refresh token
6. `AssignRole(userId, role)` → admin-only
7. `CheckPermission(userId, action)` → enforce RBAC
8. `AuditAction(userId, tenantId, action, metadata)` → log asynchronously
9. Idempotency enforcement on all commands with side-effects

# SECURITY POLICIES
- **Passwords:** hashed securely, complexity enforced, rate-limited login attempts, lockout after threshold
- **2FA:** mandatory TOTP for all users; first login verification required; recovery codes handled securely
- **JWT Tokens:** short-lived access tokens + long-lived refresh tokens with rotation
- **RBAC:** multi-tenant enforcement, method-level or application service checks
- **Idempotency:** required for side-effecting commands
- **Audit Logging:** all sensitive actions logged for compliance/analytics
- **Secrets Management:** all secrets encrypted at rest; keys stored securely outside code

# INFRASTRUCTURE / INTEGRATION
- Redis: for login attempt tracking, temporary locks
- MongoDB: for storing audit events (optional)
- Spring Security integration: JWT filter, 2FA filter, permission evaluator
- Resilience: retries, circuit breakers, bulkhead via Resilience4j
- Observability: metrics, tracing, health endpoints

# RESILIENCE & OBSERVABILITY
- Resilience4j for inter-service calls: retry, circuit breaker, bulkhead
- Micrometer + Prometheus metrics
- OpenTelemetry distributed tracing
- Health endpoints: `/actuator/health` and `/metrics`
- Grafana dashboards for orders, payments, promotions, notifications

# KAFKA TOPICS
- `orders.placed`, `orders.cancelled`, `orders.settled`
- `payments.hold_requested`, `payments.captured`, `payments.failed`
- `promotions.redeemed`
- `notifications.sent`
- `audit.events`

# FLYWAY MIGRATION
- Initialize tenants, users, user_2fa, customers, products, orders, order_items, promotions, idempotency_records

# SCAFFOLD OUTPUT INSTRUCTIONS
Claude must generate:
1. Maven multi-module backend project
2. Domain/application/adapters/controllers per module
## SCAFFOLD OUTPUT INSTRUCTIONS FOR SECURITY
```security
   Claude must generate:
        1. Identity module with domain/application/adapters/controllers
           2. Mandatory 2FA flow, TOTP verification, recovery codes
           3. JWT access + refresh token support with rotation
           4. Role/Permission evaluator and multi-tenant enforcement
           5. Audit event logging (as documents, not DB schema)
           6. Idempotency middleware for security commands
           7. Integration points for Redis / optional audit storage
           8. Config templates for Spring Security and OAuth2
           9. Unit tests for login, 2FA, refresh token, permission checks
           10. TODOs for secrets and external integration placeholders
```

3. Flyway SQL migrations
4. Kafka producer/consumer stubs
5. Resilience4j config usage
6. Idempotency filter/middleware
7. Mandatory 2FA flows
8. MongoDB integration for notifications/logs/analytics
9. Micrometer + Prometheus metrics + Grafana dashboard skeleton
10. Dockerfile per module, docker-compose.dev.yml, sample k8s deployment
11. Integration tests skeleton with Testcontainers
12. TODOs for secrets, PSP keys, promotions rules

~~END OF  MANIFEST~~