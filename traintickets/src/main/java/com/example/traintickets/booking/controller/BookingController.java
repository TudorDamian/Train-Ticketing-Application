package com.example.traintickets.booking.controller;

import com.example.traintickets.booking.dto.BookTicketRequest;
import com.example.traintickets.booking.dto.BookingResponse;
import com.example.traintickets.booking.service.BookingService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    BookingResponse create(@Valid @RequestBody BookTicketRequest request) {
        return BookingResponse.from(bookingService.bookTickets(request));
    }

    @GetMapping("/bookings/{id}")
    BookingResponse get(@PathVariable Long id) {
        return BookingResponse.from(bookingService.getById(id));
    }

    @GetMapping("/bookings")
    List<BookingResponse> findByCustomerEmail(@RequestParam @NotBlank @Email String email) {
        return bookingService.findByCustomerEmail(email).stream().map(BookingResponse::from).toList();
    }

    @GetMapping("/admin/trips/{tripId}/bookings")
    List<BookingResponse> findByTrip(@PathVariable Long tripId) {
        return bookingService.findByTrip(tripId).stream().map(BookingResponse::from).toList();
    }

    @DeleteMapping("/admin/bookings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable Long id) {
        bookingService.delete(id);
    }
}
