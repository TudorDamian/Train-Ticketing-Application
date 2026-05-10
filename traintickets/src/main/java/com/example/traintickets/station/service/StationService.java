package com.example.traintickets.station.service;

import com.example.traintickets.common.BadRequestException;
import com.example.traintickets.common.ResourceNotFoundException;
import com.example.traintickets.booking.repository.BookingRepository;
import com.example.traintickets.route.repository.RouteStopRepository;
import com.example.traintickets.station.dto.StationRequest;
import com.example.traintickets.station.model.Station;
import com.example.traintickets.station.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StationService {
    private final StationRepository stationRepository;
    private final RouteStopRepository routeStopRepository;
    private final BookingRepository bookingRepository;

    public StationService(StationRepository stationRepository,
                          RouteStopRepository routeStopRepository,
                          BookingRepository bookingRepository) {
        this.stationRepository = stationRepository;
        this.routeStopRepository = routeStopRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<Station> findAll() {
        return stationRepository.findAll();
    }

    @Transactional
    public Station create(StationRequest request) {
        String code = normalizeCode(request.code());
        if (stationRepository.existsByCodeIgnoreCase(code)) {
            throw new BadRequestException("Station code already exists: " + code);
        }
        return stationRepository.save(new Station(request.name(), code));
    }

    @Transactional
    public Station update(Long id, StationRequest request) {
        Station station = getById(id);
        String code = normalizeCode(request.code());
        if (stationRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new BadRequestException("Station code already exists: " + code);
        }
        station.setName(request.name());
        station.setCode(code);
        return station;
    }

    @Transactional
    public void delete(Long id) {
        if (!stationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Station not found: " + id);
        }
        if (routeStopRepository.existsByStationId(id)) {
            throw new BadRequestException("Station is used by one or more routes and cannot be removed");
        }
        if (bookingRepository.existsByFromStationIdOrToStationId(id, id)) {
            throw new BadRequestException("Station is used by one or more bookings and cannot be removed");
        }
        stationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Station getById(Long id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found: " + id));
    }

    @Transactional(readOnly = true)
    public Station getByCode(String code) {
        return stationRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Station not found: " + code));
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }
}
