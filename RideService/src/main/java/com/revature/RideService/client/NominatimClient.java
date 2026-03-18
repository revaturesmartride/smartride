package com.revature.RideService.client;

import com.revature.RideService.dto.response.NominatimResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Nominatim (OpenStreetMap) — converts address string → lat/lon
 * Free, no API key required.
 * Rate limit: 1 request/second (per OSM usage policy)
 *
 * Example:
 *   GET https://nominatim.openstreetmap.org/search
 *       ?q=Koramangala,Bangalore
 *       &format=json
 *       &limit=1
 */
@FeignClient(name = "nominatim-client", url = "${nominatim.base-url}")
public interface NominatimClient {

    @GetMapping("/search")
    List<NominatimResponse> search(
            @RequestParam("q")       String address,
            @RequestParam("format")  String format,
            @RequestParam("limit")   int limit
    );
}