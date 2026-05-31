# 🚀 Team Task Management API

RESTful API for managing team tasks, users, and comments with JWT authentication, refresh tokens, Redis caching, and rate limiting.

![Java](https://img.shields.io/badge/Java-17%2B-blue?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?logo=springboot)
![License](https://img.shields.io/badge/License-Apache%202.0-green)
![Build](https://img.shields.io/badge/Build-Maven%20Ready-orange?logo=apachemaven)
![Database](https://img.shields.io/badge/Database-MySQL%208.0-blue?logo=mysql)
![Redis](https://img.shields.io/badge/Cache-Redis-DC382D?logo=redis)
![JWT](https://img.shields.io/badge/Security-JWT%20%2B%20Refresh-black?logo=jsonwebtokens)

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
  - [🧠 Caching Strategy (Redis)](#-caching-strategy-redis)
  - [🛡 Rate Limiting](#-rate-limiting)


---

## 📖 About the Project

**Team Task Management API** is a production-ready backend application for team task tracking. It provides secure JWT-based authentication with access/refresh tokens, role-based access control (USER/ADMIN), task and comment management, Redis caching for high performance, and rate limiting to protect public endpoints.

---

## ✨ Key Features

- **🔐 JWT + Refresh Token Auth:** Access tokens (short-lived) and refresh tokens (stored in Redis) with dedicated `/auth/refresh` and `/auth/logout` endpoints.
- **🛡 Rate Limiting:** IP-based request limiting on `/auth/login` and `/auth/register` to prevent brute-force attacks.
- **👥 Role-Based Access Control (RBAC):** USER and ADMIN roles with granular endpoint permissions.
- **📋 Task Lifecycle:** CRUD operations with priority (`LOW`, `MEDIUM`, `HIGH`) and status (`PENDING`, `IN_PROGRESS`, `DONE`).
- **💬 Comments:** Add comments to tasks; users delete their own, admins delete any.
- **⚡ Redis Caching:** Frequently accessed data (tasks, users) cached with automatic eviction on updates/deletes.
- **📚 Swagger UI:** Interactive API documentation with full request/response examples.
- **🧩 Layered Architecture:** Clean separation between controllers, services, repositories, and security.

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
| **JWT (io.jsonwebtoken)** | Token generation, parsing, and validation. |
| **SpringDoc OpenAPI** | Swagger UI generation from annotations. |
| **BCrypt** | Password hashing. |
| **Jakarta Validation** | DTO input validation. |
| **Lombok** | Boilerplate reduction. |
| **Maven** | Build and dependency management. |

---



text

---


## 🌐 API Endpoints

Full interactive docs available at `http://localhost:8080/swagger-ui.html` after startup.

### 1. Authentication (`/auth`)

| Method | Endpoint | Description | Rate Limited |
|:---|:---|:---|:---|
| `POST` | `/auth/register` | Register new user | ✅ |
| `POST` | `/auth/login` | Login, returns access + refresh tokens | ✅ |
| `POST` | `/auth/refresh` | Exchange refresh token for new access token | ❌ |
| `POST` | `/auth/logout` | Invalidate refresh token | ❌ |

### 2. Users (`/users`)

| Method | Endpoint | Role |
|:---|:---|:---|
| `GET` | `/users/me` | Any authenticated |
| `GET` | `/users/admin/all` | ADMIN |
| `DELETE` | `/users/admin/{id}` | ADMIN |

### 3. Tasks (`/tasks`)

| Method | Endpoint | Role |
|:---|:---|:---|
| `POST` | `/tasks/create` | Any authenticated |
| `GET` | `/tasks/myTasks` | Any authenticated |
| `GET` | `/tasks/myTasks/{id}` | Owner only |
| `PUT` | `/tasks/myTasks/{id}` | Owner only |
| `DELETE` | `/tasks/myTasks/{id}` | Owner only |
| `GET` | `/tasks/admin/allTasks` | ADMIN |
| `GET` | `/tasks/admin/task/{id}` | ADMIN |
| `PUT` | `/tasks/admin/task/{id}` | ADMIN |
| `DELETE` | `/tasks/admin/task/{id}` | ADMIN |

### 4. Comments (`/comments`)

| Method | Endpoint | Role |
|:---|:---|:---|
| `POST` | `/comments/create/{taskId}` | Any authenticated |
| `DELETE` | `/comments/delete/{id}` | Comment owner |
| `DELETE` | `/comments/admin/delete/{id}` | ADMIN |

---

## 🧠 Caching Strategy (Redis)

The application uses **Redis** via Spring Cache abstraction to reduce database load.

### Cached Data

| Cache Name | Cached Method(s) | Eviction Trigger |
|:---|:---|:---|
| `tasks` | `allTasks()`, `myTasks()` | `saveTask()`, `updateTask()`, `deleteTask()` |
| `task` | `findTaskById(id)` | `updateTask(id)`, `deleteTask(id)` |
| `users` | `getCurrentUser()`, `getAllUsers()`, `findByName()`, `findById()` | `deleteUser(id)` |

### Cache Annotations Used

- `@Cacheable` — stores method result in Redis.
- `@CachePut` — updates cache entry after method execution.
- `@CacheEvict` — removes entries on data changes.
- `@Caching` — combines multiple cache operations.

---

## 🛡 Rate Limiting

Rate limiting is implemented using **Redis** counters with IP-based keys.

### Protected Endpoints

| Endpoint | Limit | Window |
|:---|:---|:---|
| `POST /auth/login` | 3 attempts | 60 seconds |
| `POST /auth/register` | 3 attempts | 60 seconds |

On exceeding the limit, the API returns HTTP `429 Too Many Requests`.
