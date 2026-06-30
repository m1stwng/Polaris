# ❄️ Polaris

Polaris is an e-commerce backend application built with Java 25 and Spring Framework as a hands-on learning project.
It serves as a practical environment for exploring backend development, software architecture and security concepts 
applying knowledge gained from books such as _Spring Starts Here_ and _Spring Security in Action_, along with official
documentation and other learning resources.

## 🔧Tech Stack

- **Java 17**
- **Spring Boot 4**
- **Spring Boot DevTools**
- **Spring Security 7**
- **Spring Data JPA**
- **PostgreSQL**
- **Flyway**
- **JWT (java-jwt 4.5.2)** - JSON Web Token implementation
- **Maven**
- **Lombok**
- **OpenAPI/SpringDoc**
- **Bean Validation**
- **MapStruct**
- **JUnit, Mockito and TestContainers**
- **Docker**

## 📂 Project Structure

```text
src
+-- main
|   +-- java/dev/m1stwng/polaris
|   |   +-- auth              # Authentication API, DTOs, services, exceptions
|   |   +-- common            # Shared persistence, normalization, exception handling
|   |   +-- config/openapi    # Swagger/OpenAPI configuration and reusable responses
|   |   +-- identity          # User and role domain
|   |   +-- security          # JWT filter, security config, user details
|   |   +-- token             # Refresh token entity, repository, service
|   +-- resources
|       +-- application.properties
|       +-- db/migration      # Flyway database migrations
+-- test
    +-- java/dev/m1stwng/polaris
        +-- auth
        +-- identity
        +-- fixture
        +-- annotation
```

## ⚙️API Endpoints

### Authentication

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/v1/auth/register` | Create a new user and return tokens |
| `POST` | `/api/v1/auth/login` | Authenticate an existing user |
| `POST` | `/api/v1/auth/refresh` | Issue a new access token from a refresh token |
| `POST` | `/api/v1/auth/logout` | Revoke a refresh token |

### Users

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/v1/users/me` | Return the authenticated user's profile |

## 🚀Getting Started

### Prerequisites

- **JDK 25**
- **Docker**, recommended for **PostgreSQL** and **Testcontainers**

### Environment Variables

The application reads these settings from environment variables, with development defaults where configured:

| Variable | Description | Default |
| --- | --- | --- |
| `JWT_SECRET` | Secret used to sign JWT access tokens | `my-very-jwt-secret` |
| `ACCESS_TOKEN_EXPIRATION_SECONDS` | Access token lifetime in seconds | `900` |
| `REFRESH_TOKEN_EXPIRATION_DAYS` | Refresh token lifetime in days | `7` |

You also need to provide PostgreSQL datasource settings for local runtime, for example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/polaris
spring.datasource.username=postgres
spring.datasource.password=postgres
```

These can be placed in a local profile, passed as environment variables, or supplied through your IDE run configuration.

### Run the Application

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS/Linux:

```bash
./mvnw spring-boot:run
```

The API starts on the default Spring Boot port:

```text
http://localhost:8080
```

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

## 🧪 Testing

Run the full test suite:

```powershell
.\mvnw.cmd test
```

The project includes both unit tests and integration tests. Integration tests use Testcontainers, so Docker should be running before executing them.

## Learning Goals

Polaris is being built as a practical way to study backend development through an e-commerce domain. The project emphasizes:

- Clean layering between controllers, services, repositories, and entities
- Stateless authentication and refresh token handling
- Database schema evolution with Flyway
- API documentation with OpenAPI
- Validation and consistent error responses
- Testable business logic and integration flows

## Status

Polaris is under active development. The current implementation focuses on authentication, user identity, and persistence foundations for the future e-commerce modules.
