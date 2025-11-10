# API Testing Guide

This guide provides step-by-step instructions to test the Delivery Route Optimization System APIs using curl commands with real example data.

## Prerequisites

1. Ensure all services are running:
   ```bash
   cd infrastructure
   docker-compose up -d
   ```

2. Wait for all services to be healthy:
   ```bash
   docker-compose ps
   ```

3. Verify services are accessible:
   - Order Service: http://localhost:8081
   - Routing Service: http://localhost:8082

## Use Case: Optimize Delivery Route for 3 Orders

### Scenario
- Delivery executive is at Koramangala (12.9352, 77.6245)
- 3 orders need to be delivered:
  - Order 1: Restaurant at Indiranagar (12.9784, 77.6408), Delivery at MG Road (12.9716, 77.5946), Prep time: 15 min
  - Order 2: Restaurant at HSR Layout (12.9121, 77.6446), Delivery at BTM Layout (12.9164, 77.6101), Prep time: 20 min
  - Order 3: Restaurant at Jayanagar (12.9239, 77.5935), Delivery at Basavanagudi (12.9428, 77.5741), Prep time: 10 min

---

## Step 1: Create Order 1

**Endpoint:** `POST /api/v1/orders`

**Request:**
```bash
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "550e8400-e29b-41d4-a716-446655440001",
    "restaurantId": "550e8400-e29b-41d4-a716-446655440010",
    "deliveryLocation": {
      "latitude": 12.9716,
      "longitude": 77.5946
    },
    "preparationTimeMinutes": 15
  }'
```

**Expected Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440101",
  "customerId": "550e8400-e29b-41d4-a716-446655440001",
  "restaurantId": "550e8400-e29b-41d4-a716-446655440010",
  "status": "PENDING",
  "preparationTimeMinutes": 15,
  "createdAt": "2025-11-03T14:30:00Z"
}
```

**Save the order ID** from the response (e.g., `ORDER_1_ID="550e8400-e29b-41d4-a716-446655440101"`)

---

## Step 2: Create Order 2

**Request:**
```bash
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "550e8400-e29b-41d4-a716-446655440002",
    "restaurantId": "550e8400-e29b-41d4-a716-446655440020",
    "deliveryLocation": {
      "latitude": 12.9164,
      "longitude": 77.6101
    },
    "preparationTimeMinutes": 20
  }'
```

**Save the order ID** (e.g., `ORDER_2_ID="550e8400-e29b-41d4-a716-446655440102"`)

---

## Step 3: Create Order 3

**Request:**
```bash
curl -X POST http://localhost:8081/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "550e8400-e29b-41d4-a716-446655440003",
    "restaurantId": "550e8400-e29b-41d4-a716-446655440030",
    "deliveryLocation": {
      "latitude": 12.9428,
      "longitude": 77.5741
    },
    "preparationTimeMinutes": 10
  }'
