package com.example.traintickets.station.controller;

import com.example.traintickets.station.dto.StationRequest;
import com.example.traintickets.station.dto.StationResponse;
import com.example.traintickets.station.service.StationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StationController {
    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping("/stations")
    List<StationResponse> findAll() {
        return stationService.findAll().stream().map(StationResponse::from).toList();
    }

    @PostMapping("/admin/stations")
    @ResponseStatus(HttpStatus.CREATED)
    StationResponse create(@Valid @RequestBody StationRequest request) {
        return StationResponse.from(stationService.create(request));
    }

    @PutMapping("/admin/stations/{id}")
    StationResponse update(@PathVariable Long id, @Valid @RequestBody StationRequest request) {
        return StationResponse.from(stationService.update(id, request));
    }

    @DeleteMapping("/admin/stations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        stationService.delete(id);
    }
}
