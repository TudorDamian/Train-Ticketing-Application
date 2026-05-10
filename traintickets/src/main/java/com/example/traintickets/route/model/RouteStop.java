package com.example.traintickets.route.model;

import com.example.traintickets.station.model.Station;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "route_stops")
public class RouteStop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id")
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id")
    private Station station;

    @Column(name = "stop_order", nullable = false)
    private int stopOrder;

    @Column(name = "arrival_offset_minutes", nullable = false)
    private int arrivalOffsetMinutes;

    @Column(name = "departure_offset_minutes", nullable = false)
    private int departureOffsetMinutes;

    protected RouteStop() {
    }

    public RouteStop(Station station, int stopOrder, int arrivalOffsetMinutes, int departureOffsetMinutes) {
        this.station = station;
        this.stopOrder = stopOrder;
        this.arrivalOffsetMinutes = arrivalOffsetMinutes;
        this.departureOffsetMinutes = departureOffsetMinutes;
    }

    public Long getId() {
        return id;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public Station getStation() {
        return station;
    }

    public int getStopOrder() {
        return stopOrder;
    }

    public int getArrivalOffsetMinutes() {
        return arrivalOffsetMinutes;
    }

    public int getDepartureOffsetMinutes() {
        return departureOffsetMinutes;
    }
}
