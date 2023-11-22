package com.ryanair.models;

import lombok.Data;

import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;

@Data
public class FlightSchedule {
    private int month;
    private List<DaySchedule> days;

    @Data
    public static class DaySchedule{
        private int day;
        private LinkedList<Flight> flights;

        @Data
        public static class Flight{
            private String carrierCode;
            private String number;
            private LocalTime departureTime;
            private LocalTime arrivalTime;
        }
    }
}
