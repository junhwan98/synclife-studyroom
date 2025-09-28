# synclife-studyroom

PostgreSQL `tstzrange + EXCLUDE USING gist`로 **동시성에서도 겹침 예약을 DB 레벨에서 차단**하는 스터디룸 예약 백엔드.

## Stack
- Java 21, Spring Boot 3.5
- Spring Web, Data JPA, Validation, springdoc-openapi
- PostgreSQL 16, HikariCP
- Build: Gradle

## Run

### 0) DB
```bash
docker compose up -d
