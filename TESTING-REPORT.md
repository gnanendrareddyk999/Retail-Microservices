# Retail Microservices — Final Testing Report

## 1. Testing Overview

The Retail Microservices application was tested across unit, integration, API, end-to-end, security/authorization, containerization, database connectivity, and CI/CD automation layers.

The objective was to verify that the microservices work correctly individually, together, and through the automated CI pipeline.

### Overall Result

**All planned functional and CI automated testing completed successfully.**

---

## 2. Application Under Test

The application consists of two Spring Boot microservices:

1. User Service — `8081`
2. Retailer Service — `8082`

Supporting components:

- PostgreSQL — `5432`
- JWT authentication
- Swagger / OpenAPI
- Docker
- Docker Compose
- Postman / Newman
- GitHub Actions CI

---

## 3. Test Results Summary

| Testing Area | Result |
|---|---|
| User Service automated tests | 18/18 PASS |
| Retailer Service automated tests | 75/75 PASS |
| Total service automated tests | 93/93 PASS |
| Postman/Newman API requests | 35/35 PASS |
| Newman assertions | 3124/3124 PASS |
| PostgreSQL connectivity | PASS |
| Docker Compose testing | PASS |
| User Service API verification | PASS |
| Retailer Service API verification | PASS |
| Swagger/OpenAPI verification | PASS |
| JWT authentication testing | PASS |
| Role-based authorization testing | PASS |
| End-to-end business-flow automation | PASS |
| GitHub Actions CI | PASS |

---

## 4. Unit Testing

Unit-level automated testing was completed for the application services.

### Results

| Service | Tests | Passed | Failed |
|---|---:|---:|---:|
| User Service | 18 | 18 | 0 |
| Retailer Service | 75 | 75 | 0 |
| **Total** | **93** | **93** | **0** |

**Result: PASS**

---

## 5. Integration Testing

Integration testing verified communication between application components and the PostgreSQL databases.

The following areas were verified:

- Service startup
- Database connectivity
- Repository/database interaction
- Authentication flow
- Service-level business operations
- Inter-service business flows

**Result: PASS**

---

## 6. API Testing

API testing was performed using the Postman collection and Newman CLI.

The automated collection contains:

- Login flows
- User profile operations
- Registration scenarios
- Inventory operations
- Order creation
- Order retrieval
- Order item retrieval
- Order status updates
- Negative/authorization scenarios

### Newman Result

| Metric | Result |
|---|---:|
| Requests | 35 |
| Passed | 35 |
| Failed | 0 |
| Assertions | 3124 |
| Failed assertions | 0 |

**Result: 35/35 requests PASS**

---

## 7. Negative and Boundary Testing

Negative and validation scenarios were included in the API test suite.

The tested areas include:

- Invalid authentication scenarios
- Unauthorized access
- Role-based access restrictions
- Invalid resource access
- Invalid identifiers
- Required-field validation
- Business-rule validation
- Error-response verification

**Result: PASS**

---

## 8. End-to-End Business Flow Testing

End-to-end API flows were automated through Postman/Newman.

The tested business flow includes:

```text
Authentication
     ↓
User / Retailer setup
     ↓
Inventory selection
     ↓
Order creation
     ↓
Order retrieval
     ↓
Order item retrieval
     ↓
Order status operations
```

A retailer-ownership issue in the order-item flow was identified and corrected.

The corrected flow ensures that:

- Retailer-owned inventory is selected.
- Orders are created using valid inventory.
- Retailer authorization is used for retailer-protected order-item operations.
- Dynamically created order and order-item IDs are reused by subsequent requests.

The complete automated flow subsequently passed.

**Result: PASS**

---

## 9. Security and Authorization Testing

Basic application security and authorization behavior was verified.

The testing included:

- JWT authentication
- USER role access
- RETAILER role access
- Protected API endpoints
- Unauthorized access scenarios
- Role-specific endpoint restrictions

JWT tokens are obtained dynamically through login requests during the Postman execution rather than relying on an expired static token.

**Result: PASS**

### Scope Limitation

This testing does not represent a full security assessment or penetration test.

---

## 10. Docker and PostgreSQL Testing

Docker Compose was used to run the application stack.

The following components were verified:

