package com.kiotapay.invoice;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.concurrent.*;
import static com.kiotapay.invoice.InvoiceModels.*;

@Configuration
class RabbitConfig { @Bean Store store(){ return new Store(); } @Bean Queue invoiceQueue(@Value("${app.queue}") String q){ return new Queue(q, true); } }

@Service
public class InvoiceWorker {
  private final Store store; private final RabbitTemplate rabbit; private final PdfUploader uploader; private final MeterRegistry metrics; private final String queue;
  public InvoiceWorker(Store s, RabbitTemplate r, PdfUploader u, MeterRegistry m, @Value("${app.queue}") String q){store=s;rabbit=r;uploader=u;metrics=m;queue=q;}
  public void enqueue(String id){ rabbit.convertAndSend(queue,id); metrics.counter("invoice.accepted").increment(); }
  @RabbitListener(queues="#{'${app.queue}'}")
  public void process(String id){ InvoiceJob old=store.jobs.get(id); if(old==null || "SUCCEEDED".equals(old.status())) return; store.jobs.put(id,new InvoiceJob(id,old.invoice(),"PROCESSING",old.objectKey(),null,Instant.now()));
    try { uploader.upload(old.invoice(),old.objectKey()); store.jobs.put(id,new InvoiceJob(id,old.invoice(),"SUCCEEDED",old.objectKey(),null,Instant.now())); metrics.counter("invoice.succeeded").increment(); }
    catch(Exception e){ store.jobs.put(id,new InvoiceJob(id,old.invoice(),"FAILED",old.objectKey(),e.getMessage(),Instant.now())); metrics.counter("invoice.failed").increment(); }
  }
}
