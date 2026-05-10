package com.example.traintickets.booking.dto;

import com.example.traintickets.common.BadRequestException;

public class NotEnoughSeatsException extends BadRequestException {
    public NotEnoughSeatsException(String message) {
        super(message);
    }
}
