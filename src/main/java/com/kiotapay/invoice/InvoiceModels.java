package com.kiotapay.invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class InvoiceModels {
  private InvoiceModels() {}
  public record LineItem(@NotBlank String description, @Min(1) int quantity, @DecimalMin("0.00") BigDecimal unitPrice) {}
  public record InvoiceRequest(@NotBlank String customerName, @NotBlank String customerEmail, @NotEmpty List<@Valid LineItem> items) {}
  public record InvoiceJob(String id, InvoiceRequest invoice, String status, String objectKey, String error, Instant updatedAt) {}
  public static class Store { public final ConcurrentHashMap<String, InvoiceJob> jobs = new ConcurrentHashMap<>(); }
}
