package com.delivery.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AssignBatchResponse {
    private int assignedCount;
    private String message;
}

