package com.kiotapay.invoice;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import static com.kiotapay.invoice.InvoiceModels.*;

@Service
public class PdfUploader {
  private final S3Client s3; private final String bucket; private final int partSize;
  public PdfUploader(S3Client s3,@Value("${app.bucket}") String bucket,@Value("${app.multipart-part-size}") int partSize){this.s3=s3;this.bucket=bucket;this.partSize=Math.max(5*1024*1024,partSize);}
  public void upload(InvoiceRequest invoice,String key) throws Exception {
    String uploadId=s3.createMultipartUpload(CreateMultipartUploadRequest.builder().bucket(bucket).key(key).contentType("application/pdf").build()).uploadId();
    List<CompletedPart> parts=new ArrayList<>(); int number=1;
    try(PipedInputStream in=new PipedInputStream(partSize); PipedOutputStream out=new PipedOutputStream(in)){
      Thread producer=Thread.startVirtualThread(()->{try{writePdf(invoice,out);}catch(Exception e){try{out.close();}catch(IOException ignored){}}});
      byte[] buffer=new byte[partSize]; int read; while((read=readChunk(in,buffer))>0){ UploadPartResponse response=s3.uploadPart(UploadPartRequest.builder().bucket(bucket).key(key).uploadId(uploadId).partNumber(number).contentLength((long)read).build(),RequestBody.fromBytes(Arrays.copyOf(buffer,read))); parts.add(CompletedPart.builder().partNumber(number++).eTag(response.eTag()).build()); }
      producer.join(); s3.completeMultipartUpload(CompleteMultipartUploadRequest.builder().bucket(bucket).key(key).uploadId(uploadId).multipartUpload(CompletedMultipartUpload.builder().parts(parts).build()).build());
    } catch(Exception e){ s3.abortMultipartUpload(AbortMultipartUploadRequest.builder().bucket(bucket).key(key).uploadId(uploadId).build()); throw e; }
  }
  private static int readChunk(InputStream in,byte[] b)throws IOException{int total=0,n; while(total<b.length && (n=in.read(b,total,b.length-total))>0) total+=n; return total;}
  static void writePdf(InvoiceRequest data,OutputStream out)throws Exception{ Document d=new Document(); PdfWriter.getInstance(d,out); d.open(); d.add(new Paragraph("KIOTAPAY INVOICE",FontFactory.getFont(FontFactory.HELVETICA_BOLD,18))); d.add(new Paragraph(data.customerName()+" <"+data.customerEmail()+">")); d.add(Chunk.NEWLINE); Table t=new Table(3, 1); t.addCell("Description");t.addCell("Qty");t.addCell("Unit price"); BigDecimal total=BigDecimal.ZERO; for(LineItem i:data.items()){t.addCell(i.description());t.addCell(String.valueOf(i.quantity()));t.addCell(i.unitPrice().toPlainString());total=total.add(i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())));} d.add(t); d.add(new Paragraph("Total: "+total.toPlainString())); d.close(); }
}
