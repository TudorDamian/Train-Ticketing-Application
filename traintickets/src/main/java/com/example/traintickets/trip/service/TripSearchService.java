package com.example.traintickets.trip.service;

import com.example.traintickets.common.BadRequestException;
import com.example.traintickets.route.model.RouteStop;
import com.example.traintickets.station.model.Station;
import com.example.traintickets.station.service.StationService;
import com.example.traintickets.trip.dto.SearchConnectionResponse;
import com.example.traintickets.trip.dto.SearchResponse;
import com.example.traintickets.trip.dto.SearchSegmentResponse;
import com.example.traintickets.trip.model.Trip;
import com.example.traintickets.trip.model.TripStatus;
import com.example.traintickets.trip.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class TripSearchService {
    private final TripRepository tripRepository;
    private final StationService stationService;

    public TripSearchService(TripRepository tripRepository, StationService stationService) {
        this.tripRepository = tripRepository;
        this.stationService = stationService;
    }

    @Transactional(readOnly = true)
    public SearchResponse search(String fromCode, String toCode, LocalDate date) {
        Station from = stationService.getByCode(fromCode);
        Station to = stationService.getByCode(toCode);
        if (from.getId().equals(to.getId())) {
            throw new BadRequestException("Departure and arrival stations must be different");
        }

        List<Trip> trips = tripRepository.findByDepartureDateTimeBetweenOrderByDepartureDateTime(
                date.atStartOfDay(), date.plusDays(2).atStartOfDay());
        List<SearchConnectionResponse> connections = new ArrayList<>();
        connections.addAll(findDirect(trips, from, to));
        connections.addAll(findOneChangeover(trips, from, to));

        return new SearchResponse(connections.stream()
                .sorted(Comparator.comparing(connection -> connection.segments().getFirst().departureTime()))
                .limit(20)
                .toList());
    }

    private List<SearchConnectionResponse> findDirect(List<Trip> trips, Station from, Station to) {
        return trips.stream()
                .filter(trip -> trip.getStatus() != TripStatus.CANCELLED)
                .flatMap(trip -> segment(trip, from, to).stream())
                .map(segment -> connection(List.of(segment)))
                .toList();
    }

    private List<SearchConnectionResponse> findOneChangeover(List<Trip> trips, Station from, Station to) {
        List<SearchConnectionResponse> results = new ArrayList<>();
        for (Trip firstTrip : trips) {
            for (RouteStop intermediateStop : firstTrip.getRoute().getStops()) {
                Station intermediate = intermediateStop.getStation();
                if (intermediate.getId().equals(from.getId()) || intermediate.getId().equals(to.getId())) {
                    continue;
                }
                Optional<SearchSegmentResponse> first = segment(firstTrip, from, intermediate);
                if (first.isEmpty()) {
                    continue;
                }
                for (Trip secondTrip : trips) {
                    Optional<SearchSegmentResponse> second = segment(secondTrip, intermediate, to);
                    if (second.isPresent()
                            && second.get().departureTime().isAfter(first.get().arrivalTime())) {
                        results.add(connection(List.of(first.get(), second.get())));
                    }
                }
            }
        }
        return results;
    }

    private Optional<SearchSegmentResponse> segment(Trip trip, Station from, Station to) {
        if (trip.getStatus() == TripStatus.CANCELLED) {
            return Optional.empty();
        }
        Optional<RouteStop> fromStop = routeStop(trip, from);
        Optional<RouteStop> toStop = routeStop(trip, to);
        if (fromStop.isEmpty() || toStop.isEmpty()
                || fromStop.get().getStopOrder() >= toStop.get().getStopOrder()) {
            return Optional.empty();
        }

        return Optional.of(new SearchSegmentResponse(
                trip.getId(),
                trip.getTrain().getName(),
                from.getName(),
                to.getName(),
                trip.getDepartureDateTime().plusMinutes(fromStop.get().getDepartureOffsetMinutes() + trip.getDelayMinutes()),
                trip.getDepartureDateTime().plusMinutes(toStop.get().getArrivalOffsetMinutes() + trip.getDelayMinutes())
        ));
    }

    private Optional<RouteStop> routeStop(Trip trip, Station station) {
        return trip.getRoute().getStops().stream()
                .filter(stop -> stop.getStation().getId().equals(station.getId()))
                .findFirst();
    }

    private SearchConnectionResponse connection(List<SearchSegmentResponse> segments) {
        LocalDateTime start = segments.getFirst().departureTime();
        LocalDateTime end = segments.getLast().arrivalTime();
        return new SearchConnectionResponse(
                segments,
                Duration.between(start, end).toMinutes(),
                segments.size() - 1
        );
    }
}
