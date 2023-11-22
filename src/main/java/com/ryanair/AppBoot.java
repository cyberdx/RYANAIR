package com.ryanair;

import com.ryanair.models.FlightRequest;
import com.ryanair.models.FlightResult;
import com.ryanair.services.FlightService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.function.Function;

@SpringBootApplication
public class AppBoot {

    private final FlightService flightService;

    public AppBoot(FlightService flightService) {
        this.flightService = flightService;
    }

    @Bean
    public Function<FlightRequest, List<FlightResult>> findFlight() {
        return flightService::processFlights;
    }

    public static void main(String[] args) {
        SpringApplication.run(AppBoot.class, args);
    }

}
