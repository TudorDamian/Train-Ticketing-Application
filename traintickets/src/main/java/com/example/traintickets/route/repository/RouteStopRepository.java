package com.example.traintickets.route.repository;

import com.example.traintickets.route.model.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {
    List<RouteStop> findByRouteIdOrderByStopOrder(Long routeId);

    Optional<RouteStop> findByRouteIdAndStationId(Long routeId, Long stationId);

    boolean existsByStationId(Long stationId);
}
