# 🚀 Team Task Management API

RESTful API for managing team tasks, users, and comments with JWT authentication, refresh tokens, Redis caching, rate limiting, and Apache Kafka event streaming.

![Java](https://img.shields.io/badge/Java-17%2B-blue?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?logo=springboot)
![License](https://img.shields.io/badge/License-Apache%202.0-green)
![Build](https://img.shields.io/badge/Build-Maven%20Ready-orange?logo=apachemaven)
![Database](https://img.shields.io/badge/Database-MySQL%208.0-blue?logo=mysql)
![Redis](https://img.shields.io/badge/Cache-Redis-DC382D?logo=redis)
![JWT](https://img.shields.io/badge/Security-JWT%20%2B%20Refresh-black?logo=jsonwebtokens)
![Kafka](https://img.shields.io/badge/Streaming-Apache%20Kafka-231F20?logo=apachekafka)
![Tests](https://img.shields.io/badge/Tests-JUnit%205%20%2B%20Mockito%20%2B%20Testcontainers-25A162?logo=junit5)

---

## 📑 Table of Contents

- [🚀 Team Task Management API](#-team-task-management-api)
  - [📑 Table of Contents](#-table-of-contents)
  - [📖 About the Project](#-about-the-project)
  - [✨ Key Features](#-key-features)
  - [🛠 Technology Stack](#-technology-stack)
  - [⚙️ Configuration](#️-configuration)
  - [🌐 API Endpoints](#-api-endpoints)
    - [1. Authentication (`/auth`)](#1-authentication-auth)
    - [2. Users (`/users`)](#2-users-users)
    - [3. Tasks (`/tasks`)](#3-tasks-tasks)
    - [4. Comments (`/comments`)](#4-comments-comments)
  - [📨 Apache Kafka Event Streaming](#-apache-kafka-event-streaming)
  - [🧠 Caching Strategy (Redis)](#-caching-strategy-redis)
  - [🛡 Rate Limiting](#-rate-limiting)
  - [🧪 Testing](#-testing)
    - [Testing Stack](#testing-stack)
    - [Test Structure](#test-structure)
    - [Test Coverage](#test-coverage)
    - [Running Tests](#running-tests)

---

## 📖 About the Project

**Team Task Management API** is a production-ready backend application for team task tracking. It provides secure JWT-based authentication with access/refresh tokens, role-based access control (USER/ADMIN), task and comment management, Redis caching for high performance, rate limiting to protect public endpoints, and **Apache Kafka** integration for event-driven notifications and audit logging.

---

## ✨ Key Features

- **🔐 JWT + Refresh Token Auth:** Access tokens (short-lived) and refresh tokens (stored in Redis) with dedicated `/auth/refresh` and `/auth/logout` endpoints.
- **🛡 Rate Limiting:** IP-based request limiting on `/auth/login` and `/auth/register` to prevent brute-force attacks.
- **👥 Role-Based Access Control (RBAC):** USER and ADMIN roles with granular endpoint permissions.
- **📋 Task Lifecycle:** CRUD operations with priority (`LOW`, `MEDIUM`, `HIGH`) and status (`PENDING`, `IN_PROGRESS`, `DONE`).
- **💬 Comments:** Add comments to tasks; users delete their own, admins delete any.
- **⚡ Redis Caching:** Frequently accessed data (tasks, users) cached with automatic eviction on updates/deletes.
- **📨 Kafka Event Streaming:** Asynchronous event publishing on task/user/comment changes. Automatic notifications on status and priority transitions. Dedicated audit log topic for change tracking.
- **🖥 Kafka UI:** Built-in web interface at `localhost:8085` for real-time message browsing across all topics.
- **📚 Swagger UI:** Interactive API documentation with full request/response examples.
- **🧩 Layered Architecture:** Clean separation between controllers, services, repositories, and security.
- **🧪 Comprehensive Testing:** Unit, integration, and Kafka tests with JUnit 5, Mockito, Testcontainers, and Embedded Kafka.

---

## 🛠 Technology Stack

| Technology | Purpose |
|:---|:---|
| **Java 17** | Core programming language (LTS). |
| **Spring Boot 3.2** | Application framework with auto-configuration. |
| **Spring Security** | Authentication and authorization with JWT filter chain. |
| **Spring Data JPA** | ORM and repository abstraction over Hibernate. |
| **MySQL 8.0** | Primary relational database. |
| **Redis** | Caching, refresh token storage, and rate limiting counters. |
| **Spring Cache** | Declarative caching abstraction (`@Cacheable`, `@CacheEvict`, `@CachePut`). |
| **Apache Kafka** | Distributed event streaming platform for async messaging. |
| **Spring Kafka** | Spring integration for Kafka producers/consumers. |
| **Docker Compose** | Container orchestration for Kafka + Zookeeper + Kafka UI. |
| **JWT (io.jsonwebtoken)** | Token generation, parsing, and validation. |
| **SpringDoc OpenAPI** | Swagger UI generation from annotations. |
| **BCrypt** | Password hashing. |
| **Jakarta Validation** | DTO input validation. |
| **Lombok** | Boilerplate reduction. |
| **Maven** | Build and dependency management. |

---

## ⚙️ Configuration

### Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **MySQL 8.0** running on `localhost:3306`
- **Redis** running on `localhost:6379`
- **Docker & Docker Compose** (for Kafka infrastructure)

### Kafka Infrastructure Setup

```bash
# Start  Redis, Zookeeper, Kafka, and Kafka UI
docker-compose up -d
This launches:

Redis on localhost:6379

Zookeeper on localhost:2181

Kafka Broker on localhost:9092

Kafka UI on http://localhost:8085

🌐 API Endpoints
Full interactive docs available at http://localhost:8080/swagger-ui.html after startup.

1. Authentication (/auth)
Method	Endpoint	Description	Rate Limited
POST	/auth/register	Register new user	✅
POST	/auth/login	Login, returns access + refresh tokens	✅
POST	/auth/refresh	Exchange refresh token for new access token	❌
POST	/auth/logout	Invalidate refresh token	❌
2. Users (/users)
Method	Endpoint	Role
GET	/users/me	Any authenticated
GET	/users/admin/all	ADMIN
DELETE	/users/admin/{id}	ADMIN
3. Tasks (/tasks)
Method	Endpoint	Role
POST	/tasks/create	Any authenticated
GET	/tasks/myTasks	Any authenticated
GET	/tasks/myTasks/{id}	Owner only
PUT	/tasks/myTasks/{id}	Owner only
DELETE	/tasks/myTasks/{id}	Owner only
GET	/tasks/admin/allTasks	ADMIN
GET	/tasks/admin/task/{id}	ADMIN
PUT	/tasks/admin/task/{id}	ADMIN
DELETE	/tasks/admin/task/{id}	ADMIN
4. Comments (/comments)
Method	Endpoint	Role
POST	/comments/create/{taskId}	Any authenticated
DELETE	/comments/delete/{id}	Comment owner
DELETE	/comments/admin/delete/{id}	ADMIN
📨 Apache Kafka Event Streaming
The application publishes events asynchronously to Apache Kafka whenever critical domain changes occur. This enables loose coupling, real-time notifications, and comprehensive audit trails without blocking the main request-response cycle.

Architecture Overview
text
[Service Layer] → EventPublisherService → KafkaProducerService → Apache Kafka Topics
                                                                       ↓
                                                              [Future Consumers]
                                                         (Notifications, Analytics, Audit)
json
{
  "taskId": 42,
  "taskTitle": "Implement login page",
  "oldValue": "PENDING",
  "newValue": "IN_PROGRESS",
  "changedField": "status",
  "message": "Task 'Implement login page' status changed from PENDING to IN_PROGRESS",
  "timestamp": "2026-06-05T14:30:00"
}
Kafka UI
Monitor all topics and messages in real-time at:

🔗 http://localhost:8085

The UI provides:

Topic list with message counts

Message browsing with key/value/header inspection

Live tail mode for real-time monitoring

🧠 Caching Strategy (Redis)
The application uses Redis via Spring Cache abstraction to reduce database load.

Cached Data
Cache Name Cached Method(s) Eviction Trigger
tasks allTasks(), myTasks() saveTask(), updateTask(), deleteTask()
task findTaskById(id) updateTask(id), deleteTask(id)
users getCurrentUser(), getAllUsers(), findByName(), findById() deleteUser(id)
Cache Annotations Used
@Cacheable — stores method result in Redis.

@CachePut — updates cache entry after method execution.

@CacheEvict — removes entries on data changes.

@Caching — combines multiple cache operations.

🛡 Rate Limiting
Rate limiting is implemented using Redis counters with IP-based keys.

Protected Endpoints
Endpoint Limit Window
POST /auth/login 3 attempts 60 seconds
POST /auth/register 3 attempts 60 seconds

🧪 Testing
Testing Stack
Technology	Purpose
JUnit 5	Test framework
Mockito	Mocking dependencies
AssertJ	Fluent assertions
Spring Security Test	Security context mocking
@DataJpaTest	Repository layer isolation with H2 in-memory database
@WebMvcTest	Controller layer isolation with MockMvc
Testcontainers	Real MySQL and Redis for integration tests
Embedded Kafka	In-memory Kafka broker for Kafka tests
Spring Kafka Test	Kafka test utilities
Awaitility	Async assertion waiting
Test Structure
```text
src/test/java/com/example/team_task/
├── controller/
│   ├── AuthControllerTest.java              @WebMvcTest + MockMvc
│   ├── TaskControllerTest.java
│   ├── UserControllerTest.java
│   ├── CommentControllerTest.java
│   ├── NotificationControllerTest.java
│   └── AdminAuditControllerTest.java
├── service/
│   ├── AuthServiceTest.java                 @ExtendWith(MockitoExtension) unit
│   ├── JwtServiceTest.java                  Pure unit without Spring
│   ├── UserServiceTest.java
│   ├── TaskServiceTest.java
│   ├── CommentServiceTest.java
│   ├── EventPublisherServiceTest.java
│   ├── KafkaProducerServiceTest.java
│   ├── AuditLogServiceTest.java
│   ├── NotificationServiceTest.java
│   ├── RateLimitServiceTest.java
│   └── RefreshTokenServiceTest.java
├── repository/
│   ├── UserRepositoryTest.java              @DataJpaTest + H2
│   ├── TaskRepositoryTest.java
│   ├── NotificationRepositoryTest.java
│   └── AuditLogRepositoryTest.java
├── kafka/
│   ├── AuditLogConsumerTest.java            @ExtendWith(MockitoExtension)
│   ├── NotificationConsumerTest.java
│   └── KafkaIntegrationTest.java            @EmbeddedKafka integration
├── integration/
│   ├── TaskServiceIntegrationTest.java      @SpringBootTest transactional
│   ├── AuthServiceIntegrationTest.java
│   └── RedisIntegrationTest.java            Testcontainers Redis
└── TestingApplicationTests.java              Spring context load test
```
Test Coverage
Layer	Type	Count	Coverage
Services	Unit	11 classes	All public methods, edge cases, exceptions
Controllers	Web layer	6 classes	200, 201, 204, 400, 401, 403, 404, 409, 423
Repositories	Data layer	4 classes	All custom query methods
Kafka	Unit + Integration	3 classes	Producers, consumers, embedded broker
Integration	E2E	3 classes	Redis, DB, Kafka end-to-end
Total		27 test classes	Full coverage
Running Tests
bash
# All tests
mvn clean test

# Specific test class
mvn test -Dtest=AuthServiceTest

# Specific test method
mvn test -Dtest=AuthServiceTest#saveUser_ValidRequest_CreatesUserSuccessfully

# Run only unit tests (exclude integration)
mvn test -Dtest="*Test" -DfailIfNoTests=false

# Run only integration tests
mvn test -Dtest="*IntegrationTest"

# Run a test suite
mvn test -Dtest=AllTestsSuite

# Run with test profile
mvn test -Dspring.profiles.active=test

# Parallel execution (4 threads)
mvn test -T 4

# Generate test reports
mvn clean test
open target/surefire-reports/index.html