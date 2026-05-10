package com.example.traintickets.route.dto;

import com.example.traintickets.route.model.RouteStop;
import com.example.traintickets.station.dto.StationResponse;

public record RouteStopResponse(
        Long id,
        StationResponse station,
        int stopOrder,
        int arrivalOffsetMinutes,
        int departureOffsetMinutes
) {
    public static RouteStopResponse from(RouteStop stop) {
        return new RouteStopResponse(
                stop.getId(),
                StationResponse.from(stop.getStation()),
                stop.getStopOrder(),
                stop.getArrivalOffsetMinutes(),
                stop.getDepartureOffsetMinutes()
        );
    }
}
