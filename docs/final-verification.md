# Final verification checklist

## Submission contents

- [x] Single Spring Boot Maven project
- [x] RabbitMQ asynchronous invoice intake
- [x] OpenPDF writes to a bounded pipe; no complete PDF byte array or temp file
- [x] S3-compatible multipart upload with a 5 MiB minimum part size
- [x] Abort-and-retry cleanup policy documented in `README.md`
- [x] Fixed worker concurrency and RabbitMQ prefetch configuration
- [x] Generic multipart logic is isolated in `PdfUploader` and can accept any generated stream
- [x] Docker Compose includes RabbitMQ, MinIO, bucket initialization, and the application
- [x] Interactive frontend at `/`
- [x] Actuator health/metrics/Prometheus endpoints and Swagger UI
- [x] Mandatory k6 script at `load/k6-smoke.js`
- [x] Requirements mapping at `docs/requirements-matrix.md`

## Presenter runbook

1. Install Java 21, Maven, Docker Compose, and optionally k6.
2. Run `docker compose up --build` from the repository root.
3. Open `http://localhost:8080/` and create an invoice.
4. Show the accepted job event, generated PDF event, and completed multipart upload event.
5. Show MinIO at `http://localhost:9001` using `minioadmin` / `minioadmin`; open the `invoices` bucket.
6. Open `http://localhost:8080/swagger-ui.html` and `http://localhost:8080/actuator/health`.
7. For load evidence, run `k6 run load/k6-smoke.js` and present the actual output. Do not claim results that were not run.

## Verification status in this environment

The source tree and static frontend were inspected successfully. Java, Maven, Docker, and k6 are not installed in the current execution environment, so the full build, Compose startup, and load test could not be executed here. The Docker build uses Maven/Java 21 images and CI is the required final compilation gate.

## Known scope limitation

Job state and idempotency state are in memory and therefore single-instance. For production scale-out, replace `InvoiceModels.Store` with a shared database/cache and persist multipart upload state before enabling resumable recovery.