```text
PostgreSQL
    ↓
User Service :8081
    ↓
Retailer Service :8082
```

Verification results:

- PostgreSQL — Healthy
- User Service — Running
- Retailer Service — Running
- User API — Responding
- Retailer API — Responding

**Result: PASS**

---

## 11. Swagger / OpenAPI Verification

Swagger/OpenAPI documentation and API endpoint availability were verified for the services.

**Result: PASS**

---

## 12. CI/CD Automated Testing

GitHub Actions is configured to automatically execute the project verification pipeline.

The CI pipeline performs the following major activities:

```text
Checkout
   ↓
Java 21 setup
   ↓
Docker verification
   ↓
PostgreSQL startup
   ↓
Database readiness check
   ↓
User Service build/test
   ↓
Retailer Service build/test
   ↓
Docker image build
   ↓
Service startup
   ↓
Service readiness checks
   ↓
Postman test-user setup
   ↓
Newman installation
   ↓
Newman API execution
   ↓
HTML report upload
   ↓
Docker cleanup
```

### CI Configuration Verification

The workflow uses:

- Ubuntu 24.04
- Java 21
- `actions/checkout@v5`
- `actions/setup-java@v5`
- `actions/upload-artifact@v6`

The CI workflow completed successfully without remaining workflow warnings.

**Result: PASS**

---

## 13. Newman HTML Reporting

Newman generates an HTML test report during CI execution.

The report is uploaded as a GitHub Actions artifact for post-run verification.

This provides test execution evidence in addition to the console output.

**Result: PASS**

---

## 14. Defect and Fix Summary

During API flow testing, an authorization issue was identified in the order-item retrieval flow.

### Issue

The order-item endpoint requires retailer authorization, but the test flow was initially using the USER token.

Additionally, inventory selection needed to ensure that the selected inventory belonged to the expected retailer.

### Resolution

The test flow was corrected to:

1. Select inventory owned by the expected retailer.
2. Create the order using the USER token.
3. Retrieve the order item using the RETAILER token.
4. Continue subsequent order assertions using the appropriate authorization context.

### Regression Result

After the correction:

- Newman requests: 35/35 PASS
- Assertions: 3124/3124 PASS
- GitHub Actions CI: PASS

---

## 15. Test Pyramid Status

The project has practical test coverage across multiple layers:

```text
              E2E / API Flow
                    ▲
                    │
             Postman / Newman
                    ▲
                    │
          Integration Testing
                    ▲
                    │
             Unit Testing
```

The project therefore demonstrates testing at multiple levels rather than relying only on API-level testing.

Test Pyramid theory remains a learning topic, while the actual project testing implementation has been completed for the planned scope.

---

## 16. Performance Testing

Dedicated performance/load testing was not included in the completed functional CI test scope.

Potential future performance testing could include:

- Concurrent API requests
- Response-time measurement
- Throughput testing
- Stress testing
- Spike testing
- Sustained-load testing

**Status: Not performed**

This is a separate non-functional testing activity and is not considered a blocker for the completed functional QA scope.

---

## 17. Known Scope Limitations

The completed testing should not be interpreted as a full enterprise security or performance certification.

The following activities are outside the completed scope:

- Full penetration testing
- Dedicated vulnerability assessment
- Large-scale load testing
- Production-scale capacity testing
- Long-duration endurance testing

---

## 18. Final QA Result

### Overall Status: PASS ✅

The planned functional testing and CI automation for the Retail Microservices project have been completed successfully.

Final verified results:

```text
User Service tests             18/18 PASS
Retailer Service tests         75/75 PASS
------------------------------------------
Service automated tests        93/93 PASS

Newman API requests            35/35 PASS
Newman assertions              3124/3124 PASS

PostgreSQL                     HEALTHY
Docker Compose                 PASS
Swagger/OpenAPI                PASS
JWT Authentication             PASS
Authorization Testing          PASS
E2E API Flow                   PASS
GitHub Actions CI              PASS
```

### QA Conclusion

**All planned functional and CI automated testing completed successfully with zero failed automated tests in the final verified execution.**

The application is considered **QA-tested and ready for the next project phase**, subject to any additional performance, penetration, or production-scale testing requirements.
