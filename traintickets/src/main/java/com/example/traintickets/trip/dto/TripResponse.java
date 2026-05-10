package com.example.traintickets.trip.dto;

import com.example.traintickets.trip.model.Trip;

import java.time.LocalDateTime;

public record TripResponse(
        Long id,
        Long trainId,
        String train,
        Long routeId,
        String route,
        LocalDateTime departureDateTime,
        String status,
        int delayMinutes
) {
    public static TripResponse from(Trip trip) {
        return new TripResponse(
                trip.getId(),
                trip.getTrain().getId(),
                trip.getTrain().getName(),
                trip.getRoute().getId(),
                trip.getRoute().getName(),
                trip.getDepartureDateTime(),
                trip.getStatus().name(),
                trip.getDelayMinutes()
        );
    }
}
