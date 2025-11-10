# Grafana Dashboard Quick Start Guide

## 🚀 Access Your Dashboards

### URLs
- **Grafana**: http://localhost:3000
- **Prometheus**: http://localhost:9090
- **Order Service**: http://localhost:8081
- **Routing Service**: http://localhost:8082

### Login Credentials
- **Username**: `admin`
- **Password**: `admin`

---

## 📊 Available Dashboards

### 1. **Order Service Dashboard**

**Tracks:**
- ✅ API throughput (requests/sec) by endpoint
- ✅ Latency percentiles (P50, P95, P99)
- ✅ Success/error rates
- ✅ Database connection pool metrics
- ✅ Database query performance
- ✅ Redis cache hit/miss rates
- ✅ JVM memory and thread usage
- ✅ Error distribution by status code

**Key Panels:**
- Request Rate Gauge
- API Throughput by Endpoint (time series)
- API Latency by Endpoint (P50, P95, P99)
- Database Connection Pool
- Cache Hit Rate
- JVM Memory Usage

**File**: `infrastructure/grafana/dashboards/order-service-dashboard.json`

---

### 2. **Routing Service Dashboard**

**Tracks:**
- ✅ API throughput and latency
- ✅ Route optimization time by algorithm (EXACT_DP vs CHRISTOFIDES)
- ✅ Optimization rate (optimizations/sec)
- ✅ Route distance distribution (km)
- ✅ Route time distribution (minutes)
- ✅ Average orders per route
- ✅ Database and cache metrics
- ✅ Circuit breaker status (Resilience4j)
- ✅ JVM metrics

**Key Panels:**
- Route Optimization P95 Latency
- Avg Orders Per Route
- Optimization Time by Algorithm
- Optimization Rate by Algorithm
- Route Distance Distribution
- Route Time Distribution
- Circuit Breaker Status

**File**: `infrastructure/grafana/dashboards/routing-service-dashboard.json`

---

## 🔧 Quick Setup

### Option 1: Automatic (Provisioned)

Dashboards are automatically loaded on Grafana startup!

1. Access Grafana: http://localhost:3000
2. Login with `admin` / `admin`
3. Navigate to **Dashboards** → **Browse**
4. Look for **"Delivery Services"** folder
5. Click on:
   - **Order Service - Performance Dashboard**
   - **Routing Service - Performance Dashboard**

### Option 2: Manual Import

If dashboards aren't auto-loaded:

1. Go to Grafana: http://localhost:3000
2. Click **"+" (Create)** → **"Import"**
3. Click **"Upload JSON file"**
4. Select one of:
   - `infrastructure/grafana/dashboards/order-service-dashboard.json`
   - `infrastructure/grafana/dashboards/routing-service-dashboard.json`
5. Click **"Load"**
6. Select **"Prometheus"** as data source
7. Click **"Import"**

---

## 📈 Key Metrics to Monitor

### Order Service

**API Performance:**
```
- Request Rate: ~10-100 req/s (normal)
- P95 Latency: <200ms (good), <500ms (acceptable)
- Success Rate: >99% (target)
```

**Database:**
```
- Active Connections: <10 (normal)
- Connection Acquire Time: <50ms (good)
```

**Cache:**
```
- Hit Rate: >80% (target)
- Miss Rate: <20%
```

### Routing Service

**Optimization Performance:**
```
- P95 Optimization Time:
  * EXACT_DP: <100ms (for ≤10 orders)
  * CHRISTOFIDES: <500ms (for >10 orders)
- Optimization Rate: 1-10 opt/s
```

**Route Quality:**
```
- Route Distance: 10-30 km (typical)
- Route Time: 30-90 minutes (typical)
- Orders per Route: 3-7 (typical)
```

---

## 🎯 Common Use Cases

### 1. Monitor Real-Time Performance

**Dashboard**: Both dashboards, top panels
- Check request rate gauges
- Monitor P95 latency
- Watch success rate

### 2. Investigate Latency Issues

**Dashboard**: API Latency by Endpoint panel
- Identify slow endpoints
- Compare P50 vs P95 vs P99
- Look for spikes or trends

### 3. Optimize Route Algorithm Selection

**Dashboard**: Routing Service → Optimization Time by Algorithm
- Compare EXACT_DP vs CHRISTOFIDES performance
- Adjust `exact-algorithm-threshold` based on data

### 4. Database Performance Tuning

**Dashboard**: Database Connection Pool & Query Performance
- Check for connection exhaustion
- Monitor query times
- Tune HikariCP pool size if needed

### 5. Cache Efficiency Analysis

**Dashboard**: Cache Operations & Hit Rate
- Monitor hit rate over time
- Identify frequently missed keys
- Adjust TTL if needed

---

## 🔍 Prometheus Query Examples

Access Prometheus: http://localhost:9090/graph

### Request Rate per Endpoint
```promql
sum(rate(http_server_requests_seconds_count{job="order-service"}[5m])) by (uri)
```

### P95 Latency
```promql
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{job="routing-service"}[5m])) by (le, uri)) * 1000
```

### Success Rate
```promql
sum(rate(http_server_requests_seconds_count{status!~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count[5m]))
```

### Cache Hit Rate
```promql
rate(cache_gets_total{result="hit"}[5m]) / (rate(cache_gets_total{result="hit"}[5m]) + rate(cache_gets_total{result="miss"}[5m]))
```

---

## 🚨 Troubleshooting

### Dashboards Not Showing Data

1. **Check Prometheus is scraping:**
   ```bash
   curl http://localhost:9090/api/v1/targets
   ```
   Both `order-service` and `routing-service` should show `"health": "up"`

2. **Check service metrics:**
   ```bash
   curl http://localhost:8081/actuator/prometheus | grep http_server
   curl http://localhost:8082/actuator/prometheus | grep http_server
   ```

3. **Restart services:**
   ```bash
   cd infrastructure
   docker compose restart order-service routing-service
   ```

### "No Data" in Panels

- Check time range (top right) - set to "Last 1 hour"
- Generate some traffic by running `./test-api.sh`
- Wait 15-30 seconds for metrics to be scraped

### Grafana Can't Connect to Prometheus

```bash
# Test from Grafana container
docker exec delivery-grafana curl http://prometheus:9090/-/healthy

# Should return: Prometheus Server is Healthy.
```

---

## 📝 Customization Tips

### Change Refresh Rate
- Dashboard Settings (gear icon) → Time Options → Refresh: `5s`, `10s`, `30s`, etc.

### Add New Panels
1. Click **"Add panel"** (top right)
2. Select **"Add a new panel"**
3. Write PromQL query
4. Choose visualization type
5. **Save dashboard**

### Adjust Thresholds
- Edit panel → Field tab → Thresholds
- Set colors: Green (good), Yellow (warning), Red (critical)

---

## 📚 Additional Resources

- Full Documentation: `infrastructure/MONITORING.md`
- Prometheus Queries: http://localhost:9090
- Service Metrics: 
  - http://localhost:8081/actuator/prometheus
  - http://localhost:8082/actuator/prometheus

---

## ✅ Current Status

```
✅ Prometheus: Running on :9090
✅ Grafana: Running on :3000
✅ Order Service Metrics: Collecting
✅ Routing Service Metrics: Collecting
✅ Dashboards: Pre-configured
✅ Auto-refresh: Every 10 seconds
```

**Ready to monitor!** 🎉

Access Grafana now: **http://localhost:3000** (admin/admin)

