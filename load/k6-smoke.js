import http from 'k6/http';
import { check, sleep } from 'k6';
export const options = { vus: 1, duration: '10s' };
export default function () {
  const body = JSON.stringify({customerName:'Load Test',customerEmail:'load@example.com',items:[{description:'Service',quantity:1,unitPrice:10.00}]});
  const res = http.post('http://localhost:8080/api/v1/invoices', body, {headers:{'Content-Type':'application/json','Idempotency-Key':`k6-${__VU}-${__ITER}`}});
  check(res, {'accepted': r => r.status === 202}); sleep(1);
}
