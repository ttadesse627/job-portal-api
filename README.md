# Job Portal

A Spring Boot REST API for managing a simple job marketplace where employers publish jobs, candidates apply with resumes, and administrators oversee users and registrations.

The project focuses on role-based access control, job lifecycle management, candidate applications, and PostgreSQL-backed persistence.

## What the Project Does

This application supports three main roles:

- `ADMIN`: views registered users, employers, and candidates
- `EMPLOYER`: registers, creates job posts, publishes/closes jobs, and reviews applicants
- `CANDIDATE`: registers with a resume, browses jobs, applies, and tracks applications

Core capabilities:

- employer registration
- candidate registration with resume upload
- optional resume parsing via SharpAPI when configured
- HTTP Basic authentication with Spring Security
- job creation, update, publish, close, delete, and listing
- job filtering by title, position, location, type, and date range
- application submission and withdrawal
- application status updates by employer or admin
- OpenAPI documentation via Scalar
- PostgreSQL persistence with Spring Data JPA

## Tech Stack

- Java `25`
- Spring Boot `4.0.4`
- Spring Web MVC
- Spring Data JPA
- Spring Security
- PostgreSQL
- Springdoc OpenAPI + Scalar UI
- Gradle Kotlin DSL
- Lombok

## Project Structure

```text
src/main/java/et/gov/osta/jobportal
|-- configs        # security and OpenAPI configuration
|-- controllers    # REST endpoints
|-- data           # startup seed data
|-- domain         # entities, enums, repositories
|-- dtos           # request and response contracts
|-- exceptions     # API exception handling
|-- helpers        # specifications and security helpers
|-- services       # business logic
|-- utils          # pagination helpers
```

Useful supporting files:

- `docker-compose.yaml`: starts PostgreSQL
- `src/main/resources/application.yaml`: app and database configuration
- `src/main/resources/testfiles/*.http`: IntelliJ HTTP client samples for manual API testing

## Prerequisites

Before running the project, make sure you have:

- JDK `25`
- Docker and Docker Compose, or a running PostgreSQL instance
- permission to use port `5432` for PostgreSQL and `8080` for the API

## Configuration

Current database settings from `application.yaml`:

- database: `job_portal`
- username: `postgres`
- password: `postgres`
- JDBC URL: `jdbc:postgresql://localhost:5432/job_portal`

Resume parser settings:

- provider: `SharpAPI Resume Parsing`
- parser mode: multipart upload plus status polling
- required config: API token

Important runtime note:

- `spring.jpa.hibernate.ddl-auto=create-drop` is enabled
- the schema is recreated on startup and dropped on shutdown
- data is not persistent across application restarts unless you change this setting

## Setup and Run

### 1. Start PostgreSQL

Using Docker Compose:

```bash
docker compose up -d
```

This starts a PostgreSQL container named `job-portal-db` on port `5432`.

### 2. Run the application

Using the Gradle wrapper:

```bash
./gradlew bootRun
```

The API will start on:

```text
http://localhost:8080
```

### 3. Run tests

```bash
./gradlew test
```

## API Documentation

After the application starts, open:

