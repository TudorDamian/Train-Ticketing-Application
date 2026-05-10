package com.example.traintickets.route.service;

import com.example.traintickets.common.BadRequestException;
import com.example.traintickets.common.ResourceNotFoundException;
import com.example.traintickets.route.dto.RouteRequest;
import com.example.traintickets.route.dto.RouteStopRequest;
import com.example.traintickets.route.model.Route;
import com.example.traintickets.route.model.RouteStop;
import com.example.traintickets.route.repository.RouteRepository;
import com.example.traintickets.trip.repository.TripRepository;
import com.example.traintickets.station.model.Station;
import com.example.traintickets.station.service.StationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RouteService {
    private final RouteRepository routeRepository;
    private final StationService stationService;
    private final TripRepository tripRepository;

    public RouteService(RouteRepository routeRepository,
                        StationService stationService,
                        TripRepository tripRepository) {
        this.routeRepository = routeRepository;
        this.stationService = stationService;
        this.tripRepository = tripRepository;
    }

    @Transactional(readOnly = true)
    public List<Route> findAll() {
        return routeRepository.findAllWithStops();
    }

    @Transactional
    public Route create(RouteRequest request) {
        Route route = new Route(request.name());
        route.replaceStops(toStops(request.stops()));
        return routeRepository.save(route);
    }

    @Transactional
    public Route update(Long id, RouteRequest request) {
        Route route = getById(id);
        if (tripRepository.existsByRouteId(id)) {
            throw new BadRequestException("Route is used by one or more trips. Remove those trips before changing route stops");
        }
        route.setName(request.name());
        route.replaceStops(List.of());
        routeRepository.flush();
        route.replaceStops(toStops(request.stops()));
        return route;
    }

    @Transactional
    public void delete(Long id) {
        if (!routeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Route not found: " + id);
        }
        if (tripRepository.existsByRouteId(id)) {
            throw new BadRequestException("Route is used by one or more trips and cannot be removed");
        }
        routeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Route getById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found: " + id));
    }

    private List<RouteStop> toStops(List<RouteStopRequest> requests) {
        validateStops(requests);
        return requests.stream()
                .sorted(Comparator.comparingInt(RouteStopRequest::stopOrder))
                .map(stop -> {
                    Station station = stationService.getById(stop.stationId());
                    return new RouteStop(station, stop.stopOrder(),
                            stop.arrivalOffsetMinutes(), stop.departureOffsetMinutes());
                })
                .toList();
    }

    private void validateStops(List<RouteStopRequest> stops) {
        Set<Long> stationIds = new HashSet<>();
        Set<Integer> orders = new HashSet<>();
        int previousDeparture = -1;
        for (RouteStopRequest stop : stops.stream()
                .sorted(Comparator.comparingInt(RouteStopRequest::stopOrder))
                .toList()) {
            if (!stationIds.add(stop.stationId())) {
                throw new BadRequestException("A route cannot contain the same station twice");
            }
            if (!orders.add(stop.stopOrder())) {
                throw new BadRequestException("Route stop orders must be unique");
            }
            if (stop.arrivalOffsetMinutes() > stop.departureOffsetMinutes()) {
                throw new BadRequestException("Arrival offset cannot be after departure offset at a stop");
            }
            if (stop.arrivalOffsetMinutes() < previousDeparture) {
                throw new BadRequestException("Route stop offsets must be non-decreasing");
            }
            previousDeparture = stop.departureOffsetMinutes();
        }
    }
}
