package com.example.traintickets.delay.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record DelayRequest(
        @Min(0) int delayMinutes,
        @NotBlank String reason
) {
}
