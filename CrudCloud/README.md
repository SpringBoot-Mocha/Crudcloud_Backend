<div align="center">

# CrudCloud Backend

### SaaS Platform for Automated Database Provisioning in Docker Containers

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](CONTRIBUTING.md)

[Features](#features) • [Tech Stack](#tech-stack) • [Installation](#installation) • [API Documentation](#api-endpoints) • [Testing](#testing)

</div>

---

## Overview

**CrudCloud** is a comprehensive SaaS backend platform that enables automated provisioning and management of database instances in Docker containers. Built with Spring Boot 3.5.7 and Java 21, it provides a robust REST API for managing multi-tenant database deployments with integrated payment processing through Mercado Pago.

### Key Highlights

- Automated database instance provisioning in isolated Docker containers
- Multi-engine support: MySQL, PostgreSQL, SQL Server, Redis, Cassandra, MongoDB
- Tiered subscription model with usage-based limits
- JWT-based authentication and authorization
- Secure payment processing via Mercado Pago integration
- Comprehensive REST API with OpenAPI/Swagger documentation
- Production-ready with 41 integration tests

---

## Features

- **Multi-Database Engine Support**: Deploy instances of MySQL, PostgreSQL, SQL Server, Redis, Cassandra, and MongoDB with a single API call
- **Subscription Management**: Three-tier plan system (Free, Standard, Premium) with automated billing
- **Secure Authentication**: JWT-based authentication with Spring Security 6
- **Payment Integration**: Seamless Mercado Pago integration for subscription payments
- **Instance Management**: Full lifecycle management - create, monitor, start, stop, delete database containers
- **User Management**: Complete user profile management with soft delete support
- **Transaction Tracking**: Comprehensive payment and transaction history
- **API Documentation**: Auto-generated OpenAPI 3.0 (Swagger) documentation
- **Docker Ready**: Jib plugin integration for containerized deployments
- **Production Quality**: Comprehensive integration test suite with H2 in-memory testing

---

## Tech Stack

### Core Framework
| Technology | Version | Purpose |
|-----------|---------|---------|
| **Spring Boot** | 3.5.7 | Application framework |
| **Java** | 21 | Programming language |
| **Maven** | - | Dependency management |

### Spring Ecosystem
- **Spring Data JPA** - Data persistence layer
- **Spring Security** - Authentication & authorization
- **Spring Validation** - Request validation
- **Spring Web** - REST API development
- **Spring Boot DevTools** - Development utilities

### Database & Persistence
- **PostgreSQL** - Primary production database
- **MySQL** - Alternate production database support
- **H2** - In-memory testing database
- **Hibernate** - ORM implementation

### Security & Authentication
- **JWT (JJWT)** - `0.12.3` - JSON Web Token implementation
- **Spring Security** - OAuth2/JWT integration

### Payment Processing
- **Mercado Pago SDK** - `2.1.26` - Payment gateway integration

### Utilities & Tools
- **Lombok** - Boilerplate code reduction
- **ModelMapper** - `3.2.5` - DTO mapping
- **SpringDoc OpenAPI** - `2.6.0` - API documentation
- **Jackson** - JSON serialization with JSR-310 support

### Build & Deployment
- **Jib Maven Plugin** - `3.4.6` - Docker image creation
- **Maven Compiler Plugin** - Java 21 compilation

### Testing
- **Spring Boot Test** - Integration testing framework
- **Spring Security Test** - Security testing utilities
- **JUnit 5** - Test framework
- **H2 Database** - In-memory test database

---

## Architecture

CrudCloud follows a layered architecture pattern:

```
┌─────────────────────────────────────────────────────────┐
│                     REST API Layer                       │
│              (Controllers + DTOs)                        │
├─────────────────────────────────────────────────────────┤
│                   Security Layer                         │
│     (JWT Filter + Spring Security Config)               │
├─────────────────────────────────────────────────────────┤
│                   Service Layer                          │
│         (Business Logic + Validation)                   │
├─────────────────────────────────────────────────────────┤
│                  Repository Layer                        │
│              (Spring Data JPA)                          │
├─────────────────────────────────────────────────────────┤
│                  Database Layer                          │
│                 (PostgreSQL/MySQL)                      │
└─────────────────────────────────────────────────────────┘

External Integrations:
├── Mercado Pago API (Payment Processing)
└── Docker Engine (Database Instance Management)
```

### Domain Model

- **User**: Platform users with authentication credentials
- **Plan**: Subscription tiers (Free: 2 instances, Standard: 5, Premium: 10)
- **Subscription**: User's active subscription to a plan
- **DatabaseEngine**: Available database types (MySQL, PostgreSQL, etc.)
- **DatabaseInstance**: Provisioned database containers
- **Credential**: Connection credentials for database instances
- **Transaction**: Payment and billing records

---

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java 21** or higher ([Download OpenJDK](https://openjdk.org/projects/jdk/21/))
- **Maven 3.8+** ([Installation Guide](https://maven.apache.org/install.html))
- **PostgreSQL 15+** or **MySQL 8+** (for production)
- **Docker** (for database instance provisioning)
- **Git** (for cloning the repository)

### Optional Tools
- **IntelliJ IDEA** or **Eclipse** (recommended IDEs)
- **Postman** or **Insomnia** (for API testing)
- **pgAdmin** or **DBeaver** (for database management)

---

## Installation

### 1. Clone the Repository

```bash
git clone <repository-url>
cd CrudCloud
```

### 2. Configure Database

Create a PostgreSQL database:

```sql
CREATE DATABASE crudcloud;
```

### 3. Configure Application Properties

Edit `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/crudcloud
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT Configuration
jwt.secret=your-super-secure-secret-key-change-this-in-production
jwt.expiration=86400000

# Mercado Pago Configuration
mercadopago.access.token=YOUR_MERCADOPAGO_ACCESS_TOKEN
mercadopago.public.key=YOUR_MERCADOPAGO_PUBLIC_KEY
```

### 4. Install Dependencies

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

---

## Configuration

### Environment Variables

The following environment variables can be configured:

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SPRING_DATASOURCE_URL` | Database JDBC URL | `jdbc:postgresql://localhost:5432/crudcloud` | Yes |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` | Yes |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `postgres` | Yes |
| `JWT_SECRET` | JWT signing secret | - | Yes |
| `JWT_EXPIRATION` | JWT expiration (ms) | `86400000` (24h) | No |
| `MERCADOPAGO_ACCESS_TOKEN` | Mercado Pago access token | - | Yes |
| `MERCADOPAGO_PUBLIC_KEY` | Mercado Pago public key | - | Yes |

### Profiles

The application supports different Spring profiles:

- **default**: Production configuration with PostgreSQL
- **test**: Test configuration with H2 in-memory database

Run with specific profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

---

## Running the Application

### Development Mode

```bash
# With Maven
mvn spring-boot:run

# With Java
mvn clean package
java -jar target/CrudCloud-0.0.1-SNAPSHOT.jar
```

### Production Mode

```bash
# Build the JAR
mvn clean package -DskipTests

# Run with production profile
java -jar target/CrudCloud-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Docker Deployment

Build Docker image using Jib:

```bash
# Build to Docker daemon
mvn compile jib:dockerBuild

# Run the container
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/crudcloud \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  -e JWT_SECRET=your-secret-key \
  -e MERCADOPAGO_ACCESS_TOKEN=your-token \
  crudcloud:latest
```

---

## Project Structure

```
CrudCloud/
├── src/
│   ├── main/
│   │   ├── java/com/crudzaso/CrudCloud/
│   │   │   ├── config/                    # Configuration classes
│   │   │   │   ├── JacksonConfig.java
│   │   │   │   └── ModelMapperConfig.java
│   │   │   ├── controller/                # REST Controllers (7 controllers)
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── UserController.java
│   │   │   │   ├── DatabaseInstanceController.java
│   │   │   │   ├── DatabaseEngineController.java
│   │   │   │   ├── PlanController.java
│   │   │   │   ├── SubscriptionController.java
│   │   │   │   └── PaymentController.java
│   │   │   ├── domain/                    # Domain entities & enums
│   │   │   │   ├── entity/
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── Plan.java
│   │   │   │   │   ├── Subscription.java
│   │   │   │   │   ├── DatabaseEngine.java
│   │   │   │   │   ├── DatabaseInstance.java
│   │   │   │   │   ├── Credential.java
│   │   │   │   │   └── Transaction.java
│   │   │   │   └── enums/
│   │   │   │       ├── InstanceStatus.java
│   │   │   │       └── TransactionStatus.java
│   │   │   ├── dto/                       # Data Transfer Objects
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateUserRequest.java
│   │   │   │   │   ├── UpdateUserRequest.java
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── CreateInstanceRequest.java
│   │   │   │   │   ├── UpdateInstanceStatusRequest.java
│   │   │   │   │   ├── CreateSubscriptionRequest.java
│   │   │   │   │   └── CreatePaymentRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── AuthResponse.java
│   │   │   │       ├── UserResponse.java
│   │   │   │       ├── DatabaseInstanceResponse.java
│   │   │   │       ├── DatabaseEngineResponse.java
│   │   │   │       ├── PlanResponse.java
│   │   │   │       ├── SubscriptionResponse.java
│   │   │   │       ├── TransactionResponse.java
│   │   │   │       └── ErrorResponse.java
│   │   │   ├── exception/                 # Custom exceptions
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── AppException.java
│   │   │   │   ├── BusinessException.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── UnauthorizedException.java
│   │   │   │   └── InvalidCredentialsException.java
│   │   │   ├── repository/                # JPA Repositories
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── PlanRepository.java
│   │   │   │   ├── SubscriptionRepository.java
│   │   │   │   ├── DatabaseEngineRepository.java
│   │   │   │   ├── DatabaseInstanceRepository.java
│   │   │   │   ├── CredentialRepository.java
│   │   │   │   └── TransactionRepository.java
│   │   │   ├── security/                  # Security configuration
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── CustomAuthenticationEntryPoint.java
│   │   │   │   └── CustomAccessDeniedHandler.java
│   │   │   ├── service/                   # Service interfaces
│   │   │   │   └── impl/                  # Service implementations
│   │   │   └── CrudCloudApplication.java  # Main application class
│   │   └── resources/
│   │       ├── application.properties     # Production configuration
│   │       ├── static/
│   │       └── templates/
│   └── test/
│       ├── java/com/crudzaso/CrudCloud/
│       │   └── controller/                # Integration tests (8 test classes)
│       │       ├── AuthControllerTest.java
│       │       ├── UserControllerTest.java
│       │       ├── DatabaseInstanceControllerTest.java
│       │       ├── SubscriptionControllerTest.java
│       │       ├── PaymentControllerTest.java
│       │       ├── PublicControllersTest.java
│       │       ├── BaseControllerTest.java
│       │       └── BasePublicControllerTest.java
│       └── resources/
│           └── application-test.properties # Test configuration
├── pom.xml                                # Maven dependencies
└── README.md                              # This file
```

### Package Overview

- **controller**: REST API endpoints with Swagger annotations
- **domain**: Core business entities and enums
- **dto**: Request/Response data transfer objects
- **exception**: Centralized exception handling
- **repository**: Database access layer
- **security**: JWT authentication and Spring Security configuration
- **service**: Business logic implementation

---

## API Endpoints

The API follows RESTful conventions and uses JWT Bearer token authentication for protected endpoints.

### Base URL
```
http://localhost:8080/api/v1
```

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/auth/register` | Register new user account | No |
| `POST` | `/auth/login` | Login and obtain JWT token | No |

### User Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `GET` | `/users/{id}` | Get user details by ID | Yes |
| `PUT` | `/users/{id}` | Update user information | Yes |
| `DELETE` | `/users/{id}` | Delete user account (soft delete) | Yes |

### Database Instances

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/instances` | Create new database instance | Yes |
| `GET` | `/instances` | List all user instances | Yes |
| `GET` | `/instances/{id}` | Get instance details | Yes |
| `DELETE` | `/instances/{id}` | Delete database instance | Yes |

### Subscriptions

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/subscriptions/upgrade` | Create or upgrade subscription | Yes |
| `GET` | `/subscriptions/current?userId={id}` | Get current active subscription | Yes |
| `GET` | `/subscriptions/{id}` | Get subscription by ID | Yes |

### Payments

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/payments` | Create payment transaction | Yes |
| `GET` | `/payments/{id}` | Get payment details | Yes |
| `POST` | `/payments/webhook` | Payment gateway webhook | No |

### Plans (Public)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `GET` | `/plans` | List all available plans | No |
| `GET` | `/plans/{id}` | Get plan details | No |

### Database Engines (Public)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `GET` | `/engines` | List all database engines | No |
| `GET` | `/engines/{id}` | Get engine details | No |

### API Documentation

Interactive API documentation is available via Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI specification:

```
http://localhost:8080/v3/api-docs
```

---

## Authentication

CrudCloud uses JWT (JSON Web Tokens) for stateless authentication.

### Obtaining a Token

1. Register a new user:
```bash
POST /api/v1/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "fullName": "John Doe"
}
```

2. Login to obtain JWT token:
```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "John Doe"
  }
}
```

### Using the Token

Include the JWT token in the Authorization header for protected endpoints:

```bash
GET /api/v1/users/1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Token Expiration

- Default expiration: 24 hours (86400000 ms)
- Configurable via `jwt.expiration` property
- No automatic refresh - client must re-authenticate

---

## Security

### Implemented Security Features

- **JWT Authentication**: Stateless token-based authentication
- **Password Encryption**: BCrypt hashing for password storage
- **Spring Security**: Method-level security and role-based access control
- **CORS Configuration**: Cross-Origin Resource Sharing enabled
- **Input Validation**: Jakarta Bean Validation on all request DTOs
- **SQL Injection Protection**: Parameterized queries via JPA
- **Global Exception Handling**: Secure error responses without sensitive data

### Security Configuration

Protected endpoints require valid JWT token. Public endpoints:
- `/api/v1/auth/**` - Authentication endpoints
- `/api/v1/plans/**` - Browse subscription plans
- `/api/v1/engines/**` - Browse database engines
- `/swagger-ui/**` - API documentation
- `/v3/api-docs/**` - OpenAPI specification

### Best Practices

1. **Change default JWT secret** in production
2. **Use HTTPS** in production environments
3. **Configure CORS** appropriately for your frontend
4. **Rotate JWT secrets** periodically
5. **Implement rate limiting** (not included, consider adding)
6. **Monitor authentication failures** for suspicious activity

---

## Testing

CrudCloud includes a comprehensive integration test suite with 41 test cases covering all controllers.

### Test Structure

- **8 test classes** covering all API endpoints
- **Integration tests** with real Spring context
- **H2 in-memory database** for fast test execution
- **Base test classes** for common test setup

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserControllerTest

# Run with coverage
mvn clean test jacoco:report

# Skip tests during build
mvn clean package -DskipTests
```

### Test Coverage

| Component | Test Class | Test Count |
|-----------|-----------|------------|
| Authentication | `AuthControllerTest` | 5 tests |
| Users | `UserControllerTest` | 6 tests |
| Database Instances | `DatabaseInstanceControllerTest` | 7 tests |
| Subscriptions | `SubscriptionControllerTest` | 6 tests |
| Payments | `PaymentControllerTest` | 5 tests |
| Public Endpoints | `PublicControllersTest` | 12 tests |

### Test Configuration

Tests use a separate configuration:
- **Database**: H2 in-memory (`jdbc:h2:mem:testdb`)
- **Profile**: `test`
- **Security**: Mock JWT authentication
- **Properties**: `application-test.properties`

### Sample Test Execution

```bash
mvn test

[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.crudzaso.CrudCloud.controller.AuthControllerTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.crudzaso.CrudCloud.controller.UserControllerTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.crudzaso.CrudCloud.controller.DatabaseInstanceControllerTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] Results:
[INFO] Tests run: 41, Failures: 0, Errors: 0, Skipped: 0
```

---

## Subscription Plans

CrudCloud offers three subscription tiers:

| Plan | Price | Max Instances | Features |
|------|-------|---------------|----------|
| **Free** | $0/month | 2 instances | Basic database support, Community support |
| **Standard** | $29/month | 5 instances | All databases, Email support, 99.5% SLA |
| **Premium** | $99/month | 10 instances | Priority support, 99.9% SLA, Advanced monitoring |

### Supported Database Engines

- **MySQL** (5.7, 8.0)
- **PostgreSQL** (13, 14, 15, 16)
- **SQL Server** (2019, 2022)
- **Redis** (6.x, 7.x)
- **Cassandra** (4.x)
- **MongoDB** (5.x, 6.x)

---

## Docker Integration

### Building with Jib

CrudCloud uses Google Jib for optimized Docker image creation:

```bash
# Build to Docker daemon
mvn compile jib:dockerBuild

# Build and push to registry
mvn compile jib:build -Dimage=your-registry/crudcloud:latest
```

### Docker Compose Example

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: crudcloud
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  crudcloud:
    image: crudcloud:latest
    depends_on:
      - postgres
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/crudcloud
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      JWT_SECRET: your-production-secret-key
      MERCADOPAGO_ACCESS_TOKEN: your-token
      MERCADOPAGO_PUBLIC_KEY: your-key
    ports:
      - "8080:8080"

volumes:
  postgres_data:
```

Run with:
```bash
docker-compose up -d
```

---

## Troubleshooting

### Common Issues

#### Database Connection Errors

```
ERROR: Connection to localhost:5432 refused
```

**Solution**: Ensure PostgreSQL is running and accessible:
```bash
# Check PostgreSQL status
sudo systemctl status postgresql

# Start PostgreSQL
sudo systemctl start postgresql
```

#### JWT Token Invalid

```
401 Unauthorized - Invalid or expired token
```

**Solution**: Token may be expired or malformed:
- Re-authenticate to obtain new token
- Check token expiration time
- Verify JWT secret is consistent

#### Port Already in Use

```
Port 8080 was already in use
```

**Solution**: Change port in `application.properties`:
```properties
server.port=8081
```

#### Maven Build Fails

```
Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin
```

**Solution**: Verify Java version:
```bash
java -version  # Should be Java 21
mvn -version   # Should use Java 21
```

### Logging

Adjust logging levels in `application.properties`:

```properties
# Enable debug logging
logging.level.root=DEBUG
logging.level.com.crudzaso=DEBUG
logging.level.org.springframework.security=DEBUG
```

---

## Contributing

We welcome contributions! Please follow these guidelines:

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/your-feature-name`
3. **Write tests** for new functionality
4. **Follow code style**: Use existing code as reference
5. **Commit with clear messages**: `git commit -m "Add feature: description"`
6. **Push to your fork**: `git push origin feature/your-feature-name`
7. **Create Pull Request** with detailed description

### Code Standards

- Follow Java naming conventions
- Use Lombok for boilerplate reduction
- Write JavaDoc for public methods
- Maintain test coverage above 80%
- Use SLF4J for logging
- Follow REST API best practices

### Development Setup

1. Install Java 21 and Maven
2. Import project into IntelliJ IDEA
3. Enable Lombok annotation processing
4. Configure code style (Spring conventions)
5. Run tests before committing

---

## Roadmap

Future enhancements planned for CrudCloud:

- [ ] Real Docker container orchestration
- [ ] Database backup and restore functionality
- [ ] Multi-region deployment support
- [ ] Custom database configurations
- [ ] Monitoring and alerting system
- [ ] Database migration tools
- [ ] GraphQL API support
- [ ] WebSocket for real-time updates
- [ ] Admin dashboard
- [ ] Usage analytics and reporting

See [ROADMAP.md](md/ROADMAP.md) for detailed plans.

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Team

### Core Contributors

- **Development Team** - Backend development and architecture
- **QA Team** - Testing and quality assurance
- **DevOps Team** - Infrastructure and deployment

### Contact

- **Project Repository**: [GitHub](<repository-url>)
- **Issue Tracker**: [GitHub Issues](<repository-url>/issues)
- **Documentation**: [Wiki](<repository-url>/wiki)

---

## Acknowledgments

- **Spring Framework Team** - For the excellent Spring Boot framework
- **Mercado Pago** - For payment processing integration
- **Docker Community** - For containerization technology
- **Open Source Community** - For the amazing libraries and tools

---

<div align="center">

**Built with Spring Boot and Java**

Made with dedication by the CrudCloud Team

[Report Bug](<repository-url>/issues) • [Request Feature](<repository-url>/issues)

</div>
