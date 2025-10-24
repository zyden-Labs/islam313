package com.zydenlabs.islam313.client;


import com.zydenlabs.islam313.dto.MasjidDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class GooglePlacesClient {

    private final RestTemplate restTemplate;

    @Value("${google.api.key}")
    private String googleApiKey;

    public GooglePlacesClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    public List<MasjidDTO> fetchNearbyMasjids(double lat, double lng, int radiusMeters) {
        String url = UriComponentsBuilder.fromHttpUrl("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
                .queryParam("key", googleApiKey)
                .queryParam("location", lat + "," + lng)
                .queryParam("radius", radiusMeters)
                .queryParam("type", "mosque")
                .toUriString();

        Map<String, Object> resp = restTemplate.getForObject(url, Map.class);
        if (resp == null || resp.get("results") == null) return new ArrayList<>();

        List<Map<String, Object>> results = (List<Map<String, Object>>) resp.get("results");
        List<MasjidDTO> dtos = new ArrayList<>();
        for (Map<String, Object> r : results) {
            MasjidDTO d = new MasjidDTO();
            d.setPlaceId((String) r.get("place_id"));
            d.setName((String) r.get("name"));
            d.setAddress((String) r.get("vicinity"));

            Map<String, Object> geometry = (Map<String, Object>) r.get("geometry");
            if (geometry != null && geometry.get("location") != null) {
                Map<String, Object> loc = (Map<String, Object>) geometry.get("location");
                d.setLatitude(((Number) loc.get("lat")).doubleValue());
                d.setLongitude(((Number) loc.get("lng")).doubleValue());
            }
            dtos.add(d);
        }
        return dtos;
    }
}

