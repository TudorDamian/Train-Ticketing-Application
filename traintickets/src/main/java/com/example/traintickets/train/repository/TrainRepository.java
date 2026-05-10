package com.example.traintickets.train.repository;

import com.example.traintickets.train.model.Train;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainRepository extends JpaRepository<Train, Long> {
}
