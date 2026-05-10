package com.example.traintickets.station.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StationRequest(
        @NotBlank String name,
        @NotBlank @Size(max = 16) String code
) {
}
