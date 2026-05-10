package com.example.traintickets.delay.repository;

import com.example.traintickets.delay.model.DelayEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DelayEventRepository extends JpaRepository<DelayEvent, Long> {
    boolean existsByTripId(Long tripId);
}
