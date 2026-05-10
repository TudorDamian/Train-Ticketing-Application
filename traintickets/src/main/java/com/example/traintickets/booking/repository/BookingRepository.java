package com.example.traintickets.booking.repository;

import com.example.traintickets.booking.model.Booking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query(value = """
            select coalesce(sum(b.ticket_count), 0)
            from bookings b
            join trips t on t.id = b.trip_id
            join route_stops existing_from on existing_from.route_id = t.route_id and existing_from.station_id = b.from_station_id
            join route_stops existing_to on existing_to.route_id = t.route_id and existing_to.station_id = b.to_station_id
            join route_stops requested_from on requested_from.route_id = t.route_id and requested_from.station_id = :fromStationId
            join route_stops requested_to on requested_to.route_id = t.route_id and requested_to.station_id = :toStationId
            where b.trip_id = :tripId
              and b.status = 'CONFIRMED'
              and existing_from.stop_order < requested_to.stop_order
              and requested_from.stop_order < existing_to.stop_order
            """, nativeQuery = true)
    int countBookedSeatsForTripSegment(@Param("tripId") Long tripId,
                                       @Param("fromStationId") Long fromStationId,
                                       @Param("toStationId") Long toStationId);

    @EntityGraph(attributePaths = {
            "trip", "trip.train", "trip.route",
            "fromStation", "toStation", "tickets"
    })
    Optional<Booking> findDetailedById(Long id);

    @EntityGraph(attributePaths = {
            "trip", "trip.train", "trip.route",
            "fromStation", "toStation", "tickets"
    })
    List<Booking> findByCustomerEmailIgnoreCaseOrderByCreatedAtDesc(String customerEmail);

    @EntityGraph(attributePaths = {"fromStation", "toStation", "tickets"})
    List<Booking> findByTripIdOrderByCreatedAtDesc(Long tripId);

    boolean existsByTripId(Long tripId);

    boolean existsByFromStationIdOrToStationId(Long fromStationId, Long toStationId);
}
