# auth-service

Registration/login, JWT access + refresh token issuance/rotation, and device (FCM token)
registration.

## Endpoints
- `POST /auth/register` - email + password (+ optional displayName)
- `POST /auth/login` - email + password (+ optional deviceId/fcmToken)
- `POST /auth/refresh` - refreshToken (+ optional deviceId/fcmToken); rotates the token
- `POST /auth/devices` - deviceId + fcmToken; requires `Authorization: Bearer <accessToken>`

## Running locally
1. Start shared infra: `docker compose up -d postgres` (from the repo root).
2. Create the service's database once: `psql -h localhost -U rockydrive -c "CREATE DATABASE authdb;"`
   (password `rockydrive`; `spring.jpa.hibernate.ddl-auto=update` creates the tables after that).
3. Set `JWT_SECRET` to a real 256-bit+ secret (the default in `application.yml` is a placeholder).
   Every other service must be configured with the same value to validate access tokens locally.
4. `mvn -pl auth-service -am spring-boot:run` (from the repo root).

Runs on port `8081`.
