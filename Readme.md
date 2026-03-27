# CrimeLink Analyzer Backend

Spring Boot backend service for the CrimeLink platform. This service provides authentication, officer/admin APIs, crime reporting, duty scheduling, weapon and vehicle management, and integrations with AI microservices for call analysis and facial recognition.

## Tech Stack

- Java 21
- Spring Boot 3.5.8
- Spring Security (JWT-based auth)
- Spring Data JPA
- PostgreSQL (Supabase-compatible)
- Maven Wrapper (`./mvnw`)
- Docker (multi-stage build, JRE 21)

## Project Structure

- `src/main/java/com/crimeLink/analyzer`
  - `controller/` REST API endpoints
  - `service/` business logic
  - `repository/` database access
  - `entity/` JPA entities
  - `config/` security, JWT filter, CORS config
- `src/main/resources/application.properties` runtime configuration
- `.env.example` environment variable template
- `database/` SQL scripts for schema/data
- `Dockerfile` container build and runtime setup

## Prerequisites

1. Java 21 installed
2. PostgreSQL database available (local or cloud/Supabase)
3. Network access to required Python services:
   - Call Analysis service URL
   - Facial Recognition service URL
4. (Optional) Docker for containerized run

## Environment Setup

Create your environment file from template:

```bash
cp .env.example .env
```

Set values in `.env` (minimum required):

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `PYTHON_CALL_ANALYSIS_URL`
- `PYTHON_FACIAL_RECOGNITION_URL`
- `SUPABASE_URL`
- `SUPABASE_SERVICE_KEY`

Optional values:

- `SERVER_PORT` (default: `8080`)
- `JWT_EXPIRATION` (default: `900000`)
- `REFRESH_TOKEN_EXPIRATION` (default: `604800000`)
- `SUPABASE_BUCKET` (default: `criminal-photos`)
- `CORS_ALLOWED_ORIGINS` (default: `*`, restrict in production)

## Database Initialization

If you are setting up a fresh database, apply SQL scripts from `database/` as needed:

- `auth_tables.sql`
- `facial_recognition_tables.sql`
- `field_officers_data.sql`
- `test_data.sql`

Use your preferred SQL client or psql tool connected to your PostgreSQL database.

## Run Locally

From project root:

```bash
./mvnw clean spring-boot:run
```

Windows:

```powershell
mvnw.cmd clean spring-boot:run
```

The API starts on:

- `http://localhost:8080` (or your `SERVER_PORT`)

## Build and Test

Build:

```bash
./mvnw clean package
```

Run tests:

```bash
./mvnw test
```

Run packaged jar:

```bash
java -jar target/*.jar
```

## Run with Docker

Build image:

```bash
docker build -t crimelink-analyzer-backend .
```

Run container with environment file:

```bash
docker run --env-file .env -p 8080:8080 --name crimelink-backend crimelink-analyzer-backend
```

Health check endpoint:

- `GET /api/health`

## API Quick Start

Base URL:

- `http://localhost:8080`

Health:

```bash
curl http://localhost:8080/api/health
```

Test endpoint:

```bash
curl http://localhost:8080/api/test
```

Login example:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"your-email@example.com","password":"your-password"}'
```

If login succeeds, use the returned access token:

```bash
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

## Main API Modules

- `/api/auth` authentication and token refresh
- `/api/admin` admin users, settings, backups, audit logs
- `/api/crime-reports` report CRUD, map, evidence upload/download
- `/api/criminals` criminal profile management
- `/api/call-analysis` AI call analysis integration
- `/api/facial` AI facial recognition integration
- `/api/duty-schedules` OIC duty scheduling
- `/api/duties` mobile duty retrieval
- `/api/leaves` leave request workflow
- `/api/weapon` weapon inventory and issue flows
- `/api/vehicles` vehicle management
- `/api/users` officer/user listings
- `/api/safety-locations` safety location features
- `/api/announcements` officer announcements

## Security Notes

- JWT auth is enabled via Spring Security.
- Some endpoints are public (for example: auth, health checks, selected read APIs).
- Role-based authorization is enforced for many routes (`Admin`, `OIC`, `Investigator`, `FieldOfficer`).
- Restrict `CORS_ALLOWED_ORIGINS` in production.
- Never commit real secrets in `.env`.

## Integration Notes

- Call analysis and facial recognition features require external Python services to be running and reachable via configured URLs.
- File/image workflows use Supabase configuration values.

## Troubleshooting

- App fails on startup with datasource error:
  - Verify `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
- 401/403 for protected APIs:
  - Ensure valid `Authorization: Bearer <token>` and correct role permissions.
- AI endpoints fail:
  - Verify `PYTHON_CALL_ANALYSIS_URL` and `PYTHON_FACIAL_RECOGNITION_URL` are reachable.
- CORS issues in frontend:
  - Set `CORS_ALLOWED_ORIGINS` to your frontend origin(s), comma-separated.

## Useful Commands

```bash
# Dependency check and compile
./mvnw clean compile

# Run only tests
./mvnw test

# Build without tests
./mvnw clean package -DskipTests
```
