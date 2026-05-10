package com.example.traintickets.train.controller;

import com.example.traintickets.train.dto.TrainRequest;
import com.example.traintickets.train.dto.TrainResponse;
import com.example.traintickets.train.service.TrainService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TrainController {
    private final TrainService trainService;

    public TrainController(TrainService trainService) {
        this.trainService = trainService;
    }

    @GetMapping("/trains")
    List<TrainResponse> findAll() {
        return trainService.findAll().stream().map(TrainResponse::from).toList();
    }

    @PostMapping("/admin/trains")
    @ResponseStatus(HttpStatus.CREATED)
    TrainResponse create(@Valid @RequestBody TrainRequest request) {
        return TrainResponse.from(trainService.create(request));
    }

    @PutMapping("/admin/trains/{id}")
    TrainResponse update(@PathVariable Long id, @Valid @RequestBody TrainRequest request) {
        return TrainResponse.from(trainService.update(id, request));
    }

    @DeleteMapping("/admin/trains/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        trainService.delete(id);
    }
}
