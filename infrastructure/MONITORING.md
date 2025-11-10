# Monitoring and Observability Setup

This document describes the monitoring infrastructure for the Delivery Route Optimization System.

## Overview

The system uses **Prometheus** for metrics collection and **Grafana** for visualization.

## Architecture

```
┌─────────────────┐
│ Order Service   │───┐
│ :8081           │   │
└─────────────────┘   │
                      ├──> ┌────────────┐      ┌──────────┐
┌─────────────────┐   │    │ Prometheus │─────>│ Grafana  │
│ Routing Service │───┘    │ :9090      │      │ :3000    │
│ :8082           │        └────────────┘      └──────────┘
└─────────────────┘
```

## Components

### 1. **Prometheus** (Port 9090)
- Scrapes metrics from services every 15 seconds
- Stores time-series data
- Provides PromQL query language

### 2. **Grafana** (Port 3000)
- Visualizes Prometheus metrics
- Pre-configured dashboards
- **Login**: admin / admin

## Quick Start

### Start Monitoring Stack

```bash
cd infrastructure
docker compose up -d prometheus grafana
```

### Access Dashboards

1. **Grafana**: http://localhost:3000
   - Username: `admin`
   - Password: `admin`
   
2. **Prometheus**: http://localhost:9090

3. **Service Metrics**:
   - Order Service: http://localhost:8081/actuator/prometheus
   - Routing Service: http://localhost:8082/actuator/prometheus

## Available Dashboards

### 1. Order Service Dashboard

**Metrics Tracked:**

#### API Performance
- **Request Rate**: Total requests/second across all endpoints
- **Throughput by Endpoint**: Requests/second per API endpoint
- **Latency Percentiles**: P50, P95, P99 response times by endpoint
- **Success Rate**: Percentage of non-5xx responses
- **Error Rate**: 5xx errors by status code

#### Database Performance
- **Connection Pool**: Active, idle, and pending connections
- **Query Performance**: P95 connection acquire time and usage time
- **Connection Health**: HikariCP metrics

#### Cache Performance (Redis)
- **Cache Operations**: Hits, misses, puts, evictions
- **Cache Hit Rate**: Percentage of successful cache hits
- **Cache Efficiency**: Hit/miss ratio over time

#### JVM Metrics
- **Memory Usage**: Heap and non-heap memory utilization
- **Thread Count**: Live, daemon, and peak threads
- **GC Activity**: Garbage collection metrics

### 2. Routing Service Dashboard

**Metrics Tracked:**

#### API Performance
- **Request Rate**: Total requests/second
- **Throughput by Endpoint**: Per-endpoint request rates
- **Latency Percentiles**: P50, P95, P99 response times
- **Success Rate**: Non-5xx response percentage

#### Route Optimization Metrics
- **Optimization Latency**: P50, P95, P99 optimization time by algorithm
  - EXACT_DP (Dynamic Programming)
  - CHRISTOFIDES (Heuristic)
- **Optimization Rate**: Optimizations/second by algorithm
- **Route Distance**: Distribution of optimized route distances (km)
- **Route Time**: Distribution of optimized route times (minutes)
- **Average Orders per Route**: Mean orders per batch

#### Database & Cache
- **Connection Pool**: Active, idle, pending connections
- **Query Performance**: Connection metrics
- **Cache Operations**: Redis hits, misses, puts
- **Cache Hit Rate**: Cache efficiency

#### JVM & Resilience
- **Memory Usage**: Heap and non-heap metrics
- **Thread Count**: Thread pool utilization
- **Circuit Breaker**: Resilience4j circuit breaker states

## Metrics Reference

### Common HTTP Metrics

```promql
# Request rate
rate(http_server_requests_seconds_count{job="order-service"}[5m])

# P95 latency
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{job="order-service"}[5m])) by (le, uri))

# Success rate
sum(rate(http_server_requests_seconds_count{status!~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count[5m]))
```

### Routing-Specific Metrics

```promql
# Optimization time by algorithm
histogram_quantile(0.95, sum(rate(route_optimization_time_ms_bucket[5m])) by (le, algorithm))

# Route distance
histogram_quantile(0.95, sum(rate(optimized_route_distance_km_bucket[5m])) by (le))

# Orders per route
avg(optimized_route_orders_count)
```

### Database Metrics

```promql
# Active connections
hikaricp_connections_active{job="order-service"}

# Connection acquire time
histogram_quantile(0.95, sum(rate(hikaricp_connections_acquire_seconds_bucket[5m])) by (le))
```

### Cache Metrics

```promql
# Cache hit rate
rate(cache_gets_total{result="hit"}[5m]) / (rate(cache_gets_total{result="hit"}[5m]) + rate(cache_gets_total{result="miss"}[5m]))
```

## Alert Rules (Recommended)

### High Error Rate
```yaml
- alert: HighErrorRate
  expr: sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count[5m])) > 0.05
  for: 5m
  annotations:
    summary: "High error rate detected"
```

### High Latency
```yaml
- alert: HighLatency
  expr: histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le)) > 1
  for: 5m
  annotations:
    summary: "P95 latency above 1 second"
```

### Database Connection Pool Exhaustion
```yaml
- alert: DatabasePoolExhausted
  expr: hikaricp_connections_pending > 0
  for: 2m
  annotations:
    summary: "Database connection pool exhausted"
```

## Dashboard Customization

### Importing Dashboards

1. Navigate to Grafana: http://localhost:3000
2. Click **"+"** → **"Import"**
3. Upload JSON from:
   - `infrastructure/grafana/dashboards/order-service-dashboard.json`
   - `infrastructure/grafana/dashboards/routing-service-dashboard.json`

### Creating Custom Panels

1. Click **"Add Panel"** on any dashboard
2. Use PromQL to query Prometheus
3. Configure visualization (graph, gauge, table, etc.)
4. Save the dashboard

## Troubleshooting

### Prometheus Not Scraping Metrics

Check Prometheus targets:
```bash
curl http://localhost:9090/api/v1/targets
```

Verify service metrics endpoint:
```bash
curl http://localhost:8081/actuator/prometheus
curl http://localhost:8082/actuator/prometheus
```

### Grafana Can't Connect to Prometheus

1. Check Prometheus is running:
   ```bash
   docker ps | grep prometheus
   ```

2. Test connection from Grafana container:
   ```bash
   docker exec delivery-grafana curl http://prometheus:9090/-/healthy
   ```

### Missing Metrics

Rebuild services to include Micrometer dependencies:
```bash
cd infrastructure
docker compose down
docker compose up --build -d
```

## Performance Tuning

### Prometheus Retention

Modify `prometheus.yml`:
```yaml
global:
  scrape_interval: 15s      # Lower for more granular data
  evaluation_interval: 15s
```

### Grafana Refresh Rate

In dashboard settings:
- Auto-refresh: `10s` (default)
- Time range: `Last 1 hour` (default)

## Best Practices

1. **Monitor Key Metrics**:
   - Request rate, latency, errors (RED method)
   - Resource utilization (CPU, memory, DB connections)
   - Business metrics (optimization time, route efficiency)

2. **Set Appropriate Alerts**:
   - High error rates (>5%)
   - High latency (P95 > 1s)
   - Resource exhaustion (DB pool, memory)

3. **Regular Review**:
   - Check dashboards daily
   - Investigate anomalies
   - Tune thresholds based on traffic patterns

4. **Capacity Planning**:
   - Track trends over time
   - Plan for growth
   - Identify bottlenecks early

## Additional Resources

- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Micrometer Documentation](https://micrometer.io/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

