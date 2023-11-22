package com.ryanair.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ryanair.TestConfig;
import com.ryanair.models.*;
import com.ryanair.services.utils.JsonService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FlightServiceImpTest extends TestConfig {

    @Autowired
    private JsonService jsonService;
    private FlightService flightService;
    private ApiDataService apiDataServiceMock;

    @Autowired
    private ApiDataService apiDataService;

    private final boolean isLive = false;

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        apiDataServiceMock = Mockito.mock(ApiDataService.class);
        List<FlightRoute> routes = jsonService.mapper(readResourceAsString("routes.json"), new TypeReference<List<FlightRoute>>() {
        });
        Mockito.when(apiDataServiceMock.getRoutes())
                .thenReturn(routes);

        Mockito.when(apiDataServiceMock.getScheduler("DUB", "LBA", 2023, 12))
                .thenReturn(jsonService.mapper(readResourceAsString("scheduler1.json"), FlightSchedule.class));

        Mockito.when(apiDataServiceMock.getScheduler("DUB", "WMI", 2023, 12))
                .thenReturn(jsonService.mapper(readResourceAsString("scheduler2.json"), FlightSchedule.class));

        Mockito.when(apiDataServiceMock.getScheduler("WMI", "LBA", 2023, 12))
                .thenReturn(jsonService.mapper(readResourceAsString("scheduler3.json"), FlightSchedule.class));

        flightService = isLive ? new FlightServiceImp(apiDataService) : new FlightServiceImp(apiDataServiceMock);
    }

    @Test
    public void testFindFlight() {
        FlightRequest flightRequest = new FlightRequest("DUB", LocalDateTime.parse("2023-12-01T05:00:00"), "LBA", LocalDateTime.parse("2023-12-01T21:00:00"));
        List<FlightResult> result = flightService.processFlights(flightRequest);

        assertNotNull(result, "error message");
        assertEquals(3, result.size(), "error message");

        int stops = result.stream()
                .mapToInt(FlightResult::getStops)
                .max()
                .orElseThrow(() -> new NoSuchElementException("error message"));
        assertEquals(1, stops, "error message");

        FlightLeg flightLeg = result.get(0).getLegs().get(0);
        assertEquals("DUB", flightLeg.getDepartureAirport());
        assertEquals("LBA", flightLeg.getArrivalAirport());
        ChronoUnit.HOURS.between(flightRequest.getDepartureDateTime(), flightLeg.getDepartureDateTime());
        assertEquals(155, ChronoUnit.MINUTES.between(flightRequest.getDepartureDateTime(), flightLeg.getDepartureDateTime()));
        assertEquals(555, ChronoUnit.MINUTES.between(flightLeg.getArrivalDateTime(), flightRequest.getArrivalDateTime()));
        assertEquals(2, result.get(2).getLegs().size());

        //and more, up to time limit
    }

    @SneakyThrows
    public String readResourceAsString(String resourceName) {
        return StreamUtils.copyToString(new ClassPathResource("/" + resourceName).getInputStream(), StandardCharsets.UTF_8);
    }
}