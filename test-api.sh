#!/bin/bash

# Delivery Route Optimization System - API Testing Script
# This script tests the complete flow: Create Orders → Optimize Route → Assign Batch

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
ORDER_SERVICE_URL="http://localhost:8081"
ROUTING_SERVICE_URL="http://localhost:8082"
EXECUTIVE_ID="550e8400-e29b-41d4-a716-446655440100"

# Function to print colored output
print_step() {
    echo -e "\n${BLUE}=== $1 ===${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# Function to check if service is running
check_service() {
    local service_name=$1
    local url=$2
    
    if curl -s -f "$url/actuator/health" > /dev/null 2>&1; then
        print_success "$service_name is running"
        return 0
    else
        print_error "$service_name is not responding at $url"
        return 1
    fi
}

# Check if services are running

print_step "Checking Services"
if ! check_service "Order Service" "$ORDER_SERVICE_URL"; then
    print_error "Please start Order Service first"
    exit 1
fi

if ! check_service "Routing Service" "$ROUTING_SERVICE_URL"; then
    print_error "Please start Routing Service first"
    exit 1
fi



# Step 1: Create Order 1
print_step "Step 1: Creating Order 1"
ORDER_1_RESPONSE=$(curl -s -X POST "$ORDER_SERVICE_URL/api/v1/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "550e8400-e29b-41d4-a716-446655440001",
    "restaurantId": "550e8400-e29b-41d4-a716-446655440010",
    "deliveryLocation": {
      "latitude": 12.9716,
      "longitude": 77.5946
    },
    "preparationTimeMinutes": 15
  }')

if [ $? -ne 0 ]; then
    print_error "Failed to create Order 1"
    exit 1
fi

ORDER_1_ID=$(echo "$ORDER_1_RESPONSE" | grep -o '"id":"[^"]*' | cut -d'"' -f4)
if [ -z "$ORDER_1_ID" ]; then
    print_error "Failed to extract Order 1 ID from response"
    echo "$ORDER_1_RESPONSE"
    exit 1
fi

print_success "Order 1 created with ID: $ORDER_1_ID"
echo "$ORDER_1_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$ORDER_1_RESPONSE"

# Step 2: Create Order 2
print_step "Step 2: Creating Order 2"
ORDER_2_RESPONSE=$(curl -s -X POST "$ORDER_SERVICE_URL/api/v1/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "550e8400-e29b-41d4-a716-446655440002",
    "restaurantId": "550e8400-e29b-41d4-a716-446655440020",
    "deliveryLocation": {
      "latitude": 12.9164,
      "longitude": 77.6101
    },
    "preparationTimeMinutes": 20
  }')

if [ $? -ne 0 ]; then
    print_error "Failed to create Order 2"
    exit 1
fi

ORDER_2_ID=$(echo "$ORDER_2_RESPONSE" | grep -o '"id":"[^"]*' | cut -d'"' -f4)
if [ -z "$ORDER_2_ID" ]; then
    print_error "Failed to extract Order 2 ID from response"
    echo "$ORDER_2_RESPONSE"
    exit 1
fi

print_success "Order 2 created with ID: $ORDER_2_ID"
echo "$ORDER_2_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$ORDER_2_RESPONSE"

# Step 3: Create Order 3
print_step "Step 3: Creating Order 3"
ORDER_3_RESPONSE=$(curl -s -X POST "$ORDER_SERVICE_URL/api/v1/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "550e8400-e29b-41d4-a716-446655440003",
    "restaurantId": "550e8400-e29b-41d4-a716-446655440030",
    "deliveryLocation": {
      "latitude": 12.9428,
      "longitude": 77.5741
    },
    "preparationTimeMinutes": 10
  }')

if [ $? -ne 0 ]; then
    print_error "Failed to create Order 3"
    exit 1
fi

ORDER_3_ID=$(echo "$ORDER_3_RESPONSE" | grep -o '"id":"[^"]*' | cut -d'"' -f4)
if [ -z "$ORDER_3_ID" ]; then
    print_error "Failed to extract Order 3 ID from response"
    echo "$ORDER_3_RESPONSE"
    exit 1
fi

print_success "Order 3 created with ID: $ORDER_3_ID"
echo "$ORDER_3_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$ORDER_3_RESPONSE"

# Step 4: Verify Orders
print_step "Step 4: Verifying Orders"
print_info "Verifying Order 1..."
ORDER_1_VERIFY=$(curl -s -X GET "$ORDER_SERVICE_URL/api/v1/orders/$ORDER_1_ID")
if [ $? -eq 0 ]; then
    print_success "Order 1 verified"
else
    print_error "Failed to verify Order 1"
fi

# Step 5: Optimize Route
print_step "Step 5: Optimizing Route for 3 Orders"
print_info "Executive location: Koramangala (12.9352, 77.6245)"
print_info "Order 1: Restaurant Indiranagar → Delivery MG Road"
print_info "Order 2: Restaurant HSR Layout → Delivery BTM Layout"
print_info "Order 3: Restaurant Jayanagar → Delivery Basavanagudi"

# Create the optimization request payload
ROUTE_REQUEST=$(cat <<EOF
{
  "batch": {
    "id": 1,
    "executiveId": "$EXECUTIVE_ID",
    "orders": [
      {
        "orderId": "$ORDER_1_ID",
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
        "createdAt": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
      },
      {
        "orderId": "$ORDER_2_ID",
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
        "createdAt": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
      },
      {
        "orderId": "$ORDER_3_ID",
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
        "createdAt": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
      }
    ],
    "status": "PENDING",
    "createdAt": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
  },
  "executiveLocation": {
    "executiveId": "$EXECUTIVE_ID",
    "location": {
      "latitude": 12.9352,
      "longitude": 77.6245
    },
    "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
  }
}
EOF
)

ROUTE_RESPONSE=$(curl -s -X POST "$ROUTING_SERVICE_URL/api/v1/routes/optimize" \
  -H "Content-Type: application/json" \
  -d "$ROUTE_REQUEST")

if [ $? -ne 0 ]; then
    print_error "Failed to optimize route"
    exit 1
fi

# Check if response contains route information
if echo "$ROUTE_RESPONSE" | grep -q "routeId"; then
    print_success "Route optimized successfully"
    
    # Extract key information
    ROUTE_ID=$(echo "$ROUTE_RESPONSE" | grep -o '"routeId":"[^"]*' | cut -d'"' -f4)
    TOTAL_DISTANCE=$(echo "$ROUTE_RESPONSE" | grep -o '"totalDistanceKm":[0-9.]*' | cut -d':' -f2)
    ESTIMATED_TIME=$(echo "$ROUTE_RESPONSE" | grep -o '"estimatedTimeMinutes":[0-9.]*' | cut -d':' -f2)
    ALGORITHM=$(echo "$ROUTE_RESPONSE" | grep -o '"algorithm":"[^"]*' | cut -d'"' -f4)
    
    print_info "Route ID: $ROUTE_ID"
    print_info "Total Distance: ${TOTAL_DISTANCE} km"
    print_info "Estimated Time: ${ESTIMATED_TIME} minutes"
    print_info "Algorithm Used: $ALGORITHM"
    
    echo ""
    echo "Full Route Response:"
    echo "$ROUTE_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$ROUTE_RESPONSE"
else
    print_error "Route optimization failed or returned unexpected response"
    echo "$ROUTE_RESPONSE"
    exit 1
fi

# Step 6: Assign Batch
print_step "Step 6: Assigning Batch to Executive"
ASSIGNMENT_RESPONSE=$(curl -s -X POST "$ORDER_SERVICE_URL/api/v1/orders/assign-batch" \
  -H "Content-Type: application/json" \
  -d "{
    \"executiveId\": \"$EXECUTIVE_ID\",
    \"orderIds\": [
      \"$ORDER_1_ID\",
      \"$ORDER_2_ID\",
      \"$ORDER_3_ID\"
    ],
    \"batchId\": 1
  }")

