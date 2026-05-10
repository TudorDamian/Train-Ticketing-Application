package com.example.traintickets.train.dto;

import com.example.traintickets.train.model.Train;

public record TrainResponse(Long id, String name, int capacity) {
    public static TrainResponse from(Train train) {
        return new TrainResponse(train.getId(), train.getName(), train.getCapacity());
    }
}
