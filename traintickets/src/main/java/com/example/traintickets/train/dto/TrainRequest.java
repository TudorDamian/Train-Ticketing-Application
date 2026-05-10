package com.example.traintickets.train.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record TrainRequest(
        @NotBlank String name,
        @Min(1) int capacity
) {
}
