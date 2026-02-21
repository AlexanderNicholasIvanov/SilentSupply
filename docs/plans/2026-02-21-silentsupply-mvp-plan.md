# SilentSupply MVP Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build the SilentSupply B2B marketplace MVP — company registration, product catalog, direct orders, RFQ/proposal system, and automated negotiation engine, all exposed as a REST API with JWT auth.

**Architecture:** Spring Boot layered architecture with domain-driven package structure. Each domain (company, product, order, rfq, proposal, negotiation) has its own entity, repository, service, controller, and DTOs. Flyway manages schema migrations. Spring Security + JWT handles auth.

**Tech Stack:** Java 21, Spring Boot 3.4, Spring Data JPA, PostgreSQL 16, Maven, Flyway, SpringDoc OpenAPI, Lombok, MapStruct, jjwt, JUnit 5, Mockito, TestContainers

---

## Phase 1: Project Bootstrap

### Task 1 — Initialize Maven project with all dependencies

**Files:**
- Create: `pom.xml`
- Create: `src/main/java/com/silentsupply/SilentSupplyApplication.java`
- Create: `src/main/resources/application.yml`
- Create: `src/main/resources/application-dev.yml`
- Create: `src/main/resources/application-test.yml`
- Test: `src/test/java/com/silentsupply/SilentSupplyApplicationTest.java`

**Steps:**

1. Create `pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.0</version>
        <relativePath/>
    </parent>

    <groupId>com.silentsupply</groupId>
    <artifactId>silentsupply</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <name>SilentSupply</name>
    <description>B2B supply chain marketplace with automated negotiation</description>

    <properties>
        <java.version>21</java.version>
        <mapstruct.version>1.6.3</mapstruct.version>
        <lombok-mapstruct-binding.version>0.2.0</lombok-mapstruct-binding.version>
        <jjwt.version>0.12.6</jjwt.version>
        <springdoc.version>2.7.0</springdoc.version>
        <testcontainers.version>1.20.4</testcontainers.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- OpenAPI / Swagger -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>${springdoc.version}</version>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- MapStruct -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>${mapstruct.version}</version>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <version>${testcontainers.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${testcontainers.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok-mapstruct-binding</artifactId>
                            <version>${lombok-mapstruct-binding.version}</version>
                        </path>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${mapstruct.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

2. Create `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: silentsupply
  profiles:
    active: dev
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8080

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

app:
  jwt:
    secret: super-secret-key-that-must-be-at-least-256-bits-long-for-hs256-signing
    expiration-ms: 86400000
```

3. Create `src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/silentsupply
    username: silentsupply
    password: silentsupply
  jpa:
    show-sql: true

logging:
  level:
    com.silentsupply: DEBUG
    org.springframework.security: DEBUG
```

4. Create `src/main/resources/application-test.yml`:

```yaml
spring:
  datasource:
    url: jdbc:tc:postgresql:16:///silentsupply_test
    driver-class-name: org.testcontainers.jdbc.ContainerDatabaseDriver
  jpa:
    show-sql: false
  flyway:
    enabled: true

app:
  jwt:
    secret: test-secret-key-that-must-be-at-least-256-bits-long-for-hs256-signing-algo
    expiration-ms: 3600000

logging:
  level:
    com.silentsupply: INFO
```

5. Create `src/main/java/com/silentsupply/SilentSupplyApplication.java`:

```java
package com.silentsupply;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the SilentSupply B2B marketplace application.
 */
@SpringBootApplication
public class SilentSupplyApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(SilentSupplyApplication.class, args);
    }
}
```

6. Create `src/test/java/com/silentsupply/SilentSupplyApplicationTest.java`:

```java
package com.silentsupply;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test to verify the Spring application context loads successfully.
 */
@SpringBootTest
@ActiveProfiles("test")
class SilentSupplyApplicationTest {

    @Test
    void contextLoads() {
        // Verifies the application context starts without errors
    }
}
```

7. Run tests (this will fail until Task 2 adds migrations, so defer running until after Task 2):

```bash
./mvnw test -pl . -Dtest=SilentSupplyApplicationTest
```

8. Commit: `chore: initialize maven project with spring boot and all dependencies`

---

### Task 2 — Create Flyway baseline migration with all tables

**Files:**
- Create: `src/main/resources/db/migration/V1__baseline_schema.sql`

**Steps:**

1. Create `src/main/resources/db/migration/V1__baseline_schema.sql`:

```sql
-- V1__baseline_schema.sql
-- Baseline schema for SilentSupply B2B marketplace

-- ============================================================
-- Companies
-- ============================================================
CREATE TABLE companies (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(255)    NOT NULL,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    password        VARCHAR(255)    NOT NULL,
    role            VARCHAR(20)     NOT NULL CHECK (role IN ('SUPPLIER', 'BUYER')),
    contact_phone   VARCHAR(50),
    address         VARCHAR(500),
    verified        BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_companies_email ON companies (email);
CREATE INDEX idx_companies_role ON companies (role);

-- ============================================================
-- Products
-- ============================================================
CREATE TABLE products (
    id                  BIGSERIAL       PRIMARY KEY,
    supplier_id         BIGINT          NOT NULL REFERENCES companies(id),
    name                VARCHAR(255)    NOT NULL,
    description         TEXT,
    category            VARCHAR(100)    NOT NULL,
    sku                 VARCHAR(100)    NOT NULL,
    unit_of_measure     VARCHAR(50)     NOT NULL,
    base_price          NUMERIC(15,2)   NOT NULL CHECK (base_price > 0),
    available_quantity  INTEGER         NOT NULL CHECK (available_quantity >= 0),
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE'
                            CHECK (status IN ('ACTIVE', 'OUT_OF_STOCK', 'DISCONTINUED')),
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    UNIQUE (supplier_id, sku)
);

CREATE INDEX idx_products_supplier ON products (supplier_id);
CREATE INDEX idx_products_category ON products (category);
CREATE INDEX idx_products_status ON products (status);
CREATE INDEX idx_products_name ON products (name);

-- ============================================================
-- Catalog Orders (direct purchase at listed price)
-- ============================================================
CREATE TABLE catalog_orders (
    id              BIGSERIAL       PRIMARY KEY,
    buyer_id        BIGINT          NOT NULL REFERENCES companies(id),
    product_id      BIGINT          NOT NULL REFERENCES products(id),
    supplier_id     BIGINT          NOT NULL REFERENCES companies(id),
    quantity        INTEGER         NOT NULL CHECK (quantity > 0),
    unit_price      NUMERIC(15,2)   NOT NULL CHECK (unit_price > 0),
    total_price     NUMERIC(15,2)   NOT NULL CHECK (total_price > 0),
    status          VARCHAR(20)     NOT NULL DEFAULT 'PLACED'
                        CHECK (status IN ('PLACED', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_catalog_orders_buyer ON catalog_orders (buyer_id);
CREATE INDEX idx_catalog_orders_supplier ON catalog_orders (supplier_id);
CREATE INDEX idx_catalog_orders_product ON catalog_orders (product_id);
CREATE INDEX idx_catalog_orders_status ON catalog_orders (status);

-- ============================================================
-- RFQs (Request for Quote)
-- ============================================================
CREATE TABLE rfqs (
    id                  BIGSERIAL       PRIMARY KEY,
    buyer_id            BIGINT          NOT NULL REFERENCES companies(id),
    product_id          BIGINT          NOT NULL REFERENCES products(id),
    supplier_id         BIGINT          NOT NULL REFERENCES companies(id),
    desired_quantity    INTEGER         NOT NULL CHECK (desired_quantity > 0),
    target_price        NUMERIC(15,2)   NOT NULL CHECK (target_price > 0),
    delivery_deadline   DATE            NOT NULL,
    notes               TEXT,
    status              VARCHAR(20)     NOT NULL DEFAULT 'SUBMITTED'
                            CHECK (status IN ('SUBMITTED', 'UNDER_REVIEW', 'COUNTERED',
                                              'ACCEPTED', 'REJECTED', 'EXPIRED')),
    current_round       INTEGER         NOT NULL DEFAULT 0,
    max_rounds          INTEGER         NOT NULL DEFAULT 3,
    expires_at          TIMESTAMP       NOT NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rfqs_buyer ON rfqs (buyer_id);
CREATE INDEX idx_rfqs_supplier ON rfqs (supplier_id);
CREATE INDEX idx_rfqs_product ON rfqs (product_id);
CREATE INDEX idx_rfqs_status ON rfqs (status);

-- ============================================================
-- Proposals (offers within an RFQ negotiation)
-- ============================================================
CREATE TABLE proposals (
    id              BIGSERIAL       PRIMARY KEY,
    rfq_id          BIGINT          NOT NULL REFERENCES rfqs(id),
    proposer_type   VARCHAR(20)     NOT NULL CHECK (proposer_type IN ('BUYER', 'SYSTEM')),
    proposed_price  NUMERIC(15,2)   NOT NULL CHECK (proposed_price > 0),
    proposed_qty    INTEGER         NOT NULL CHECK (proposed_qty > 0),
    delivery_days   INTEGER         NOT NULL CHECK (delivery_days > 0),
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'COUNTERED', 'EXPIRED')),
    round_number    INTEGER         NOT NULL,
    reason_code     VARCHAR(100),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_proposals_rfq ON proposals (rfq_id);
CREATE INDEX idx_proposals_status ON proposals (status);

-- ============================================================
-- Negotiation Rules (supplier-defined per product)
-- ============================================================
CREATE TABLE negotiation_rules (
    id                      BIGSERIAL       PRIMARY KEY,
    supplier_id             BIGINT          NOT NULL REFERENCES companies(id),
    product_id              BIGINT          NOT NULL REFERENCES products(id),
    price_floor             NUMERIC(15,2)   NOT NULL CHECK (price_floor > 0),
    auto_accept_threshold   NUMERIC(15,2)   NOT NULL CHECK (auto_accept_threshold > 0),
    max_delivery_days       INTEGER         NOT NULL CHECK (max_delivery_days > 0),
    max_rounds              INTEGER         NOT NULL DEFAULT 3 CHECK (max_rounds > 0),
    volume_discount_pct     NUMERIC(5,2)    NOT NULL DEFAULT 0 CHECK (volume_discount_pct >= 0 AND volume_discount_pct <= 100),
    volume_threshold        INTEGER         NOT NULL DEFAULT 0 CHECK (volume_threshold >= 0),
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
    UNIQUE (supplier_id, product_id)
);

CREATE INDEX idx_negotiation_rules_supplier ON negotiation_rules (supplier_id);
CREATE INDEX idx_negotiation_rules_product ON negotiation_rules (product_id);
```

2. Run the context-loads test to verify Flyway migration applies cleanly:

```bash
./mvnw test -Dtest=SilentSupplyApplicationTest -Dspring.profiles.active=test
```

3. Commit: `feat: add flyway baseline migration with all domain tables`

---

### Task 3 — Create the common package (base entity, exceptions, error handling)

**Files:**
- Create: `src/main/java/com/silentsupply/common/entity/BaseEntity.java`
- Create: `src/main/java/com/silentsupply/common/exception/ResourceNotFoundException.java`
- Create: `src/main/java/com/silentsupply/common/exception/BusinessRuleException.java`
- Create: `src/main/java/com/silentsupply/common/exception/AuthenticationFailedException.java`
- Create: `src/main/java/com/silentsupply/common/exception/AccessDeniedException.java`
- Create: `src/main/java/com/silentsupply/common/dto/ErrorResponse.java`
- Create: `src/main/java/com/silentsupply/common/exception/GlobalExceptionHandler.java`
- Test: `src/test/java/com/silentsupply/common/exception/GlobalExceptionHandlerTest.java`

**Steps:**

1. Create `src/main/java/com/silentsupply/common/entity/BaseEntity.java`:

```java
package com.silentsupply.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Base entity providing common fields for all domain entities.
 * Includes auto-generated ID and automatic timestamp management.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    /** Unique identifier for the entity. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Timestamp when the entity was created. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Timestamp when the entity was last updated. */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Sets creation and update timestamps before the entity is first persisted.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the update timestamp before the entity is updated.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

2. Create `src/main/java/com/silentsupply/common/exception/ResourceNotFoundException.java`:

```java
package com.silentsupply.common.exception;

/**
 * Thrown when a requested resource cannot be found in the system.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Creates a new ResourceNotFoundException.
     *
     * @param resourceName the type of resource that was not found
     * @param fieldName    the field used in the lookup
     * @param fieldValue   the value that was searched for
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
    }
}
```

3. Create `src/main/java/com/silentsupply/common/exception/BusinessRuleException.java`:

```java
package com.silentsupply.common.exception;

/**
 * Thrown when a business rule is violated during a domain operation.
 */
public class BusinessRuleException extends RuntimeException {

    /**
     * Creates a new BusinessRuleException with the given message.
     *
     * @param message description of the violated business rule
     */
    public BusinessRuleException(String message) {
        super(message);
    }
}
```

4. Create `src/main/java/com/silentsupply/common/exception/AuthenticationFailedException.java`:

```java
package com.silentsupply.common.exception;

/**
 * Thrown when authentication fails due to invalid credentials.
 */
public class AuthenticationFailedException extends RuntimeException {

    /**
     * Creates a new AuthenticationFailedException with the given message.
     *
     * @param message description of why authentication failed
     */
    public AuthenticationFailedException(String message) {
        super(message);
    }
}
```

5. Create `src/main/java/com/silentsupply/common/exception/AccessDeniedException.java`:

```java
package com.silentsupply.common.exception;

/**
 * Thrown when a user attempts an action they are not authorized to perform.
 */
public class AccessDeniedException extends RuntimeException {

    /**
     * Creates a new AccessDeniedException with the given message.
     *
     * @param message description of why access was denied
     */
    public AccessDeniedException(String message) {
        super(message);
    }
}
```

6. Create `src/main/java/com/silentsupply/common/dto/ErrorResponse.java`:

```java
package com.silentsupply.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error response returned by the API for all error conditions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /** HTTP status code. */
    private int status;

    /** Short error description. */
    private String message;

    /** Timestamp when the error occurred. */
    private LocalDateTime timestamp;

    /** Detailed validation errors, if applicable. */
    private List<String> errors;
}
```

7. Create `src/main/java/com/silentsupply/common/exception/GlobalExceptionHandler.java`:

```java
package com.silentsupply.common.exception;

import com.silentsupply.common.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Global exception handler that converts exceptions into structured API error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles resource-not-found exceptions.
     *
     * @param ex the exception
     * @return 404 error response
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles business rule violation exceptions.
     *
     * @param ex the exception
     * @return 400 error response
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles authentication failure exceptions.
     *
     * @param ex the exception
     * @return 401 error response
     */
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationFailed(AuthenticationFailedException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Handles access-denied exceptions.
     *
     * @param ex the exception
     * @return 403 error response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Handles bean validation errors from {@code @Valid} annotated parameters.
     *
     * @param ex the validation exception
     * @return 400 error response with field-level details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .timestamp(LocalDateTime.now())
                .errors(fieldErrors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Catch-all handler for unexpected exceptions.
     *
     * @param ex the exception
     * @return 500 error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

8. Create `src/test/java/com/silentsupply/common/exception/GlobalExceptionHandlerTest.java`:

```java
package com.silentsupply.common.exception;

import com.silentsupply.common.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link GlobalExceptionHandler}.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleResourceNotFound_returns404WithMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Company", "id", 99L);

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).contains("Company not found with id: '99'");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleBusinessRule_returns400WithMessage() {
        BusinessRuleException ex = new BusinessRuleException("Insufficient stock");

        ResponseEntity<ErrorResponse> response = handler.handleBusinessRule(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Insufficient stock");
    }

    @Test
    void handleAuthenticationFailed_returns401() {
        AuthenticationFailedException ex = new AuthenticationFailedException("Invalid credentials");

        ResponseEntity<ErrorResponse> response = handler.handleAuthenticationFailed(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid credentials");
    }

    @Test
    void handleAccessDenied_returns403() {
        AccessDeniedException ex = new AccessDeniedException("Not authorized");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Not authorized");
    }

    @Test
    void handleGeneral_returns500() {
        Exception ex = new RuntimeException("Something broke");

        ResponseEntity<ErrorResponse> response = handler.handleGeneral(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }
}
```

9. Run tests:

```bash
./mvnw test -Dtest=GlobalExceptionHandlerTest
```

10. Commit: `feat: add common package with base entity, exceptions, and error handler`

---

## Phase 2: Company & Auth

### Task 4 — Company entity, repository, DTO, mapper

**Files:**
- Create: `src/main/java/com/silentsupply/company/CompanyRole.java`
- Create: `src/main/java/com/silentsupply/company/Company.java`
- Create: `src/main/java/com/silentsupply/company/CompanyRepository.java`
- Create: `src/main/java/com/silentsupply/company/dto/CompanyRequest.java`
- Create: `src/main/java/com/silentsupply/company/dto/CompanyResponse.java`
- Create: `src/main/java/com/silentsupply/company/CompanyMapper.java`
- Test: `src/test/java/com/silentsupply/company/CompanyMapperTest.java`

**Steps:**

1. Create `src/main/java/com/silentsupply/company/CompanyRole.java`:

```java
package com.silentsupply.company;

/**
 * Defines the role a company plays in the SilentSupply marketplace.
 * A company is either a supplier or a buyer, never both.
 */
public enum CompanyRole {

    /** Supplier: lists products, configures negotiation rules, fulfills orders. */
    SUPPLIER,

    /** Buyer: browses products, places catalog orders, submits RFQs. */
    BUYER
}
```

2. Create `src/main/java/com/silentsupply/company/Company.java`:

```java
package com.silentsupply.company;

import com.silentsupply.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Represents a registered business in the SilentSupply marketplace.
 * Each company has a role (SUPPLIER or BUYER) and holds contact information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "companies")
public class Company extends BaseEntity {

    /** Business name of the company. */
    @Column(nullable = false)
    private String name;

    /** Contact email address, used as the login credential. */
    @Column(nullable = false, unique = true)
    private String email;

    /** BCrypt-hashed password. */
    @Column(nullable = false)
    private String password;

    /** Role of this company in the marketplace. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyRole role;

    /** Contact phone number. */
    @Column(name = "contact_phone")
    private String contactPhone;

    /** Business address. */
    private String address;

    /** Whether the company has been verified by an admin. */
    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false;
}
```

3. Create `src/main/java/com/silentsupply/company/CompanyRepository.java`:

```java
package com.silentsupply.company;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data repository for {@link Company} entities.
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * Finds a company by its email address.
     *
     * @param email the email to search for
     * @return the company if found
     */
    Optional<Company> findByEmail(String email);

    /**
     * Checks whether a company with the given email already exists.
     *
     * @param email the email to check
     * @return true if a company with this email exists
     */
    boolean existsByEmail(String email);
}
```

4. Create `src/main/java/com/silentsupply/company/dto/CompanyRequest.java`:

```java
package com.silentsupply.company.dto;

import com.silentsupply.company.CompanyRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for registering a new company.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRequest {

    /** Business name. */
    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    private String name;

    /** Contact email, used as login credential. */
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    /** Password for authentication. */
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    /** Role: SUPPLIER or BUYER. */
    @NotNull(message = "Role is required")
    private CompanyRole role;

    /** Optional contact phone number. */
    private String contactPhone;

    /** Optional business address. */
    private String address;
}
```

5. Create `src/main/java/com/silentsupply/company/dto/CompanyResponse.java`:

```java
package com.silentsupply.company.dto;

import com.silentsupply.company.CompanyRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO representing a company. Excludes sensitive fields like password.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponse {

    /** Company ID. */
    private Long id;

    /** Business name. */
    private String name;

    /** Contact email. */
    private String email;

    /** Role in the marketplace. */
    private CompanyRole role;

    /** Contact phone number. */
    private String contactPhone;

    /** Business address. */
    private String address;

    /** Whether the company is verified. */
    private boolean verified;

    /** When the company was registered. */
    private LocalDateTime createdAt;
}
```

6. Create `src/main/java/com/silentsupply/company/CompanyMapper.java`:

```java
package com.silentsupply.company;

import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.company.dto.CompanyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between {@link Company} entities and DTOs.
 */
@Mapper(componentModel = "spring")
public interface CompanyMapper {

    /**
     * Converts a registration request DTO to a Company entity.
     * The password field is mapped but must be encoded by the service before persisting.
     *
     * @param request the registration request
     * @return the Company entity (password still in plain text)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Company toEntity(CompanyRequest request);

    /**
     * Converts a Company entity to a response DTO. Excludes the password.
     *
     * @param company the company entity
     * @return the response DTO
     */
    CompanyResponse toResponse(Company company);
}
```

7. Create `src/test/java/com/silentsupply/company/CompanyMapperTest.java`:

```java
package com.silentsupply.company;

import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.company.dto.CompanyResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CompanyMapper}.
 */
class CompanyMapperTest {

    private final CompanyMapper mapper = Mappers.getMapper(CompanyMapper.class);

    @Test
    void toEntity_mapsAllFieldsExceptIdAndTimestamps() {
        CompanyRequest request = CompanyRequest.builder()
                .name("Acme Corp")
                .email("acme@example.com")
                .password("password123")
                .role(CompanyRole.SUPPLIER)
                .contactPhone("+1234567890")
                .address("123 Main St")
                .build();

        Company company = mapper.toEntity(request);

        assertThat(company.getId()).isNull();
        assertThat(company.getName()).isEqualTo("Acme Corp");
        assertThat(company.getEmail()).isEqualTo("acme@example.com");
        assertThat(company.getPassword()).isEqualTo("password123");
        assertThat(company.getRole()).isEqualTo(CompanyRole.SUPPLIER);
        assertThat(company.getContactPhone()).isEqualTo("+1234567890");
        assertThat(company.getAddress()).isEqualTo("123 Main St");
    }

    @Test
    void toResponse_mapsAllFieldsExcludingPassword() {
        Company company = Company.builder()
                .name("Acme Corp")
                .email("acme@example.com")
                .password("hashed-password")
                .role(CompanyRole.SUPPLIER)
                .contactPhone("+1234567890")
                .address("123 Main St")
                .verified(true)
                .build();
        company.setId(1L);
        company.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));

        CompanyResponse response = mapper.toResponse(company);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Acme Corp");
        assertThat(response.getEmail()).isEqualTo("acme@example.com");
        assertThat(response.getRole()).isEqualTo(CompanyRole.SUPPLIER);
        assertThat(response.isVerified()).isTrue();
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
    }
}
```

8. Run tests:

```bash
./mvnw test -Dtest=CompanyMapperTest
```

9. Commit: `feat: add company entity, repository, DTOs, and mapper`

---

### Task 5 — Company service + tests

**Files:**
- Create: `src/main/java/com/silentsupply/company/CompanyService.java`
- Test: `src/test/java/com/silentsupply/company/CompanyServiceTest.java`

**Steps:**

1. Create `src/main/java/com/silentsupply/company/CompanyService.java`:

```java
package com.silentsupply.company;

import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.common.exception.ResourceNotFoundException;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.company.dto.CompanyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for company registration and retrieval.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new company. Encodes the password and persists the entity.
     *
     * @param request the registration request
     * @return the created company as a response DTO
     * @throws BusinessRuleException if the email is already registered
     */
    @Transactional
    public CompanyResponse register(CompanyRequest request) {
        if (companyRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Email already registered: " + request.getEmail());
        }

        Company company = companyMapper.toEntity(request);
        company.setPassword(passwordEncoder.encode(request.getPassword()));

        Company saved = companyRepository.save(company);
        return companyMapper.toResponse(saved);
    }

    /**
     * Retrieves a company by its ID.
     *
     * @param id the company ID
     * @return the company response DTO
     * @throws ResourceNotFoundException if no company exists with the given ID
     */
    public CompanyResponse getById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));
        return companyMapper.toResponse(company);
    }

    /**
     * Lists all registered companies.
     *
     * @return list of company response DTOs
     */
    public List<CompanyResponse> listAll() {
        return companyRepository.findAll().stream()
                .map(companyMapper::toResponse)
                .toList();
    }
}
```

2. Write failing test first, then create `src/test/java/com/silentsupply/company/CompanyServiceTest.java`:

```java
package com.silentsupply.company;

import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.common.exception.ResourceNotFoundException;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.company.dto.CompanyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CompanyService}.
 */