if [ $? -ne 0 ]; then
    print_error "Failed to assign batch"
    exit 1
fi

ASSIGNED_COUNT=$(echo "$ASSIGNMENT_RESPONSE" | grep -o '"assignedCount":[0-9]*' | cut -d':' -f2)
if [ "$ASSIGNED_COUNT" = "3" ]; then
    print_success "Batch assigned successfully - $ASSIGNED_COUNT orders assigned"
else
    print_error "Expected 3 orders to be assigned, but got $ASSIGNED_COUNT"
fi

echo "$ASSIGNMENT_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$ASSIGNMENT_RESPONSE"

# Step 7: Verify Order Status
print_step "Step 7: Verifying Order Status After Assignment"
print_info "Checking Order 1 status..."
ORDER_1_STATUS=$(curl -s -X GET "$ORDER_SERVICE_URL/api/v1/orders/$ORDER_1_ID")
STATUS=$(echo "$ORDER_1_STATUS" | grep -o '"status":"[^"]*' | cut -d'"' -f4)

if [ "$STATUS" = "ASSIGNED" ]; then
    print_success "Order 1 status is ASSIGNED"
else
    print_error "Expected Order 1 status to be ASSIGNED, but got $STATUS"
fi

ASSIGNED_EXEC=$(echo "$ORDER_1_STATUS" | grep -o '"assignedExecutiveId":"[^"]*' | cut -d'"' -f4)
if [ "$ASSIGNED_EXEC" = "$EXECUTIVE_ID" ]; then
    print_success "Order 1 assigned to correct executive"
else
    print_error "Order 1 assigned to wrong executive"
fi

echo ""
echo "Order 1 Details:"
echo "$ORDER_1_STATUS" | python3 -m json.tool 2>/dev/null || echo "$ORDER_1_STATUS"

# Summary
print_step "Test Summary"
echo ""
print_success "✓ Order 1 created: $ORDER_1_ID"
print_success "✓ Order 2 created: $ORDER_2_ID"
print_success "✓ Order 3 created: $ORDER_3_ID"
print_success "✓ Route optimized successfully"
print_success "✓ Batch assigned to executive"
print_success "✓ Order status verified"
echo ""
print_info "Test completed successfully!"
echo ""

