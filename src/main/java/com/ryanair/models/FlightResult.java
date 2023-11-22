package com.ryanair.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FlightResult {
    private int stops;
    private List<FlightLeg> legs = new ArrayList<>();

    public FlightResult addLeg(FlightLeg leg) {
        this.legs.add(leg);
        this.stops = legs.size() -1;
        return this;
    }
}