# Employee Management API

REST API for managing employees with Spring Boot, Spring Web, Spring Data JPA, Hibernate, H2, Bean Validation, and Springdoc OpenAPI.

The database is an in-memory H2 database. It is intentionally ephemeral: all data disappears when the application shuts down.

## Technology Stack

- Java 21
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Data JPA
- Hibernate
- H2 Database
- Jakarta Bean Validation
- Springdoc OpenAPI 3.0.3
- Maven
- JUnit 5, Spring Boot Test, Mockito, MockMvc

## Project Structure

```text
src/
 ├── main/
 │   ├── java/
 │   │   └── com/example/employee/
 │   │       ├── EmployeeApplication.java
 │   │       ├── controller/EmployeeController.java
 │   │       ├── service/EmployeeService.java
 │   │       ├── service/EmployeeServiceImpl.java
 │   │       ├── repository/EmployeeRepository.java
 │   │       ├── entity/Employee.java
 │   │       ├── dto/EmployeeRequest.java
 │   │       ├── dto/EmployeeResponse.java
 │   │       ├── exception/EmployeeNotFoundException.java
 │   │       ├── exception/ApiError.java
 │   │       ├── exception/GlobalExceptionHandler.java
 │   │       └── config/OpenApiConfig.java
 │   └── resources/
 │       ├── application-base.yml
 │       ├── application-staging.yml
 │       ├── application-prod.yml
 │       └── data.sql
 └── test/
     └── java/
         └── com/example/employee/
             ├── controller/EmployeeControllerTest.java
             ├── service/EmployeeServiceImplTest.java
             └── repository/EmployeeRepositoryTest.java
```

## Prerequisites

- Java 21 or newer LTS-compatible runtime
- Maven 3.6.3 or newer

## Build

```bash
mvn clean install
```

## Run

```bash
mvn spring-boot:run
```

The application starts on `http://localhost:8080`.

Run with the packaged staging or production config:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.config.location=classpath:application-staging.yml
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.config.location=classpath:application-prod.yml
```

Run with a specific YAML config file:

```bash
java -jar target/employee-service-0.0.1-SNAPSHOT.jar --spring.config.location=classpath:application-staging.yml
java -jar target/employee-service-0.0.1-SNAPSHOT.jar --spring.config.location=file:/path/to/application-staging.yml
```

Run with the local profile:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.config.location=classpath:application-local.yml
java -jar target/employee-service-0.0.1-SNAPSHOT.jar --spring.config.location=classpath:application-local.yml
```

`application-local.yml` imports `application-base.yml`, then overrides S3 seed loading to `enabled: false`.

## Docker

Build the Ubuntu-based image:

```bash
docker build -t employee-service:latest .
```

Run it on HTTP port `8080`, mount a host log directory into the container, and pass a YAML config file:

```bash
mkdir -p logs
docker run --rm \
  --name employee-service \
  -p 8080:8080 \
  -v "$(pwd)/logs:/tmp/employee-service" \
  -v /path/to/application-staging.yml:/config/application.yml:ro \
  employee-service:latest \
  --spring.config.location=file:/config/application.yml
```

Application logs are written to `/tmp/employee-service/employee-service.log` inside the container, which maps to `./logs/employee-service.log` on the host in the example above. The image uses `/tmp/employee-service` by default to avoid permission errors when the container is run with a non-root user or restricted root filesystem.

To use the packaged staging or production YAML instead of mounting an external file:

```bash
docker run --rm \
  --name employee-service \
  -p 8080:8080 \
  -v "$(pwd)/logs:/tmp/employee-service" \
  employee-service:latest \
  --spring.config.location=classpath:application-staging.yml
```

To write logs to a different mounted directory, override `LOG_FILE` and mount the same directory into the container:

```bash
docker run --rm \
  --name employee-service \
  -p 8080:8080 \
  -e LOG_FILE=/app/logs/employee-service.log \
  -v "$(pwd)/logs:/app/logs" \
  employee-service:latest
```

## H2 Database

- JDBC URL: `jdbc:h2:mem:employeedb`
- Username: `sa`
- Password: empty
- H2 console: `http://localhost:8080/h2-console`

JPA/Hibernate creates the schema automatically, then Spring SQL initialization loads employee seed data.
Local/default runs still use the classpath `data.sql`; staging and production download that file from S3 before SQL initialization.

S3 seed-data settings are read from YAML under `employee.sql-init.s3` and can be overridden with environment variables:

| Variable | Description |
| --- | --- |
| `DATA_SQL_S3_ENABLED` | Enable or disable S3 download. Defaults to `true` in staging/prod. |
| `DATA_SQL_S3_BUCKET` | S3 bucket containing the SQL file. |
| `DATA_SQL_S3_KEY` | S3 object key. Defaults to `data.sql`. |
| `AWS_REGION` / `AWS_DEFAULT_REGION` | AWS region used by the S3 client. |
| `DATA_SQL_DOWNLOAD_PATH` | Local startup download target. |

Credentials are resolved by the AWS SDK default credentials provider chain.

## API Documentation

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Endpoints

| Method | Path | Description | Success |
| --- | --- | --- | --- |
| POST | `/api/v1/employees` | Create an employee | `201 Created` |
| GET | `/api/v1/employees/{id}` | Get an employee by id | `200 OK` |
| GET | `/api/v1/employees` | Get all employees | `200 OK` |
| PUT | `/api/v1/employees/{id}` | Update an employee | `200 OK` |
| DELETE | `/api/v1/employees/{id}` | Delete an employee | `204 No Content` |

Errors use a consistent JSON response with `timestamp`, `status`, `error`, `message`, `path`, and validation-specific `fieldErrors`.

## Curl Examples

Create:

```bash
curl -i -X POST http://localhost:8080/api/v1/employees \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Smith",
    "email": "john.smith@example.com",
    "phoneNumber": "212-555-1234",
    "department": "Engineering",
    "jobTitle": "Software Engineer",
    "salary": 150000,
    "hireDate": "2026-01-15"
  }'
```

Get one:

```bash
curl -i http://localhost:8080/api/v1/employees/1
```

Get all:

```bash
curl -i http://localhost:8080/api/v1/employees
```

Update:

```bash
curl -i -X PUT http://localhost:8080/api/v1/employees/1 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Smith",
    "email": "john.smith@example.com",
    "phoneNumber": "212-555-1234",
    "department": "Engineering",
    "jobTitle": "Staff Software Engineer",
    "salary": 175000,
    "hireDate": "2026-01-15"
  }'
```

Delete:

```bash
curl -i -X DELETE http://localhost:8080/api/v1/employees/1
```

Validation failure:

```bash
curl -i -X POST http://localhost:8080/api/v1/employees \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "",
    "lastName": "Smith",
    "email": "invalid",
    "salary": -1
  }'
```

## Tests

```bash
mvn test
```

The test suite covers repository persistence and uniqueness, service CRUD behavior and not-found handling, and controller HTTP behavior with MockMvc.

## Architecture

The API follows a Controller -> Service -> Repository -> H2 Database flow.

- Controllers handle HTTP requests, validation, response status codes, and `Location` headers.
- Services own business logic, entity/DTO mapping, uniqueness checks, and not-found handling.
- Repositories provide persistence through Spring Data JPA.
- JPA entities are not exposed directly from REST responses.
- `@RestControllerAdvice` centralizes exception handling and avoids leaking internal details.
