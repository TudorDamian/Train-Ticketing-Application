package com.example.traintickets.delay.service;

import com.example.traintickets.booking.model.Booking;
import com.example.traintickets.booking.repository.BookingRepository;
import com.example.traintickets.common.ResourceNotFoundException;
import com.example.traintickets.delay.dto.DelayRequest;
import com.example.traintickets.delay.dto.DelayResponse;
import com.example.traintickets.delay.model.DelayEvent;
import com.example.traintickets.delay.repository.DelayEventRepository;
import com.example.traintickets.notification.service.NotificationService;
import com.example.traintickets.trip.model.Trip;
import com.example.traintickets.trip.model.TripStatus;
import com.example.traintickets.trip.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DelayService {
    private final TripRepository tripRepository;
    private final DelayEventRepository delayEventRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    public DelayService(TripRepository tripRepository,
                        DelayEventRepository delayEventRepository,
                        BookingRepository bookingRepository,
                        NotificationService notificationService) {
        this.tripRepository = tripRepository;
        this.delayEventRepository = delayEventRepository;
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public DelayResponse applyDelay(Long tripId, DelayRequest request) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + tripId));
        trip.setDelayMinutes(request.delayMinutes());
        trip.setStatus(request.delayMinutes() > 0 ? TripStatus.DELAYED : TripStatus.SCHEDULED);

        DelayEvent event = delayEventRepository.save(new DelayEvent(trip, request.delayMinutes(), request.reason()));
        List<Booking> bookings = bookingRepository.findByTripIdOrderByCreatedAtDesc(tripId);
        bookings.forEach(booking -> notificationService.sendDelayNotification(
                booking, request.delayMinutes(), request.reason()));
        return DelayResponse.from(event, bookings.size());
    }
}
