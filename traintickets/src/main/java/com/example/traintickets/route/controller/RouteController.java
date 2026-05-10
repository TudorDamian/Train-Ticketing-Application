package com.example.traintickets.route.controller;

import com.example.traintickets.route.dto.RouteRequest;
import com.example.traintickets.route.dto.RouteResponse;
import com.example.traintickets.route.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
public class RouteController {
    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping("/routes")
    List<RouteResponse> findAll() {
        return routeService.findAll().stream().map(RouteResponse::from).toList();
    }

    @PostMapping("/admin/routes")
    @ResponseStatus(HttpStatus.CREATED)
    RouteResponse create(@Valid @RequestBody RouteRequest request) {
        return RouteResponse.from(routeService.create(request));
    }

    @PutMapping("/admin/routes/{id}")
    RouteResponse update(@PathVariable Long id, @Valid @RequestBody RouteRequest request) {
        return RouteResponse.from(routeService.update(id, request));
    }

    @DeleteMapping("/admin/routes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        routeService.delete(id);
    }
}