```

**Save the order ID** (e.g., `ORDER_3_ID="550e8400-e29b-41d4-a716-446655440103"`)

---

## Step 4: Verify Orders Were Created

**Endpoint:** `GET /api/v1/orders/{id}`

**Request (replace with actual order ID):**
```bash
# Replace ORDER_1_ID with the actual ID from Step 1
curl -X GET http://localhost:8081/api/v1/orders/550e8400-e29b-41d4-a716-446655440101 \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440101",
  "customerId": "550e8400-e29b-41d4-a716-446655440001",
  "restaurantId": "550e8400-e29b-41d4-a716-446655440010",
  "status": "PENDING",
  "preparationTimeMinutes": 15,
  "createdAt": "2025-11-03T14:30:00Z"
}
```

---

## Step 5: Optimize Route for the 3 Orders

**Endpoint:** `POST /api/v1/routes/optimize`

**Request:**
```bash
curl -X POST http://localhost:8082/api/v1/routes/optimize \
  -H "Content-Type: application/json" \
  -d '{
    "batch": {
      "id": 1,
      "executiveId": "550e8400-e29b-41d4-a716-446655440100",
      "orders": [
        {
          "orderId": "550e8400-e29b-41d4-a716-446655440101",
          "restaurantId": "550e8400-e29b-41d4-a716-446655440010",
          "restaurantLocation": {
            "latitude": 12.9784,
            "longitude": 77.6408
          },
          "deliveryLocation": {
            "latitude": 12.9716,
            "longitude": 77.5946
          },
          "preparationTimeMinutes": 15,
          "status": "PENDING",
          "createdAt": "2025-11-03T14:30:00Z"
        },
        {
          "orderId": "550e8400-e29b-41d4-a716-446655440102",
          "restaurantId": "550e8400-e29b-41d4-a716-446655440020",
          "restaurantLocation": {
            "latitude": 12.9121,
            "longitude": 77.6446
          },
          "deliveryLocation": {
            "latitude": 12.9164,
            "longitude": 77.6101
          },
          "preparationTimeMinutes": 20,
          "status": "PENDING",
          "createdAt": "2025-11-03T14:30:00Z"
        },
        {
          "orderId": "550e8400-e29b-41d4-a716-446655440103",
          "restaurantId": "550e8400-e29b-41d4-a716-446655440030",
          "restaurantLocation": {
            "latitude": 12.9239,
            "longitude": 77.5935
          },
          "deliveryLocation": {
            "latitude": 12.9428,
            "longitude": 77.5741
          },
          "preparationTimeMinutes": 10,
          "status": "PENDING",
          "createdAt": "2025-11-03T14:30:00Z"
        }
      ],
      "status": "PENDING",
      "createdAt": "2025-11-03T14:30:00Z"
    },
    "executiveLocation": {
      "executiveId": "550e8400-e29b-41d4-a716-446655440100",
      "location": {
        "latitude": 12.9352,
        "longitude": 77.6245
      },
      "timestamp": "2025-11-03T14:30:00Z"
    }
  }'
```

**Expected Response:**
```json
{
  "routeId": "550e8400-e29b-41d4-a716-446655440200",
  "batchId": 1,
  "steps": [
    {
      "sequence": 1,
      "locationId": "550e8400-e29b-41d4-a716-446655440100",
      "type": "EXECUTIVE_START",
      "location": {
        "latitude": 12.9352,
        "longitude": 77.6245
      },
      "distanceFromPreviousKm": 0.0,
      "timeFromPreviousMinutes": 0.0,
      "estimatedArrivalTimeMinutes": 0.0,
      "instructions": "Start from executive location"
    },
    {
      "sequence": 2,
      "locationId": "550e8400-e29b-41d4-a716-446655440010",
      "type": "RESTAURANT_PICKUP",
      "location": {
        "latitude": 12.9784,
        "longitude": 77.6408
      },
      "distanceFromPreviousKm": 5.2,
      "timeFromPreviousMinutes": 15.6,
      "estimatedArrivalTimeMinutes": 15.6,
      "instructions": "Pick up order"
    }
  ],
  "totalDistanceKm": 25.8,
  "estimatedTimeMinutes": 77.4,
  "metadata": {
    "algorithm": "EXACT_DP",
    "optimizationTimeMs": 420,
    "orderCount": 3
  }
}
```

**Verify:**
- ✅ Response contains optimized route with steps
- ✅ Total distance is calculated
- ✅ Estimated time is reasonable
- ✅ Steps are in optimal sequence
- ✅ Algorithm used is "EXACT_DP" (for N ≤ 10) or "CHRISTOFIDES" (for N > 10)

---

## Step 6: Assign Orders to Executive

**Endpoint:** `POST /api/v1/orders/assign-batch`

**Request:**
```bash
curl -X POST http://localhost:8081/api/v1/orders/assign-batch \
  -H "Content-Type: application/json" \
  -d '{
    "executiveId": "550e8400-e29b-41d4-a716-446655440100",
    "orderIds": [
      "550e8400-e29b-41d4-a716-446655440101",
      "550e8400-e29b-41d4-a716-446655440102",
      "550e8400-e29b-41d4-a716-446655440103"
    ],
    "batchId": 1
  }'
