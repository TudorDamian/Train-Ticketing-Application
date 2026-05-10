package com.example.traintickets.train.service;

import com.example.traintickets.common.ResourceNotFoundException;
import com.example.traintickets.common.BadRequestException;
import com.example.traintickets.train.dto.TrainRequest;
import com.example.traintickets.train.model.Train;
import com.example.traintickets.train.repository.TrainRepository;
import com.example.traintickets.trip.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainService {
    private final TrainRepository trainRepository;
    private final TripRepository tripRepository;

    public TrainService(TrainRepository trainRepository, TripRepository tripRepository) {
        this.trainRepository = trainRepository;
        this.tripRepository = tripRepository;
    }

    @Transactional(readOnly = true)
    public List<Train> findAll() {
        return trainRepository.findAll();
    }

    @Transactional
    public Train create(TrainRequest request) {
        return trainRepository.save(new Train(request.name(), request.capacity()));
    }

    @Transactional
    public Train update(Long id, TrainRequest request) {
        Train train = getById(id);
        train.setName(request.name());
        train.setCapacity(request.capacity());
        return train;
    }

    @Transactional
    public void delete(Long id) {
        if (!trainRepository.existsById(id)) {
            throw new ResourceNotFoundException("Train not found: " + id);
        }
        if (tripRepository.existsByTrainId(id)) {
            throw new BadRequestException("Train is used by one or more trips and cannot be removed");
        }
        trainRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Train getById(Long id) {
        return trainRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Train not found: " + id));
    }
}