@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyMapper companyMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CompanyService companyService;

    private CompanyRequest request;
    private Company company;
    private CompanyResponse response;

    @BeforeEach
    void setUp() {
        request = CompanyRequest.builder()
                .name("Acme Corp")
                .email("acme@example.com")
                .password("password123")
                .role(CompanyRole.SUPPLIER)
                .contactPhone("+1234567890")
                .address("123 Main St")
                .build();

        company = Company.builder()
                .name("Acme Corp")
                .email("acme@example.com")
                .password("encoded-password")
                .role(CompanyRole.SUPPLIER)
                .contactPhone("+1234567890")
                .address("123 Main St")
                .build();
        company.setId(1L);
        company.setCreatedAt(LocalDateTime.now());

        response = CompanyResponse.builder()
                .id(1L)
                .name("Acme Corp")
                .email("acme@example.com")
                .role(CompanyRole.SUPPLIER)
                .contactPhone("+1234567890")
                .address("123 Main St")
                .verified(false)
                .createdAt(company.getCreatedAt())
                .build();
    }

    @Test
    void register_withValidRequest_savesAndReturnsResponse() {
        when(companyRepository.existsByEmail("acme@example.com")).thenReturn(false);
        when(companyMapper.toEntity(request)).thenReturn(company);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(companyRepository.save(company)).thenReturn(company);
        when(companyMapper.toResponse(company)).thenReturn(response);

        CompanyResponse result = companyService.register(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Acme Corp");
        assertThat(result.getEmail()).isEqualTo("acme@example.com");
        assertThat(result.getRole()).isEqualTo(CompanyRole.SUPPLIER);
        verify(companyRepository).save(company);
    }

    @Test
    void register_withDuplicateEmail_throwsBusinessRuleException() {
        when(companyRepository.existsByEmail("acme@example.com")).thenReturn(true);

        assertThatThrownBy(() -> companyService.register(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Email already registered");

        verify(companyRepository, never()).save(any());
    }

    @Test
    void getById_withExistingId_returnsResponse() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyMapper.toResponse(company)).thenReturn(response);

        CompanyResponse result = companyService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Acme Corp");
    }

    @Test
    void getById_withNonExistingId_throwsResourceNotFoundException() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company not found");
    }

    @Test
    void listAll_returnsAllCompanies() {
        Company buyer = Company.builder()
                .name("BuyerCo")
                .email("buyer@example.com")
                .password("encoded")
                .role(CompanyRole.BUYER)
                .build();
        buyer.setId(2L);
        buyer.setCreatedAt(LocalDateTime.now());

        CompanyResponse buyerResponse = CompanyResponse.builder()
                .id(2L)
                .name("BuyerCo")
                .email("buyer@example.com")
                .role(CompanyRole.BUYER)
                .build();

        when(companyRepository.findAll()).thenReturn(List.of(company, buyer));
        when(companyMapper.toResponse(company)).thenReturn(response);
        when(companyMapper.toResponse(buyer)).thenReturn(buyerResponse);

        List<CompanyResponse> result = companyService.listAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Acme Corp");
        assertThat(result.get(1).getName()).isEqualTo("BuyerCo");
    }
}
```

3. Run tests:

```bash
./mvnw test -Dtest=CompanyServiceTest
```

4. Commit: `feat: add company service with register, getById, and listAll`

---

### Task 6 — Company controller + integration tests

**Files:**
- Create: `src/main/java/com/silentsupply/company/CompanyController.java`
- Test: `src/test/java/com/silentsupply/company/CompanyControllerIntegrationTest.java`
- Create: `src/test/java/com/silentsupply/common/BaseIntegrationTest.java`

**Steps:**

1. Create `src/test/java/com/silentsupply/common/BaseIntegrationTest.java`:

```java
package com.silentsupply.common;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Base class for integration tests. Starts a PostgreSQL TestContainer
 * and configures Spring datasource properties dynamically.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class BaseIntegrationTest {

    /** Shared PostgreSQL container for all integration tests. */
    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("silentsupply_test")
                    .withUsername("test")
                    .withPassword("test");

    /**
     * Registers dynamic datasource properties from the running container.
     *
     * @param registry the dynamic property registry
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
```

2. Create `src/main/java/com/silentsupply/company/CompanyController.java`:

```java
package com.silentsupply.company;

import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.company.dto.CompanyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for company registration and retrieval.
 */
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Company registration and management")
public class CompanyController {

    private final CompanyService companyService;

    /**
     * Registers a new company in the marketplace.
     *
     * @param request the company registration details
     * @return the created company with HTTP 201
     */
    @PostMapping
    @Operation(summary = "Register a new company")
    public ResponseEntity<CompanyResponse> register(@Valid @RequestBody CompanyRequest request) {
        CompanyResponse response = companyService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a company by its ID.
     *
     * @param id the company ID
     * @return the company details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get company by ID")
    public ResponseEntity<CompanyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getById(id));
    }

    /**
     * Lists all registered companies.
     *
     * @return list of all companies
     */
    @GetMapping
    @Operation(summary = "List all companies")
    public ResponseEntity<List<CompanyResponse>> listAll() {
        return ResponseEntity.ok(companyService.listAll());
    }
}
```

3. Create `src/test/java/com/silentsupply/company/CompanyControllerIntegrationTest.java`:

```java
package com.silentsupply.company;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silentsupply.common.BaseIntegrationTest;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.company.dto.CompanyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link CompanyController}.
 * Uses a real PostgreSQL container and the full Spring context.
 */
class CompanyControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CompanyRepository companyRepository;

    @BeforeEach
    void cleanUp() {
        companyRepository.deleteAll();
    }

    @Test
    void register_withValidRequest_returns201() {
        CompanyRequest request = CompanyRequest.builder()
                .name("Acme Corp")
                .email("acme@example.com")
                .password("password123")
                .role(CompanyRole.SUPPLIER)
                .contactPhone("+1234567890")
                .address("123 Main St")
                .build();

        ResponseEntity<CompanyResponse> response = restTemplate.postForEntity(
                "/api/companies", request, CompanyResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Acme Corp");
        assertThat(response.getBody().getEmail()).isEqualTo("acme@example.com");
        assertThat(response.getBody().getRole()).isEqualTo(CompanyRole.SUPPLIER);
    }

    @Test
    void register_withDuplicateEmail_returns400() {
        CompanyRequest request = CompanyRequest.builder()
                .name("Acme Corp")
                .email("dup@example.com")
                .password("password123")
                .role(CompanyRole.SUPPLIER)
                .build();

        restTemplate.postForEntity("/api/companies", request, CompanyResponse.class);
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/companies", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_withMissingName_returns400() {
        CompanyRequest request = CompanyRequest.builder()
                .email("noname@example.com")
                .password("password123")
                .role(CompanyRole.BUYER)
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/companies", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getById_withExistingCompany_returns200() {
        CompanyRequest request = CompanyRequest.builder()
                .name("BuyerCo")
                .email("buyer@example.com")
                .password("password123")
                .role(CompanyRole.BUYER)
                .build();

        ResponseEntity<CompanyResponse> created = restTemplate.postForEntity(
                "/api/companies", request, CompanyResponse.class);
        Long id = created.getBody().getId();

        ResponseEntity<CompanyResponse> response = restTemplate.getForEntity(
                "/api/companies/" + id, CompanyResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("BuyerCo");
    }

    @Test
    void getById_withNonExistingId_returns404() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/companies/99999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void listAll_returnsAllRegisteredCompanies() {
        CompanyRequest supplier = CompanyRequest.builder()
                .name("SupplierCo")
                .email("supplier@example.com")
                .password("password123")
                .role(CompanyRole.SUPPLIER)
                .build();
        CompanyRequest buyer = CompanyRequest.builder()
                .name("BuyerCo")
                .email("buyer2@example.com")
                .password("password123")
                .role(CompanyRole.BUYER)
                .build();

        restTemplate.postForEntity("/api/companies", supplier, CompanyResponse.class);
        restTemplate.postForEntity("/api/companies", buyer, CompanyResponse.class);

        ResponseEntity<CompanyResponse[]> response = restTemplate.getForEntity(
                "/api/companies", CompanyResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }
}
```

**Note:** These integration tests will initially fail because Spring Security blocks all requests by default. They will pass after Task 7 configures security to permit company and auth endpoints. Run them after Task 7 to verify everything wires together.

4. Run tests:

```bash
./mvnw test -Dtest=CompanyControllerIntegrationTest
```

5. Commit: `feat: add company controller with registration and retrieval endpoints`

---

### Task 7 — Auth: JWT token provider, security filter, security config

**Files:**
- Create: `src/main/java/com/silentsupply/config/CompanyUserDetails.java`
- Create: `src/main/java/com/silentsupply/config/CompanyUserDetailsService.java`
- Create: `src/main/java/com/silentsupply/config/JwtTokenProvider.java`
- Create: `src/main/java/com/silentsupply/config/JwtAuthenticationFilter.java`
- Create: `src/main/java/com/silentsupply/config/SecurityConfig.java`
- Create: `src/main/java/com/silentsupply/config/PasswordEncoderConfig.java`
- Test: `src/test/java/com/silentsupply/config/JwtTokenProviderTest.java`

**Steps:**

1. Create `src/main/java/com/silentsupply/config/PasswordEncoderConfig.java`:

```java
package com.silentsupply.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration class for the password encoder bean.
 * Separated to avoid circular dependency between SecurityConfig and CompanyService.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Creates a BCrypt password encoder.
     *
     * @return the password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

2. Create `src/main/java/com/silentsupply/config/CompanyUserDetails.java`:

```java
package com.silentsupply.config;

import com.silentsupply.company.Company;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security {@link UserDetails} implementation backed by a {@link Company} entity.
 * The company's role (SUPPLIER/BUYER) is mapped to a Spring Security authority with ROLE_ prefix.
 */
@Getter
public class CompanyUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Creates user details from a Company entity.
     *
     * @param company the company entity
     */
    public CompanyUserDetails(Company company) {
        this.id = company.getId();
        this.email = company.getEmail();
        this.password = company.getPassword();
        this.role = company.getRole().name();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + company.getRole().name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

3. Create `src/main/java/com/silentsupply/config/CompanyUserDetailsService.java`:

```java
package com.silentsupply.config;

import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads user details from the Company table for Spring Security authentication.
 */
@Service
@RequiredArgsConstructor
public class CompanyUserDetailsService implements UserDetailsService {

    private final CompanyRepository companyRepository;

    /**
     * Loads a company by email for authentication.
     *
     * @param email the company's email address
     * @return the user details
     * @throws UsernameNotFoundException if no company exists with the given email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Company company = companyRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Company not found with email: " + email));
        return new CompanyUserDetails(company);
    }
}
```

4. Create `src/main/java/com/silentsupply/config/JwtTokenProvider.java`:

```java
package com.silentsupply.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility component for generating, validating, and parsing JWT tokens.
 */
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMs;

    /**
     * Creates a JWT token provider with the configured secret and expiration.
     *
     * @param secret       the HMAC signing secret (min 256 bits)
     * @param expirationMs token validity duration in milliseconds
     */
    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a JWT token for the given user details.
     *
     * @param userDetails the authenticated user
     * @return a signed JWT token string
     */
    public String generateToken(CompanyUserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userDetails.getEmail())
                .claim("companyId", userDetails.getId())
                .claim("role", userDetails.getRole())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * Extracts the email (subject) from a JWT token.
     *
     * @param token the JWT token
     * @return the email address
     */
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extracts the company ID from a JWT token.
     *
     * @param token the JWT token
     * @return the company ID
     */
    public Long getCompanyIdFromToken(String token) {
        return parseClaims(token).get("companyId", Long.class);
    }

    /**
     * Validates whether a JWT token is well-formed and not expired.
     *
     * @param token the JWT token to validate
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Parses and returns the claims from a JWT token.
     *
     * @param token the JWT token
     * @return the token claims
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

5. Create `src/main/java/com/silentsupply/config/JwtAuthenticationFilter.java`:

```java
package com.silentsupply.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet filter that extracts and validates JWT tokens from the Authorization header.
 * On successful validation, sets the Spring Security authentication context.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CompanyUserDetailsService userDetailsService;

    /**
     * Processes each request to check for a valid JWT token.
     *
     * @param request     the HTTP request
     * @param response    the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getEmailFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the Authorization header.
     *
     * @param request the HTTP request
     * @return the token string, or null if not present
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

6. Create `src/main/java/com/silentsupply/config/SecurityConfig.java`:

```java
package com.silentsupply.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration. Enables JWT-based stateless authentication
 * and defines endpoint access rules based on company roles.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configures the security filter chain with JWT auth and endpoint rules.
     *
     * @param http the HttpSecurity builder
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/companies").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                // Supplier-only endpoints
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("SUPPLIER")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("SUPPLIER")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("SUPPLIER")
                .requestMatchers("/api/suppliers/*/negotiation-rules/**").hasRole("SUPPLIER")
                // Buyer-only endpoints
                .requestMatchers(HttpMethod.POST, "/api/orders").hasRole("BUYER")
                .requestMatchers(HttpMethod.POST, "/api/rfqs").hasRole("BUYER")
                .requestMatchers(HttpMethod.POST, "/api/rfqs/*/proposals").hasRole("BUYER")
                // All authenticated
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Exposes the authentication manager for use by the auth controller.
     *
     * @param config the authentication configuration
     * @return the authentication manager
     * @throws Exception if retrieval fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

7. Create `src/test/java/com/silentsupply/config/JwtTokenProviderTest.java`:

```java
package com.silentsupply.config;

import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link JwtTokenProvider}.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        String secret = "test-secret-key-that-must-be-at-least-256-bits-long-for-hs256-signing-algo";
        tokenProvider = new JwtTokenProvider(secret, 3600000L);
    }

    @Test
    void generateToken_createsValidToken() {
        CompanyUserDetails userDetails = createUserDetails();

        String token = tokenProvider.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(tokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void getEmailFromToken_returnsCorrectEmail() {
        CompanyUserDetails userDetails = createUserDetails();
        String token = tokenProvider.generateToken(userDetails);

        String email = tokenProvider.getEmailFromToken(token);

        assertThat(email).isEqualTo("acme@example.com");
    }

    @Test
    void getCompanyIdFromToken_returnsCorrectId() {
        CompanyUserDetails userDetails = createUserDetails();
        String token = tokenProvider.generateToken(userDetails);

        Long companyId = tokenProvider.getCompanyIdFromToken(token);

        assertThat(companyId).isEqualTo(1L);
    }

    @Test
    void validateToken_withInvalidToken_returnsFalse() {
        assertThat(tokenProvider.validateToken("not-a-real-token")).isFalse();
    }

    @Test
    void validateToken_withTamperedToken_returnsFalse() {
        CompanyUserDetails userDetails = createUserDetails();
        String token = tokenProvider.generateToken(userDetails);

        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThat(tokenProvider.validateToken(tampered)).isFalse();
    }

    private CompanyUserDetails createUserDetails() {
        Company company = Company.builder()
                .name("Acme Corp")
                .email("acme@example.com")
                .password("encoded-password")
                .role(CompanyRole.SUPPLIER)
                .build();
        company.setId(1L);
        return new CompanyUserDetails(company);
    }
}
```

8. Run tests:

```bash
./mvnw test -Dtest=JwtTokenProviderTest
```

9. Commit: `feat: add JWT auth with token provider, security filter, and security config`

---

### Task 8 — Auth controller (register + login) + tests

**Files:**
- Create: `src/main/java/com/silentsupply/config/dto/AuthRequest.java`
- Create: `src/main/java/com/silentsupply/config/dto/AuthResponse.java`
- Create: `src/main/java/com/silentsupply/config/AuthController.java`
- Test: `src/test/java/com/silentsupply/config/AuthControllerIntegrationTest.java`

**Steps:**

1. Create `src/main/java/com/silentsupply/config/dto/AuthRequest.java`:

```java
package com.silentsupply.config.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for login authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    /** The company's email address. */
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    /** The company's password. */
    @NotBlank(message = "Password is required")
    private String password;
}
```

2. Create `src/main/java/com/silentsupply/config/dto/AuthResponse.java`:

```java
package com.silentsupply.config.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned after successful authentication.
 * Contains the JWT token and basic company info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /** The JWT access token. */
    private String token;

    /** The authenticated company's ID. */
    private Long companyId;

    /** The authenticated company's email. */
    private String email;

    /** The authenticated company's role (SUPPLIER or BUYER). */
    private String role;
}
```

3. Create `src/main/java/com/silentsupply/config/AuthController.java`:

```java
package com.silentsupply.config;

import com.silentsupply.common.exception.AuthenticationFailedException;
import com.silentsupply.company.CompanyService;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.company.dto.CompanyResponse;
import com.silentsupply.config.dto.AuthRequest;
import com.silentsupply.config.dto.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication operations: registration and login.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login to obtain JWT tokens")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CompanyService companyService;

    /**
     * Registers a new company and returns a JWT token.
     *
     * @param request the company registration details
     * @return the JWT token and company info
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new company and get a JWT token")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody CompanyRequest request) {
        CompanyResponse company = companyService.register(request);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        CompanyUserDetails userDetails = (CompanyUserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .companyId(company.getId())
                .email(company.getEmail())
                .role(company.getRole().name())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    /**
     * Authenticates a company with email and password, returns a JWT token.
     *
     * @param request the login credentials
     * @return the JWT token and company info
     * @throws AuthenticationFailedException if credentials are invalid
     */
    @PostMapping("/login")
    @Operation(summary = "Login with email and password to get a JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            CompanyUserDetails userDetails = (CompanyUserDetails) authentication.getPrincipal();
            String token = jwtTokenProvider.generateToken(userDetails);

            AuthResponse authResponse = AuthResponse.builder()
                    .token(token)
                    .companyId(userDetails.getId())
                    .email(userDetails.getEmail())
                    .role(userDetails.getRole())
                    .build();

            return ResponseEntity.ok(authResponse);
        } catch (BadCredentialsException e) {
            throw new AuthenticationFailedException("Invalid email or password");
        }
    }
}
```

4. Create `src/test/java/com/silentsupply/config/AuthControllerIntegrationTest.java`:

```java
package com.silentsupply.config;

import com.silentsupply.common.BaseIntegrationTest;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.config.dto.AuthRequest;
import com.silentsupply.config.dto.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AuthController}.
 */
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CompanyRepository companyRepository;

    @BeforeEach
    void cleanUp() {
        companyRepository.deleteAll();
    }

    @Test
    void register_withValidRequest_returns201WithToken() {
        CompanyRequest request = CompanyRequest.builder()
                .name("Acme Corp")
                .email("acme@example.com")
                .password("password123")
                .role(CompanyRole.SUPPLIER)
                .build();

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/register", request, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
        assertThat(response.getBody().getCompanyId()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("acme@example.com");
        assertThat(response.getBody().getRole()).isEqualTo("SUPPLIER");
    }

    @Test
    void login_withValidCredentials_returns200WithToken() {
        CompanyRequest registerRequest = CompanyRequest.builder()
                .name("Acme Corp")
                .email("acme@example.com")
                .password("password123")
                .role(CompanyRole.SUPPLIER)
                .build();
        restTemplate.postForEntity("/api/auth/register", registerRequest, AuthResponse.class);

        AuthRequest loginRequest = AuthRequest.builder()
                .email("acme@example.com")
                .password("password123")
                .build();

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/login", loginRequest, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
        assertThat(response.getBody().getEmail()).isEqualTo("acme@example.com");
    }

    @Test
    void login_withInvalidPassword_returns401() {
        CompanyRequest registerRequest = CompanyRequest.builder()
                .name("Acme Corp")
                .email("acme@example.com")
                .password("password123")
                .role(CompanyRole.SUPPLIER)
                .build();
        restTemplate.postForEntity("/api/auth/register", registerRequest, AuthResponse.class);

        AuthRequest loginRequest = AuthRequest.builder()
                .email("acme@example.com")
                .password("wrongpassword")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login", loginRequest, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_withNonExistentEmail_returns401() {
        AuthRequest loginRequest = AuthRequest.builder()
                .email("nobody@example.com")
                .password("password123")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/login", loginRequest, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
```

5. Run tests:

```bash
./mvnw test -Dtest=AuthControllerIntegrationTest
```

6. Run the full test suite to verify everything from Phases 1 and 2 passes:

```bash
./mvnw test
```

7. Commit: `feat: add auth controller with register and login endpoints`

---

## Phase 3: Product Catalog

### Task 9 — Product entity, repository, DTO, mapper

**Files:**
- Create: `src/main/java/com/silentsupply/product/ProductStatus.java`
- Create: `src/main/java/com/silentsupply/product/Product.java`
- Create: `src/main/java/com/silentsupply/product/ProductRepository.java`
- Create: `src/main/java/com/silentsupply/product/dto/ProductRequest.java`
- Create: `src/main/java/com/silentsupply/product/dto/ProductResponse.java`
- Create: `src/main/java/com/silentsupply/product/dto/ProductSearchCriteria.java`
- Create: `src/main/java/com/silentsupply/product/ProductMapper.java`
- Test: `src/test/java/com/silentsupply/product/ProductMapperTest.java`

**Steps:**

1. Create `src/main/java/com/silentsupply/product/ProductStatus.java`:

```java
package com.silentsupply.product;

/**
 * Status of a product listing in the catalog.
 */
public enum ProductStatus {

    /** Product is available for purchase. */
    ACTIVE,

    /** Product is temporarily unavailable. */
    OUT_OF_STOCK,

    /** Product has been permanently removed from the catalog. */
    DISCONTINUED
}
```

2. Create `src/main/java/com/silentsupply/product/Product.java`:

```java
package com.silentsupply.product;

import com.silentsupply.common.entity.BaseEntity;
import com.silentsupply.company.Company;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents a product listed by a supplier in the SilentSupply catalog.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    /** The supplier who owns this product listing. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Company supplier;

    /** Product name. */
    @Column(nullable = false)
    private String name;

    /** Detailed product description. */
    private String description;

    /** Product category for filtering. */
    @Column(nullable = false)
    private String category;

    /** Stock Keeping Unit — unique per supplier. */
    @Column(nullable = false)
    private String sku;

    /** Unit of measure (e.g., "kg", "piece", "liter"). */
    @Column(name = "unit_of_measure", nullable = false)
    private String unitOfMeasure;

    /** Base price per unit. */
    @Column(name = "base_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal basePrice;

    /** Available stock quantity. */
    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    /** Current listing status. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;
}
```

3. Create `src/main/java/com/silentsupply/product/ProductRepository.java`:

```java
package com.silentsupply.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Spring Data repository for {@link Product} entities.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Finds all products owned by a specific supplier.
     *
     * @param supplierId the supplier's company ID
     * @return list of products for that supplier
     */
    List<Product> findBySupplierId(Long supplierId);

    /**
     * Searches products with optional filters. All parameters are nullable — null means no filter.
     *
     * @param category    category filter (exact match)
     * @param name        name filter (case-insensitive contains)
     * @param minPrice    minimum base price
     * @param maxPrice    maximum base price
     * @param status      product status filter
     * @return list of matching products
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:minPrice IS NULL OR p.basePrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.basePrice <= :maxPrice) AND " +
           "(:status IS NULL OR p.status = :status)")
    List<Product> search(@Param("category") String category,
                         @Param("name") String name,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice,
                         @Param("status") ProductStatus status);
}
```

4. Create `src/main/java/com/silentsupply/product/dto/ProductRequest.java`:

```java
package com.silentsupply.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating or updating a product listing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    /** Product name. */
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String name;

    /** Product description. */
    private String description;

    /** Product category. */
    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    /** Stock Keeping Unit. */
    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;

    /** Unit of measure. */
    @NotBlank(message = "Unit of measure is required")
    @Size(max = 50, message = "Unit of measure must not exceed 50 characters")
    private String unitOfMeasure;

    /** Base price per unit. */
    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be positive")
    private BigDecimal basePrice;

    /** Available stock quantity. */
    @NotNull(message = "Available quantity is required")
    @PositiveOrZero(message = "Available quantity must not be negative")
    private Integer availableQuantity;
}
```

5. Create `src/main/java/com/silentsupply/product/dto/ProductResponse.java`:

```java
package com.silentsupply.product.dto;

import com.silentsupply.product.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO representing a product listing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    /** Product ID. */
    private Long id;

    /** Supplier's company ID. */
    private Long supplierId;

    /** Supplier's company name. */
    private String supplierName;

    /** Product name. */
    private String name;

    /** Product description. */
    private String description;

    /** Product category. */
    private String category;

    /** Stock Keeping Unit. */
    private String sku;

    /** Unit of measure. */
    private String unitOfMeasure;

    /** Base price per unit. */
    private BigDecimal basePrice;

    /** Available stock quantity. */
    private int availableQuantity;

    /** Current listing status. */
    private ProductStatus status;

    /** When the product was created. */
    private LocalDateTime createdAt;
}
```

6. Create `src/main/java/com/silentsupply/product/dto/ProductSearchCriteria.java`:

```java
package com.silentsupply.product.dto;

import com.silentsupply.product.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Search criteria DTO for filtering products. All fields are optional.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchCriteria {

    /** Filter by exact category match. */
    private String category;

    /** Filter by name (case-insensitive contains). */
    private String name;

    /** Minimum price filter. */
    private BigDecimal minPrice;

    /** Maximum price filter. */
    private BigDecimal maxPrice;

    /** Filter by product status. */
    private ProductStatus status;
}
```

7. Create `src/main/java/com/silentsupply/product/ProductMapper.java`:

```java
package com.silentsupply.product;

import com.silentsupply.product.dto.ProductRequest;
import com.silentsupply.product.dto.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for converting between {@link Product} entities and DTOs.
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    /**
     * Converts a product request DTO to a Product entity.
     * The supplier must be set separately by the service.
     *
     * @param request the product request
     * @return the Product entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toEntity(ProductRequest request);

    /**
     * Converts a Product entity to a response DTO.
     *
     * @param product the product entity
     * @return the response DTO
     */
    @Mapping(source = "supplier.id", target = "supplierId")
    @Mapping(source = "supplier.name", target = "supplierName")
    ProductResponse toResponse(Product product);

    /**
     * Updates an existing product entity with values from the request DTO.
     *
     * @param request the updated product details
     * @param product the existing product to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(ProductRequest request, @MappingTarget Product product);
}
```

8. Create `src/test/java/com/silentsupply/product/ProductMapperTest.java`:

```java
package com.silentsupply.product;

import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.product.dto.ProductRequest;
import com.silentsupply.product.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ProductMapper}.
 */
class ProductMapperTest {

    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void toEntity_mapsRequestFields() {
        ProductRequest request = ProductRequest.builder()
                .name("Widget A")
                .description("A fine widget")
                .category("Electronics")
                .sku("WDG-001")
                .unitOfMeasure("piece")
                .basePrice(new BigDecimal("29.99"))
                .availableQuantity(100)
                .build();

        Product product = mapper.toEntity(request);

        assertThat(product.getId()).isNull();
        assertThat(product.getSupplier()).isNull();
        assertThat(product.getName()).isEqualTo("Widget A");
        assertThat(product.getCategory()).isEqualTo("Electronics");
        assertThat(product.getSku()).isEqualTo("WDG-001");
        assertThat(product.getBasePrice()).isEqualByComparingTo(new BigDecimal("29.99"));
        assertThat(product.getAvailableQuantity()).isEqualTo(100);
    }

    @Test
    void toResponse_mapsEntityFieldsIncludingSupplier() {
        Company supplier = Company.builder()
                .name("Acme Corp")
                .email("acme@example.com")
                .password("hashed")
                .role(CompanyRole.SUPPLIER)
                .build();
        supplier.setId(1L);

        Product product = Product.builder()
                .supplier(supplier)
                .name("Widget A")
                .description("A fine widget")
                .category("Electronics")
                .sku("WDG-001")
                .unitOfMeasure("piece")
                .basePrice(new BigDecimal("29.99"))
                .availableQuantity(100)
                .status(ProductStatus.ACTIVE)
                .build();
        product.setId(10L);

        ProductResponse response = mapper.toResponse(product);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getSupplierId()).isEqualTo(1L);
        assertThat(response.getSupplierName()).isEqualTo("Acme Corp");
        assertThat(response.getName()).isEqualTo("Widget A");
        assertThat(response.getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    void updateEntity_updatesExistingProduct() {
        Product existing = Product.builder()
                .name("Old Name")
                .description("Old desc")
                .category("Old Cat")
                .sku("OLD-001")
                .unitOfMeasure("kg")
                .basePrice(new BigDecimal("10.00"))
                .availableQuantity(50)
                .build();
        existing.setId(5L);

        ProductRequest request = ProductRequest.builder()
                .name("New Name")
                .description("New desc")
                .category("New Cat")
                .sku("NEW-001")
                .unitOfMeasure("piece")
                .basePrice(new BigDecimal("25.00"))
                .availableQuantity(200)
                .build();

        mapper.updateEntity(request, existing);

        assertThat(existing.getId()).isEqualTo(5L);
        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.getBasePrice()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(existing.getAvailableQuantity()).isEqualTo(200);
    }
}
```

9. Run tests:

```bash
./mvnw test -Dtest=ProductMapperTest
```

10. Commit: `feat: add product entity, repository, DTOs, and mapper`

---

### Task 10 — Product service + tests

**Files:**
- Create: `src/main/java/com/silentsupply/product/ProductService.java`
- Test: `src/test/java/com/silentsupply/product/ProductServiceTest.java`

**Steps:**

1. Create `src/main/java/com/silentsupply/product/ProductService.java`:

```java
package com.silentsupply.product;

import com.silentsupply.common.exception.AccessDeniedException;
import com.silentsupply.common.exception.ResourceNotFoundException;
import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.product.dto.ProductRequest;
import com.silentsupply.product.dto.ProductResponse;
import com.silentsupply.product.dto.ProductSearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for product catalog CRUD operations and search.
 * Enforces that only the owning supplier can create, update, or delete products.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final ProductMapper productMapper;

    /**
     * Creates a new product listing for the given supplier.
     *
     * @param supplierId the supplier's company ID
     * @param request    the product details
     * @return the created product
     */
    @Transactional
    public ProductResponse create(Long supplierId, ProductRequest request) {
        Company supplier = companyRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", supplierId));

        Product product = productMapper.toEntity(request);
        product.setSupplier(supplier);

        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id the product ID
     * @return the product details
     * @throws ResourceNotFoundException if no product exists with the given ID
     */
    public ProductResponse getById(Long id) {
        Product product = findProductOrThrow(id);
        return productMapper.toResponse(product);
    }

    /**
     * Updates a product listing. Only the owning supplier can update.
     *
     * @param productId  the product ID to update
     * @param supplierId the requesting supplier's ID
     * @param request    the updated product details
     * @return the updated product
     * @throws AccessDeniedException if the supplier does not own this product
     */
    @Transactional
    public ProductResponse update(Long productId, Long supplierId, ProductRequest request) {
        Product product = findProductOrThrow(productId);
        verifyOwnership(product, supplierId);

        productMapper.updateEntity(request, product);
        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    /**
     * Deletes a product listing. Only the owning supplier can delete.
     *
     * @param productId  the product ID to delete
     * @param supplierId the requesting supplier's ID
     * @throws AccessDeniedException if the supplier does not own this product
     */
    @Transactional
    public void delete(Long productId, Long supplierId) {
        Product product = findProductOrThrow(productId);
        verifyOwnership(product, supplierId);
        productRepository.delete(product);
    }

    /**
     * Lists all products for a given supplier.
     *
     * @param supplierId the supplier's company ID
     * @return list of products for that supplier
     */
    public List<ProductResponse> listBySupplier(Long supplierId) {
        return productRepository.findBySupplierId(supplierId).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    /**
     * Searches products using optional filter criteria.
     *
     * @param criteria the search filters
     * @return list of matching products
     */
    public List<ProductResponse> search(ProductSearchCriteria criteria) {
        return productRepository.search(
                criteria.getCategory(),
                criteria.getName(),
                criteria.getMinPrice(),
                criteria.getMaxPrice(),
                criteria.getStatus()
        ).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    /**
     * Finds a product by ID or throws.
     *
     * @param id the product ID
     * @return the product entity
     */
    private Product findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    /**
     * Verifies that the given supplier owns the product.
     *
     * @param product    the product entity
     * @param supplierId the requesting supplier's ID
     */
    private void verifyOwnership(Product product, Long supplierId) {
        if (!product.getSupplier().getId().equals(supplierId)) {
            throw new AccessDeniedException("You can only manage your own products");
        }
    }
}
```

2. Create `src/test/java/com/silentsupply/product/ProductServiceTest.java`:

```java
package com.silentsupply.product;

import com.silentsupply.common.exception.AccessDeniedException;
import com.silentsupply.common.exception.ResourceNotFoundException;
import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.product.dto.ProductRequest;
import com.silentsupply.product.dto.ProductResponse;
import com.silentsupply.product.dto.ProductSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ProductService}.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Company supplier;
    private Product product;
    private ProductRequest request;
    private ProductResponse response;

    @BeforeEach
    void setUp() {
        supplier = Company.builder()
                .name("Acme Corp")
                .email("acme@example.com")
                .password("hashed")
                .role(CompanyRole.SUPPLIER)
                .build();
        supplier.setId(1L);

        product = Product.builder()
                .supplier(supplier)
                .name("Widget A")
                .description("A fine widget")
                .category("Electronics")
                .sku("WDG-001")
                .unitOfMeasure("piece")
                .basePrice(new BigDecimal("29.99"))
                .availableQuantity(100)
                .status(ProductStatus.ACTIVE)
                .build();
        product.setId(10L);

        request = ProductRequest.builder()
                .name("Widget A")
                .description("A fine widget")
                .category("Electronics")
                .sku("WDG-001")
                .unitOfMeasure("piece")
                .basePrice(new BigDecimal("29.99"))
                .availableQuantity(100)
                .build();

        response = ProductResponse.builder()
                .id(10L)
                .supplierId(1L)
                .supplierName("Acme Corp")
                .name("Widget A")
                .category("Electronics")
                .sku("WDG-001")
                .unitOfMeasure("piece")
                .basePrice(new BigDecimal("29.99"))
                .availableQuantity(100)
                .status(ProductStatus.ACTIVE)
                .build();
    }

    @Test
    void create_withValidRequest_savesAndReturnsResponse() {
        when(companyRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productMapper.toEntity(request)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.create(1L, request);

        assertThat(result.getName()).isEqualTo("Widget A");
        assertThat(result.getSupplierId()).isEqualTo(1L);
        verify(productRepository).save(product);
    }

    @Test
    void create_withNonExistentSupplier_throwsResourceNotFoundException() {
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Company not found");

        verify(productRepository, never()).save(any());
    }

    @Test
    void getById_withExistingId_returnsResponse() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.getById(10L);

        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void getById_withNonExistingId_throwsResourceNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_byOwningSupplier_updatesAndReturnsResponse() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(response);

        ProductResponse result = productService.update(10L, 1L, request);

        assertThat(result.getName()).isEqualTo("Widget A");
        verify(productMapper).updateEntity(request, product);
    }

    @Test
    void update_byDifferentSupplier_throwsAccessDeniedException() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.update(10L, 999L, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("your own products");
    }

    @Test
    void delete_byOwningSupplier_deletesProduct() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        productService.delete(10L, 1L);

        verify(productRepository).delete(product);
    }

    @Test
    void delete_byDifferentSupplier_throwsAccessDeniedException() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.delete(10L, 999L))
                .isInstanceOf(AccessDeniedException.class);

        verify(productRepository, never()).delete(any());
    }

    @Test
    void listBySupplier_returnsProductsForSupplier() {
        when(productRepository.findBySupplierId(1L)).thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        List<ProductResponse> result = productService.listBySupplier(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSupplierId()).isEqualTo(1L);
    }

    @Test
    void search_withCriteria_returnsMatchingProducts() {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .category("Electronics")
                .build();

        when(productRepository.search("Electronics", null, null, null, null))
                .thenReturn(List.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        List<ProductResponse> result = productService.search(criteria);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("Electronics");
    }
}
```

3. Run tests:

```bash
./mvnw test -Dtest=ProductServiceTest
```

4. Commit: `feat: add product service with CRUD, ownership check, and search`

---

### Task 11 — Product controller + integration tests

**Files:**
- Create: `src/main/java/com/silentsupply/product/ProductController.java`
- Test: `src/test/java/com/silentsupply/product/ProductControllerIntegrationTest.java`

**Steps:**

1. Create `src/main/java/com/silentsupply/product/ProductController.java`:

```java
package com.silentsupply.product;

import com.silentsupply.config.CompanyUserDetails;
import com.silentsupply.product.dto.ProductRequest;
import com.silentsupply.product.dto.ProductResponse;
import com.silentsupply.product.dto.ProductSearchCriteria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for product catalog operations.
 * Suppliers can create, update, and delete their own products.
 * All authenticated users can browse and search products.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog CRUD and search")
public class ProductController {

    private final ProductService productService;

    /**
     * Creates a new product listing. Supplier-only.
     *
     * @param userDetails the authenticated supplier
     * @param request     the product details
     * @return the created product with HTTP 201
     */
    @PostMapping
    @Operation(summary = "Create a new product (supplier only)")
    public ResponseEntity<ProductResponse> create(
            @AuthenticationPrincipal CompanyUserDetails userDetails,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.create(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id the product ID
     * @return the product details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    /**
     * Updates a product listing. Supplier-only, must own the product.
     *
     * @param id          the product ID
     * @param userDetails the authenticated supplier
     * @param request     the updated product details
     * @return the updated product
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a product (supplier only, must own)")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @AuthenticationPrincipal CompanyUserDetails userDetails,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.update(id, userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a product listing. Supplier-only, must own the product.
     *
     * @param id          the product ID
     * @param userDetails the authenticated supplier
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product (supplier only, must own)")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CompanyUserDetails userDetails) {
        productService.delete(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Searches products with optional filters.
     *
     * @param category category filter
     * @param name     name filter (case-insensitive contains)
     * @param minPrice minimum price filter
     * @param maxPrice maximum price filter
     * @param status   status filter
     * @return list of matching products
     */
    @GetMapping
    @Operation(summary = "Search products with optional filters")
    public ResponseEntity<List<ProductResponse>> search(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) ProductStatus status) {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .category(category)
                .name(name)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .status(status)
                .build();
        return ResponseEntity.ok(productService.search(criteria));
    }
}
```

2. Create `src/test/java/com/silentsupply/product/ProductControllerIntegrationTest.java`:

```java
package com.silentsupply.product;

import com.silentsupply.common.BaseIntegrationTest;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.config.dto.AuthResponse;
import com.silentsupply.product.dto.ProductRequest;
import com.silentsupply.product.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ProductController}.
 * Tests CRUD operations and supplier-only access enforcement.
 */
class ProductControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private String supplierToken;
    private String buyerToken;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        companyRepository.deleteAll();

        supplierToken = registerAndGetToken("SupplierCo", "supplier@example.com", CompanyRole.SUPPLIER);
        buyerToken = registerAndGetToken("BuyerCo", "buyer@example.com", CompanyRole.BUYER);
    }

    @Test
    void create_asSupplier_returns201() {
        ProductRequest request = buildProductRequest("Widget A", "WDG-001");

        ResponseEntity<ProductResponse> response = postProduct(request, supplierToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Widget A");
        assertThat(response.getBody().getSupplierId()).isNotNull();
    }

    @Test
    void create_asBuyer_returns403() {
        ProductRequest request = buildProductRequest("Widget A", "WDG-001");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(buyerToken)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void create_withoutAuth_returns401() {
        ProductRequest request = buildProductRequest("Widget A", "WDG-001");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/products", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getById_returnsProduct() {
        ProductRequest request = buildProductRequest("Widget A", "WDG-001");
        ResponseEntity<ProductResponse> created = postProduct(request, supplierToken);
        Long id = created.getBody().getId();

        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "/api/products/" + id, HttpMethod.GET,
                new HttpEntity<>(authHeaders(supplierToken)),
                ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("Widget A");
    }

    @Test
    void search_byCategory_returnsFilteredResults() {
        postProduct(buildProductRequest("Widget A", "WDG-001"), supplierToken);

        ProductRequest otherProduct = ProductRequest.builder()
                .name("Gadget B")
                .category("Hardware")
                .sku("GDG-001")
                .unitOfMeasure("piece")
                .basePrice(new BigDecimal("49.99"))
                .availableQuantity(50)
                .build();
        postProduct(otherProduct, supplierToken);

        ResponseEntity<ProductResponse[]> response = restTemplate.exchange(
                "/api/products?category=Electronics", HttpMethod.GET,
                new HttpEntity<>(authHeaders(supplierToken)),
                ProductResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].getCategory()).isEqualTo("Electronics");
    }

    @Test
    void delete_byOwningSupplier_returns204() {
        ResponseEntity<ProductResponse> created = postProduct(
                buildProductRequest("Widget A", "WDG-001"), supplierToken);
        Long id = created.getBody().getId();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/products/" + id, HttpMethod.DELETE,
                new HttpEntity<>(authHeaders(supplierToken)),
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private String registerAndGetToken(String name, String email, CompanyRole role) {
        CompanyRequest request = CompanyRequest.builder()
                .name(name)
                .email(email)
                .password("password123")
                .role(role)
                .build();
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/register", request, AuthResponse.class);
        return response.getBody().getToken();
    }

    private ProductRequest buildProductRequest(String name, String sku) {
        return ProductRequest.builder()
                .name(name)
                .description("Test product")
                .category("Electronics")
                .sku(sku)
                .unitOfMeasure("piece")
                .basePrice(new BigDecimal("29.99"))
                .availableQuantity(100)
                .build();
    }

    private ResponseEntity<ProductResponse> postProduct(ProductRequest request, String token) {
        return restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(token)),
                ProductResponse.class);
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
```

3. Run tests:

```bash
./mvnw test -Dtest=ProductControllerIntegrationTest
```

4. Commit: `feat: add product controller with CRUD endpoints and supplier-only access`

---

## Phase 4: Catalog Orders

### Task 12 — CatalogOrder entity, repository, DTO, mapper

**Files:**
- Create: `src/main/java/com/silentsupply/order/OrderStatus.java`
- Create: `src/main/java/com/silentsupply/order/CatalogOrder.java`
- Create: `src/main/java/com/silentsupply/order/CatalogOrderRepository.java`
- Create: `src/main/java/com/silentsupply/order/dto/OrderRequest.java`
- Create: `src/main/java/com/silentsupply/order/dto/OrderResponse.java`
- Create: `src/main/java/com/silentsupply/order/dto/OrderStatusUpdate.java`
- Create: `src/main/java/com/silentsupply/order/CatalogOrderMapper.java`
- Test: `src/test/java/com/silentsupply/order/CatalogOrderMapperTest.java`

**Steps:**

1. Create `src/main/java/com/silentsupply/order/OrderStatus.java`:

```java
package com.silentsupply.order;

/**
 * Status flow for catalog orders.
 * Valid transitions: PLACED -> CONFIRMED -> SHIPPED -> DELIVERED, or any -> CANCELLED.
 */
public enum OrderStatus {

    /** Order has been placed by the buyer. */
    PLACED,

    /** Order confirmed by the supplier. */
    CONFIRMED,

    /** Order has been shipped. */
    SHIPPED,

    /** Order delivered to the buyer. */
    DELIVERED,

    /** Order has been cancelled. */
    CANCELLED
}
```

2. Create `src/main/java/com/silentsupply/order/CatalogOrder.java`:

```java
package com.silentsupply.order;

import com.silentsupply.common.entity.BaseEntity;
import com.silentsupply.company.Company;
import com.silentsupply.product.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents a direct catalog order — a purchase at the listed price without negotiation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "catalog_orders")
public class CatalogOrder extends BaseEntity {

    /** The buyer who placed the order. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Company buyer;

    /** The product being ordered. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** The supplier fulfilling the order. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Company supplier;

    /** Quantity ordered. */
    @Column(nullable = false)
    private int quantity;

    /** Price per unit at time of order. */
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    /** Total order value (quantity * unitPrice). */
    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;

    /** Current order status. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PLACED;
}
```

3. Create `src/main/java/com/silentsupply/order/CatalogOrderRepository.java`:

```java
package com.silentsupply.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for {@link CatalogOrder} entities.
 */
@Repository
public interface CatalogOrderRepository extends JpaRepository<CatalogOrder, Long> {

    /**
     * Finds all orders placed by a specific buyer.
     *
     * @param buyerId the buyer's company ID
     * @return list of orders for that buyer
     */
    List<CatalogOrder> findByBuyerId(Long buyerId);

    /**
     * Finds all orders for a specific supplier.
     *
     * @param supplierId the supplier's company ID
     * @return list of orders for that supplier
     */
    List<CatalogOrder> findBySupplierId(Long supplierId);
}
```

4. Create `src/main/java/com/silentsupply/order/dto/OrderRequest.java`:

```java
package com.silentsupply.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for placing a new catalog order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    /** The product to order. */
    @NotNull(message = "Product ID is required")
    private Long productId;

    /** Quantity to order. */
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
}
```

5. Create `src/main/java/com/silentsupply/order/dto/OrderResponse.java`:

```java
package com.silentsupply.order.dto;

import com.silentsupply.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO representing a catalog order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    /** Order ID. */
    private Long id;

    /** Buyer's company ID. */
    private Long buyerId;

    /** Buyer's company name. */
    private String buyerName;

    /** Product ID. */
    private Long productId;

    /** Product name. */
    private String productName;

    /** Supplier's company ID. */
    private Long supplierId;

    /** Supplier's company name. */
    private String supplierName;

    /** Quantity ordered. */
    private int quantity;

    /** Price per unit at time of order. */
    private BigDecimal unitPrice;

    /** Total order value. */
    private BigDecimal totalPrice;

    /** Current order status. */
    private OrderStatus status;

    /** When the order was placed. */
    private LocalDateTime createdAt;
}
```

6. Create `src/main/java/com/silentsupply/order/dto/OrderStatusUpdate.java`:

```java
package com.silentsupply.order.dto;

import com.silentsupply.order.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an order's status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusUpdate {

    /** The new status to transition to. */
    @NotNull(message = "Status is required")
    private OrderStatus status;
}
```

7. Create `src/main/java/com/silentsupply/order/CatalogOrderMapper.java`:

```java
package com.silentsupply.order;

import com.silentsupply.order.dto.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting {@link CatalogOrder} entities to response DTOs.
 */
@Mapper(componentModel = "spring")
public interface CatalogOrderMapper {

    /**
     * Converts a CatalogOrder entity to a response DTO.
     *
     * @param order the catalog order entity
     * @return the response DTO
     */
    @Mapping(source = "buyer.id", target = "buyerId")
    @Mapping(source = "buyer.name", target = "buyerName")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "supplier.id", target = "supplierId")
    @Mapping(source = "supplier.name", target = "supplierName")
    OrderResponse toResponse(CatalogOrder order);
}
```

8. Create `src/test/java/com/silentsupply/order/CatalogOrderMapperTest.java`:

```java
package com.silentsupply.order;

import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.order.dto.OrderResponse;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CatalogOrderMapper}.
 */
class CatalogOrderMapperTest {

    private final CatalogOrderMapper mapper = Mappers.getMapper(CatalogOrderMapper.class);

    @Test
    void toResponse_mapsAllNestedFields() {
        Company buyer = Company.builder().name("BuyerCo").email("b@b.com").password("p").role(CompanyRole.BUYER).build();
        buyer.setId(1L);

        Company supplier = Company.builder().name("SupplierCo").email("s@s.com").password("p").role(CompanyRole.SUPPLIER).build();
        supplier.setId(2L);

        Product product = Product.builder()
                .supplier(supplier).name("Widget").category("Cat").sku("W-1")
                .unitOfMeasure("pc").basePrice(new BigDecimal("10.00")).availableQuantity(100)
                .status(ProductStatus.ACTIVE).build();
        product.setId(10L);

        CatalogOrder order = CatalogOrder.builder()
                .buyer(buyer).product(product).supplier(supplier)
                .quantity(5).unitPrice(new BigDecimal("10.00")).totalPrice(new BigDecimal("50.00"))
                .status(OrderStatus.PLACED).build();
        order.setId(100L);

        OrderResponse response = mapper.toResponse(order);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getBuyerId()).isEqualTo(1L);
        assertThat(response.getBuyerName()).isEqualTo("BuyerCo");
        assertThat(response.getProductId()).isEqualTo(10L);
        assertThat(response.getProductName()).isEqualTo("Widget");
        assertThat(response.getSupplierId()).isEqualTo(2L);
        assertThat(response.getSupplierName()).isEqualTo("SupplierCo");
        assertThat(response.getQuantity()).isEqualTo(5);
        assertThat(response.getTotalPrice()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PLACED);
    }
}
```

9. Run tests:

```bash
./mvnw test -Dtest=CatalogOrderMapperTest
```

10. Commit: `feat: add catalog order entity, repository, DTOs, and mapper`

---

### Task 13 — Order service + tests

**Files:**
- Create: `src/main/java/com/silentsupply/order/CatalogOrderService.java`
- Test: `src/test/java/com/silentsupply/order/CatalogOrderServiceTest.java`

**Steps:**

1. Create `src/main/java/com/silentsupply/order/CatalogOrderService.java`:

```java
package com.silentsupply.order;

import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.common.exception.ResourceNotFoundException;
import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.order.dto.OrderRequest;
import com.silentsupply.order.dto.OrderResponse;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductRepository;
import com.silentsupply.product.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service layer for catalog order operations.
 * Handles order placement with stock validation, status transitions, and listing.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogOrderService {

    /** Defines the valid status transitions for catalog orders. */
    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
            OrderStatus.PLACED, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
            OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, Set.of(),
            OrderStatus.CANCELLED, Set.of()
    );

    private final CatalogOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final CatalogOrderMapper orderMapper;

    /**
     * Places a new catalog order. Validates stock availability and deducts quantity.
     *
     * @param buyerId the buyer's company ID
     * @param request the order request
     * @return the created order
     * @throws BusinessRuleException if the product is not active or has insufficient stock
     */
    @Transactional
    public OrderResponse placeOrder(Long buyerId, OrderRequest request) {
        Company buyer = companyRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", buyerId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BusinessRuleException("Product is not available for purchase: " + product.getStatus());
        }

        if (product.getAvailableQuantity() < request.getQuantity()) {
            throw new BusinessRuleException(
                    "Insufficient stock. Available: " + product.getAvailableQuantity()
                    + ", requested: " + request.getQuantity());
        }

        product.setAvailableQuantity(product.getAvailableQuantity() - request.getQuantity());
        productRepository.save(product);

        BigDecimal totalPrice = product.getBasePrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        CatalogOrder order = CatalogOrder.builder()
                .buyer(buyer)
                .product(product)
                .supplier(product.getSupplier())
                .quantity(request.getQuantity())
                .unitPrice(product.getBasePrice())
                .totalPrice(totalPrice)
                .status(OrderStatus.PLACED)
                .build();

        CatalogOrder saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param id the order ID
     * @return the order details
     */
    public OrderResponse getById(Long id) {
        CatalogOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return orderMapper.toResponse(order);
    }

    /**
     * Lists all orders for a specific buyer.
     *
     * @param buyerId the buyer's company ID
     * @return list of orders
     */
    public List<OrderResponse> listByBuyer(Long buyerId) {
        return orderRepository.findByBuyerId(buyerId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    /**
     * Lists all orders for a specific supplier.
     *
     * @param supplierId the supplier's company ID
     * @return list of orders
     */
    public List<OrderResponse> listBySupplier(Long supplierId) {
        return orderRepository.findBySupplierId(supplierId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    /**
     * Transitions an order to a new status. Validates the transition is allowed.
     *
     * @param orderId   the order ID
     * @param newStatus the target status
     * @return the updated order
     * @throws BusinessRuleException if the status transition is not valid
     */
    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        CatalogOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        Set<OrderStatus> allowed = VALID_TRANSITIONS.getOrDefault(order.getStatus(), Set.of());
        if (!allowed.contains(newStatus)) {
            throw new BusinessRuleException(
                    "Invalid status transition from " + order.getStatus() + " to " + newStatus);
        }

        order.setStatus(newStatus);
        CatalogOrder saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }
}
```

2. Create `src/test/java/com/silentsupply/order/CatalogOrderServiceTest.java`:

```java
package com.silentsupply.order;

import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.common.exception.ResourceNotFoundException;
import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.order.dto.OrderRequest;
import com.silentsupply.order.dto.OrderResponse;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductRepository;
import com.silentsupply.product.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CatalogOrderService}.
 */
@ExtendWith(MockitoExtension.class)
class CatalogOrderServiceTest {

    @Mock
    private CatalogOrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private CatalogOrderMapper orderMapper;

    @InjectMocks
    private CatalogOrderService orderService;

    private Company buyer;
    private Company supplier;
    private Product product;

    @BeforeEach
    void setUp() {
        supplier = Company.builder().name("SupplierCo").email("s@s.com").password("p").role(CompanyRole.SUPPLIER).build();
        supplier.setId(1L);

        buyer = Company.builder().name("BuyerCo").email("b@b.com").password("p").role(CompanyRole.BUYER).build();
        buyer.setId(2L);

        product = Product.builder()
                .supplier(supplier).name("Widget").category("Cat").sku("W-1")
                .unitOfMeasure("pc").basePrice(new BigDecimal("10.00")).availableQuantity(100)
                .status(ProductStatus.ACTIVE).build();
        product.setId(10L);
    }

    @Test
    void placeOrder_withValidRequest_createsOrderAndDeductsStock() {
        OrderRequest request = OrderRequest.builder().productId(10L).quantity(5).build();
        OrderResponse expectedResponse = OrderResponse.builder()
                .id(100L).buyerId(2L).productId(10L).supplierId(1L)
                .quantity(5).unitPrice(new BigDecimal("10.00")).totalPrice(new BigDecimal("50.00"))
                .status(OrderStatus.PLACED).build();

        when(companyRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(CatalogOrder.class))).thenAnswer(invocation -> {
            CatalogOrder order = invocation.getArgument(0);
            order.setId(100L);
            return order;
        });
        when(orderMapper.toResponse(any(CatalogOrder.class))).thenReturn(expectedResponse);

        OrderResponse result = orderService.placeOrder(2L, request);

        assertThat(result.getQuantity()).isEqualTo(5);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(product.getAvailableQuantity()).isEqualTo(95);
        verify(productRepository).save(product);
    }

    @Test
    void placeOrder_withInsufficientStock_throwsBusinessRuleException() {
        OrderRequest request = OrderRequest.builder().productId(10L).quantity(200).build();

        when(companyRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.placeOrder(2L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Insufficient stock");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_withDiscontinuedProduct_throwsBusinessRuleException() {
        product.setStatus(ProductStatus.DISCONTINUED);
        OrderRequest request = OrderRequest.builder().productId(10L).quantity(5).build();

        when(companyRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.placeOrder(2L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void placeOrder_withNonExistentProduct_throwsResourceNotFoundException() {
        OrderRequest request = OrderRequest.builder().productId(99L).quantity(5).build();

        when(companyRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(2L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_withValidTransition_updatesStatus() {
        CatalogOrder order = CatalogOrder.builder()
                .buyer(buyer).product(product).supplier(supplier)
                .quantity(5).unitPrice(new BigDecimal("10.00")).totalPrice(new BigDecimal("50.00"))
                .status(OrderStatus.PLACED).build();
        order.setId(100L);

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(100L).status(OrderStatus.CONFIRMED).build();

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(expectedResponse);

        OrderResponse result = orderService.updateStatus(100L, OrderStatus.CONFIRMED);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void updateStatus_withInvalidTransition_throwsBusinessRuleException() {
        CatalogOrder order = CatalogOrder.builder()
                .buyer(buyer).product(product).supplier(supplier)
                .quantity(5).unitPrice(new BigDecimal("10.00")).totalPrice(new BigDecimal("50.00"))
                .status(OrderStatus.PLACED).build();
        order.setId(100L);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(100L, OrderStatus.DELIVERED))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void updateStatus_fromDelivered_throwsBusinessRuleException() {
        CatalogOrder order = CatalogOrder.builder()
                .buyer(buyer).product(product).supplier(supplier)
                .quantity(5).unitPrice(new BigDecimal("10.00")).totalPrice(new BigDecimal("50.00"))
                .status(OrderStatus.DELIVERED).build();
        order.setId(100L);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(100L, OrderStatus.CANCELLED))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Invalid status transition");
    }
}
```

3. Run tests:

```bash
./mvnw test -Dtest=CatalogOrderServiceTest
```

4. Commit: `feat: add catalog order service with placement, stock validation, and status transitions`

---

### Task 14 — Order controller + integration tests

**Files:**
- Create: `src/main/java/com/silentsupply/order/CatalogOrderController.java`
- Test: `src/test/java/com/silentsupply/order/CatalogOrderControllerIntegrationTest.java`

**Steps:**

1. Create `src/main/java/com/silentsupply/order/CatalogOrderController.java`:

```java
package com.silentsupply.order;

import com.silentsupply.config.CompanyUserDetails;
import com.silentsupply.order.dto.OrderRequest;
import com.silentsupply.order.dto.OrderResponse;
import com.silentsupply.order.dto.OrderStatusUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for catalog order operations.
 * Buyers place orders; suppliers and buyers can view and track them.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Catalog order placement and management")
public class CatalogOrderController {

    private final CatalogOrderService orderService;

    /**
     * Places a new catalog order. Buyer-only.
     *
     * @param userDetails the authenticated buyer
     * @param request     the order details
     * @return the created order with HTTP 201
     */
    @PostMapping
    @Operation(summary = "Place a new catalog order (buyer only)")
    public ResponseEntity<OrderResponse> placeOrder(
            @AuthenticationPrincipal CompanyUserDetails userDetails,
            @Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.placeOrder(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param id the order ID
     * @return the order details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    /**
     * Lists orders for the authenticated user (buyer or supplier).
     *
     * @param userDetails the authenticated user
     * @return list of orders
     */
    @GetMapping
    @Operation(summary = "List orders for authenticated user")
    public ResponseEntity<List<OrderResponse>> listOrders(
            @AuthenticationPrincipal CompanyUserDetails userDetails) {
        List<OrderResponse> orders;
        if ("SUPPLIER".equals(userDetails.getRole())) {
            orders = orderService.listBySupplier(userDetails.getId());
        } else {
            orders = orderService.listByBuyer(userDetails.getId());
        }
        return ResponseEntity.ok(orders);
    }

    /**
     * Updates the status of an order.
     *
     * @param id           the order ID
     * @param statusUpdate the new status
     * @return the updated order
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdate statusUpdate) {
        return ResponseEntity.ok(orderService.updateStatus(id, statusUpdate.getStatus()));
    }
}
```

2. Create `src/test/java/com/silentsupply/order/CatalogOrderControllerIntegrationTest.java`:

```java
package com.silentsupply.order;

import com.silentsupply.common.BaseIntegrationTest;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.config.dto.AuthResponse;
import com.silentsupply.order.dto.OrderRequest;
import com.silentsupply.order.dto.OrderResponse;
import com.silentsupply.order.dto.OrderStatusUpdate;
import com.silentsupply.product.ProductRepository;
import com.silentsupply.product.dto.ProductRequest;
import com.silentsupply.product.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link CatalogOrderController}.
 */
class CatalogOrderControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CatalogOrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private String supplierToken;
    private String buyerToken;
    private Long productId;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        companyRepository.deleteAll();

        supplierToken = registerAndGetToken("SupplierCo", "supplier@example.com", CompanyRole.SUPPLIER);
        buyerToken = registerAndGetToken("BuyerCo", "buyer@example.com", CompanyRole.BUYER);

        ProductRequest productRequest = ProductRequest.builder()
                .name("Widget").description("Test").category("Electronics").sku("W-1")
                .unitOfMeasure("piece").basePrice(new BigDecimal("10.00")).availableQuantity(100)
                .build();
        ResponseEntity<ProductResponse> productResponse = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                new HttpEntity<>(productRequest, authHeaders(supplierToken)),
                ProductResponse.class);
        productId = productResponse.getBody().getId();
    }

    @Test
    void placeOrder_asBuyer_returns201() {
        OrderRequest request = OrderRequest.builder().productId(productId).quantity(5).build();

        ResponseEntity<OrderResponse> response = restTemplate.exchange(
                "/api/orders", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(buyerToken)),
                OrderResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getQuantity()).isEqualTo(5);
        assertThat(response.getBody().getUnitPrice()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(response.getBody().getTotalPrice()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(response.getBody().getStatus()).isEqualTo(OrderStatus.PLACED);
    }

    @Test
    void placeOrder_asSupplier_returns403() {
        OrderRequest request = OrderRequest.builder().productId(productId).quantity(5).build();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/orders", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(supplierToken)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void placeOrder_withInsufficientStock_returns400() {
        OrderRequest request = OrderRequest.builder().productId(productId).quantity(200).build();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/orders", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(buyerToken)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateStatus_toConfirmed_returns200() {
        OrderRequest orderReq = OrderRequest.builder().productId(productId).quantity(5).build();
        ResponseEntity<OrderResponse> created = restTemplate.exchange(
                "/api/orders", HttpMethod.POST,
                new HttpEntity<>(orderReq, authHeaders(buyerToken)),
                OrderResponse.class);
        Long orderId = created.getBody().getId();

        OrderStatusUpdate statusUpdate = OrderStatusUpdate.builder()
                .status(OrderStatus.CONFIRMED).build();

        ResponseEntity<OrderResponse> response = restTemplate.exchange(
                "/api/orders/" + orderId + "/status", HttpMethod.PATCH,
                new HttpEntity<>(statusUpdate, authHeaders(supplierToken)),
                OrderResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void listOrders_asBuyer_returnsBuyerOrders() {
        OrderRequest request = OrderRequest.builder().productId(productId).quantity(3).build();
        restTemplate.exchange("/api/orders", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(buyerToken)), OrderResponse.class);

        ResponseEntity<OrderResponse[]> response = restTemplate.exchange(
                "/api/orders", HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                OrderResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    private String registerAndGetToken(String name, String email, CompanyRole role) {
        CompanyRequest request = CompanyRequest.builder()
                .name(name).email(email).password("password123").role(role).build();
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/register", request, AuthResponse.class);
        return response.getBody().getToken();
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
```

3. Run tests:

```bash
./mvnw test -Dtest=CatalogOrderControllerIntegrationTest
```

4. Commit: `feat: add catalog order controller with placement and status update endpoints`

---

## Phase 5: RFQ & Proposals

### Task 15 — RFQ entity, repository, DTO, mapper

**Files:**
- Create: `src/main/java/com/silentsupply/rfq/RfqStatus.java`
- Create: `src/main/java/com/silentsupply/rfq/Rfq.java`
- Create: `src/main/java/com/silentsupply/rfq/RfqRepository.java`
- Create: `src/main/java/com/silentsupply/rfq/dto/RfqRequest.java`
- Create: `src/main/java/com/silentsupply/rfq/dto/RfqResponse.java`
- Create: `src/main/java/com/silentsupply/rfq/RfqMapper.java`
- Test: `src/test/java/com/silentsupply/rfq/RfqMapperTest.java`

**Steps:**

1. Create `src/main/java/com/silentsupply/rfq/RfqStatus.java`:

```java
package com.silentsupply.rfq;

/**
 * Status flow for RFQs.
 * SUBMITTED -> UNDER_REVIEW -> COUNTERED/ACCEPTED/REJECTED, or -> EXPIRED.
 */
public enum RfqStatus {

    /** RFQ has been submitted by the buyer. */
    SUBMITTED,

    /** RFQ is being evaluated by the negotiation engine. */
    UNDER_REVIEW,

    /** A counter-proposal has been generated. */
    COUNTERED,

    /** RFQ has been accepted — terms agreed. */
    ACCEPTED,

    /** RFQ has been rejected — no agreement possible. */
    REJECTED,

    /** RFQ has expired without resolution. */
    EXPIRED
}
```

2. Create `src/main/java/com/silentsupply/rfq/Rfq.java`:

```java
package com.silentsupply.rfq;

import com.silentsupply.common.entity.BaseEntity;
import com.silentsupply.company.Company;
import com.silentsupply.product.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a Request for Quote submitted by a buyer for bulk or custom orders.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "rfqs")
public class Rfq extends BaseEntity {

    /** The buyer who submitted the RFQ. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Company buyer;

    /** The product being requested. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** The supplier who owns the product. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Company supplier;

    /** Desired quantity. */
    @Column(name = "desired_quantity", nullable = false)
    private int desiredQuantity;

    /** Target price per unit the buyer hopes to achieve. */
    @Column(name = "target_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal targetPrice;

    /** Required delivery deadline. */
    @Column(name = "delivery_deadline", nullable = false)
    private LocalDate deliveryDeadline;

    /** Optional notes from the buyer. */
    private String notes;

    /** Current RFQ status. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RfqStatus status = RfqStatus.SUBMITTED;

    /** Current negotiation round (0 = no proposals yet). */
    @Column(name = "current_round", nullable = false)
    @Builder.Default
    private int currentRound = 0;

    /** Maximum allowed negotiation rounds. */
    @Column(name = "max_rounds", nullable = false)
    @Builder.Default
    private int maxRounds = 3;

    /** When this RFQ expires. */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
```

3. Create `src/main/java/com/silentsupply/rfq/RfqRepository.java`:

```java
package com.silentsupply.rfq;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data repository for {@link Rfq} entities.
 */
@Repository
public interface RfqRepository extends JpaRepository<Rfq, Long> {

    /**
     * Finds all RFQs submitted by a specific buyer.
     *
     * @param buyerId the buyer's company ID
     * @return list of RFQs
     */
    List<Rfq> findByBuyerId(Long buyerId);

    /**
     * Finds all RFQs directed to a specific supplier.
     *
     * @param supplierId the supplier's company ID
     * @return list of RFQs
     */
    List<Rfq> findBySupplierId(Long supplierId);

    /**
     * Finds RFQs that have expired but are still in an active status.
     *
     * @param now            the current timestamp
     * @param activeStatuses the statuses considered active
     * @return list of expired RFQs
     */
    List<Rfq> findByExpiresAtBeforeAndStatusIn(LocalDateTime now, List<RfqStatus> activeStatuses);
}
```

4. Create `src/main/java/com/silentsupply/rfq/dto/RfqRequest.java`:

```java
package com.silentsupply.rfq.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for submitting a new RFQ.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RfqRequest {

    /** The product to request a quote for. */
    @NotNull(message = "Product ID is required")
    private Long productId;

    /** Desired quantity. */
    @NotNull(message = "Desired quantity is required")
    @Positive(message = "Desired quantity must be positive")
    private Integer desiredQuantity;

    /** Target price per unit. */
    @NotNull(message = "Target price is required")
    @Positive(message = "Target price must be positive")
    private BigDecimal targetPrice;

    /** Required delivery deadline. */
    @NotNull(message = "Delivery deadline is required")
    @Future(message = "Delivery deadline must be in the future")
    private LocalDate deliveryDeadline;

    /** Optional notes for the supplier. */
    private String notes;
}
```

5. Create `src/main/java/com/silentsupply/rfq/dto/RfqResponse.java`:

```java
package com.silentsupply.rfq.dto;

import com.silentsupply.rfq.RfqStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO representing an RFQ.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RfqResponse {

    /** RFQ ID. */
    private Long id;

    /** Buyer's company ID. */
    private Long buyerId;

    /** Buyer's company name. */
    private String buyerName;

    /** Product ID. */
    private Long productId;

    /** Product name. */
    private String productName;

    /** Supplier's company ID. */
    private Long supplierId;

    /** Supplier's company name. */
    private String supplierName;

    /** Desired quantity. */
    private int desiredQuantity;

    /** Target price per unit. */
    private BigDecimal targetPrice;

    /** Required delivery deadline. */
    private LocalDate deliveryDeadline;

    /** Optional notes. */
    private String notes;

    /** Current status. */
    private RfqStatus status;

    /** Current negotiation round. */
    private int currentRound;

    /** Maximum negotiation rounds. */
    private int maxRounds;

    /** Expiration timestamp. */
    private LocalDateTime expiresAt;

    /** When the RFQ was created. */
    private LocalDateTime createdAt;
}
```

6. Create `src/main/java/com/silentsupply/rfq/RfqMapper.java`:

```java
package com.silentsupply.rfq;

import com.silentsupply.rfq.dto.RfqResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting {@link Rfq} entities to response DTOs.
 */
@Mapper(componentModel = "spring")
public interface RfqMapper {

    /**
     * Converts an RFQ entity to a response DTO.
     *
     * @param rfq the RFQ entity
     * @return the response DTO
     */
    @Mapping(source = "buyer.id", target = "buyerId")
    @Mapping(source = "buyer.name", target = "buyerName")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "supplier.id", target = "supplierId")
    @Mapping(source = "supplier.name", target = "supplierName")
    RfqResponse toResponse(Rfq rfq);
}
```

7. Create `src/test/java/com/silentsupply/rfq/RfqMapperTest.java`:

```java
package com.silentsupply.rfq;

import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductStatus;
import com.silentsupply.rfq.dto.RfqResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link RfqMapper}.
 */
class RfqMapperTest {

    private final RfqMapper mapper = Mappers.getMapper(RfqMapper.class);

    @Test
    void toResponse_mapsAllFields() {
        Company buyer = Company.builder().name("BuyerCo").email("b@b.com").password("p").role(CompanyRole.BUYER).build();
        buyer.setId(1L);

        Company supplier = Company.builder().name("SupplierCo").email("s@s.com").password("p").role(CompanyRole.SUPPLIER).build();
        supplier.setId(2L);

        Product product = Product.builder()
                .supplier(supplier).name("Widget").category("Cat").sku("W-1")
                .unitOfMeasure("pc").basePrice(new BigDecimal("10.00")).availableQuantity(100)
                .status(ProductStatus.ACTIVE).build();
        product.setId(10L);

        Rfq rfq = Rfq.builder()
                .buyer(buyer).product(product).supplier(supplier)
                .desiredQuantity(50).targetPrice(new BigDecimal("8.00"))
                .deliveryDeadline(LocalDate.of(2026, 4, 1))
                .notes("Urgent").status(RfqStatus.SUBMITTED)
                .currentRound(0).maxRounds(3)
                .expiresAt(LocalDateTime.of(2026, 3, 21, 12, 0))
                .build();
        rfq.setId(100L);

        RfqResponse response = mapper.toResponse(rfq);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getBuyerId()).isEqualTo(1L);
        assertThat(response.getBuyerName()).isEqualTo("BuyerCo");
        assertThat(response.getProductId()).isEqualTo(10L);
        assertThat(response.getProductName()).isEqualTo("Widget");
        assertThat(response.getSupplierId()).isEqualTo(2L);
        assertThat(response.getDesiredQuantity()).isEqualTo(50);
        assertThat(response.getTargetPrice()).isEqualByComparingTo(new BigDecimal("8.00"));
        assertThat(response.getStatus()).isEqualTo(RfqStatus.SUBMITTED);
    }
}
```

8. Run tests:

```bash
./mvnw test -Dtest=RfqMapperTest
```

9. Commit: `feat: add RFQ entity, repository, DTOs, and mapper`

---

### Task 16 — RFQ service + tests

**Files:**
- Create: `src/main/java/com/silentsupply/rfq/RfqService.java`
- Test: `src/test/java/com/silentsupply/rfq/RfqServiceTest.java`

**Steps:**

1. Create `src/main/java/com/silentsupply/rfq/RfqService.java`:

```java
package com.silentsupply.rfq;

import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.common.exception.ResourceNotFoundException;
import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductRepository;
import com.silentsupply.product.ProductStatus;
import com.silentsupply.rfq.dto.RfqRequest;
import com.silentsupply.rfq.dto.RfqResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service layer for RFQ lifecycle management.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RfqService {

    private static final int DEFAULT_EXPIRY_DAYS = 7;

    private final RfqRepository rfqRepository;
    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final RfqMapper rfqMapper;

    /**
     * Submits a new RFQ for a product. Only buyers can submit RFQs.
     *
     * @param buyerId the buyer's company ID
     * @param request the RFQ details
     * @return the created RFQ
     * @throws BusinessRuleException if the product is not active
     */
    @Transactional
    public RfqResponse submit(Long buyerId, RfqRequest request) {
        Company buyer = companyRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", buyerId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BusinessRuleException("Cannot submit RFQ for inactive product");
        }

        Rfq rfq = Rfq.builder()
                .buyer(buyer)
                .product(product)
                .supplier(product.getSupplier())
                .desiredQuantity(request.getDesiredQuantity())
                .targetPrice(request.getTargetPrice())
                .deliveryDeadline(request.getDeliveryDeadline())
                .notes(request.getNotes())
                .status(RfqStatus.SUBMITTED)
                .currentRound(0)
                .maxRounds(3)
                .expiresAt(LocalDateTime.now().plusDays(DEFAULT_EXPIRY_DAYS))
                .build();

        Rfq saved = rfqRepository.save(rfq);
        return rfqMapper.toResponse(saved);
    }

    /**
     * Retrieves an RFQ by its ID.
     *
     * @param id the RFQ ID
     * @return the RFQ details
     */
    public RfqResponse getById(Long id) {
        Rfq rfq = findRfqOrThrow(id);
        return rfqMapper.toResponse(rfq);
    }

    /**
     * Lists all RFQs for a specific buyer.
     *
     * @param buyerId the buyer's company ID
     * @return list of RFQs
     */
    public List<RfqResponse> listByBuyer(Long buyerId) {
        return rfqRepository.findByBuyerId(buyerId).stream()
                .map(rfqMapper::toResponse)
                .toList();
    }

    /**
     * Lists all RFQs for a specific supplier.
     *
     * @param supplierId the supplier's company ID
     * @return list of RFQs
     */
    public List<RfqResponse> listBySupplier(Long supplierId) {
        return rfqRepository.findBySupplierId(supplierId).stream()
                .map(rfqMapper::toResponse)
                .toList();
    }

    /**
     * Expires all RFQs that have passed their expiration date and are still in an active status.
     *
     * @return the number of RFQs expired
     */
    @Transactional
    public int expireOverdueRfqs() {
        List<RfqStatus> activeStatuses = List.of(
                RfqStatus.SUBMITTED, RfqStatus.UNDER_REVIEW, RfqStatus.COUNTERED);
        List<Rfq> expired = rfqRepository.findByExpiresAtBeforeAndStatusIn(
                LocalDateTime.now(), activeStatuses);

        expired.forEach(rfq -> rfq.setStatus(RfqStatus.EXPIRED));
        rfqRepository.saveAll(expired);
        return expired.size();
    }

    /**
     * Finds an RFQ by ID or throws ResourceNotFoundException.
     *
     * @param id the RFQ ID
     * @return the RFQ entity
     */
    Rfq findRfqOrThrow(Long id) {
        return rfqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "id", id));
    }
}
```

2. Create `src/test/java/com/silentsupply/rfq/RfqServiceTest.java`:

```java
package com.silentsupply.rfq;

import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.common.exception.ResourceNotFoundException;
import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductRepository;
import com.silentsupply.product.ProductStatus;
import com.silentsupply.rfq.dto.RfqRequest;
import com.silentsupply.rfq.dto.RfqResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RfqService}.
 */
@ExtendWith(MockitoExtension.class)
class RfqServiceTest {

    @Mock
    private RfqRepository rfqRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private RfqMapper rfqMapper;

    @InjectMocks
    private RfqService rfqService;

    private Company buyer;
    private Company supplier;
    private Product product;

    @BeforeEach
    void setUp() {
        supplier = Company.builder().name("SupplierCo").email("s@s.com").password("p").role(CompanyRole.SUPPLIER).build();
        supplier.setId(1L);

        buyer = Company.builder().name("BuyerCo").email("b@b.com").password("p").role(CompanyRole.BUYER).build();
        buyer.setId(2L);

        product = Product.builder()
                .supplier(supplier).name("Widget").category("Cat").sku("W-1")
                .unitOfMeasure("pc").basePrice(new BigDecimal("10.00")).availableQuantity(100)
                .status(ProductStatus.ACTIVE).build();
        product.setId(10L);
    }

    @Test
    void submit_withValidRequest_createsRfq() {
        RfqRequest request = RfqRequest.builder()
                .productId(10L).desiredQuantity(50).targetPrice(new BigDecimal("8.00"))
                .deliveryDeadline(LocalDate.of(2026, 4, 1)).notes("Urgent").build();

        RfqResponse expectedResponse = RfqResponse.builder()
                .id(100L).buyerId(2L).productId(10L).supplierId(1L)
                .desiredQuantity(50).targetPrice(new BigDecimal("8.00"))
                .status(RfqStatus.SUBMITTED).currentRound(0).maxRounds(3).build();

        when(companyRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(rfqRepository.save(any(Rfq.class))).thenAnswer(inv -> {
            Rfq rfq = inv.getArgument(0);
            rfq.setId(100L);
            return rfq;
        });
        when(rfqMapper.toResponse(any(Rfq.class))).thenReturn(expectedResponse);

        RfqResponse result = rfqService.submit(2L, request);

        assertThat(result.getDesiredQuantity()).isEqualTo(50);
        assertThat(result.getStatus()).isEqualTo(RfqStatus.SUBMITTED);
        verify(rfqRepository).save(any(Rfq.class));
    }

    @Test
    void submit_withInactiveProduct_throwsBusinessRuleException() {
        product.setStatus(ProductStatus.DISCONTINUED);
        RfqRequest request = RfqRequest.builder()
                .productId(10L).desiredQuantity(50).targetPrice(new BigDecimal("8.00"))
                .deliveryDeadline(LocalDate.of(2026, 4, 1)).build();

        when(companyRepository.findById(2L)).thenReturn(Optional.of(buyer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> rfqService.submit(2L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("inactive product");
    }

    @Test
    void getById_withExistingId_returnsResponse() {
        Rfq rfq = Rfq.builder()
                .buyer(buyer).product(product).supplier(supplier)
                .desiredQuantity(50).targetPrice(new BigDecimal("8.00"))
                .deliveryDeadline(LocalDate.of(2026, 4, 1))
                .status(RfqStatus.SUBMITTED).currentRound(0).maxRounds(3)
                .expiresAt(LocalDateTime.now().plusDays(7)).build();
        rfq.setId(100L);

        RfqResponse expectedResponse = RfqResponse.builder().id(100L).status(RfqStatus.SUBMITTED).build();

        when(rfqRepository.findById(100L)).thenReturn(Optional.of(rfq));
        when(rfqMapper.toResponse(rfq)).thenReturn(expectedResponse);

        RfqResponse result = rfqService.getById(100L);

        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    void getById_withNonExistingId_throwsResourceNotFoundException() {
        when(rfqRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rfqService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void expireOverdueRfqs_expiresActiveRfqsPastDeadline() {
        Rfq expiredRfq = Rfq.builder()
                .buyer(buyer).product(product).supplier(supplier)
                .desiredQuantity(50).targetPrice(new BigDecimal("8.00"))
                .deliveryDeadline(LocalDate.of(2026, 3, 1))
                .status(RfqStatus.SUBMITTED).currentRound(0).maxRounds(3)
                .expiresAt(LocalDateTime.now().minusDays(1)).build();
        expiredRfq.setId(100L);

        when(rfqRepository.findByExpiresAtBeforeAndStatusIn(any(), any()))
                .thenReturn(List.of(expiredRfq));

        int count = rfqService.expireOverdueRfqs();

        assertThat(count).isEqualTo(1);
        assertThat(expiredRfq.getStatus()).isEqualTo(RfqStatus.EXPIRED);
    }
}
```

3. Run tests:

```bash
./mvnw test -Dtest=RfqServiceTest
```

4. Commit: `feat: add RFQ service with submission, listing, and expiration`

---

### Task 17 — RFQ controller + integration tests

**Files:**
- Create: `src/main/java/com/silentsupply/rfq/RfqController.java`
- Test: `src/test/java/com/silentsupply/rfq/RfqControllerIntegrationTest.java`

**Steps:**

1. Create `src/main/java/com/silentsupply/rfq/RfqController.java`:

```java
package com.silentsupply.rfq;

import com.silentsupply.config.CompanyUserDetails;
import com.silentsupply.rfq.dto.RfqRequest;
import com.silentsupply.rfq.dto.RfqResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for RFQ operations.
 * Buyers submit RFQs; both buyers and suppliers can view them.
 */
@RestController
@RequestMapping("/api/rfqs")
@RequiredArgsConstructor
@Tag(name = "RFQs", description = "Request for Quote submission and management")
public class RfqController {

    private final RfqService rfqService;

    /**
     * Submits a new RFQ. Buyer-only.
     *
     * @param userDetails the authenticated buyer
     * @param request     the RFQ details
     * @return the created RFQ with HTTP 201
     */
    @PostMapping
    @Operation(summary = "Submit a new RFQ (buyer only)")
    public ResponseEntity<RfqResponse> submit(
            @AuthenticationPrincipal CompanyUserDetails userDetails,
            @Valid @RequestBody RfqRequest request) {
        RfqResponse response = rfqService.submit(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves an RFQ by its ID.
     *
     * @param id the RFQ ID
     * @return the RFQ details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get RFQ by ID")
    public ResponseEntity<RfqResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(rfqService.getById(id));
    }

    /**
     * Lists RFQs for the authenticated user (buyer or supplier).
     *
     * @param userDetails the authenticated user
     * @return list of RFQs
     */
    @GetMapping
    @Operation(summary = "List RFQs for authenticated user")
    public ResponseEntity<List<RfqResponse>> listRfqs(
            @AuthenticationPrincipal CompanyUserDetails userDetails) {
        List<RfqResponse> rfqs;
        if ("SUPPLIER".equals(userDetails.getRole())) {
            rfqs = rfqService.listBySupplier(userDetails.getId());
        } else {
            rfqs = rfqService.listByBuyer(userDetails.getId());
        }
        return ResponseEntity.ok(rfqs);
    }
}
```

2. Create `src/test/java/com/silentsupply/rfq/RfqControllerIntegrationTest.java`:

```java
package com.silentsupply.rfq;

import com.silentsupply.common.BaseIntegrationTest;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.config.dto.AuthResponse;
import com.silentsupply.product.ProductRepository;
import com.silentsupply.product.dto.ProductRequest;
import com.silentsupply.product.dto.ProductResponse;
import com.silentsupply.rfq.dto.RfqRequest;
import com.silentsupply.rfq.dto.RfqResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link RfqController}.
 */
class RfqControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RfqRepository rfqRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private String supplierToken;
    private String buyerToken;
    private Long productId;

    @BeforeEach
    void setUp() {
        rfqRepository.deleteAll();
        productRepository.deleteAll();
        companyRepository.deleteAll();

        supplierToken = registerAndGetToken("SupplierCo", "supplier@example.com", CompanyRole.SUPPLIER);
        buyerToken = registerAndGetToken("BuyerCo", "buyer@example.com", CompanyRole.BUYER);

        ProductRequest productRequest = ProductRequest.builder()
                .name("Widget").description("Test").category("Electronics").sku("W-1")
                .unitOfMeasure("piece").basePrice(new BigDecimal("10.00")).availableQuantity(100)
                .build();
        ResponseEntity<ProductResponse> productResponse = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                new HttpEntity<>(productRequest, authHeaders(supplierToken)),
                ProductResponse.class);
        productId = productResponse.getBody().getId();
    }

    @Test
    void submit_asBuyer_returns201() {
        RfqRequest request = RfqRequest.builder()
                .productId(productId).desiredQuantity(50).targetPrice(new BigDecimal("8.00"))
                .deliveryDeadline(LocalDate.now().plusDays(30)).notes("Urgent order").build();

        ResponseEntity<RfqResponse> response = restTemplate.exchange(
                "/api/rfqs", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(buyerToken)),
                RfqResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDesiredQuantity()).isEqualTo(50);
        assertThat(response.getBody().getStatus()).isEqualTo(RfqStatus.SUBMITTED);
    }

    @Test
    void submit_asSupplier_returns403() {
        RfqRequest request = RfqRequest.builder()
                .productId(productId).desiredQuantity(50).targetPrice(new BigDecimal("8.00"))
                .deliveryDeadline(LocalDate.now().plusDays(30)).build();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/rfqs", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(supplierToken)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getById_returnsRfq() {
        RfqRequest request = RfqRequest.builder()
                .productId(productId).desiredQuantity(50).targetPrice(new BigDecimal("8.00"))
                .deliveryDeadline(LocalDate.now().plusDays(30)).build();
        ResponseEntity<RfqResponse> created = restTemplate.exchange(
                "/api/rfqs", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(buyerToken)),
                RfqResponse.class);
        Long rfqId = created.getBody().getId();

        ResponseEntity<RfqResponse> response = restTemplate.exchange(
                "/api/rfqs/" + rfqId, HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                RfqResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(rfqId);
    }

    @Test
    void listRfqs_asBuyer_returnsBuyerRfqs() {
        RfqRequest request = RfqRequest.builder()
                .productId(productId).desiredQuantity(50).targetPrice(new BigDecimal("8.00"))
                .deliveryDeadline(LocalDate.now().plusDays(30)).build();
        restTemplate.exchange("/api/rfqs", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(buyerToken)), RfqResponse.class);

        ResponseEntity<RfqResponse[]> response = restTemplate.exchange(
                "/api/rfqs", HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                RfqResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    private String registerAndGetToken(String name, String email, CompanyRole role) {
        CompanyRequest request = CompanyRequest.builder()
                .name(name).email(email).password("password123").role(role).build();
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/register", request, AuthResponse.class);
        return response.getBody().getToken();
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
```

3. Run tests:

```bash
./mvnw test -Dtest=RfqControllerIntegrationTest
```

4. Commit: `feat: add RFQ controller with submission and listing endpoints`

---

### Task 18 — Proposal entity, repository, DTO, mapper

**Files:**
- Create: `src/main/java/com/silentsupply/proposal/ProposalStatus.java`
- Create: `src/main/java/com/silentsupply/proposal/ProposerType.java`
- Create: `src/main/java/com/silentsupply/proposal/Proposal.java`
- Create: `src/main/java/com/silentsupply/proposal/ProposalRepository.java`
- Create: `src/main/java/com/silentsupply/proposal/dto/ProposalRequest.java`
- Create: `src/main/java/com/silentsupply/proposal/dto/ProposalResponse.java`
- Create: `src/main/java/com/silentsupply/proposal/ProposalMapper.java`
- Test: `src/test/java/com/silentsupply/proposal/ProposalMapperTest.java`

**Steps:**

1. Create `src/main/java/com/silentsupply/proposal/ProposalStatus.java`:

```java
package com.silentsupply.proposal;

/**
 * Status of a proposal within an RFQ negotiation.
 */
public enum ProposalStatus {

    /** Proposal is awaiting evaluation. */
    PENDING,

    /** Proposal has been accepted. */
    ACCEPTED,

    /** Proposal has been rejected. */
    REJECTED,

    /** Proposal was countered with a new offer. */
    COUNTERED,

    /** Proposal expired without response. */
    EXPIRED
}
```

2. Create `src/main/java/com/silentsupply/proposal/ProposerType.java`:

```java
package com.silentsupply.proposal;

/**
 * Identifies who created a proposal within the RFQ negotiation.
 */
public enum ProposerType {

    /** Proposal submitted by the buyer. */
    BUYER,

    /** Counter-proposal generated by the negotiation engine. */
    SYSTEM
}
```

3. Create `src/main/java/com/silentsupply/proposal/Proposal.java`:

```java
package com.silentsupply.proposal;

import com.silentsupply.common.entity.BaseEntity;
import com.silentsupply.rfq.Rfq;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents an individual proposal (offer) within an RFQ negotiation.
 * Can be submitted by the buyer or auto-generated by the negotiation engine.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "proposals")
public class Proposal extends BaseEntity {

    /** The RFQ this proposal belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rfq_id", nullable = false)
    private Rfq rfq;

    /** Who created this proposal. */
    @Enumerated(EnumType.STRING)
    @Column(name = "proposer_type", nullable = false)
    private ProposerType proposerType;

    /** Proposed price per unit. */
    @Column(name = "proposed_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal proposedPrice;

    /** Proposed quantity. */
    @Column(name = "proposed_qty", nullable = false)
    private int proposedQty;

    /** Proposed delivery time in days. */
    @Column(name = "delivery_days", nullable = false)
    private int deliveryDays;

    /** Current proposal status. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProposalStatus status = ProposalStatus.PENDING;

    /** Which negotiation round this proposal belongs to. */
    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    /** Reason code for rejection or counter (e.g., "PRICE_BELOW_FLOOR"). */
    @Column(name = "reason_code")
    private String reasonCode;
}
```

4. Create `src/main/java/com/silentsupply/proposal/ProposalRepository.java`:

```java
package com.silentsupply.proposal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for {@link Proposal} entities.
 */
@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    /**
     * Finds all proposals for a given RFQ, ordered by round number.
     *
     * @param rfqId the RFQ ID
     * @return list of proposals ordered by round
     */
    List<Proposal> findByRfqIdOrderByRoundNumberAsc(Long rfqId);
}
```

5. Create `src/main/java/com/silentsupply/proposal/dto/ProposalRequest.java`:

```java
package com.silentsupply.proposal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new proposal within an RFQ.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalRequest {

    /** Proposed price per unit. */
    @NotNull(message = "Proposed price is required")
    @Positive(message = "Proposed price must be positive")
    private BigDecimal proposedPrice;

    /** Proposed quantity. */
    @NotNull(message = "Proposed quantity is required")
    @Positive(message = "Proposed quantity must be positive")
    private Integer proposedQty;

    /** Proposed delivery time in days. */
    @NotNull(message = "Delivery days is required")
    @Positive(message = "Delivery days must be positive")
    private Integer deliveryDays;
}
```

6. Create `src/main/java/com/silentsupply/proposal/dto/ProposalResponse.java`:

```java
package com.silentsupply.proposal.dto;

import com.silentsupply.proposal.ProposalStatus;
import com.silentsupply.proposal.ProposerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO representing a proposal.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposalResponse {

    /** Proposal ID. */
    private Long id;

    /** RFQ ID this proposal belongs to. */
    private Long rfqId;

    /** Who created this proposal. */
    private ProposerType proposerType;

    /** Proposed price per unit. */
    private BigDecimal proposedPrice;

    /** Proposed quantity. */
    private int proposedQty;

    /** Proposed delivery time in days. */
    private int deliveryDays;

    /** Current status. */
    private ProposalStatus status;

    /** Negotiation round number. */
    private int roundNumber;

    /** Reason code for rejection or counter. */
    private String reasonCode;

    /** When the proposal was created. */
    private LocalDateTime createdAt;
}
```

7. Create `src/main/java/com/silentsupply/proposal/ProposalMapper.java`:

```java
package com.silentsupply.proposal;

import com.silentsupply.proposal.dto.ProposalResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting {@link Proposal} entities to response DTOs.
 */
@Mapper(componentModel = "spring")
public interface ProposalMapper {

    /**
     * Converts a Proposal entity to a response DTO.
     *
     * @param proposal the proposal entity
     * @return the response DTO
     */
    @Mapping(source = "rfq.id", target = "rfqId")
    ProposalResponse toResponse(Proposal proposal);
}
```

8. Create `src/test/java/com/silentsupply/proposal/ProposalMapperTest.java`:

```java
package com.silentsupply.proposal;

import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductStatus;
import com.silentsupply.proposal.dto.ProposalResponse;
import com.silentsupply.rfq.Rfq;
import com.silentsupply.rfq.RfqStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ProposalMapper}.
 */
class ProposalMapperTest {

    private final ProposalMapper mapper = Mappers.getMapper(ProposalMapper.class);

    @Test
    void toResponse_mapsAllFields() {
        Company buyer = Company.builder().name("BuyerCo").email("b@b.com").password("p").role(CompanyRole.BUYER).build();
        buyer.setId(1L);
        Company supplier = Company.builder().name("SupplierCo").email("s@s.com").password("p").role(CompanyRole.SUPPLIER).build();
        supplier.setId(2L);
        Product product = Product.builder()
                .supplier(supplier).name("Widget").category("Cat").sku("W-1")
                .unitOfMeasure("pc").basePrice(new BigDecimal("10.00")).availableQuantity(100)
                .status(ProductStatus.ACTIVE).build();
        product.setId(10L);

        Rfq rfq = Rfq.builder()
                .buyer(buyer).product(product).supplier(supplier)
                .desiredQuantity(50).targetPrice(new BigDecimal("8.00"))
                .deliveryDeadline(LocalDate.of(2026, 4, 1))
                .status(RfqStatus.SUBMITTED).currentRound(1).maxRounds(3)
                .expiresAt(LocalDateTime.now().plusDays(7)).build();
        rfq.setId(100L);

        Proposal proposal = Proposal.builder()
                .rfq(rfq).proposerType(ProposerType.BUYER)
                .proposedPrice(new BigDecimal("8.50")).proposedQty(50).deliveryDays(14)
                .status(ProposalStatus.PENDING).roundNumber(1).build();
        proposal.setId(200L);

        ProposalResponse response = mapper.toResponse(proposal);

        assertThat(response.getId()).isEqualTo(200L);
        assertThat(response.getRfqId()).isEqualTo(100L);
        assertThat(response.getProposerType()).isEqualTo(ProposerType.BUYER);
        assertThat(response.getProposedPrice()).isEqualByComparingTo(new BigDecimal("8.50"));
        assertThat(response.getProposedQty()).isEqualTo(50);
        assertThat(response.getDeliveryDays()).isEqualTo(14);
        assertThat(response.getStatus()).isEqualTo(ProposalStatus.PENDING);
        assertThat(response.getRoundNumber()).isEqualTo(1);
    }
}
```

9. Run tests:

```bash
./mvnw test -Dtest=ProposalMapperTest
```

10. Commit: `feat: add proposal entity, repository, DTOs, and mapper`

---

### Task 19 — Proposal service + tests

**Files:**
- Create: `src/main/java/com/silentsupply/proposal/ProposalService.java`
- Test: `src/test/java/com/silentsupply/proposal/ProposalServiceTest.java`

**Steps:**

1. Create `src/main/java/com/silentsupply/proposal/ProposalService.java`:

```java
package com.silentsupply.proposal;

import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.proposal.dto.ProposalRequest;
import com.silentsupply.proposal.dto.ProposalResponse;
import com.silentsupply.rfq.Rfq;
import com.silentsupply.rfq.RfqRepository;
import com.silentsupply.rfq.RfqService;
import com.silentsupply.rfq.RfqStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Service layer for proposal creation and retrieval within RFQ negotiations.
 * The negotiation engine is wired in during Task 25.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProposalService {

    /** RFQ statuses that allow new proposals. */
    private static final Set<RfqStatus> PROPOSABLE_STATUSES = Set.of(
            RfqStatus.SUBMITTED, RfqStatus.UNDER_REVIEW, RfqStatus.COUNTERED);

    private final ProposalRepository proposalRepository;
    private final RfqRepository rfqRepository;
    private final RfqService rfqService;
    private final ProposalMapper proposalMapper;

    /**
     * Creates a buyer proposal for an RFQ. Increments the RFQ round counter
     * and transitions status to UNDER_REVIEW.
     *
     * @param rfqId   the RFQ ID
     * @param buyerId the buyer's company ID
     * @param request the proposal details
     * @return the created proposal
     * @throws BusinessRuleException if the RFQ is not in a proposable status or max rounds exceeded
     */
    @Transactional
    public ProposalResponse createBuyerProposal(Long rfqId, Long buyerId, ProposalRequest request) {
        Rfq rfq = rfqService.findRfqOrThrow(rfqId);

        if (!rfq.getBuyer().getId().equals(buyerId)) {
            throw new BusinessRuleException("Only the RFQ owner can submit proposals");
        }

        if (!PROPOSABLE_STATUSES.contains(rfq.getStatus())) {
            throw new BusinessRuleException("RFQ is not in a status that accepts proposals: " + rfq.getStatus());
        }

        if (rfq.getCurrentRound() >= rfq.getMaxRounds()) {
            throw new BusinessRuleException("Maximum negotiation rounds reached: " + rfq.getMaxRounds());
        }

        int nextRound = rfq.getCurrentRound() + 1;
        rfq.setCurrentRound(nextRound);
        rfq.setStatus(RfqStatus.UNDER_REVIEW);
        rfqRepository.save(rfq);

        Proposal proposal = Proposal.builder()
                .rfq(rfq)
                .proposerType(ProposerType.BUYER)
                .proposedPrice(request.getProposedPrice())
                .proposedQty(request.getProposedQty())
                .deliveryDays(request.getDeliveryDays())
                .status(ProposalStatus.PENDING)
                .roundNumber(nextRound)
                .build();

        Proposal saved = proposalRepository.save(proposal);
        return proposalMapper.toResponse(saved);
    }

    /**
     * Lists all proposals for a given RFQ, ordered by round number.
     *
     * @param rfqId the RFQ ID
     * @return list of proposals
     */
    public List<ProposalResponse> listByRfq(Long rfqId) {
        return proposalRepository.findByRfqIdOrderByRoundNumberAsc(rfqId).stream()
                .map(proposalMapper::toResponse)
                .toList();
    }
}
```

2. Create `src/test/java/com/silentsupply/proposal/ProposalServiceTest.java`:

```java
package com.silentsupply.proposal;

import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductStatus;
import com.silentsupply.proposal.dto.ProposalRequest;
import com.silentsupply.proposal.dto.ProposalResponse;
import com.silentsupply.rfq.Rfq;
import com.silentsupply.rfq.RfqRepository;
import com.silentsupply.rfq.RfqService;
import com.silentsupply.rfq.RfqStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ProposalService}.
 */
@ExtendWith(MockitoExtension.class)
class ProposalServiceTest {

    @Mock
    private ProposalRepository proposalRepository;
    @Mock
    private RfqRepository rfqRepository;
    @Mock
    private RfqService rfqService;
    @Mock
    private ProposalMapper proposalMapper;

    @InjectMocks
    private ProposalService proposalService;

    private Company buyer;
    private Company supplier;
    private Product product;
    private Rfq rfq;

    @BeforeEach
    void setUp() {
        supplier = Company.builder().name("SupplierCo").email("s@s.com").password("p").role(CompanyRole.SUPPLIER).build();
        supplier.setId(1L);
        buyer = Company.builder().name("BuyerCo").email("b@b.com").password("p").role(CompanyRole.BUYER).build();
        buyer.setId(2L);
        product = Product.builder()
                .supplier(supplier).name("Widget").category("Cat").sku("W-1")
                .unitOfMeasure("pc").basePrice(new BigDecimal("10.00")).availableQuantity(100)
                .status(ProductStatus.ACTIVE).build();
        product.setId(10L);

        rfq = Rfq.builder()
                .buyer(buyer).product(product).supplier(supplier)
                .desiredQuantity(50).targetPrice(new BigDecimal("8.00"))
                .deliveryDeadline(LocalDate.of(2026, 4, 1))
                .status(RfqStatus.SUBMITTED).currentRound(0).maxRounds(3)
                .expiresAt(LocalDateTime.now().plusDays(7)).build();
        rfq.setId(100L);
    }

    @Test
    void createBuyerProposal_withValidRequest_createsProposal() {
        ProposalRequest request = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("8.50")).proposedQty(50).deliveryDays(14).build();

        ProposalResponse expectedResponse = ProposalResponse.builder()
                .id(200L).rfqId(100L).proposerType(ProposerType.BUYER)
                .proposedPrice(new BigDecimal("8.50")).proposedQty(50).deliveryDays(14)
                .status(ProposalStatus.PENDING).roundNumber(1).build();

        when(rfqService.findRfqOrThrow(100L)).thenReturn(rfq);
        when(proposalRepository.save(any(Proposal.class))).thenAnswer(inv -> {
            Proposal p = inv.getArgument(0);
            p.setId(200L);
            return p;
        });
        when(proposalMapper.toResponse(any(Proposal.class))).thenReturn(expectedResponse);

        ProposalResponse result = proposalService.createBuyerProposal(100L, 2L, request);

        assertThat(result.getRoundNumber()).isEqualTo(1);
        assertThat(result.getProposerType()).isEqualTo(ProposerType.BUYER);
        assertThat(rfq.getCurrentRound()).isEqualTo(1);
        assertThat(rfq.getStatus()).isEqualTo(RfqStatus.UNDER_REVIEW);
        verify(rfqRepository).save(rfq);
    }

    @Test
    void createBuyerProposal_byNonOwner_throwsBusinessRuleException() {
        ProposalRequest request = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("8.50")).proposedQty(50).deliveryDays(14).build();

        when(rfqService.findRfqOrThrow(100L)).thenReturn(rfq);

        assertThatThrownBy(() -> proposalService.createBuyerProposal(100L, 999L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Only the RFQ owner");
    }

    @Test
    void createBuyerProposal_onAcceptedRfq_throwsBusinessRuleException() {
        rfq.setStatus(RfqStatus.ACCEPTED);
        ProposalRequest request = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("8.50")).proposedQty(50).deliveryDays(14).build();

        when(rfqService.findRfqOrThrow(100L)).thenReturn(rfq);

        assertThatThrownBy(() -> proposalService.createBuyerProposal(100L, 2L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not in a status");
    }

    @Test
    void createBuyerProposal_atMaxRounds_throwsBusinessRuleException() {
        rfq.setCurrentRound(3);
        rfq.setMaxRounds(3);
        ProposalRequest request = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("8.50")).proposedQty(50).deliveryDays(14).build();

        when(rfqService.findRfqOrThrow(100L)).thenReturn(rfq);

        assertThatThrownBy(() -> proposalService.createBuyerProposal(100L, 2L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Maximum negotiation rounds");
    }
}
```

3. Run tests:

```bash
./mvnw test -Dtest=ProposalServiceTest
```

4. Commit: `feat: add proposal service with buyer proposal creation and RFQ round tracking`

---

### Task 20 — Proposal controller + integration tests

**Files:**
- Create: `src/main/java/com/silentsupply/proposal/ProposalController.java`
- Test: `src/test/java/com/silentsupply/proposal/ProposalControllerIntegrationTest.java`

**Steps:**

1. Create `src/main/java/com/silentsupply/proposal/ProposalController.java`:

```java
package com.silentsupply.proposal;

import com.silentsupply.config.CompanyUserDetails;
import com.silentsupply.proposal.dto.ProposalRequest;
import com.silentsupply.proposal.dto.ProposalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for proposal operations within RFQ negotiations.
 */
@RestController
@RequestMapping("/api/rfqs/{rfqId}/proposals")
@RequiredArgsConstructor
@Tag(name = "Proposals", description = "Proposal creation and listing within RFQs")
public class ProposalController {

    private final ProposalService proposalService;

    /**
     * Creates a new buyer proposal for an RFQ. Triggers the negotiation engine.
     *
     * @param rfqId       the RFQ ID
     * @param userDetails the authenticated buyer
     * @param request     the proposal details
     * @return the created proposal with HTTP 201
     */
    @PostMapping
    @Operation(summary = "Create a buyer proposal for an RFQ (buyer only)")
    public ResponseEntity<ProposalResponse> createProposal(
            @PathVariable Long rfqId,
            @AuthenticationPrincipal CompanyUserDetails userDetails,
            @Valid @RequestBody ProposalRequest request) {
        ProposalResponse response = proposalService.createBuyerProposal(
                rfqId, userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists all proposals for an RFQ, ordered by round number.
     *
     * @param rfqId the RFQ ID
     * @return list of proposals
     */
    @GetMapping
    @Operation(summary = "List all proposals for an RFQ")
    public ResponseEntity<List<ProposalResponse>> listProposals(@PathVariable Long rfqId) {
        return ResponseEntity.ok(proposalService.listByRfq(rfqId));
    }
}
```

2. Create `src/test/java/com/silentsupply/proposal/ProposalControllerIntegrationTest.java`:

```java
package com.silentsupply.proposal;

import com.silentsupply.common.BaseIntegrationTest;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.config.dto.AuthResponse;
import com.silentsupply.product.ProductRepository;
import com.silentsupply.product.dto.ProductRequest;
import com.silentsupply.product.dto.ProductResponse;
import com.silentsupply.proposal.dto.ProposalRequest;
import com.silentsupply.proposal.dto.ProposalResponse;
import com.silentsupply.rfq.RfqRepository;
import com.silentsupply.rfq.dto.RfqRequest;
import com.silentsupply.rfq.dto.RfqResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ProposalController}.
 */
class ProposalControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProposalRepository proposalRepository;
    @Autowired
    private RfqRepository rfqRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CompanyRepository companyRepository;

    private String supplierToken;
    private String buyerToken;
    private Long rfqId;

    @BeforeEach
    void setUp() {
        proposalRepository.deleteAll();
        rfqRepository.deleteAll();
        productRepository.deleteAll();
        companyRepository.deleteAll();

        supplierToken = registerAndGetToken("SupplierCo", "supplier@example.com", CompanyRole.SUPPLIER);
        buyerToken = registerAndGetToken("BuyerCo", "buyer@example.com", CompanyRole.BUYER);

        ProductRequest productRequest = ProductRequest.builder()
                .name("Widget").description("Test").category("Electronics").sku("W-1")
                .unitOfMeasure("piece").basePrice(new BigDecimal("10.00")).availableQuantity(100)
                .build();
        ResponseEntity<ProductResponse> productResponse = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                new HttpEntity<>(productRequest, authHeaders(supplierToken)),
                ProductResponse.class);
        Long productId = productResponse.getBody().getId();

        RfqRequest rfqRequest = RfqRequest.builder()
                .productId(productId).desiredQuantity(50).targetPrice(new BigDecimal("8.00"))
                .deliveryDeadline(LocalDate.now().plusDays(30)).build();
        ResponseEntity<RfqResponse> rfqResponse = restTemplate.exchange(
                "/api/rfqs", HttpMethod.POST,
                new HttpEntity<>(rfqRequest, authHeaders(buyerToken)),
                RfqResponse.class);
        rfqId = rfqResponse.getBody().getId();
    }

    @Test
    void createProposal_asBuyer_returns201() {
        ProposalRequest request = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("8.50")).proposedQty(50).deliveryDays(14).build();

        ResponseEntity<ProposalResponse> response = restTemplate.exchange(
                "/api/rfqs/" + rfqId + "/proposals", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(buyerToken)),
                ProposalResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProposerType()).isEqualTo(ProposerType.BUYER);
        assertThat(response.getBody().getRoundNumber()).isEqualTo(1);
    }

    @Test
    void listProposals_afterCreation_returnsProposals() {
        ProposalRequest request = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("8.50")).proposedQty(50).deliveryDays(14).build();
        restTemplate.exchange("/api/rfqs/" + rfqId + "/proposals", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(buyerToken)), ProposalResponse.class);

        ResponseEntity<ProposalResponse[]> response = restTemplate.exchange(
                "/api/rfqs/" + rfqId + "/proposals", HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                ProposalResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    private String registerAndGetToken(String name, String email, CompanyRole role) {
        CompanyRequest request = CompanyRequest.builder()
                .name(name).email(email).password("password123").role(role).build();
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/auth/register", request, AuthResponse.class);
        return response.getBody().getToken();
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
```

3. Run tests:

```bash
./mvnw test -Dtest=ProposalControllerIntegrationTest
```

4. Commit: `feat: add proposal controller with creation and listing endpoints`

---

## Phase 6: Negotiation Engine

### Task 21 — NegotiationRule entity, repository, DTO, mapper

**Files:**
- Create: `src/main/java/com/silentsupply/negotiation/NegotiationRule.java`
- Create: `src/main/java/com/silentsupply/negotiation/NegotiationRuleRepository.java`
- Create: `src/main/java/com/silentsupply/negotiation/dto/NegotiationRuleRequest.java`
- Create: `src/main/java/com/silentsupply/negotiation/dto/NegotiationRuleResponse.java`
- Create: `src/main/java/com/silentsupply/negotiation/NegotiationRuleMapper.java`
- Test: `src/test/java/com/silentsupply/negotiation/NegotiationRuleMapperTest.java`

**Steps:**

1. Create `src/main/java/com/silentsupply/negotiation/NegotiationRule.java`:

```java
package com.silentsupply.negotiation;

import com.silentsupply.common.entity.BaseEntity;
import com.silentsupply.company.Company;
import com.silentsupply.product.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Supplier-defined negotiation rules for a specific product.
 * The negotiation engine uses these to auto-accept, counter, or reject proposals.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "negotiation_rules")
public class NegotiationRule extends BaseEntity {

    /** The supplier who owns these rules. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Company supplier;

    /** The product these rules apply to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Absolute minimum price the supplier will accept. */
    @Column(name = "price_floor", nullable = false, precision = 15, scale = 2)
    private BigDecimal priceFloor;

    /** Price at or above which the system auto-accepts without negotiation. */
    @Column(name = "auto_accept_threshold", nullable = false, precision = 15, scale = 2)
    private BigDecimal autoAcceptThreshold;

    /** Maximum delivery window in days the supplier can fulfill. */
    @Column(name = "max_delivery_days", nullable = false)
    private int maxDeliveryDays;

    /** Maximum negotiation rounds before auto-expiring the RFQ. */
    @Column(name = "max_rounds", nullable = false)
    @Builder.Default
    private int maxRounds = 3;

    /** Volume discount percentage applied when order exceeds the volume threshold. */
    @Column(name = "volume_discount_pct", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal volumeDiscountPct = BigDecimal.ZERO;

    /** Minimum quantity required to qualify for the volume discount. */
    @Column(name = "volume_threshold", nullable = false)
    @Builder.Default
    private int volumeThreshold = 0;
}
```

2. Create `src/main/java/com/silentsupply/negotiation/NegotiationRuleRepository.java`:

```java
package com.silentsupply.negotiation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for {@link NegotiationRule} entities.
 */
@Repository
public interface NegotiationRuleRepository extends JpaRepository<NegotiationRule, Long> {

    /**
     * Finds all negotiation rules for a given supplier.
     *
     * @param supplierId the supplier's company ID
     * @return list of rules
     */
    List<NegotiationRule> findBySupplierId(Long supplierId);

    /**
     * Finds the negotiation rule for a specific supplier-product pair.
     *
     * @param supplierId the supplier's company ID
     * @param productId  the product ID
     * @return the rule if found
     */
    Optional<NegotiationRule> findBySupplierIdAndProductId(Long supplierId, Long productId);
}
```

3. Create `src/main/java/com/silentsupply/negotiation/dto/NegotiationRuleRequest.java`:

```java
package com.silentsupply.negotiation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating or updating negotiation rules.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationRuleRequest {

    /** Product ID to apply rules to. */
    @NotNull(message = "Product ID is required")
    private Long productId;

    /** Absolute minimum price. */
    @NotNull(message = "Price floor is required")
    @Positive(message = "Price floor must be positive")
    private BigDecimal priceFloor;

    /** Price at or above which auto-accept triggers. */
    @NotNull(message = "Auto-accept threshold is required")
    @Positive(message = "Auto-accept threshold must be positive")
    private BigDecimal autoAcceptThreshold;

    /** Maximum delivery days. */
    @NotNull(message = "Max delivery days is required")
    @Positive(message = "Max delivery days must be positive")
    private Integer maxDeliveryDays;

    /** Maximum negotiation rounds. */
    @NotNull(message = "Max rounds is required")
    @Min(value = 1, message = "Max rounds must be at least 1")
    @Max(value = 10, message = "Max rounds must not exceed 10")
    private Integer maxRounds;

    /** Volume discount percentage (0-100). */
    @NotNull(message = "Volume discount percentage is required")
    @Min(value = 0, message = "Volume discount must be non-negative")
    @Max(value = 100, message = "Volume discount must not exceed 100")
    private BigDecimal volumeDiscountPct;

    /** Minimum quantity for volume discount. */
    @NotNull(message = "Volume threshold is required")
    @PositiveOrZero(message = "Volume threshold must not be negative")
    private Integer volumeThreshold;
}
```

4. Create `src/main/java/com/silentsupply/negotiation/dto/NegotiationRuleResponse.java`:

```java
package com.silentsupply.negotiation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO representing a negotiation rule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationRuleResponse {

    /** Rule ID. */
    private Long id;

    /** Supplier's company ID. */
    private Long supplierId;

    /** Product ID. */
    private Long productId;

    /** Product name. */
    private String productName;

    /** Absolute minimum price. */
    private BigDecimal priceFloor;

    /** Auto-accept price threshold. */
    private BigDecimal autoAcceptThreshold;

    /** Maximum delivery days. */
    private int maxDeliveryDays;

    /** Maximum negotiation rounds. */
    private int maxRounds;

    /** Volume discount percentage. */
    private BigDecimal volumeDiscountPct;

    /** Minimum quantity for volume discount. */
    private int volumeThreshold;

    /** When the rule was created. */
    private LocalDateTime createdAt;
}
```

5. Create `src/main/java/com/silentsupply/negotiation/NegotiationRuleMapper.java`:

```java
package com.silentsupply.negotiation;

import com.silentsupply.negotiation.dto.NegotiationRuleRequest;
import com.silentsupply.negotiation.dto.NegotiationRuleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper for converting between {@link NegotiationRule} entities and DTOs.
 */
@Mapper(componentModel = "spring")
public interface NegotiationRuleMapper {

    /**
     * Converts a NegotiationRule entity to a response DTO.
     *
     * @param rule the negotiation rule entity
     * @return the response DTO
     */
    @Mapping(source = "supplier.id", target = "supplierId")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    NegotiationRuleResponse toResponse(NegotiationRule rule);

    /**
     * Converts a request DTO to a NegotiationRule entity.
     *
     * @param request the rule request
     * @return the entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    NegotiationRule toEntity(NegotiationRuleRequest request);

    /**
     * Updates an existing NegotiationRule entity from a request DTO.
     *
     * @param request the updated values
     * @param rule    the existing entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(NegotiationRuleRequest request, @MappingTarget NegotiationRule rule);
}
```

6. Create `src/test/java/com/silentsupply/negotiation/NegotiationRuleMapperTest.java`:

```java
package com.silentsupply.negotiation;

import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.negotiation.dto.NegotiationRuleResponse;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link NegotiationRuleMapper}.
 */
class NegotiationRuleMapperTest {

    private final NegotiationRuleMapper mapper = Mappers.getMapper(NegotiationRuleMapper.class);

    @Test
    void toResponse_mapsAllFields() {
        Company supplier = Company.builder().name("SupplierCo").email("s@s.com").password("p").role(CompanyRole.SUPPLIER).build();
        supplier.setId(1L);

        Product product = Product.builder()
                .supplier(supplier).name("Widget").category("Cat").sku("W-1")
                .unitOfMeasure("pc").basePrice(new BigDecimal("10.00")).availableQuantity(100)
                .status(ProductStatus.ACTIVE).build();
        product.setId(10L);

        NegotiationRule rule = NegotiationRule.builder()
                .supplier(supplier).product(product)
                .priceFloor(new BigDecimal("7.00")).autoAcceptThreshold(new BigDecimal("9.50"))
                .maxDeliveryDays(30).maxRounds(3)
                .volumeDiscountPct(new BigDecimal("5.00")).volumeThreshold(100).build();
        rule.setId(50L);

        NegotiationRuleResponse response = mapper.toResponse(rule);

        assertThat(response.getId()).isEqualTo(50L);
        assertThat(response.getSupplierId()).isEqualTo(1L);
        assertThat(response.getProductId()).isEqualTo(10L);
        assertThat(response.getProductName()).isEqualTo("Widget");
        assertThat(response.getPriceFloor()).isEqualByComparingTo(new BigDecimal("7.00"));
        assertThat(response.getAutoAcceptThreshold()).isEqualByComparingTo(new BigDecimal("9.50"));
        assertThat(response.getMaxDeliveryDays()).isEqualTo(30);
        assertThat(response.getMaxRounds()).isEqualTo(3);
        assertThat(response.getVolumeDiscountPct()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(response.getVolumeThreshold()).isEqualTo(100);
    }
}
```

7. Run tests:

```bash
./mvnw test -Dtest=NegotiationRuleMapperTest
```

8. Commit: `feat: add negotiation rule entity, repository, DTOs, and mapper`

---

### Task 22 — NegotiationRule service + tests

**Files:**
- Create: `src/main/java/com/silentsupply/negotiation/NegotiationRuleService.java`
- Test: `src/test/java/com/silentsupply/negotiation/NegotiationRuleServiceTest.java`

**Steps:**

1. Create `src/main/java/com/silentsupply/negotiation/NegotiationRuleService.java`:

```java
package com.silentsupply.negotiation;

import com.silentsupply.common.exception.AccessDeniedException;
import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.common.exception.ResourceNotFoundException;
import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.negotiation.dto.NegotiationRuleRequest;
import com.silentsupply.negotiation.dto.NegotiationRuleResponse;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for CRUD operations on negotiation rules.
 * Only the owning supplier can manage rules for their products.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NegotiationRuleService {

    private final NegotiationRuleRepository ruleRepository;
    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;
    private final NegotiationRuleMapper ruleMapper;

    /**
     * Creates a new negotiation rule for a product.
     *
     * @param supplierId the supplier's company ID
     * @param request    the rule details
     * @return the created rule
     * @throws BusinessRuleException if a rule already exists for this supplier-product pair
     * @throws AccessDeniedException if the supplier does not own the product
     */
    @Transactional
    public NegotiationRuleResponse create(Long supplierId, NegotiationRuleRequest request) {
        Company supplier = companyRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", supplierId));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (!product.getSupplier().getId().equals(supplierId)) {
            throw new AccessDeniedException("You can only set rules for your own products");
        }

        if (ruleRepository.findBySupplierIdAndProductId(supplierId, request.getProductId()).isPresent()) {
            throw new BusinessRuleException("Negotiation rule already exists for this product");
        }

        if (request.getPriceFloor().compareTo(request.getAutoAcceptThreshold()) > 0) {
            throw new BusinessRuleException("Price floor must not exceed auto-accept threshold");
        }

        NegotiationRule rule = ruleMapper.toEntity(request);
        rule.setSupplier(supplier);
        rule.setProduct(product);

        NegotiationRule saved = ruleRepository.save(rule);
        return ruleMapper.toResponse(saved);
    }

    /**
     * Updates an existing negotiation rule.
     *
     * @param ruleId     the rule ID
     * @param supplierId the supplier's company ID
     * @param request    the updated rule details
     * @return the updated rule
     */
    @Transactional
    public NegotiationRuleResponse update(Long ruleId, Long supplierId, NegotiationRuleRequest request) {
        NegotiationRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("NegotiationRule", "id", ruleId));

        if (!rule.getSupplier().getId().equals(supplierId)) {
            throw new AccessDeniedException("You can only manage your own negotiation rules");
        }

        if (request.getPriceFloor().compareTo(request.getAutoAcceptThreshold()) > 0) {
            throw new BusinessRuleException("Price floor must not exceed auto-accept threshold");
        }

        ruleMapper.updateEntity(request, rule);
        NegotiationRule saved = ruleRepository.save(rule);
        return ruleMapper.toResponse(saved);
    }

    /**
     * Deletes a negotiation rule.
     *
     * @param ruleId     the rule ID
     * @param supplierId the supplier's company ID
     */
    @Transactional
    public void delete(Long ruleId, Long supplierId) {
        NegotiationRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("NegotiationRule", "id", ruleId));

        if (!rule.getSupplier().getId().equals(supplierId)) {
            throw new AccessDeniedException("You can only delete your own negotiation rules");
        }

        ruleRepository.delete(rule);
    }

    /**
     * Lists all negotiation rules for a supplier.
     *
     * @param supplierId the supplier's company ID
     * @return list of rules
     */
    public List<NegotiationRuleResponse> listBySupplier(Long supplierId) {
        return ruleRepository.findBySupplierId(supplierId).stream()
                .map(ruleMapper::toResponse)
                .toList();
    }
}
```

2. Create `src/test/java/com/silentsupply/negotiation/NegotiationRuleServiceTest.java`:

```java
package com.silentsupply.negotiation;

import com.silentsupply.common.exception.AccessDeniedException;
import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.negotiation.dto.NegotiationRuleRequest;
import com.silentsupply.negotiation.dto.NegotiationRuleResponse;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductRepository;
import com.silentsupply.product.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link NegotiationRuleService}.
 */
@ExtendWith(MockitoExtension.class)
class NegotiationRuleServiceTest {

    @Mock
    private NegotiationRuleRepository ruleRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private NegotiationRuleMapper ruleMapper;

    @InjectMocks
    private NegotiationRuleService ruleService;

    private Company supplier;
    private Product product;
    private NegotiationRuleRequest request;

    @BeforeEach
    void setUp() {
        supplier = Company.builder().name("SupplierCo").email("s@s.com").password("p").role(CompanyRole.SUPPLIER).build();
        supplier.setId(1L);

        product = Product.builder()
                .supplier(supplier).name("Widget").category("Cat").sku("W-1")
                .unitOfMeasure("pc").basePrice(new BigDecimal("10.00")).availableQuantity(100)
                .status(ProductStatus.ACTIVE).build();
        product.setId(10L);

        request = NegotiationRuleRequest.builder()
                .productId(10L).priceFloor(new BigDecimal("7.00"))
                .autoAcceptThreshold(new BigDecimal("9.50")).maxDeliveryDays(30)
                .maxRounds(3).volumeDiscountPct(new BigDecimal("5.00")).volumeThreshold(100).build();
    }

    @Test
    void create_withValidRequest_createsRule() {
        NegotiationRule rule = NegotiationRule.builder()
                .supplier(supplier).product(product)
                .priceFloor(new BigDecimal("7.00")).autoAcceptThreshold(new BigDecimal("9.50"))
                .maxDeliveryDays(30).maxRounds(3)
                .volumeDiscountPct(new BigDecimal("5.00")).volumeThreshold(100).build();
        rule.setId(50L);

        NegotiationRuleResponse expectedResponse = NegotiationRuleResponse.builder()
                .id(50L).supplierId(1L).productId(10L).productName("Widget")
                .priceFloor(new BigDecimal("7.00")).autoAcceptThreshold(new BigDecimal("9.50"))
                .maxDeliveryDays(30).maxRounds(3)
                .volumeDiscountPct(new BigDecimal("5.00")).volumeThreshold(100).build();

        when(companyRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(ruleRepository.findBySupplierIdAndProductId(1L, 10L)).thenReturn(Optional.empty());
        when(ruleMapper.toEntity(request)).thenReturn(rule);
        when(ruleRepository.save(rule)).thenReturn(rule);
        when(ruleMapper.toResponse(rule)).thenReturn(expectedResponse);

        NegotiationRuleResponse result = ruleService.create(1L, request);

        assertThat(result.getPriceFloor()).isEqualByComparingTo(new BigDecimal("7.00"));
        verify(ruleRepository).save(rule);
    }

    @Test
    void create_forOtherSuppliersProduct_throwsAccessDeniedException() {
        when(companyRepository.findById(999L)).thenReturn(Optional.of(
                Company.builder().name("Other").email("o@o.com").password("p").role(CompanyRole.SUPPLIER).build()));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> ruleService.create(999L, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("your own products");
    }

    @Test
    void create_withDuplicateRule_throwsBusinessRuleException() {
        NegotiationRule existingRule = NegotiationRule.builder().build();

        when(companyRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(ruleRepository.findBySupplierIdAndProductId(1L, 10L)).thenReturn(Optional.of(existingRule));

        assertThatThrownBy(() -> ruleService.create(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already exists");

        verify(ruleRepository, never()).save(any());
    }

    @Test
    void create_withFloorAboveThreshold_throwsBusinessRuleException() {
        request.setPriceFloor(new BigDecimal("15.00"));
        request.setAutoAcceptThreshold(new BigDecimal("9.50"));

        when(companyRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(ruleRepository.findBySupplierIdAndProductId(1L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ruleService.create(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Price floor must not exceed");
    }
}
```

3. Run tests:

```bash
./mvnw test -Dtest=NegotiationRuleServiceTest
```

4. Commit: `feat: add negotiation rule service with CRUD and ownership validation`

---

### Task 23 — NegotiationRule controller + integration tests

**Files:**
- Create: `src/main/java/com/silentsupply/negotiation/NegotiationRuleController.java`
- Test: `src/test/java/com/silentsupply/negotiation/NegotiationRuleControllerIntegrationTest.java`

**Steps:**

1. Create `src/main/java/com/silentsupply/negotiation/NegotiationRuleController.java`:

```java
package com.silentsupply.negotiation;

import com.silentsupply.config.CompanyUserDetails;
import com.silentsupply.negotiation.dto.NegotiationRuleRequest;
import com.silentsupply.negotiation.dto.NegotiationRuleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing negotiation rules.
 * Supplier-only: each supplier manages rules for their own products.
 */
@RestController
@RequestMapping("/api/suppliers/{supplierId}/negotiation-rules")
@RequiredArgsConstructor
@Tag(name = "Negotiation Rules", description = "Supplier-defined negotiation rules per product")
public class NegotiationRuleController {

    private final NegotiationRuleService ruleService;

    /**
     * Creates a new negotiation rule for a product.
     *
     * @param supplierId  the supplier's company ID (from path)
     * @param userDetails the authenticated supplier
     * @param request     the rule details
     * @return the created rule with HTTP 201
     */
    @PostMapping
    @Operation(summary = "Create a negotiation rule (supplier only)")
    public ResponseEntity<NegotiationRuleResponse> create(
            @PathVariable Long supplierId,
            @AuthenticationPrincipal CompanyUserDetails userDetails,
            @Valid @RequestBody NegotiationRuleRequest request) {
        NegotiationRuleResponse response = ruleService.create(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing negotiation rule.
     *
     * @param supplierId  the supplier's company ID
     * @param ruleId      the rule ID
     * @param userDetails the authenticated supplier
     * @param request     the updated rule details
     * @return the updated rule
     */
    @PutMapping("/{ruleId}")
    @Operation(summary = "Update a negotiation rule (supplier only)")
    public ResponseEntity<NegotiationRuleResponse> update(
            @PathVariable Long supplierId,
            @PathVariable Long ruleId,
            @AuthenticationPrincipal CompanyUserDetails userDetails,
            @Valid @RequestBody NegotiationRuleRequest request) {
        NegotiationRuleResponse response = ruleService.update(ruleId, userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a negotiation rule.
     *
     * @param supplierId  the supplier's company ID
     * @param ruleId      the rule ID
     * @param userDetails the authenticated supplier
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{ruleId}")
    @Operation(summary = "Delete a negotiation rule (supplier only)")
    public ResponseEntity<Void> delete(
            @PathVariable Long supplierId,
            @PathVariable Long ruleId,
            @AuthenticationPrincipal CompanyUserDetails userDetails) {
        ruleService.delete(ruleId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Lists all negotiation rules for the supplier.
     *
     * @param supplierId  the supplier's company ID
     * @param userDetails the authenticated supplier
     * @return list of rules
     */
    @GetMapping
    @Operation(summary = "List all negotiation rules for a supplier")
    public ResponseEntity<List<NegotiationRuleResponse>> listBySupplier(
            @PathVariable Long supplierId,
            @AuthenticationPrincipal CompanyUserDetails userDetails) {
        return ResponseEntity.ok(ruleService.listBySupplier(userDetails.getId()));
    }
}
```

2. Create `src/test/java/com/silentsupply/negotiation/NegotiationRuleControllerIntegrationTest.java`:

```java
package com.silentsupply.negotiation;

import com.silentsupply.common.BaseIntegrationTest;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.config.dto.AuthResponse;
import com.silentsupply.negotiation.dto.NegotiationRuleRequest;
import com.silentsupply.negotiation.dto.NegotiationRuleResponse;
import com.silentsupply.product.ProductRepository;
import com.silentsupply.product.dto.ProductRequest;
import com.silentsupply.product.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link NegotiationRuleController}.
 */
class NegotiationRuleControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private NegotiationRuleRepository ruleRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CompanyRepository companyRepository;

    private String supplierToken;
    private String buyerToken;
    private Long supplierId;
    private Long productId;

    @BeforeEach
    void setUp() {
        ruleRepository.deleteAll();
        productRepository.deleteAll();
        companyRepository.deleteAll();

        AuthResponse supplierAuth = registerCompany("SupplierCo", "supplier@example.com", CompanyRole.SUPPLIER);
        supplierToken = supplierAuth.getToken();
        supplierId = supplierAuth.getCompanyId();

        buyerToken = registerCompany("BuyerCo", "buyer@example.com", CompanyRole.BUYER).getToken();

        ProductRequest productRequest = ProductRequest.builder()
                .name("Widget").description("Test").category("Electronics").sku("W-1")
                .unitOfMeasure("piece").basePrice(new BigDecimal("10.00")).availableQuantity(100)
                .build();
        ResponseEntity<ProductResponse> productResponse = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                new HttpEntity<>(productRequest, authHeaders(supplierToken)),
                ProductResponse.class);
        productId = productResponse.getBody().getId();
    }

    @Test
    void create_asSupplier_returns201() {
        NegotiationRuleRequest request = buildRuleRequest();

        ResponseEntity<NegotiationRuleResponse> response = restTemplate.exchange(
                "/api/suppliers/" + supplierId + "/negotiation-rules", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(supplierToken)),
                NegotiationRuleResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPriceFloor()).isEqualByComparingTo(new BigDecimal("7.00"));
        assertThat(response.getBody().getProductName()).isEqualTo("Widget");
    }

    @Test
    void create_asBuyer_returns403() {
        NegotiationRuleRequest request = buildRuleRequest();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/suppliers/" + supplierId + "/negotiation-rules", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders(buyerToken)),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void list_afterCreation_returnsRules() {
        restTemplate.exchange(
                "/api/suppliers/" + supplierId + "/negotiation-rules", HttpMethod.POST,
                new HttpEntity<>(buildRuleRequest(), authHeaders(supplierToken)),
                NegotiationRuleResponse.class);

        ResponseEntity<NegotiationRuleResponse[]> response = restTemplate.exchange(
                "/api/suppliers/" + supplierId + "/negotiation-rules", HttpMethod.GET,
                new HttpEntity<>(authHeaders(supplierToken)),
                NegotiationRuleResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void delete_asSupplier_returns204() {
        ResponseEntity<NegotiationRuleResponse> created = restTemplate.exchange(
                "/api/suppliers/" + supplierId + "/negotiation-rules", HttpMethod.POST,
                new HttpEntity<>(buildRuleRequest(), authHeaders(supplierToken)),
                NegotiationRuleResponse.class);
        Long ruleId = created.getBody().getId();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/suppliers/" + supplierId + "/negotiation-rules/" + ruleId, HttpMethod.DELETE,
                new HttpEntity<>(authHeaders(supplierToken)),
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private NegotiationRuleRequest buildRuleRequest() {
        return NegotiationRuleRequest.builder()
                .productId(productId).priceFloor(new BigDecimal("7.00"))
                .autoAcceptThreshold(new BigDecimal("9.50")).maxDeliveryDays(30)
                .maxRounds(3).volumeDiscountPct(new BigDecimal("5.00")).volumeThreshold(100).build();
    }

    private AuthResponse registerCompany(String name, String email, CompanyRole role) {
        CompanyRequest request = CompanyRequest.builder()
                .name(name).email(email).password("password123").role(role).build();
        return restTemplate.postForEntity("/api/auth/register", request, AuthResponse.class).getBody();
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
```

3. Run tests:

```bash
./mvnw test -Dtest=NegotiationRuleControllerIntegrationTest
```

4. Commit: `feat: add negotiation rule controller with CRUD endpoints`

---

### Task 24 — NegotiationEngine service (core auto-negotiation logic) + tests

**Files:**
- Create: `src/main/java/com/silentsupply/negotiation/NegotiationResult.java`
- Create: `src/main/java/com/silentsupply/negotiation/NegotiationEngine.java`
- Test: `src/test/java/com/silentsupply/negotiation/NegotiationEngineTest.java`

**Steps:**

1. Create `src/main/java/com/silentsupply/negotiation/NegotiationResult.java`:

```java
package com.silentsupply.negotiation;

import com.silentsupply.proposal.ProposalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Encapsulates the outcome of the negotiation engine's evaluation of a buyer proposal.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationResult {

    /** The outcome for the buyer's proposal. */
    private ProposalStatus buyerProposalStatus;

    /** Reason code explaining the outcome. */
    private String reasonCode;

    /** Whether a counter-proposal should be generated. */
    private boolean counterGenerated;

    /** Counter-proposed price (if counter generated). */
    private BigDecimal counterPrice;

    /** Counter-proposed quantity (if counter generated). */
    private int counterQty;

    /** Counter-proposed delivery days (if counter generated). */
    private int counterDeliveryDays;
}
```

2. Create `src/main/java/com/silentsupply/negotiation/NegotiationEngine.java`:

```java
package com.silentsupply.negotiation;

import com.silentsupply.proposal.Proposal;
import com.silentsupply.proposal.ProposalStatus;
import com.silentsupply.rfq.Rfq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Core negotiation engine implementing deterministic, rule-based evaluation of proposals.
 *
 * <p>Decision logic:
 * <ul>
 *   <li>If proposed price >= auto-accept threshold AND delivery within limits: AUTO-ACCEPT</li>
 *   <li>If proposed price >= price floor AND delivery within limits: AUTO-COUNTER with best terms</li>
 *   <li>If proposed price < price floor OR delivery exceeds max: AUTO-REJECT</li>
 *   <li>If max rounds exceeded: AUTO-REJECT with MAX_ROUNDS_EXCEEDED</li>
 * </ul>
 *
 * <p>Volume discounts are applied when the proposed quantity meets or exceeds the volume threshold,
 * reducing the effective price floor and auto-accept threshold accordingly.
 */
@Service
@Slf4j
public class NegotiationEngine {

    /**
     * Evaluates a buyer proposal against supplier-defined negotiation rules.
     *
     * @param proposal the buyer's proposal
     * @param rfq      the associated RFQ
     * @param rule     the supplier's negotiation rules for this product
     * @return the negotiation result describing the outcome
     */
    public NegotiationResult evaluate(Proposal proposal, Rfq rfq, NegotiationRule rule) {
        log.debug("Evaluating proposal {} for RFQ {} against rule {}", proposal.getId(), rfq.getId(), rule.getId());

        if (rfq.getCurrentRound() > rule.getMaxRounds()) {
            return NegotiationResult.builder()
                    .buyerProposalStatus(ProposalStatus.REJECTED)
                    .reasonCode("MAX_ROUNDS_EXCEEDED")
                    .counterGenerated(false)
                    .build();
        }

        BigDecimal effectiveFloor = calculateEffectivePrice(rule.getPriceFloor(), proposal.getProposedQty(), rule);
        BigDecimal effectiveThreshold = calculateEffectivePrice(rule.getAutoAcceptThreshold(), proposal.getProposedQty(), rule);

        boolean priceAcceptable = proposal.getProposedPrice().compareTo(effectiveThreshold) >= 0;
        boolean priceNegotiable = proposal.getProposedPrice().compareTo(effectiveFloor) >= 0;
        boolean deliveryAcceptable = proposal.getDeliveryDays() <= rule.getMaxDeliveryDays();

        if (!deliveryAcceptable) {
            return NegotiationResult.builder()
                    .buyerProposalStatus(ProposalStatus.REJECTED)
                    .reasonCode("DELIVERY_EXCEEDS_MAX")
                    .counterGenerated(false)
                    .build();
        }

        if (!priceNegotiable) {
            return NegotiationResult.builder()
                    .buyerProposalStatus(ProposalStatus.REJECTED)
                    .reasonCode("PRICE_BELOW_FLOOR")
                    .counterGenerated(false)
                    .build();
        }

        if (priceAcceptable) {
            return NegotiationResult.builder()
                    .buyerProposalStatus(ProposalStatus.ACCEPTED)
                    .reasonCode("AUTO_ACCEPTED")
                    .counterGenerated(false)
                    .build();
        }

        BigDecimal counterPrice = effectiveThreshold;
        return NegotiationResult.builder()
                .buyerProposalStatus(ProposalStatus.COUNTERED)
                .reasonCode("AUTO_COUNTERED")
                .counterGenerated(true)
                .counterPrice(counterPrice)
                .counterQty(proposal.getProposedQty())
                .counterDeliveryDays(Math.min(proposal.getDeliveryDays(), rule.getMaxDeliveryDays()))
                .build();
    }

    /**
     * Calculates the effective price after applying volume discounts.
     * If the proposed quantity meets or exceeds the volume threshold,
     * the price is reduced by the volume discount percentage.
     *
     * @param basePrice    the original price
     * @param proposedQty  the proposed quantity
     * @param rule         the negotiation rule with discount parameters
     * @return the effective price
     */
    BigDecimal calculateEffectivePrice(BigDecimal basePrice, int proposedQty, NegotiationRule rule) {
        if (rule.getVolumeThreshold() > 0
                && proposedQty >= rule.getVolumeThreshold()
                && rule.getVolumeDiscountPct().compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                    rule.getVolumeDiscountPct().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
            return basePrice.multiply(discountMultiplier).setScale(2, RoundingMode.HALF_UP);
        }
        return basePrice;
    }
}
```

3. Create `src/test/java/com/silentsupply/negotiation/NegotiationEngineTest.java`:

```java
package com.silentsupply.negotiation;

import com.silentsupply.company.Company;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.product.Product;
import com.silentsupply.product.ProductStatus;
import com.silentsupply.proposal.Proposal;
import com.silentsupply.proposal.ProposalStatus;
import com.silentsupply.proposal.ProposerType;
import com.silentsupply.rfq.Rfq;
import com.silentsupply.rfq.RfqStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link NegotiationEngine}.
 * Tests all outcomes: auto-accept, auto-counter, auto-reject, max rounds, and volume discounts.
 */
class NegotiationEngineTest {

    private NegotiationEngine engine;
    private NegotiationRule rule;
    private Rfq rfq;
    private Company supplier;
    private Company buyer;
    private Product product;

    @BeforeEach
    void setUp() {
        engine = new NegotiationEngine();

        supplier = Company.builder().name("SupplierCo").email("s@s.com").password("p").role(CompanyRole.SUPPLIER).build();
        supplier.setId(1L);
        buyer = Company.builder().name("BuyerCo").email("b@b.com").password("p").role(CompanyRole.BUYER).build();
        buyer.setId(2L);
        product = Product.builder()
                .supplier(supplier).name("Widget").category("Cat").sku("W-1")
                .unitOfMeasure("pc").basePrice(new BigDecimal("10.00")).availableQuantity(1000)
                .status(ProductStatus.ACTIVE).build();
        product.setId(10L);

        rule = NegotiationRule.builder()
                .supplier(supplier).product(product)
                .priceFloor(new BigDecimal("7.00"))
                .autoAcceptThreshold(new BigDecimal("9.50"))
                .maxDeliveryDays(30)
                .maxRounds(3)
                .volumeDiscountPct(new BigDecimal("5.00"))
                .volumeThreshold(100)
                .build();
        rule.setId(50L);

        rfq = Rfq.builder()
                .buyer(buyer).product(product).supplier(supplier)
                .desiredQuantity(50).targetPrice(new BigDecimal("8.00"))
                .deliveryDeadline(LocalDate.of(2026, 4, 1))
                .status(RfqStatus.UNDER_REVIEW).currentRound(1).maxRounds(3)
                .expiresAt(LocalDateTime.now().plusDays(7)).build();
        rfq.setId(100L);
    }

    @Test
    void evaluate_priceAboveThreshold_autoAccepts() {
        Proposal proposal = buildProposal(new BigDecimal("9.50"), 50, 14);

        NegotiationResult result = engine.evaluate(proposal, rfq, rule);

        assertThat(result.getBuyerProposalStatus()).isEqualTo(ProposalStatus.ACCEPTED);
        assertThat(result.getReasonCode()).isEqualTo("AUTO_ACCEPTED");
        assertThat(result.isCounterGenerated()).isFalse();
    }

    @Test
    void evaluate_priceAboveFloorBelowThreshold_autoCounters() {
        Proposal proposal = buildProposal(new BigDecimal("8.00"), 50, 14);

        NegotiationResult result = engine.evaluate(proposal, rfq, rule);

        assertThat(result.getBuyerProposalStatus()).isEqualTo(ProposalStatus.COUNTERED);
        assertThat(result.getReasonCode()).isEqualTo("AUTO_COUNTERED");
        assertThat(result.isCounterGenerated()).isTrue();
        assertThat(result.getCounterPrice()).isEqualByComparingTo(new BigDecimal("9.50"));
        assertThat(result.getCounterQty()).isEqualTo(50);
    }

    @Test
    void evaluate_priceBelowFloor_autoRejects() {
        Proposal proposal = buildProposal(new BigDecimal("5.00"), 50, 14);

        NegotiationResult result = engine.evaluate(proposal, rfq, rule);

        assertThat(result.getBuyerProposalStatus()).isEqualTo(ProposalStatus.REJECTED);
        assertThat(result.getReasonCode()).isEqualTo("PRICE_BELOW_FLOOR");
        assertThat(result.isCounterGenerated()).isFalse();
    }

    @Test
    void evaluate_deliveryExceedsMax_autoRejects() {
        Proposal proposal = buildProposal(new BigDecimal("9.00"), 50, 60);

        NegotiationResult result = engine.evaluate(proposal, rfq, rule);

        assertThat(result.getBuyerProposalStatus()).isEqualTo(ProposalStatus.REJECTED);
        assertThat(result.getReasonCode()).isEqualTo("DELIVERY_EXCEEDS_MAX");
    }

    @Test
    void evaluate_maxRoundsExceeded_autoRejects() {
        rfq.setCurrentRound(4);
        Proposal proposal = buildProposal(new BigDecimal("9.00"), 50, 14);

        NegotiationResult result = engine.evaluate(proposal, rfq, rule);

        assertThat(result.getBuyerProposalStatus()).isEqualTo(ProposalStatus.REJECTED);
        assertThat(result.getReasonCode()).isEqualTo("MAX_ROUNDS_EXCEEDED");
    }

    @Test
    void evaluate_withVolumeDiscount_lowersEffectivePrices() {
        Proposal proposal = buildProposal(new BigDecimal("9.03"), 150, 14);

        NegotiationResult result = engine.evaluate(proposal, rfq, rule);

        assertThat(result.getBuyerProposalStatus()).isEqualTo(ProposalStatus.ACCEPTED);
        assertThat(result.getReasonCode()).isEqualTo("AUTO_ACCEPTED");
    }

    @Test
    void evaluate_withVolumeDiscount_belowDiscountedFloor_rejects() {
        Proposal proposal = buildProposal(new BigDecimal("6.00"), 150, 14);

        NegotiationResult result = engine.evaluate(proposal, rfq, rule);

        assertThat(result.getBuyerProposalStatus()).isEqualTo(ProposalStatus.REJECTED);
        assertThat(result.getReasonCode()).isEqualTo("PRICE_BELOW_FLOOR");
    }

    @Test
    void evaluate_withVolumeDiscount_inNegotiableRange_countersWithDiscountedPrice() {
        Proposal proposal = buildProposal(new BigDecimal("7.50"), 150, 14);

        NegotiationResult result = engine.evaluate(proposal, rfq, rule);

        assertThat(result.getBuyerProposalStatus()).isEqualTo(ProposalStatus.COUNTERED);
        assertThat(result.isCounterGenerated()).isTrue();
        assertThat(result.getCounterPrice()).isEqualByComparingTo(new BigDecimal("9.03"));
    }

    @Test
    void calculateEffectivePrice_belowThreshold_noDiscount() {
        BigDecimal result = engine.calculateEffectivePrice(new BigDecimal("10.00"), 50, rule);

        assertThat(result).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    void calculateEffectivePrice_atThreshold_appliesDiscount() {
        BigDecimal result = engine.calculateEffectivePrice(new BigDecimal("10.00"), 100, rule);

        assertThat(result).isEqualByComparingTo(new BigDecimal("9.50"));
    }

    private Proposal buildProposal(BigDecimal price, int qty, int deliveryDays) {
        Proposal proposal = Proposal.builder()
                .rfq(rfq).proposerType(ProposerType.BUYER)
                .proposedPrice(price).proposedQty(qty).deliveryDays(deliveryDays)
                .status(ProposalStatus.PENDING).roundNumber(1).build();
        proposal.setId(200L);
        return proposal;
    }
}
```

4. Run tests:

```bash
./mvnw test -Dtest=NegotiationEngineTest
```

5. Commit: `feat: add negotiation engine with auto-accept, counter, reject, and volume discount logic`

---

### Task 25 — Wire NegotiationEngine into ProposalService + integration test

**Files:**
- Modify: `src/main/java/com/silentsupply/proposal/ProposalService.java`
- Test: `src/test/java/com/silentsupply/negotiation/NegotiationIntegrationTest.java`

**Steps:**

1. Modify `src/main/java/com/silentsupply/proposal/ProposalService.java` — add the negotiation engine wiring. Replace the entire class with:

```java
package com.silentsupply.proposal;

import com.silentsupply.common.exception.BusinessRuleException;
import com.silentsupply.negotiation.NegotiationEngine;
import com.silentsupply.negotiation.NegotiationResult;
import com.silentsupply.negotiation.NegotiationRule;
import com.silentsupply.negotiation.NegotiationRuleRepository;
import com.silentsupply.proposal.dto.ProposalRequest;
import com.silentsupply.proposal.dto.ProposalResponse;
import com.silentsupply.rfq.Rfq;
import com.silentsupply.rfq.RfqRepository;
import com.silentsupply.rfq.RfqService;
import com.silentsupply.rfq.RfqStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service layer for proposal creation and retrieval within RFQ negotiations.
 * When a buyer submits a proposal and negotiation rules exist, the negotiation
 * engine is triggered automatically to evaluate and potentially counter or resolve.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProposalService {

    /** RFQ statuses that allow new proposals. */
    private static final Set<RfqStatus> PROPOSABLE_STATUSES = Set.of(
            RfqStatus.SUBMITTED, RfqStatus.UNDER_REVIEW, RfqStatus.COUNTERED);

    private final ProposalRepository proposalRepository;
    private final RfqRepository rfqRepository;
    private final RfqService rfqService;
    private final ProposalMapper proposalMapper;
    private final NegotiationRuleRepository ruleRepository;
    private final NegotiationEngine negotiationEngine;

    /**
     * Creates a buyer proposal for an RFQ. If negotiation rules exist for the product,
     * the negotiation engine evaluates the proposal and may auto-accept, counter, or reject.
     *
     * @param rfqId   the RFQ ID
     * @param buyerId the buyer's company ID
     * @param request the proposal details
     * @return the created proposal (may already be resolved by the engine)
     * @throws BusinessRuleException if the RFQ is not in a proposable status or max rounds exceeded
     */
    @Transactional
    public ProposalResponse createBuyerProposal(Long rfqId, Long buyerId, ProposalRequest request) {
        Rfq rfq = rfqService.findRfqOrThrow(rfqId);

        if (!rfq.getBuyer().getId().equals(buyerId)) {
            throw new BusinessRuleException("Only the RFQ owner can submit proposals");
        }

        if (!PROPOSABLE_STATUSES.contains(rfq.getStatus())) {
            throw new BusinessRuleException("RFQ is not in a status that accepts proposals: " + rfq.getStatus());
        }

        if (rfq.getCurrentRound() >= rfq.getMaxRounds()) {
            throw new BusinessRuleException("Maximum negotiation rounds reached: " + rfq.getMaxRounds());
        }

        int nextRound = rfq.getCurrentRound() + 1;
        rfq.setCurrentRound(nextRound);
        rfq.setStatus(RfqStatus.UNDER_REVIEW);
        rfqRepository.save(rfq);

        Proposal proposal = Proposal.builder()
                .rfq(rfq)
                .proposerType(ProposerType.BUYER)
                .proposedPrice(request.getProposedPrice())
                .proposedQty(request.getProposedQty())
                .deliveryDays(request.getDeliveryDays())
                .status(ProposalStatus.PENDING)
                .roundNumber(nextRound)
                .build();

        Proposal savedProposal = proposalRepository.save(proposal);

        Optional<NegotiationRule> ruleOpt = ruleRepository.findBySupplierIdAndProductId(
                rfq.getSupplier().getId(), rfq.getProduct().getId());

        if (ruleOpt.isPresent()) {
            NegotiationResult result = negotiationEngine.evaluate(savedProposal, rfq, ruleOpt.get());
            applyNegotiationResult(savedProposal, rfq, result);
        }

        return proposalMapper.toResponse(savedProposal);
    }

    /**
     * Lists all proposals for a given RFQ, ordered by round number.
     *
     * @param rfqId the RFQ ID
     * @return list of proposals
     */
    public List<ProposalResponse> listByRfq(Long rfqId) {
        return proposalRepository.findByRfqIdOrderByRoundNumberAsc(rfqId).stream()
                .map(proposalMapper::toResponse)
                .toList();
    }

    /**
     * Applies the negotiation engine's result to the proposal and RFQ.
     *
     * @param buyerProposal the buyer's proposal
     * @param rfq           the associated RFQ
     * @param result        the negotiation result
     */
    private void applyNegotiationResult(Proposal buyerProposal, Rfq rfq, NegotiationResult result) {
        buyerProposal.setStatus(result.getBuyerProposalStatus());
        buyerProposal.setReasonCode(result.getReasonCode());
        proposalRepository.save(buyerProposal);

        switch (result.getBuyerProposalStatus()) {
            case ACCEPTED -> {
                rfq.setStatus(RfqStatus.ACCEPTED);
                rfqRepository.save(rfq);
                log.info("RFQ {} auto-accepted at round {}", rfq.getId(), rfq.getCurrentRound());
            }
            case REJECTED -> {
                rfq.setStatus(RfqStatus.REJECTED);
                rfqRepository.save(rfq);
                log.info("RFQ {} auto-rejected: {}", rfq.getId(), result.getReasonCode());
            }
            case COUNTERED -> {
                rfq.setStatus(RfqStatus.COUNTERED);
                rfqRepository.save(rfq);

                Proposal counterProposal = Proposal.builder()
                        .rfq(rfq)
                        .proposerType(ProposerType.SYSTEM)
                        .proposedPrice(result.getCounterPrice())
                        .proposedQty(result.getCounterQty())
                        .deliveryDays(result.getCounterDeliveryDays())
                        .status(ProposalStatus.PENDING)
                        .roundNumber(rfq.getCurrentRound())
                        .reasonCode("AUTO_COUNTERED")
                        .build();
                proposalRepository.save(counterProposal);
                log.info("RFQ {} auto-countered at round {} with price {}",
                        rfq.getId(), rfq.getCurrentRound(), result.getCounterPrice());
            }
            default -> log.warn("Unexpected proposal status from engine: {}", result.getBuyerProposalStatus());
        }
    }
}
```

2. Create `src/test/java/com/silentsupply/negotiation/NegotiationIntegrationTest.java`:

```java
package com.silentsupply.negotiation;

import com.silentsupply.common.BaseIntegrationTest;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.config.dto.AuthResponse;
import com.silentsupply.negotiation.dto.NegotiationRuleRequest;
import com.silentsupply.negotiation.dto.NegotiationRuleResponse;
import com.silentsupply.product.ProductRepository;
import com.silentsupply.product.dto.ProductRequest;
import com.silentsupply.product.dto.ProductResponse;
import com.silentsupply.proposal.ProposalRepository;
import com.silentsupply.proposal.ProposalStatus;
import com.silentsupply.proposal.ProposerType;
import com.silentsupply.proposal.dto.ProposalRequest;
import com.silentsupply.proposal.dto.ProposalResponse;
import com.silentsupply.rfq.RfqRepository;
import com.silentsupply.rfq.RfqStatus;
import com.silentsupply.rfq.dto.RfqRequest;
import com.silentsupply.rfq.dto.RfqResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the full RFQ -> Proposal -> Auto-Negotiation flow.
 */
class NegotiationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProposalRepository proposalRepository;
    @Autowired
    private RfqRepository rfqRepository;
    @Autowired
    private NegotiationRuleRepository ruleRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CompanyRepository companyRepository;

    private String supplierToken;
    private String buyerToken;
    private Long supplierId;
    private Long productId;

    @BeforeEach
    void setUp() {
        proposalRepository.deleteAll();
        rfqRepository.deleteAll();
        ruleRepository.deleteAll();
        productRepository.deleteAll();
        companyRepository.deleteAll();

        AuthResponse supplierAuth = registerCompany("SupplierCo", "supplier@example.com", CompanyRole.SUPPLIER);
        supplierToken = supplierAuth.getToken();
        supplierId = supplierAuth.getCompanyId();

        buyerToken = registerCompany("BuyerCo", "buyer@example.com", CompanyRole.BUYER).getToken();

        ProductRequest productReq = ProductRequest.builder()
                .name("Widget").description("Test").category("Electronics").sku("W-1")
                .unitOfMeasure("piece").basePrice(new BigDecimal("10.00")).availableQuantity(1000)
                .build();
        ResponseEntity<ProductResponse> productResp = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                new HttpEntity<>(productReq, authHeaders(supplierToken)),
                ProductResponse.class);
        productId = productResp.getBody().getId();

        NegotiationRuleRequest ruleReq = NegotiationRuleRequest.builder()
                .productId(productId).priceFloor(new BigDecimal("7.00"))
                .autoAcceptThreshold(new BigDecimal("9.50")).maxDeliveryDays(30)
                .maxRounds(3).volumeDiscountPct(new BigDecimal("5.00")).volumeThreshold(100)
                .build();
        restTemplate.exchange(
                "/api/suppliers/" + supplierId + "/negotiation-rules", HttpMethod.POST,
                new HttpEntity<>(ruleReq, authHeaders(supplierToken)),
                NegotiationRuleResponse.class);
    }

    @Test
    void fullFlow_proposalAboveThreshold_autoAccepts() {
        Long rfqId = submitRfq();

        ProposalRequest proposalReq = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("9.50")).proposedQty(50).deliveryDays(14).build();

        ResponseEntity<ProposalResponse> response = restTemplate.exchange(
                "/api/rfqs/" + rfqId + "/proposals", HttpMethod.POST,
                new HttpEntity<>(proposalReq, authHeaders(buyerToken)),
                ProposalResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getStatus()).isEqualTo(ProposalStatus.ACCEPTED);
        assertThat(response.getBody().getReasonCode()).isEqualTo("AUTO_ACCEPTED");

        RfqResponse rfq = getRfq(rfqId);
        assertThat(rfq.getStatus()).isEqualTo(RfqStatus.ACCEPTED);
    }

    @Test
    void fullFlow_proposalInNegotiableRange_autoCounters() {
        Long rfqId = submitRfq();

        ProposalRequest proposalReq = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("8.00")).proposedQty(50).deliveryDays(14).build();

        ResponseEntity<ProposalResponse> response = restTemplate.exchange(
                "/api/rfqs/" + rfqId + "/proposals", HttpMethod.POST,
                new HttpEntity<>(proposalReq, authHeaders(buyerToken)),
                ProposalResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getStatus()).isEqualTo(ProposalStatus.COUNTERED);

        ResponseEntity<ProposalResponse[]> proposals = restTemplate.exchange(
                "/api/rfqs/" + rfqId + "/proposals", HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                ProposalResponse[].class);

        assertThat(proposals.getBody()).hasSize(2);
        ProposalResponse counterProposal = proposals.getBody()[1];
        assertThat(counterProposal.getProposerType()).isEqualTo(ProposerType.SYSTEM);
        assertThat(counterProposal.getProposedPrice()).isEqualByComparingTo(new BigDecimal("9.50"));

        RfqResponse rfq = getRfq(rfqId);
        assertThat(rfq.getStatus()).isEqualTo(RfqStatus.COUNTERED);
    }

    @Test
    void fullFlow_proposalBelowFloor_autoRejects() {
        Long rfqId = submitRfq();

        ProposalRequest proposalReq = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("5.00")).proposedQty(50).deliveryDays(14).build();

        ResponseEntity<ProposalResponse> response = restTemplate.exchange(
                "/api/rfqs/" + rfqId + "/proposals", HttpMethod.POST,
                new HttpEntity<>(proposalReq, authHeaders(buyerToken)),
                ProposalResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getStatus()).isEqualTo(ProposalStatus.REJECTED);
        assertThat(response.getBody().getReasonCode()).isEqualTo("PRICE_BELOW_FLOOR");

        RfqResponse rfq = getRfq(rfqId);
        assertThat(rfq.getStatus()).isEqualTo(RfqStatus.REJECTED);
    }

    @Test
    void fullFlow_volumeDiscount_lowersThresholdAndAccepts() {
        Long rfqId = submitRfqWithQuantity(150);

        ProposalRequest proposalReq = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("9.03")).proposedQty(150).deliveryDays(14).build();

        ResponseEntity<ProposalResponse> response = restTemplate.exchange(
                "/api/rfqs/" + rfqId + "/proposals", HttpMethod.POST,
                new HttpEntity<>(proposalReq, authHeaders(buyerToken)),
                ProposalResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getStatus()).isEqualTo(ProposalStatus.ACCEPTED);
    }

    private Long submitRfq() {
        return submitRfqWithQuantity(50);
    }

    private Long submitRfqWithQuantity(int qty) {
        RfqRequest rfqReq = RfqRequest.builder()
                .productId(productId).desiredQuantity(qty).targetPrice(new BigDecimal("8.00"))
                .deliveryDeadline(LocalDate.now().plusDays(30)).build();
        ResponseEntity<RfqResponse> rfqResp = restTemplate.exchange(
                "/api/rfqs", HttpMethod.POST,
                new HttpEntity<>(rfqReq, authHeaders(buyerToken)),
                RfqResponse.class);
        return rfqResp.getBody().getId();
    }

    private RfqResponse getRfq(Long rfqId) {
        return restTemplate.exchange(
                "/api/rfqs/" + rfqId, HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                RfqResponse.class).getBody();
    }

    private AuthResponse registerCompany(String name, String email, CompanyRole role) {
        CompanyRequest request = CompanyRequest.builder()
                .name(name).email(email).password("password123").role(role).build();
        return restTemplate.postForEntity("/api/auth/register", request, AuthResponse.class).getBody();
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
```

3. Run tests:

```bash
./mvnw test -Dtest=NegotiationIntegrationTest
```

4. Run the full test suite:

```bash
./mvnw test
```

5. Commit: `feat: wire negotiation engine into proposal service with auto-accept, counter, and reject`

---

## Phase 7: Final Integration

### Task 26 — OpenAPI config with Swagger UI metadata and endpoint grouping

**Files:**
- Create: `src/main/java/com/silentsupply/config/OpenApiConfig.java`

**Steps:**

1. Create `src/main/java/com/silentsupply/config/OpenApiConfig.java`:

```java
package com.silentsupply.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration.
 * Customizes the API documentation with metadata, JWT auth scheme, and endpoint grouping.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures the OpenAPI specification with JWT bearer auth and API metadata.
     *
     * @return the customized OpenAPI definition
     */
    @Bean
    public OpenAPI silentSupplyOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SilentSupply API")
                        .description("B2B supply chain marketplace with automated negotiation. "
                                + "Register as a supplier or buyer, manage products, place orders, "
                                + "submit RFQs, and let the negotiation engine handle the rest.")
                        .version("0.1.0")
                        .contact(new Contact()
                                .name("SilentSupply Team")
                                .email("team@silentsupply.com")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token obtained from /api/auth/login")));
    }
}
```

2. Verify by running the application and visiting `http://localhost:8080/swagger-ui.html` (manual check).

3. Commit: `chore: add OpenAPI config with JWT security scheme and API metadata`

---

### Task 27 — End-to-end integration test (full workflow)

**Files:**
- Test: `src/test/java/com/silentsupply/EndToEndIntegrationTest.java`

**Steps:**

1. Create `src/test/java/com/silentsupply/EndToEndIntegrationTest.java`:

```java
package com.silentsupply;

import com.silentsupply.common.BaseIntegrationTest;
import com.silentsupply.company.CompanyRepository;
import com.silentsupply.company.CompanyRole;
import com.silentsupply.company.dto.CompanyRequest;
import com.silentsupply.config.dto.AuthResponse;
import com.silentsupply.negotiation.NegotiationRuleRepository;
import com.silentsupply.negotiation.dto.NegotiationRuleRequest;
import com.silentsupply.negotiation.dto.NegotiationRuleResponse;
import com.silentsupply.order.CatalogOrderRepository;
import com.silentsupply.order.OrderStatus;
import com.silentsupply.order.dto.OrderRequest;
import com.silentsupply.order.dto.OrderResponse;
import com.silentsupply.order.dto.OrderStatusUpdate;
import com.silentsupply.product.ProductRepository;
import com.silentsupply.product.dto.ProductRequest;
import com.silentsupply.product.dto.ProductResponse;
import com.silentsupply.proposal.ProposalRepository;
import com.silentsupply.proposal.ProposalStatus;
import com.silentsupply.proposal.ProposerType;
import com.silentsupply.proposal.dto.ProposalRequest;
import com.silentsupply.proposal.dto.ProposalResponse;
import com.silentsupply.rfq.RfqRepository;
import com.silentsupply.rfq.RfqStatus;
import com.silentsupply.rfq.dto.RfqRequest;
import com.silentsupply.rfq.dto.RfqResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration test covering the full SilentSupply workflow:
 * 1. Register supplier and buyer
 * 2. Supplier lists a product and sets negotiation rules
 * 3. Buyer places a catalog order (direct purchase)
 * 4. Buyer submits an RFQ
 * 5. Negotiation engine auto-resolves the RFQ via proposals
 * 6. Verify final state of all entities
 */
class EndToEndIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProposalRepository proposalRepository;
    @Autowired
    private RfqRepository rfqRepository;
    @Autowired
    private NegotiationRuleRepository ruleRepository;
    @Autowired
    private CatalogOrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CompanyRepository companyRepository;

    @BeforeEach
    void cleanAll() {
        proposalRepository.deleteAll();
        rfqRepository.deleteAll();
        ruleRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    void fullWorkflow_catalogOrderAndRfqWithNegotiation() {
        // === Step 1: Register supplier and buyer ===
        AuthResponse supplierAuth = registerCompany("MegaSupplier Inc.", "supplier@megasupply.com", CompanyRole.SUPPLIER);
        String supplierToken = supplierAuth.getToken();
        Long supplierId = supplierAuth.getCompanyId();

        AuthResponse buyerAuth = registerCompany("GlobalBuyer Corp.", "buyer@globalbuyer.com", CompanyRole.BUYER);
        String buyerToken = buyerAuth.getToken();

        assertThat(supplierAuth.getRole()).isEqualTo("SUPPLIER");
        assertThat(buyerAuth.getRole()).isEqualTo("BUYER");

        // === Step 2: Supplier lists a product ===
        ProductRequest productReq = ProductRequest.builder()
                .name("Industrial Bearing XL-500")
                .description("High-performance industrial bearing for heavy machinery")
                .category("Industrial Parts")
                .sku("BRG-XL-500")
                .unitOfMeasure("piece")
                .basePrice(new BigDecimal("25.00"))
                .availableQuantity(500)
                .build();

        ResponseEntity<ProductResponse> productResp = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                new HttpEntity<>(productReq, authHeaders(supplierToken)),
                ProductResponse.class);

        assertThat(productResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long productId = productResp.getBody().getId();
        assertThat(productResp.getBody().getSupplierName()).isEqualTo("MegaSupplier Inc.");

        // === Step 3: Supplier sets negotiation rules ===
        NegotiationRuleRequest ruleReq = NegotiationRuleRequest.builder()
                .productId(productId)
                .priceFloor(new BigDecimal("18.00"))
                .autoAcceptThreshold(new BigDecimal("23.00"))
                .maxDeliveryDays(45)
                .maxRounds(3)
                .volumeDiscountPct(new BigDecimal("10.00"))
                .volumeThreshold(200)
                .build();

        ResponseEntity<NegotiationRuleResponse> ruleResp = restTemplate.exchange(
                "/api/suppliers/" + supplierId + "/negotiation-rules", HttpMethod.POST,
                new HttpEntity<>(ruleReq, authHeaders(supplierToken)),
                NegotiationRuleResponse.class);

        assertThat(ruleResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(ruleResp.getBody().getProductName()).isEqualTo("Industrial Bearing XL-500");

        // === Step 4: Buyer places a catalog order (direct purchase) ===
        OrderRequest orderReq = OrderRequest.builder()
                .productId(productId)
                .quantity(10)
                .build();

        ResponseEntity<OrderResponse> orderResp = restTemplate.exchange(
                "/api/orders", HttpMethod.POST,
                new HttpEntity<>(orderReq, authHeaders(buyerToken)),
                OrderResponse.class);

        assertThat(orderResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(orderResp.getBody().getQuantity()).isEqualTo(10);
        assertThat(orderResp.getBody().getUnitPrice()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(orderResp.getBody().getTotalPrice()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(orderResp.getBody().getStatus()).isEqualTo(OrderStatus.PLACED);
        Long orderId = orderResp.getBody().getId();

        // Supplier confirms the order
        OrderStatusUpdate confirmUpdate = OrderStatusUpdate.builder().status(OrderStatus.CONFIRMED).build();
        ResponseEntity<OrderResponse> confirmedResp = restTemplate.exchange(
                "/api/orders/" + orderId + "/status", HttpMethod.PATCH,
                new HttpEntity<>(confirmUpdate, authHeaders(supplierToken)),
                OrderResponse.class);
        assertThat(confirmedResp.getBody().getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        // === Step 5: Buyer submits an RFQ for a bulk order ===
        RfqRequest rfqReq = RfqRequest.builder()
                .productId(productId)
                .desiredQuantity(250)
                .targetPrice(new BigDecimal("20.00"))
                .deliveryDeadline(LocalDate.now().plusDays(60))
                .notes("Need bulk order for Q3 production run")
                .build();

        ResponseEntity<RfqResponse> rfqResp = restTemplate.exchange(
                "/api/rfqs", HttpMethod.POST,
                new HttpEntity<>(rfqReq, authHeaders(buyerToken)),
                RfqResponse.class);

        assertThat(rfqResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long rfqId = rfqResp.getBody().getId();
        assertThat(rfqResp.getBody().getStatus()).isEqualTo(RfqStatus.SUBMITTED);

        // === Step 6: Buyer submits first proposal — price in negotiable range ===
        // Volume discount applies: 10% off -> floor=16.20, threshold=20.70
        // Price 20.00 is below 20.70 threshold but above 16.20 floor -> COUNTERED
        ProposalRequest proposal1 = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("20.00"))
                .proposedQty(250)
                .deliveryDays(30)
                .build();

        ResponseEntity<ProposalResponse> prop1Resp = restTemplate.exchange(
                "/api/rfqs/" + rfqId + "/proposals", HttpMethod.POST,
                new HttpEntity<>(proposal1, authHeaders(buyerToken)),
                ProposalResponse.class);

        assertThat(prop1Resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(prop1Resp.getBody().getStatus()).isEqualTo(ProposalStatus.COUNTERED);
        assertThat(prop1Resp.getBody().getReasonCode()).isEqualTo("AUTO_COUNTERED");

        // Verify RFQ is in COUNTERED state
        RfqResponse rfqAfterCounter = getRfq(rfqId, buyerToken);
        assertThat(rfqAfterCounter.getStatus()).isEqualTo(RfqStatus.COUNTERED);
        assertThat(rfqAfterCounter.getCurrentRound()).isEqualTo(1);

        // Verify counter-proposal was generated
        ResponseEntity<ProposalResponse[]> proposalsResp = restTemplate.exchange(
                "/api/rfqs/" + rfqId + "/proposals", HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                ProposalResponse[].class);
        assertThat(proposalsResp.getBody()).hasSize(2);

        ProposalResponse counterProp = proposalsResp.getBody()[1];
        assertThat(counterProp.getProposerType()).isEqualTo(ProposerType.SYSTEM);
        assertThat(counterProp.getProposedPrice()).isEqualByComparingTo(new BigDecimal("20.70"));

        // === Step 7: Buyer accepts the counter by proposing at the counter price ===
        ProposalRequest proposal2 = ProposalRequest.builder()
                .proposedPrice(new BigDecimal("20.70"))
                .proposedQty(250)
                .deliveryDays(30)
                .build();

        ResponseEntity<ProposalResponse> prop2Resp = restTemplate.exchange(
                "/api/rfqs/" + rfqId + "/proposals", HttpMethod.POST,
                new HttpEntity<>(proposal2, authHeaders(buyerToken)),
                ProposalResponse.class);

        assertThat(prop2Resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(prop2Resp.getBody().getStatus()).isEqualTo(ProposalStatus.ACCEPTED);
        assertThat(prop2Resp.getBody().getReasonCode()).isEqualTo("AUTO_ACCEPTED");

        // === Step 8: Verify final state ===
        RfqResponse finalRfq = getRfq(rfqId, buyerToken);
        assertThat(finalRfq.getStatus()).isEqualTo(RfqStatus.ACCEPTED);
        assertThat(finalRfq.getCurrentRound()).isEqualTo(2);

        // Verify product stock was reduced by the catalog order
        ResponseEntity<ProductResponse> finalProduct = restTemplate.exchange(
                "/api/products/" + productId, HttpMethod.GET,
                new HttpEntity<>(authHeaders(supplierToken)),
                ProductResponse.class);
        assertThat(finalProduct.getBody().getAvailableQuantity()).isEqualTo(490);

        // Verify total proposals: 2 buyer + 1 system counter = 3
        ResponseEntity<ProposalResponse[]> finalProposals = restTemplate.exchange(
                "/api/rfqs/" + rfqId + "/proposals", HttpMethod.GET,
                new HttpEntity<>(authHeaders(buyerToken)),
                ProposalResponse[].class);
        assertThat(finalProposals.getBody()).hasSize(3);
    }

    private AuthResponse registerCompany(String name, String email, CompanyRole role) {
        CompanyRequest request = CompanyRequest.builder()
                .name(name).email(email).password("password123").role(role).build();
        return restTemplate.postForEntity("/api/auth/register", request, AuthResponse.class).getBody();
    }

    private RfqResponse getRfq(Long rfqId, String token) {
        return restTemplate.exchange(
                "/api/rfqs/" + rfqId, HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)),
                RfqResponse.class).getBody();
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
```

2. Run the end-to-end test:

```bash
./mvnw test -Dtest=EndToEndIntegrationTest
```

3. Run the full test suite to confirm nothing is broken:

```bash
./mvnw test
```

4. Commit: `test: add end-to-end integration test covering full marketplace workflow`

---

### Task 28 — Docker Compose, dev config, and README

**Files:**
- Create: `docker-compose.yml`
- Modify: `src/main/resources/application-dev.yml` (already exists, verify configuration)
- Create: `README.md`

**Steps:**

1. Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: silentsupply-db
    environment:
      POSTGRES_DB: silentsupply
      POSTGRES_USER: silentsupply
      POSTGRES_PASSWORD: silentsupply
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

2. Create `README.md`:

```markdown
# SilentSupply

B2B supply chain marketplace with automated negotiation.

## Prerequisites

- Java 21
- Maven 3.9+
- Docker & Docker Compose (for local PostgreSQL)

## Quick Start

1. Start PostgreSQL:

```bash
docker compose up -d
```

2. Run the application:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

3. Open Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Running Tests

Tests use TestContainers (requires Docker running):

```bash
./mvnw test
```

## API Overview

| Area | Endpoints |
|------|-----------|
| Auth | `POST /api/auth/register`, `POST /api/auth/login` |
| Companies | `POST /api/companies`, `GET /api/companies/{id}`, `GET /api/companies` |
| Products | CRUD at `/api/products`, search with filters |
| Orders | `POST /api/orders`, `GET /api/orders/{id}`, `PATCH /api/orders/{id}/status` |
| RFQs | `POST /api/rfqs`, `GET /api/rfqs/{id}`, `GET /api/rfqs` |
| Proposals | `POST /api/rfqs/{id}/proposals`, `GET /api/rfqs/{id}/proposals` |
| Rules | CRUD at `/api/suppliers/{id}/negotiation-rules` |

## Architecture

Spring Boot layered architecture with domain-driven packages:

```
com.silentsupply
├── config/        # Security, JWT, OpenAPI
├── common/        # Base entity, exceptions, error handling
├── company/       # Company registration & auth
├── product/       # Product catalog
├── order/         # Catalog orders (direct purchase)
├── rfq/           # RFQ lifecycle
├── proposal/      # Proposals within RFQ negotiations
└── negotiation/   # Rules engine & auto-negotiation
```
```

3. Run the full test suite one final time:

```bash
./mvnw test
```

4. Commit: `chore: add docker compose, dev config, and README with setup instructions`

---

## Summary

| Phase | Tasks | What's built |
|-------|-------|-------------|
| 1: Bootstrap | 1-3 | Maven project, Flyway schema, common package |
| 2: Company & Auth | 4-8 | Company CRUD, JWT auth (register + login), security config |
| 3: Product Catalog | 9-11 | Product CRUD, search/filter, supplier-only enforcement |
| 4: Catalog Orders | 12-14 | Order placement, stock validation, status transitions |
| 5: RFQ & Proposals | 15-20 | RFQ submission, proposals, round tracking |
| 6: Negotiation Engine | 21-25 | Rules CRUD, auto-accept/counter/reject, volume discounts |
| 7: Final Integration | 26-28 | Swagger docs, E2E test, Docker + README |

**Total: 28 tasks across 7 phases.**