```

**Expected Response:**
```json
{
  "assignedCount": 3,
  "message": "Orders assigned successfully"
}
```

**Verify:**
- ✅ All 3 orders are assigned
- ✅ Response confirms successful assignment

---

## Step 7: Verify Order Status After Assignment

**Request:**
```bash
curl -X GET http://localhost:8081/api/v1/orders/550e8400-e29b-41d4-a716-446655440101 \
  -H "Content-Type: application/json"
```

**Expected Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440101",
  "customerId": "550e8400-e29b-41d4-a716-446655440001",
  "restaurantId": "550e8400-e29b-41d4-a716-446655440010",
  "status": "ASSIGNED",
  "assignedExecutiveId": "550e8400-e29b-41d4-a716-446655440100",
  "batchId": 1,
  "assignedAt": "2025-11-03T14:31:00Z"
}
```

**Verify:**
- ✅ Status changed from "PENDING" to "ASSIGNED"
- ✅ `assignedExecutiveId` is set
- ✅ `batchId` is set
- ✅ `assignedAt` timestamp is present

---

## Quick Start: Run the Test Script

The easiest way to test everything is to use the provided test script:

```bash
# Make the script executable
chmod +x test-api.sh

# Run the test script
./test-api.sh
```

The script (`test-api.sh`) is located in the project root and will:
- ✅ Check if services are running
- ✅ Create 3 orders
- ✅ Optimize the route
- ✅ Assign the batch
- ✅ Verify order status
- ✅ Display colored output for easy reading

## Individual curl Commands (One-Line Format)

If you prefer to run individual commands, here are the one-line versions:

### Step 1: Create Order 1
```bash
curl -X POST http://localhost:8081/api/v1/orders -H "Content-Type: application/json" -d '{"customerId":"550e8400-e29b-41d4-a716-446655440001","restaurantId":"550e8400-e29b-41d4-a716-446655440010","deliveryLocation":{"latitude":12.9716,"longitude":77.5946},"preparationTimeMinutes":15}'
```

### Step 2: Create Order 2
```bash
curl -X POST http://localhost:8081/api/v1/orders -H "Content-Type: application/json" -d '{"customerId":"550e8400-e29b-41d4-a716-446655440002","restaurantId":"550e8400-e29b-41d4-a716-446655440020","deliveryLocation":{"latitude":12.9164,"longitude":77.6101},"preparationTimeMinutes":20}'
```

### Step 3: Create Order 3
```bash
curl -X POST http://localhost:8081/api/v1/orders -H "Content-Type: application/json" -d '{"customerId":"550e8400-e29b-41d4-a716-446655440003","restaurantId":"550e8400-e29b-41d4-a716-446655440030","deliveryLocation":{"latitude":12.9428,"longitude":77.5741},"preparationTimeMinutes":10}'
```

### Step 4: Get Order (Replace ORDER_ID with actual ID from Step 1)
```bash
curl -X GET http://localhost:8081/api/v1/orders/ORDER_ID -H "Content-Type: application/json"
```

