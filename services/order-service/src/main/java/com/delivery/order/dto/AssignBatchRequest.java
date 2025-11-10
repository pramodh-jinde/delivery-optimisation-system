package com.delivery.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AssignBatchRequest {
    @NotNull(message = "Executive ID is required")
    private UUID executiveId;

    @NotEmpty(message = "Order IDs list cannot be empty")
    private List<UUID> orderIds;

    @NotNull(message = "Batch ID is required")
    private Integer batchId;
}

