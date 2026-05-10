package com.example.traintickets.route.repository;

import com.example.traintickets.route.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {
    @Query("select distinct r from Route r left join fetch r.stops s left join fetch s.station")
    List<Route> findAllWithStops();
}
