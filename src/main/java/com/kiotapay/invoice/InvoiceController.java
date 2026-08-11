package com.kiotapay.invoice;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import static com.kiotapay.invoice.InvoiceModels.*;

@RestController
@RequestMapping("/api/v1")
public class InvoiceController {
  private final Store store; private final InvoiceWorker worker;
  public InvoiceController(Store store, InvoiceWorker worker) { this.store = store; this.worker = worker; }
  @PostMapping("/invoices")
  public ResponseEntity<InvoiceJob> create(@Valid @RequestBody InvoiceRequest request, @RequestHeader(value="Idempotency-Key", required=false) String key) {
    String id = key == null || key.isBlank() ? UUID.randomUUID().toString() : key;
    InvoiceJob job = store.jobs.computeIfAbsent(id, k -> new InvoiceJob(k, request, "QUEUED", "invoices/"+k+".pdf", null, Instant.now()));
    if (job.status().equals("QUEUED")) worker.enqueue(id);
    return ResponseEntity.accepted().location(URI.create("/api/v1/jobs/"+id)).body(job);
  }
  @GetMapping("/jobs/{id}") public ResponseEntity<InvoiceJob> status(@PathVariable String id) { return ResponseEntity.of(java.util.Optional.ofNullable(store.jobs.get(id))); }
}
