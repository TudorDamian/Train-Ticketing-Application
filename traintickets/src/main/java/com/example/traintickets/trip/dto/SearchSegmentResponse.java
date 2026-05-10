package com.example.traintickets.trip.dto;

import java.time.LocalDateTime;

public record SearchSegmentResponse(
        Long tripId,
        String train,
        String from,
        String to,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime
) {
}
