package com.revature.RideService.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Maps one result from Nominatim /search response.
 *
 * Actual JSON shape:
 * [
 *   {
 *     "lat": "12.9352403",
 *     "lon": "77.6244807",
 *     "display_name": "Koramangala, Bangalore..."
 *   }
 * ]
 *
 * Note: lat/lon come as Strings from Nominatim — parsed to double in FareCalculator.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NominatimResponse {

    @JsonProperty("lat") //to get value of name lat from json
    private String lat;

    @JsonProperty("lon")
    private String lon;

    @JsonProperty("display_name")
    private String displayName;
}