package com.ryanair.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ryanair.models.FlightRoute;
import com.ryanair.models.FlightSchedule;
import com.ryanair.services.utils.JsonService;
import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ApiDataServiceImp implements ApiDataService {
    @Value("${request.url.routes}")
    private String routesUrl;
    @Value("${request.url.schedulers}")
    private String schedulersUrl;
    @Autowired
    private JsonService jsonService;
    private final Cache<String, List<FlightRoute>> flightRoutesCache = CacheBuilder.newBuilder()
            .expireAfterAccess(3, TimeUnit.DAYS)
            .build();
    private final Cache<String, FlightSchedule> flightScheduleCache = CacheBuilder.newBuilder()
            .expireAfterAccess(3, TimeUnit.DAYS)
            .build();

    @SneakyThrows
    @Override
    public List<FlightRoute> getRoutes() {
        if (flightRoutesCache.asMap().containsKey(routesUrl)) {
            return flightRoutesCache.asMap().get(routesUrl);
        }

        String jsonContent = Jsoup.connect(routesUrl)
                .header("Content-Type", "application/json")
                .ignoreContentType(true)
                .method(Connection.Method.GET)
                .execute()
                .body();

        List<FlightRoute> flightRoutes = jsonService.mapper(jsonContent, new TypeReference<List<FlightRoute>>() {
        });
        flightRoutesCache.put(routesUrl, flightRoutes);

        return flightRoutes;
    }

    @SneakyThrows
    @Override
    public FlightSchedule getScheduler(String departure, String arrival, int year, int month) {
        String requestUri = String.format(schedulersUrl, departure, arrival, year, month);

        if (flightScheduleCache.asMap().containsKey(requestUri)) {
            return flightScheduleCache.asMap().get(requestUri);
        }

        String jsonContent = Jsoup.connect(requestUri)
                .header("Content-Type", "application/json")
                .ignoreContentType(true)
                .method(Connection.Method.GET)
                .execute()
                .body();

        FlightSchedule schedule = jsonService.mapper(jsonContent, FlightSchedule.class);
        flightScheduleCache.put(requestUri, schedule);

        return schedule;
    }
}
