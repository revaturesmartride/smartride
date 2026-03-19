package com.revature.RideService.client;

import com.revature.RideService.dto.response.OsrmResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * OSRM (Open Source Routing Machine) — calculates real road distance
 * from two lat/lon coordinates. Free, no API key required.
 *
 * Example:
 *   GET http://router.project-osrm.org/route/v1/driving
 *       /77.6245,12.9352;77.7499,12.9698
 *       ?overview=false
 *
 * Coordinates format: lon,lat  (OSRM uses longitude first)
 */
@FeignClient(name = "osrm-client", url = "${osrm.base-url}")
public interface OsrmClient {

    @GetMapping("/route/v1/driving/{coordinates}?overview=false")
    OsrmResponse getRoute(
            @PathVariable("coordinates") String coordinates  // "lon1,lat1;lon2,lat2"
    );
}
