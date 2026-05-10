package com.example.traintickets.station.dto;

import com.example.traintickets.station.model.Station;

public record StationResponse(Long id, String name, String code) {
    public static StationResponse from(Station station) {
        return new StationResponse(station.getId(), station.getName(), station.getCode());
    }
}
