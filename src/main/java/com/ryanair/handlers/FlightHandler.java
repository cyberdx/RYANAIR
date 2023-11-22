package com.ryanair.handlers;

import com.ryanair.models.FlightRequest;
import com.ryanair.models.FlightResult;
import org.springframework.cloud.function.adapter.aws.SpringBootRequestHandler;

import java.util.List;

public class FlightHandler extends SpringBootRequestHandler<FlightRequest, List<FlightResult>> {
}
