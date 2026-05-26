# NeighborAlert

A microservices-based application for reporting and managing neighborhood issues.

## Services

| Service | Port | Description |
|---------|------|-------------|
| api-gateway | 8080 | Single entry point, JWT authentication |
| user-service | 8081 | User management and authentication |
| report-service | 8082 | Issue reports management |
| interaction-service | 8083 | Comments, notifications, flags |
| administration-service | 8084 | Report assignment and administration |
| eureka-server | 8761 | Service discovery and registry |

## Tech Stack

- Java 21
- Spring Boot 3.4.1
- Spring Cloud 2024.0.0 (Gateway, Eureka, OpenFeign)
- Spring Security + JWT (jjwt 0.12.6)
- MySQL
- Maven

## Getting Started

### Prerequisites
- Java 21
- MySQL running on localhost:3306
- Maven

### Running the application

Start services in this order:

```bash
# 1. Service discovery
cd eureka-server && ./mvnw spring-boot:run

# 2. Core services
cd user-service && ./mvnw spring-boot:run
cd report-service && ./mvnw spring-boot:run
cd interaction-service && ./mvnw spring-boot:run
cd administration-service && ./mvnw spring-boot:run

# 3. Gateway 
cd api-gateway && ./mvnw spring-boot:run

# 4. Frontend
cd frontend && npm run dev
```

All external requests go through the gateway on port **8080**.
