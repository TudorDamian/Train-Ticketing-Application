package com.example.traintickets.trip.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateTripRequest(
        @NotNull Long trainId,
        @NotNull Long routeId,
        @NotNull @FutureOrPresent LocalDateTime departureDateTime
) {
}
