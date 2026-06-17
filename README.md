# NeighborAlert

A microservices-based community platform for reporting and managing neighborhood issues. Citizens can submit reports, leave comments, and track resolution progress, while administrators manage assignments and update statuses.

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
- RabbitMQ (async communication, Saga pattern)
- MySQL
- React 18 (Vite)
- Maven

## Getting Started

### Prerequisites
- Java 21
- Maven
- MySQL running on `localhost:3306`
- RabbitMQ running on `localhost:5672`
- Node.js 18+

### Running locally

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

Frontend is available at `http://localhost:3000`.

All API requests go through the gateway at `http://localhost:8080`.

### Running with Docker
#### Prerequisites
- Docker Desktop

### Steps
 
```bash
# 1. Build JAR files for all services
cd eureka-server && mvn clean package -DskipTests && cd ..
cd api-gateway && mvn clean package -DskipTests && cd ..
cd user-service && mvn clean package -DskipTests && cd ..
cd report-service && mvn clean package -DskipTests && cd ..
cd interaction-service && mvn clean package -DskipTests && cd ..
cd administration-service && mvn clean package -DskipTests && cd ..
 
# 2. Create environment file
cp .env.example .env
 
# 3. Start all services
docker compose up --build
```
 
##### On subsequent runs (no code changes):
```bash
docker compose up
```
 
##### To stop:
```bash
docker compose down
```
### Docker URLs
 
| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| API Gateway | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |

### Team members
+ Gičević Ajša
+ Hadžić Lejla
+ Rokša Amina
+ Smajović Azra

[Application Demo](https://drive.google.com/file/d/1vim-1iT99W_n2qPI20v7Hx_TP8iFVMTs/view?usp=sharing)
