package com.example.traintickets.trip.dto;

import java.util.List;

public record SearchConnectionResponse(
        List<SearchSegmentResponse> segments,
        long totalDurationMinutes,
        int changeovers
) {
}
