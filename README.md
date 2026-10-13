# CanteenGO Backend

Spring Boot 3.2.5 REST backend for the CanteenGO college food ordering system. Built with Java 17, Maven, and MySQL.

## Features

- **Authentication**: Custom Bearer token authentication with BCrypt password hashing.
- **Role-Based Access**: Public endpoints, student authenticated ordering, and admin management endpoints.
- **Admin Bootstrap**: Safe seeding of initial administrator from environment variables; existing admin accounts are never overwritten.
- **Production-Ready**: Configurable CORS, database TLS support, containerized via Dockerfile.

## Tech Stack

- Java 17
- Spring Boot 3.2.5 (Spring Web, Spring Data JPA, Spring Security Crypto, Validation)
- MySQL 8.0 (`mysql-connector-j`)
- Maven 3.9+

## Configuration & Environment Variables

| Variable | Description | Default / Example |
|---|---|---|
| `PORT` | HTTP server port (automatically injected by PaaS such as Render, Railway, Heroku) | `8080` |
| `DB_URL` | JDBC connection string | `jdbc:mysql://localhost:3306/canteengo?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `DB_USER` | Database username | `root` |
| `DB_PASSWORD` | Database password | `root` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated list of allowed frontend origins (no trailing slash) | `http://localhost:5173,http://localhost:3000` |
| `ADMIN_EMAIL` | Initial admin email (required on fresh DB) | `admin@canteengo.com` |
| `ADMIN_PASSWORD` | Initial admin password (required on fresh DB) | `your_secure_password` |

### Production Database TLS

When connecting to managed cloud databases (such as AWS RDS, Aiven, or PlanetScale), enforce TLS by setting `sslMode=REQUIRED` in `DB_URL`:

```text
jdbc:mysql://<host>:<port>/<database>?createDatabaseIfNotExist=true&sslMode=REQUIRED&serverTimezone=UTC
```

### Production CORS

Add your deployed frontend URL(s) to `CORS_ALLOWED_ORIGINS`:

```text
CORS_ALLOWED_ORIGINS=https://canteengo.vercel.app,http://localhost:5173
```

## Running Locally

### Prerequisites

- Java 17 JDK
- Maven 3.8+
- MySQL 8 running locally

### Commands

Run directly with Spring Boot:

```bash
mvn spring-boot:run
```

Build the executable JAR (`target/canteengo-1.0.0.jar`):

```bash
mvn -B package -DskipTests
```

Run the built JAR:

```bash
java -jar target/canteengo-1.0.0.jar
```

## Docker Containerization

Build the multi-stage Docker image:

```bash
docker build -t canteengo-backend .
```

Run container:

```bash
docker run -p 8080:8080 \
  -e DB_URL="jdbc:mysql://host.docker.internal:3306/canteengo?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
  -e DB_USER="root" \
  -e DB_PASSWORD="secretpassword" \
  -e ADMIN_EMAIL="admin@canteengo.com" \
  -e ADMIN_PASSWORD="adminpassword" \
  canteengo-backend
```

## REST API Overview

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register new student user |
| `POST` | `/api/auth/login` | Public | Login (returns bearer token and user role) |
| `GET` | `/api/foods` | Public | List all available food items |
| `POST` | `/api/orders` | Student | Place order with cart items |
| `GET` | `/api/orders/my` | Student | View order history and status |
| `GET` | `/api/admin/foods` | Admin | List all food items (including unavailable) |
| `POST` | `/api/admin/foods` | Admin | Create a new food item |
| `PUT` | `/api/admin/foods/{id}` | Admin | Update food item details or availability |
| `DELETE` | `/api/admin/foods/{id}` | Admin | Delete food item |
| `GET` | `/api/admin/orders` | Admin | View all incoming and existing orders |
| `PUT` | `/api/admin/orders/{id}/status` | Admin | Update order status (`PENDING`, `PREPARING`, `READY`, `COMPLETED`) |
