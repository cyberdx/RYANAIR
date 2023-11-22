package com.ryanair.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightRequest {
    private String departure;
    private LocalDateTime departureDateTime;
    private String arrival;
    private LocalDateTime arrivalDateTime;
}
