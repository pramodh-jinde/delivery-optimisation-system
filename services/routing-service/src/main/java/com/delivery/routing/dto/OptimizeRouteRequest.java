package com.delivery.routing.dto;

import com.delivery.routing.domain.DeliveryBatch;
import com.delivery.routing.domain.ExecutiveLocation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptimizeRouteRequest {
    @NotNull(message = "Batch is required")
    @Valid
    private DeliveryBatch batch;

    @NotNull(message = "Executive location is required")
    @Valid
    private ExecutiveLocation executiveLocation;
}

