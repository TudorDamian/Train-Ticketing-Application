package com.example.traintickets.delay.dto;

import com.example.traintickets.delay.model.DelayEvent;

import java.time.Instant;

public record DelayResponse(
        Long id,
        Long tripId,
        int delayMinutes,
        String reason,
        Instant createdAt,
        int notifiedBookings
) {
    public static DelayResponse from(DelayEvent event, int notifiedBookings) {
        return new DelayResponse(
                event.getId(),
                event.getTrip().getId(),
                event.getDelayMinutes(),
                event.getReason(),
                event.getCreatedAt(),
                notifiedBookings
        );
    }
}
