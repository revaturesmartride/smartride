package com.revature.RideService.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * Maps the OSRM /route/v1/driving response.
 *
 * Actual JSON shape:
 * {
 *   "code": "Ok",
 *   "routes": [
 *     {
 *       "distance": 14823.4,   ← metres (double)
 *       "duration": 1920.1     ← seconds (double)
 *     }
 *   ]
 * }
 *
 * routes[0].distance → metres → divide by 1000.0 → km
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OsrmResponse {

    @JsonProperty("code")
    private String code;        // "Ok" on success

    @JsonProperty("routes")
    private List<Route> routes;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Route {

        @JsonProperty("distance")
        private double distance;    // metres

        @JsonProperty("duration")
        private double duration;    // seconds
    }
}