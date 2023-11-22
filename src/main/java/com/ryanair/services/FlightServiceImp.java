package com.ryanair.services;

import com.ryanair.models.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Log4j2
@Component
public class FlightServiceImp implements FlightService {
    private final ApiDataService apiDataService;
    private static final String RYANAIR_OPERATOR = "RYANAIR";

    public FlightServiceImp(ApiDataService apiDataService) {
        this.apiDataService = apiDataService;
    }

    @Override
    public List<FlightResult> processFlights(FlightRequest request) {
        List<FlightRoute> allRoutes = apiDataService.getRoutes();

        List<FlightRoute> routesTo = allRoutes.stream()
                .filter(f -> f.getOperator().equals(RYANAIR_OPERATOR) && f.getAirportTo().equals(request.getArrival()))
                .collect(Collectors.toList());

        List<FlightRoute> activeRoutes = allRoutes.stream()
                .filter(f -> f.getOperator().equals(RYANAIR_OPERATOR) && f.getAirportFrom().equals(request.getDeparture()))
                .filter(f -> routesTo.stream().anyMatch(t -> t.getAirportFrom().equals(f.getAirportTo())) || f.getAirportTo().equals(request.getArrival()))
                .collect(Collectors.toList());

        return generateFlights(activeRoutes, request);
    }

    private List<FlightResult> generateFlights(List<FlightRoute> flightRoutes, FlightRequest request) {
        List<FlightResult> flights = new ArrayList<>();

        flightRoutes.forEach(route -> {
            FlightResult flightResult = new FlightResult();
            flightResult.addLeg(FlightLeg.builder()
                    .departureAirport(request.getDeparture())
                    .arrivalAirport(route.getAirportTo())
                    .build());
            if (!route.getAirportTo().equals(request.getArrival())) {
                flightResult.addLeg(FlightLeg.builder()
                        .departureAirport(route.getAirportTo())
                        .arrivalAirport(request.getArrival())
                        .build());
            }
            List<FlightResult> flightResultList = findFlights(flightResult, request);

            if (flightResultList != null) {
                flights.addAll(flightResultList);
            }
        });

        return flights;
    }

    private List<FlightResult> findFlights(FlightResult flightResult, FlightRequest request) {
        List<FlightResult> flightResultList = new ArrayList<>();
        List<FlightLeg> flightLegs = new ArrayList<>();

        //pick up all matched flights
        for (FlightLeg leg : flightResult.getLegs()) {
            FlightSchedule.DaySchedule scheduleDays = getFilteredDates(leg, request);
            if (Objects.isNull(scheduleDays)) {
                continue;
            }

            scheduleDays.getFlights().forEach(flight -> {
                leg.setDepartureDateTime(request.getDepartureDateTime().toLocalDate().atTime(flight.getDepartureTime()));
                leg.setArrivalDateTime(request.getArrivalDateTime().toLocalDate().atTime(flight.getArrivalTime()));

                flightLegs.add(FlightLeg.builder()
                        .departureAirport(leg.getDepartureAirport())
                        .arrivalAirport(leg.getArrivalAirport())
                        .departureDateTime(leg.getDepartureDateTime())
                        .arrivalDateTime(leg.getArrivalDateTime())
                        .build());
            });
        }

        if ((flightLegs.size() < flightResult.getStops() + 1)
                || flightLegs.stream().noneMatch(l -> l.getDepartureAirport().equals(request.getDeparture()))
                || flightLegs.stream().noneMatch(l -> l.getArrivalAirport().equals(request.getArrival()))
        ) {
            return null;
        }

        //create route legs
        if (flightResult.getStops() == 1) {
            flightLegs.forEach(leg -> {
                if (leg.getDepartureAirport().equals(request.getDeparture())) {
                    flightLegs.forEach(transit -> {
                        if (transit.getArrivalAirport().equals(request.getArrival()) && ChronoUnit.HOURS.between(leg.getArrivalDateTime(), transit.getDepartureDateTime()) >= 2) {
                            FlightResult flightTransit = new FlightResult();
                            flightTransit.addLeg(leg);
                            flightTransit.addLeg(transit);
                            flightResultList.add(flightTransit);
                        }
                    });
                }
            });
        } else {
            flightLegs.forEach(flightLeg -> flightResultList.add(new FlightResult().addLeg(flightLeg)));
        }

        return flightResultList.size() == 0 ? null : flightResultList;
    }

    private FlightSchedule.DaySchedule getFilteredDates(FlightLeg flightLeg, FlightRequest request) {
        int flightDay = request.getDepartureDateTime().getDayOfMonth();
        FlightSchedule schedule = apiDataService.getScheduler(flightLeg.getDepartureAirport(), flightLeg.getArrivalAirport(), request.getDepartureDateTime().getYear(), request.getDepartureDateTime().getMonthValue());

        if (Objects.isNull(schedule)) {
            log.error("Error message");
            return null;
        }

        FlightSchedule.DaySchedule scheduleDays = schedule.getDays().stream()
                .filter(s -> s.getDay() == flightDay)
                .findFirst()
                .orElse(null);

        if (Objects.isNull(scheduleDays)) {
            return null;
        }

        //filter in time range
        scheduleDays.getFlights().removeIf(flight ->
                flight.getDepartureTime().isBefore(request.getDepartureDateTime().toLocalTime())
                        || flight.getArrivalTime().isAfter(request.getArrivalDateTime().toLocalTime())
        );

        return scheduleDays.getFlights().size() == 0 ? null : scheduleDays;
    }
}
