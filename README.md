# Web Security Application – Lab 10

This project is developed for the course **Security of Web Applications L2 [W.SIE.IN.5020]**.  
The goal of Lab 10 is to create a basic HTTP application server and prepare a clean project structure for future security features.

The application is built using **Spring Boot** and includes database integration, migration support, and basic security configuration.

---

## Technologies Used

- Java (JDK 17 / 23)
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- Flyway Migration
- SQLite
- Maven
- Git & GitHub

---

## Project Structure

The project follows a layered architecture:

- `controller` – handles HTTP requests
- `service` – contains business logic
- `repository` – database access layer
- `model` – entity classes
- `config` – security configuration

This structure makes the project easier to understand and extend in future labs.

---

## Environment Configuration

Sensitive configuration values are stored in a `.env` file, which is **not committed to Git**.

### `.env.example`
```properties
DB_URL=