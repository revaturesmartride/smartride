package com.revature.RideService.util;


import com.revature.RideService.client.NominatimClient;
import com.revature.RideService.client.OsrmClient;
import com.revature.RideService.dto.response.NominatimResponse;
import com.revature.RideService.dto.response.OsrmResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FareCalculator {

    private final NominatimClient nominatimClient;
    private final OsrmClient osrmClient;

    private static final double BASE_FARE    = 30.0;
    private static final double RATE_PER_KM  = 12.0;
    private static final double MINIMUM_FARE = 50.0;

    public double calculate(String pickup, String drop) {
        log.info("Calculating fare | '{}' → '{}'", pickup, drop);
        validate(pickup, drop);
        double km   = getRoadDistanceKm(pickup, drop);
        double fare = round(Math.max(BASE_FARE + km * RATE_PER_KM, MINIMUM_FARE));
        log.info("Fare: {}km → ₹{}", km, fare);
        return fare;
    }

    public double estimateDistance(String pickup, String drop) {
        validate(pickup, drop);
        return getRoadDistanceKm(pickup, drop);
    }

    private double getRoadDistanceKm(String pickup, String drop) {
        double[] from = geocode(pickup);
        double[] to   = geocode(drop);
        String coordinates = from[1] + "," + from[0] + ";" + to[1] + "," + to[0];
        log.info("OSRM coordinates: {}", coordinates);
        OsrmResponse osrm = osrmClient.getRoute(coordinates);
        if (osrm == null || !"Ok".equalsIgnoreCase(osrm.getCode())
                || osrm.getRoutes() == null || osrm.getRoutes().isEmpty()) {
            throw new IllegalStateException(
                    "OSRM could not find a route between: " + pickup + " → " + drop);
        }
        double metres = osrm.getRoutes().get(0).getDistance();
        double km     = round(metres / 1000.0);
        log.info("Road distance: {}m = {}km", metres, km);
        return km;
    }

    private double[] geocode(String address) {
        log.info("Geocoding: '{}'", address);
        List<NominatimResponse> results = nominatimClient.search(address, "json", 1);
        if (results == null || results.isEmpty()) {
            throw new IllegalStateException(
                    "Nominatim could not geocode address: " + address);
        }
        NominatimResponse result = results.get(0);
        double lat = Double.parseDouble(result.getLat());
        double lon = Double.parseDouble(result.getLon());
        log.info("Geocoded '{}' → lat={} lon={}", address, lat, lon);
        return new double[]{ lat, lon };
    }

    private void validate(String pickup, String drop) {
        if (pickup == null || pickup.isBlank())
            throw new IllegalArgumentException("Pickup location must not be blank");
        if (drop == null || drop.isBlank())
            throw new IllegalArgumentException("Drop location must not be blank");
        if (pickup.trim().equalsIgnoreCase(drop.trim()))
            throw new IllegalArgumentException("Pickup and drop must be different");
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}