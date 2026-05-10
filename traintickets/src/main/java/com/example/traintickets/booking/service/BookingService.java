package com.example.traintickets.booking.service;

import com.example.traintickets.booking.dto.BookTicketRequest;
import com.example.traintickets.booking.dto.NotEnoughSeatsException;
import com.example.traintickets.booking.model.Booking;
import com.example.traintickets.booking.model.Ticket;
import com.example.traintickets.booking.repository.BookingRepository;
import com.example.traintickets.common.BadRequestException;
import com.example.traintickets.common.ResourceNotFoundException;
import com.example.traintickets.route.model.RouteStop;
import com.example.traintickets.station.model.Station;
import com.example.traintickets.station.service.StationService;
import com.example.traintickets.trip.model.Trip;
import com.example.traintickets.trip.repository.TripRepository;
import com.example.traintickets.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final StationService stationService;
    private final NotificationService notificationService;

    public BookingService(BookingRepository bookingRepository,
                          TripRepository tripRepository,
                          StationService stationService,
                          NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.tripRepository = tripRepository;
        this.stationService = stationService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Booking bookTickets(BookTicketRequest request) {
        Trip trip = tripRepository.findByIdForUpdate(request.tripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + request.tripId()));
        Station from = stationService.getById(request.fromStationId());
        Station to = stationService.getById(request.toStationId());
        validateTripSegment(trip, from, to);

        int alreadyBooked = bookingRepository.countBookedSeatsForTripSegment(
                trip.getId(), from.getId(), to.getId());
        if (alreadyBooked + request.ticketCount() > trip.getTrain().getCapacity()) {
            throw new NotEnoughSeatsException("Not enough seats available for this trip segment");
        }

        Booking booking = new Booking(request.customerEmail(), trip, from, to, request.ticketCount());
        addTickets(booking, request.ticketCount(), request.passengerNames());
        Booking saved = bookingRepository.save(booking);
        notificationService.sendBookingConfirmation(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public Booking getById(Long id) {
        return bookingRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Booking> findByCustomerEmail(String customerEmail) {
        return bookingRepository.findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(customerEmail);
    }

    @Transactional(readOnly = true)
    public List<Booking> findByTrip(Long tripId) {
        return bookingRepository.findByTripIdOrderByCreatedAtDesc(tripId);
    }

    @Transactional
    public void delete(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Booking not found: " + id);
        }
        bookingRepository.deleteById(id);
    }

    private void addTickets(Booking booking, int ticketCount, List<String> passengerNames) {
        for (int i = 0; i < ticketCount; i++) {
            String passengerName = passengerNames != null && i < passengerNames.size()
                    ? passengerNames.get(i)
                    : null;
            booking.addTicket(new Ticket(passengerName));
        }
    }

    private void validateTripSegment(Trip trip, Station from, Station to) {
        RouteStop fromStop = routeStop(trip, from);
        RouteStop toStop = routeStop(trip, to);
        if (fromStop == null || toStop == null) {
            throw new BadRequestException("Both stations must be present on the trip route");
        }
        if (fromStop.getStopOrder() >= toStop.getStopOrder()) {
            throw new BadRequestException("Departure station must be before arrival station on the route");
        }
    }

    private RouteStop routeStop(Trip trip, Station station) {
        return trip.getRoute().getStops().stream()
                .filter(stop -> stop.getStation().getId().equals(station.getId()))
                .findFirst()
                .orElse(null);
    }
}
