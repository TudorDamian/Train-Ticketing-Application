package com.example.traintickets.trip.controller;

import com.example.traintickets.trip.dto.CreateTripRequest;
import com.example.traintickets.trip.dto.SearchResponse;
import com.example.traintickets.trip.dto.TripResponse;
import com.example.traintickets.trip.service.TripSearchService;
import com.example.traintickets.trip.service.TripService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api")
public class TripController {
    private final TripSearchService tripSearchService;
    private final TripService tripService;

    public TripController(TripSearchService tripSearchService, TripService tripService) {
        this.tripSearchService = tripSearchService;
        this.tripService = tripService;
    }

    @GetMapping("/trips/search")
    SearchResponse search(@RequestParam @NotBlank String from,
                          @RequestParam @NotBlank String to,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return tripSearchService.search(from, to, date);
    }

    @PostMapping("/admin/trips")
    @ResponseStatus(HttpStatus.CREATED)
    TripResponse create(@Valid @RequestBody CreateTripRequest request) {
        return TripResponse.from(tripService.create(request));
    }

    @GetMapping("/admin/trips")
    List<TripResponse> findAll() {
        return tripService.findAll().stream().map(TripResponse::from).toList();
    }

    @PutMapping("/admin/trips/{id}")
    TripResponse update(@PathVariable Long id, @Valid @RequestBody CreateTripRequest request) {
        return TripResponse.from(tripService.update(id, request));
    }

    @DeleteMapping("/admin/trips/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        tripService.delete(id);
    }
}