### Step 5: Optimize Route (Replace ORDER_1_ID, ORDER_2_ID, ORDER_3_ID with actual IDs)
```bash
curl -X POST http://localhost:8082/api/v1/routes/optimize -H "Content-Type: application/json" -d '{"batch":{"id":1,"executiveId":"550e8400-e29b-41d4-a716-446655440100","orders":[{"orderId":"ORDER_1_ID","restaurantId":"550e8400-e29b-41d4-a716-446655440010","restaurantLocation":{"latitude":12.9784,"longitude":77.6408},"deliveryLocation":{"latitude":12.9716,"longitude":77.5946},"preparationTimeMinutes":15,"status":"PENDING","createdAt":"2025-11-03T14:30:00Z"},{"orderId":"ORDER_2_ID","restaurantId":"550e8400-e29b-41d4-a716-446655440020","restaurantLocation":{"latitude":12.9121,"longitude":77.6446},"deliveryLocation":{"latitude":12.9164,"longitude":77.6101},"preparationTimeMinutes":20,"status":"PENDING","createdAt":"2025-11-03T14:30:00Z"},{"orderId":"ORDER_3_ID","restaurantId":"550e8400-e29b-41d4-a716-446655440030","restaurantLocation":{"latitude":12.9239,"longitude":77.5935},"deliveryLocation":{"latitude":12.9428,"longitude":77.5741},"preparationTimeMinutes":10,"status":"PENDING","createdAt":"2025-11-03T14:30:00Z"}],"status":"PENDING","createdAt":"2025-11-03T14:30:00Z"},"executiveLocation":{"executiveId":"550e8400-e29b-41d4-a716-446655440100","location":{"latitude":12.9352,"longitude":77.6245},"timestamp":"2025-11-03T14:30:00Z"}}'
```

### Step 6: Assign Batch (Replace ORDER_1_ID, ORDER_2_ID, ORDER_3_ID with actual IDs)
```bash
curl -X POST http://localhost:8081/api/v1/orders/assign-batch -H "Content-Type: application/json" -d '{"executiveId":"550e8400-e29b-41d4-a716-446655440100","orderIds":["ORDER_1_ID","ORDER_2_ID","ORDER_3_ID"],"batchId":1}'
```

### Step 7: Verify Order Status (Replace ORDER_ID with actual ID)
```bash
curl -X GET http://localhost:8081/api/v1/orders/ORDER_ID -H "Content-Type: application/json"
```

---

## Health Check Endpoints

Verify services are healthy:

```bash
# Order Service Health
curl http://localhost:8081/actuator/health

# Routing Service Health
curl http://localhost:8082/actuator/health

# Order Service Metrics
curl http://localhost:8081/actuator/metrics

# Routing Service Metrics
curl http://localhost:8082/actuator/metrics
```

---

## Expected Results Summary

✅ **Order Creation**: 3 orders created successfully with status "PENDING"  
✅ **Route Optimization**: Route optimized with optimal sequence of stops  
✅ **Algorithm Selection**: Uses "EXACT_DP" for 3 orders (N ≤ 10)  
✅ **Distance Calculation**: Total distance calculated using Haversine formula  
✅ **Time Estimation**: Estimated time includes travel time at 20 km/hr  
✅ **Order Assignment**: All 3 orders assigned to executive with batch ID  
✅ **Status Update**: Order status changed from "PENDING" to "ASSIGNED"  

---

## Troubleshooting

### Service not responding
```bash
# Check if services are running
docker-compose ps

# Check service logs
docker-compose logs order-service
docker-compose logs routing-service

# Restart services
docker-compose restart order-service routing-service
```

### Database connection errors
```bash
# Check PostgreSQL is running
docker-compose ps postgres

# Check PostgreSQL logs
docker-compose logs postgres
```

### Port already in use
```bash
# Check what's using the port
lsof -i :8081
lsof -i :8082

# Stop conflicting services or change ports in docker-compose.yml
```

---

## Performance Validation

After running the tests, verify:

1. **Response Time**: Route optimization should complete in < 1 second for 3 orders
2. **Algorithm**: Should use "EXACT_DP" for N ≤ 10 orders
3. **Distance Accuracy**: Distance should be reasonable (check with Google Maps)
4. **Route Sequence**: Pickup should come before delivery for each order
5. **SLA Compliance**: Total estimated time should be < 40 minutes per order

---

## Additional Test Scenarios

### Test with 5 Orders (Still uses EXACT_DP)
Follow the same steps but create 5 orders instead of 3.

### Test with 11 Orders (Uses CHRISTOFIDES)
Create 11 orders to trigger the heuristic algorithm.

### Test with Invalid Data
Try creating orders with invalid coordinates or missing fields to test error handling.

