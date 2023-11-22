package com.ryanair.services;

import com.ryanair.models.FlightRequest;
import com.ryanair.models.FlightResult;

import java.util.List;

public interface FlightService {
    List<FlightResult> processFlights(FlightRequest request);

}
