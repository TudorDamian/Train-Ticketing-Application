package com.example.traintickets.booking.dto;

import com.example.traintickets.booking.model.Booking;

import java.time.Instant;
import java.util.List;

public record BookingResponse(
        Long id,
        String customerEmail,
        Long tripId,
        String fromStation,
        String toStation,
        int ticketCount,
        String status,
        Instant createdAt,
        List<TicketResponse> tickets
) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getCustomerEmail(),
                booking.getTrip().getId(),
                booking.getFromStation().getName(),
                booking.getToStation().getName(),
                booking.getTicketCount(),
                booking.getStatus().name(),
                booking.getCreatedAt(),
                booking.getTickets().stream().map(TicketResponse::from).toList()
        );
    }
}
