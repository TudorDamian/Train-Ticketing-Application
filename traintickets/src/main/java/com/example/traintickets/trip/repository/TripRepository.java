package com.example.traintickets.trip.repository;

import com.example.traintickets.trip.model.Trip;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"train", "route", "route.stops", "route.stops.station"})
    @Query("select t from Trip t where t.id = :id")
    Optional<Trip> findByIdForUpdate(Long id);

    @EntityGraph(attributePaths = {"train", "route", "route.stops", "route.stops.station"})
    List<Trip> findByDepartureDateTimeBetweenOrderByDepartureDateTime(LocalDateTime start, LocalDateTime end);

    @EntityGraph(attributePaths = {"train", "route"})
    Optional<Trip> findWithTrainAndRouteById(Long id);

    @EntityGraph(attributePaths = {"train", "route"})
    List<Trip> findAllByOrderByDepartureDateTimeDesc();

    boolean existsByTrainId(Long trainId);

    boolean existsByRouteId(Long routeId);
}
