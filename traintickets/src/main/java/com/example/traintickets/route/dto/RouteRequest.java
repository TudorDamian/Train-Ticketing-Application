package com.example.traintickets.route.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RouteRequest(
        @NotBlank String name,
        @Size(min = 2) List<@Valid RouteStopRequest> stops
) {
}
