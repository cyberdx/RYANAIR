package com.ryanair.models;

import lombok.Data;

import java.util.List;

@Data
public class FlightResponse {
    private List<FlightResult> flights;
}