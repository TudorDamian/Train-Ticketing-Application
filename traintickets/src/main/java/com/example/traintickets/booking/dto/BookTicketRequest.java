package com.example.traintickets.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BookTicketRequest(
        @NotBlank @Email String customerEmail,
        @NotNull Long tripId,
        @NotNull Long fromStationId,
        @NotNull Long toStationId,
        @Min(1) int ticketCount,
        List<String> passengerNames
) {
}
