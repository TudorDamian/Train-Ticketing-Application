package com.example.traintickets.route.dto;

import com.example.traintickets.route.model.Route;

import java.util.List;

public record RouteResponse(Long id, String name, List<RouteStopResponse> stops) {
    public static RouteResponse from(Route route) {
        return new RouteResponse(
                route.getId(),
                route.getName(),
                route.getStops().stream().map(RouteStopResponse::from).toList()
        );
    }
}
