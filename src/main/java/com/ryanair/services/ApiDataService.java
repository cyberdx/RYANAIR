package com.ryanair.services;

import com.ryanair.models.FlightRoute;
import com.ryanair.models.FlightSchedule;

import java.util.List;

public interface ApiDataService {
    List<FlightRoute> getRoutes();

    FlightSchedule getScheduler(String departure, String arrival, int year, int month);
}
