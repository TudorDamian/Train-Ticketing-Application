package com.example.traintickets.trip.service;

import com.example.traintickets.booking.repository.BookingRepository;
import com.example.traintickets.common.BadRequestException;
import com.example.traintickets.common.ResourceNotFoundException;
import com.example.traintickets.delay.repository.DelayEventRepository;
import com.example.traintickets.route.model.Route;
import com.example.traintickets.route.service.RouteService;
import com.example.traintickets.train.model.Train;
import com.example.traintickets.train.service.TrainService;
import com.example.traintickets.trip.dto.CreateTripRequest;
import com.example.traintickets.trip.model.Trip;
import com.example.traintickets.trip.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TripService {
    private final TripRepository tripRepository;
    private final TrainService trainService;
    private final RouteService routeService;
    private final BookingRepository bookingRepository;
    private final DelayEventRepository delayEventRepository;

    public TripService(TripRepository tripRepository,
                       TrainService trainService,
                       RouteService routeService,
                       BookingRepository bookingRepository,
                       DelayEventRepository delayEventRepository) {
        this.tripRepository = tripRepository;
        this.trainService = trainService;
        this.routeService = routeService;
        this.bookingRepository = bookingRepository;
        this.delayEventRepository = delayEventRepository;
    }

    @Transactional
    public Trip create(CreateTripRequest request) {
        Train train = trainService.getById(request.trainId());
        Route route = routeService.getById(request.routeId());
        return tripRepository.save(new Trip(train, route, request.departureDateTime()));
    }

    @Transactional(readOnly = true)
    public List<Trip> findAll() {
        return tripRepository.findAllByOrderByDepartureDateTimeDesc();
    }

    @Transactional
    public Trip update(Long id, CreateTripRequest request) {
        Trip trip = getById(id);
        if (bookingRepository.existsByTripId(id)) {
            throw new BadRequestException("Trip has bookings and cannot be changed");
        }
        if (delayEventRepository.existsByTripId(id)) {
            throw new BadRequestException("Trip has delay history and cannot be changed");
        }
        Train train = trainService.getById(request.trainId());
        Route route = routeService.getById(request.routeId());
        trip.setTrain(train);
        trip.setRoute(route);
        trip.setDepartureDateTime(request.departureDateTime());
        return trip;
    }

    @Transactional
    public void delete(Long id) {
        if (!tripRepository.existsById(id)) {
            throw new ResourceNotFoundException("Trip not found: " + id);
        }
        if (bookingRepository.existsByTripId(id)) {
            throw new BadRequestException("Trip has bookings and cannot be removed");
        }
        if (delayEventRepository.existsByTripId(id)) {
            throw new BadRequestException("Trip has delay history and cannot be removed");
        }
        tripRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Trip getById(Long id) {
        return tripRepository.findWithTrainAndRouteById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + id));
    }
}
