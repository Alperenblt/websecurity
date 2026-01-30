# Web Security Application – Lab 11

This project is developed for the course **Security of Web Applications L2 [W.SIE.IN.5020]**.
The goal of Lab 11 is to integrate Spring Security and implement robust security mechanisms for a modern web application.

The application demonstrates **session-based authentication**, **JWT (JSON Web Token) authentication**, **CSRF protection**, **Rate Limiting**, and **Role-Based Access Control (RBAC)** using Spring Boot 3 and Spring Security 6.

## Features

### Authentication & Authorization
- **JWT Authentication**: Stateless authentication using Access Tokens (short-lived) and Refresh Tokens (long-lived, rotated).
- **Refresh Token Rotation**: Securely rotates refresh tokens on reuse to prevent token theft.
- **Role-Based Access Control (RBAC)**:
  - `ROLE_USER`: Can access personal notes and profile.
  - `ROLE_ADMIN`: Can access administrative endpoints (`/admin/**`).
- **Secure Password Storage**: Uses BCrypt with strong work factors.

### Security Mechanisms
- **CSRF Protection**: Cookie-based CSRF protection (`XSRF-TOKEN`) integrated with the frontend.
- **Rate Limiting**: Implements Bucket4j to prevent abuse (Brute Force/DDoS protection) on sensitive endpoints.
- **Input Validation**: Comprehensive DTO validation (`@Valid`, `@NotBlank`, `@Size`, etc.) and custom validators (e.g., `@UniqueUsername`).
- **Secure Headers**:
  - `Content-Security-Policy` (CSP)
  - `X-Frame-Options` (DENY)
  - `X-Content-Type-Options` (nosniff)
- **SQL Injection Prevention**: Demonstrates safe SQL queries using `JdbcTemplate` with prepared statements.

### Frontend
- **Simple Dashboard**: A clean, responsive HTML/JS dashboard to demonstrate the security features.
- **Secure API Integration**: JavaScript fetch API wrapper to handle JWTs and CSRF tokens automatically.

## Technologies Used

- **Java 17+**
- **Spring Boot 3.x**
- **Spring Security 6**
- **Spring Data JPA** & **SQLite**
- **Flyway** (Database Migration)
- **Bucket4j** (Rate Limiting)
- **JJWT** (JWT Library)
- **Maven**

## Project Structure

```
src/main/java/com/alperen/websecurity
├── config       # Security & Web configuration (SecurityConfig, WebConfig)
├── controller   # REST & MVC Controllers (AuthController, NoteController)
├── dto          # Data Transfer Objects (Requests/Responses)
├── filter       # Security Filters (JwtAuthFilter, CsrfCookieFilter, RateLimitFilter)
├── model        # JPA Entities (User, Note, RefreshToken)
├── repository   # Data Access Layer
├── security     # Security Utilities (JwtService, UserDetailsService)
├── service      # Business Logic
└── validation   # Custom Validators
```

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven

### Running the Application

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/Alperenblt/websecurity.git
    cd websecurity
    ```

2.  **Build and Run:**
    ```bash
    ./mvnw spring-boot:run
    ```
    The application will start on `http://localhost:8080`.

3.  **Access the Dashboard:**
    Open your browser and navigate to [http://localhost:8080](http://localhost:8080).

### Default Users (Seeded via Flyway)

| Role | Username | Password |
| :--- | :--- | :--- |
| **Admin** | `admin` | `password` |
| **User** | `user` | `password` |

## API Endpoints

### Authentication
- `POST /auth/register` - Register a new user.
- `POST /auth/login` - Login to receive Access Token & HttpOnly Refresh Cookie.
- `POST /auth/refresh` - Rotate Refresh Token and get new Access Token.
- `POST /auth/logout` - Secure logout (clears cookies and revokes tokens).

### Notes (Protected)
- `GET /api/notes` - List user's notes.
- `POST /api/notes` - Create a new note.
- `GET /api/notes/{id}` - Get a specific note.
- `DELETE /api/notes/{id}` - Delete a note.

### Admin
- `GET /admin/test` - Test admin access.

## Configuration

Sensitive configuration is managed via `application.properties` or environment variables.

```properties
# JWT Configuration
security.jwt.secret=YOUR_SECURE_SECRET_KEY
security.jwt.access.expiration-seconds=3600
security.jwt.refresh.expiration-seconds=604800
```

## License

This project is open-source and available under the MIT License.
