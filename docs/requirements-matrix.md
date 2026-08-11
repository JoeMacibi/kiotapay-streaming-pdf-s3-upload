# Requirements evidence

| Requirement | Implementation |
|---|---|
| Async intake | `InvoiceController` returns 202 and publishes RabbitMQ job |
| Stable idempotency | `Idempotency-Key` maps to stable job/object key |
| Streaming PDF | `PdfUploader.writePdf` writes OpenPDF to a bounded `PipedOutputStream` |
| Multipart S3 | `PdfUploader` creates, uploads, completes, and aborts multipart uploads |
| Backpressure | bounded pipe and RabbitMQ prefetch/concurrency configuration |
| Failure cleanup | abort on any generation/upload failure; job marked failed |
| Observability | Actuator, Prometheus registry, custom job counters |
| Reproducibility | Maven, Dockerfile, Compose, CI workflow |

The repository intentionally keeps job state in memory and uses abort-and-retry rather than persisted multipart resume to minimize complexity for a single-instance assignment deployment.
