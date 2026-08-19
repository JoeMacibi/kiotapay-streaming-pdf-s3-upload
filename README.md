# KiotaPay streaming invoice service

Java 21 / Spring Boot service that accepts invoice jobs asynchronously, generates PDFs directly into a bounded pipe, and uploads them to S3-compatible storage with multipart upload.

## Run

```bash
mvn clean verify
# or
docker compose up --build
```

POST JSON to `/api/v1/invoices` with `customerName`, `customerEmail`, and `items` (`description`, `quantity`, `unitPrice`). The endpoint returns `202 Accepted`; poll `/api/v1/jobs/{id}`. Swagger is at `/swagger-ui.html`, health at `/actuator/health`, Prometheus metrics at `/actuator/prometheus`, and the interactive operations console is served at `/`.

The frontend works against the live API when the service is running and automatically falls back to a clearly labeled demo mode when the API is unavailable, so the invoice flow can still be presented locally.

The implementation intentionally uses an in-memory job store for the simplest single-instance deployment. Failed multipart uploads are aborted and retried by RabbitMQ policy rather than persisted-resumed. MinIO credentials and endpoint are configurable via environment variables.
