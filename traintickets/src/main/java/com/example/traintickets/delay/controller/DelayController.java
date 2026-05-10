package com.example.traintickets.delay.controller;

import com.example.traintickets.delay.dto.DelayRequest;
import com.example.traintickets.delay.dto.DelayResponse;
import com.example.traintickets.delay.service.DelayService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DelayController {
    private final DelayService delayService;

    public DelayController(DelayService delayService) {
        this.delayService = delayService;
    }

    @PostMapping("/admin/trips/{tripId}/delay")
    DelayResponse applyDelay(@PathVariable Long tripId, @Valid @RequestBody DelayRequest request) {
        return delayService.applyDelay(tripId, request);
    }
}
