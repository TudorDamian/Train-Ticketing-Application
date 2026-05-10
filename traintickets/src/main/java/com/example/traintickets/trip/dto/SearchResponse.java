package com.example.traintickets.trip.dto;

import java.util.List;

public record SearchResponse(List<SearchConnectionResponse> connections) {
}
