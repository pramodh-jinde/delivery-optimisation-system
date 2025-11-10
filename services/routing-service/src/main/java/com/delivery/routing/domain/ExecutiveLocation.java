package com.delivery.routing.domain;

import com.delivery.common.domain.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutiveLocation {
    private UUID executiveId;
    private Location location;
    private Instant timestamp;

    public Location toLocation() {
        return location;
    }
}

