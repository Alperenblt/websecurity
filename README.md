# Web Security Application – Lab 11 (Final Version)

This project is the final implementation for **Security of Web Applications L2 [W.SIE.IN.5020] - Lab 11**. 
It demonstrates a secure, production-ready web application architecture integrating **Spring Security 6**, **JWT Authentication**, and advanced security defenses.

## 🚀 Project Overview

The application is a secure Note-Taking platform that implements two distinct security tracks:
1.  **Part A (MVC)**: Session-based authentication concepts (CSRF protection, Form Login).
2.  **Part B (REST)**: Stateless JWT authentication with Refresh Token Rotation.

**Current Version Status**: `v1.0.0` (Complete)
- ✅ All critical security flaws resolved (CSRF, IDOR, SQLi).
- ✅ Rate limiting implemented and active.
- ✅ Frontend securely integrated with JWT/CSRF handling.

## 🛡️ Key Security Features

### 1. Advanced Authentication System
- **Stateless JWTs**: Access tokens are short-lived (1 hour) and signed with HMAC-SHA256.
- **Refresh Token Rotation**:
    - Refresh tokens are opaque, stored in **HttpOnly, Secure, SameSite=Strict** cookies.
    - **Rotation**: Every time a refresh token is used, it is invalidated and replaced.
    - **Reuse Detection**: If an invalidated token is reused, the entire token family (all tokens for that user) is revoked immediately to prevent theft.

### 2. Robust Defense Mechanisms
- **CSRF Protection**:
    - Implemented `CsrfCookieFilter` to ensure the `XSRF-TOKEN` cookie is strictly managed.
    - Frontend automatically extracts and attaches the token to `X-XSRF-TOKEN` header for all state-changing requests (`POST`, `PUT`, `DELETE`).
- **Rate Limiting (DoS Protection)**:
    - **Bucket4j** integration limits requests to prevent brute-force and DoS attacks.
    - **Limit**: 10 requests per minute per IP address on authentication endpoints.
- **SQL Injection Prevention**:
    - Uses **JPA/Hibernate** for standard data access.
    - Explicit `JdbcTemplate` implementation with **PreparedStatement** demonstrates safe raw SQL execution.
- **Secure Headers**:
    - `Content-Security-Policy`: Restricts script sources to `self`.
    - `X-Frame-Options`: Deny (prevents Clickjacking).
    - `X-Content-Type-Options`: Nosniff.

### 3. Data Isolation & RBAC
- **Role-Based Access Control**:
    - `ROLE_ADMIN`: Full access to `/admin/**`.
    - `ROLE_USER`: Access to `/api/notes/**` and `/user`.
- **Horizontal Privilege Escalation (IDOR) Protection**:
    - Service layer enforces strict ownership checks. Users can only access/modify their *own* notes. accessing another user's note returns `404 Not Found` (to prevent ID enumeration) rather than `403`.

## 🛠️ Technical Stack

- **Framework**: Spring Boot 3.4.2
- **Security**: Spring Security 6
- **Database**: SQLite (Production-ready file mode)
- **Migration**: Flyway (Database schema version control)
- **Rate Limiting**: Bucket4j
- **Testing**: JUnit 5, MockMvc, Spring Security Test

## 📂 Project Structure

```
src/main/java/com/alperen/websecurity
├── config/
│   ├── SecurityConfig.java      # Main security filter chain & HTTP security
│   ├── WebConfig.java           # MVC & Interceptor config
│   └── JwtProperties.java       # Configuration properties binding
├── filter/
│   ├── JwtAuthFilter.java       # Validates Access Tokens per request
│   ├── CsrfCookieFilter.java    # Ensures CSRF cookie availability
│   └── RateLimitFilter.java     # Bucket4j rate limiting logic
├── controller/
│   ├── AuthController.java      # Login, Register, Refresh, Logout
│   └── NoteController.java      # Protected CRUD endpoints
├── service/
│   ├── JwtService.java          # Token generation & validation
│   └── RefreshTokenService.java # Rotation logic & DB management
└── repository/                  # Data access with ownership checks
```

## 🚀 Quick Start

### 1. Run the Application
```bash
./mvnw spring-boot:run
```

### 2. Access the Dashboard
Visit **[http://localhost:8080](http://localhost:8080)**

### 3. Test Credentials (Pre-seeded)

| Role | Username | Password | Access |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `password` | Can access Admin Panel & Test Endpoint |
| **User** | `user` | `password` | Can Create/Read/Delete own Notes |

## 🧪 Testing the Security

### Test CSRF Protection
Attempt to send a `POST` request to `/api/notes` without the `X-XSRF-TOKEN` header.
- **Expected Result**: `403 Forbidden`

### Test Rate Limiting
Send >10 requests to `/auth/login` within 1 minute.
- **Expected Result**: `429 Too Many Requests`

### Test Token Rotation
Login, then manually use the `/auth/refresh` endpoint twice with the same cookie.
- **Expected Result**: The second attempt will fail, and the user will be forced to re-login (Token Theft Detection).

## 📄 License
This project is open-source and available under the MIT License.
