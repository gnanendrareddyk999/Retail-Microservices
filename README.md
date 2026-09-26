# Retail Microservices

A Spring Boot based Retail Microservices application consisting of User Service and Retailer Service.

The application uses PostgreSQL for database management, JWT for authentication, Docker for containerization, Docker Compose for running all services together, and Postman for API testing.

---
## GitHub Repository
This project is maintained using Git and GitHub.
---

## 1. Project Overview

The Retail Microservices application is divided into two main microservices:

1. User Service
2. Retailer Service

The application also uses:

- PostgreSQL Database
- JWT Authentication
- Swagger / OpenAPI
- Docker
- Docker Compose
- Postman

---

## 2. Architecture

```text
                         Client
                           |
                           |
                    +------+------+
                    |             |
                    v             v
              User Service   Retailer Service
                 :8081            :8082
                    |             |
                    |             |
                    +------+------+
                           |
                           v
                      PostgreSQL
                         :5432
                           |
                 +---------+---------+
                 |                   |
              user_db           retailer_db