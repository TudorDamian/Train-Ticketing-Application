package com.example.traintickets.booking.dto;

import com.example.traintickets.booking.model.Ticket;

public record TicketResponse(Long id, String passengerName) {
    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(ticket.getId(), ticket.getPassengerName());
    }
}
