package com.ryanair.models;

import lombok.Data;

import java.util.List;

@Data
public class FlightRoute {
    private String airportFrom;
    private String airportTo;
    private String connectingAirport;
    private boolean newRoute;
    private boolean seasonalRoute;
    private String operator;
    private String carrierCode;
    private String group;
    private List<String> similarArrivalAirportCodes;
    private List<String> tags;
}
