package com.example.traintickets.route.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RouteStopRequest(
        @NotNull Long stationId,
        @Min(1) int stopOrder,
        @Min(0) int arrivalOffsetMinutes,
        @Min(0) int departureOffsetMinutes
) {
}
