# Web Security Application – Lab 11

This project is developed for the course  
**Security of Web Applications L2 [W.SIE.IN.5020]**.

The goal of **Lab 11** is to integrate **Spring Security** and implement both:

- **Part A:** Session-based authentication (MVC track)
- **Part B:** Token-based authentication using JWT (REST track)

The application demonstrates user registration, login, role-based authorization,
and protected endpoints using modern Spring Security practices.

---

## Technologies Used

- Java (JDK 17 / 23)
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- Flyway Migration
- SQLite
- JWT (io.jsonwebtoken – jjwt)
- Maven
- Git & GitHub

---

## Project Structure

The project follows a layered architecture:

- `controller` – REST and MVC controllers
- `service` – business logic (authentication, JWT)
- `repository` – database access layer
- `model` – entity classes
- `config` – Spring Security configuration
- `security` – JWT filter and security-related classes

---

## Part A – Session-based Authentication (MVC)

The following features are implemented:

1. **Custom UserDetailsService**
    - Loads users from the database for authentication.

2. **SecurityFilterChain Configuration**
    - All pages require authentication except:
        - `/login`
        - `/register`

3. **Login & Logout**
    - Handled by Spring Security using HTTP sessions.

4. **CSRF Protection**
    - Enabled for form-based authentication (mandatory).

5. **Role-based Authorization**
    - `/admin/**` → `ROLE_ADMIN`
    - `/user/**` → `ROLE_USER`

This part demonstrates classic **session-based authentication** for MVC applications.

---

## Part B – Token-based Authentication (JWT / REST)

The following features are implemented:

1. **JWT Login Endpoint**
    - `POST /auth/login`
    - Returns a signed JWT on successful authentication.

2. **JWT Transport**
    - JWT is sent using:
        - `Authorization: Bearer <token>` header.

3. **JwtAuthFilter**
    - Intercepts requests.
    - Validates JWT.
    - Sets authentication in `SecurityContextHolder`.

4. **Method-level Authorization**
    - Uses `@PreAuthorize` annotations.
    - Example:
        - `@PreAuthorize("hasRole('USER')")`
        - `@PreAuthorize("hasRole('ADMIN')")`

5. **Authenticated User Access**
    - `SecurityContextHolder` is used to identify the authenticated user
      inside controllers.

This part demonstrates **stateless, token-based authentication** suitable for REST APIs.

---

## Example Endpoints

### Authentication
- `POST /auth/login` → returns JWT

### Protected Endpoints
- `GET /user` → requires `ROLE_USER`
- `GET /admin` → requires `ROLE_ADMIN`

---

## Environment Configuration

Sensitive configuration values are stored in a `.env` file, which is **not committed to Git**.

### `.env.example`
```properties
DB_URL=jdbc:sqlite:database.db
JWT_SECRET=your-secret-key