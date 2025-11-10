package com.delivery.routing.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TimeWindow {
    int earliest;
    int latest;
    int serviceTime;
}