- Scalar UI: `http://localhost:8080/scalar`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`

## Default Seeded Admin Account

On startup, the application seeds one admin user if it does not already exist:

- email: `tola@gmail.com`
- password: `tola@123`

Use this account for admin-only endpoints.

## Authentication and Authorization

The API uses HTTP Basic authentication.

Example header:

```text
Authorization: Basic <base64(email:password)>
```

Role access summary:

- public access includes `POST /api/employers`, `POST /api/candidates`, `GET /api/jobs`, and the documentation endpoints
- admin access includes listing users, employers, and candidates
- employer access includes creating and managing jobs, viewing applicants, and updating application status
- candidate access includes applying to jobs, viewing personal applications, and withdrawing applications

## Main Endpoints

### Employer

- `POST /api/employers` - register a new employer
- `GET /api/employers` - list employers (`ADMIN`)
- `GET /api/employers/{id}` - get employer by id (`ADMIN` or owner)
- `PUT /api/employers/{id}` - update employer (`EMPLOYER`)

### Candidate

- `POST /api/candidates` - register candidate with multipart form data and resume
- `GET /api/candidates` - list candidates (`ADMIN`)
- `GET /api/candidates/{id}` - get candidate by id (`ADMIN` or owner)
- `PUT /api/candidates/{id}` - update candidate (`EMPLOYER` in the current security configuration)

### Job

- `POST /api/jobs` - create a job (`EMPLOYER`)
- `PUT /api/jobs/{id}` - update a job (`EMPLOYER`)
- `PATCH /api/jobs/{id}/publish` - publish a draft job (`EMPLOYER`)
- `PATCH /api/jobs/{id}/close` - close a job (`EMPLOYER`)
- `DELETE /api/jobs/{id}` - delete a job (`EMPLOYER`)
- `GET /api/jobs` - list/filter jobs
- `GET /api/jobs/{id}` - view job details

### Application

- `POST /api/applications/jobs/{jobId}` - apply to a job (`CANDIDATE`)
- `GET /api/applications/me` - list current candidate applications
- `GET /api/applications/job/{jobId}` - list applicants for a job (`EMPLOYER` or `ADMIN`)
- `PATCH /api/applications/{id}/status?status=SHORTLISTED` - update application status (`EMPLOYER` or `ADMIN`)
- `PATCH /api/applications/{id}/withdraw` - withdraw an application (`CANDIDATE`)

### User

- `GET /api/users` - list users (`ADMIN`)
- `GET /api/users/{id}` - get user by id (`ADMIN` or owner)
- `GET /api/users/me` - get current authenticated user id

## Example Requests

### Register an employer

```http
POST /api/employers
Content-Type: application/json

{
  "companyName": "Oromia Science and Technology Authority",
  "user": {
    "phoneNumbers": ["+251150100001", "+251150100002"],
    "email": "info@osta.gov.et",
    "password": "osta@123"
  }
}
```

### Create a job as an employer

```http
POST /api/jobs
Authorization: Basic info@osta.gov.et osta@123
Content-Type: application/json

{
  "title": "Senior Data Scientist",
  "position": "Sr. Data Scientist",
  "location": "Addis Ababa",
  "jobType": "FULL_TIME",
  "description": "We are seeking a talented Data Scientist to join our AI research team.",
  "companyName": "DataDriven PLC",
  "salaryMin": 40000,
  "salaryMax": 55000,
  "deadline": "2026-04-30"
}
```

### Register a candidate

Candidate registration expects `multipart/form-data` and a resume file.

Expected form fields:

- `firstName`
- `lastName`
- `phoneNumbers`
- `email`
- `password`
- `resume`

If `resume-parser.apy-hub.enabled=true`, the uploaded resume is sent directly to SharpAPI and the app polls the returned job status URL for the final parsed result.

## Validation Notes

- email must be valid
- password length must be between `6` and `10` characters
- password must include uppercase, lowercase, a digit, and a special character
- candidate first and last names must be between `3` and `55` characters
- candidate registration requires a resume file
- job creation/update requires an application deadline that is today or later

## File Uploads

Candidate resumes are stored locally under:

```text
./uploads/resume/
```

This means:

- uploaded files are stored on the application host filesystem
- the uploads directory should be writable
- local file storage is suitable for development, not ideal for production

## Development Notes

- the project includes sample HTTP request files under `src/main/resources/testfiles/`
- OpenAPI security is configured for Basic Auth
- security debug logging is enabled in the current configuration
- only a basic context-load test currently exists

## Suggested Improvements

If this project is intended for production or team use, the next useful improvements would be:

- move secrets to environment variables
- replace `create-drop` with a migration-based strategy such as Flyway or Liquibase
- add controller and service tests
- add JWT or session-based auth if needed by frontend clients
- store resumes in object storage instead of local disk
- add profile-specific configuration for local, test, and production environments

## License

Add a license section if you plan to share or publish the project publicly.
