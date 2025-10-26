package com.zydenlabs.islam313.service;



import com.zydenlabs.islam313.client.GooglePlacesClient;
import com.zydenlabs.islam313.dto.MasjidDTO;
import com.zydenlabs.islam313.entity.Masjid;
import com.zydenlabs.islam313.repository.MasjidRepository;
import com.zydenlabs.islam313.utils.GeoUtils;
import jakarta.transaction.Transactional;
import org.locationtech.jts.geom.Point;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MasjidService {

    private final MasjidRepository masjidRepository;
    private final GooglePlacesClient googlePlacesClient;

    public MasjidService(MasjidRepository masjidRepository, GooglePlacesClient googlePlacesClient) {
        this.masjidRepository = masjidRepository;
        this.googlePlacesClient = googlePlacesClient;
    }

    @Transactional
    public List<MasjidDTO> fetchNearbyAndSave(double lat, double lng, int radiusMeters) {
        List<MasjidDTO> google = googlePlacesClient.fetchNearbyMasjids(lat, lng, radiusMeters);
        if (google.isEmpty()) return Collections.emptyList();

        List<String> placeIds = google.stream()
                .map(MasjidDTO::getPlaceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<Masjid> existing = masjidRepository.findByPlaceIdIn(placeIds);
        Map<String, Masjid> existingMap = existing.stream()
                .collect(Collectors.toMap(Masjid::getPlaceId, Function.identity()));

        List<MasjidDTO> result = new ArrayList<>();
        existing.forEach(m -> result.add(mapToDTO(m)));

        List<Masjid> toInsert = new ArrayList<>();
        for (MasjidDTO dto : google) {
            if (!existingMap.containsKey(dto.getPlaceId())) {
                Masjid e = mapToEntity(dto);
                toInsert.add(e);
            }
        }

        if (!toInsert.isEmpty()) {
            try {
                List<Masjid> saved = masjidRepository.saveAll(toInsert);
                saved.forEach(m -> result.add(mapToDTO(m)));
            } catch (DataIntegrityViolationException ex) {
                List<String> newPlaceIds = toInsert.stream()
                        .map(Masjid::getPlaceId)
                        .collect(Collectors.toList());
                List<Masjid> reloaded = masjidRepository.findByPlaceIdIn(newPlaceIds);
                reloaded.forEach(m -> result.add(mapToDTO(m)));
            }
        }

        // ✅ Use spatial query for distance and sorting
        String wktPoint = String.format("POINT(%f %f)", lng, lat);
        List<Masjid> nearbyMasjids = masjidRepository.findNearbyUsingSpatial(wktPoint, radiusMeters);

        List<MasjidDTO> spatialResult = nearbyMasjids.stream()
                .map(m -> {
                    MasjidDTO dto = mapToDTO(m);
                    // distance is already calculated by ST_Distance_Sphere in SQL
                    return dto;
                })
                .collect(Collectors.toList());

        return spatialResult;
    }

    public List<MasjidDTO> findNearbyFromDb(double lat, double lng, double radiusMeters) {
        // ✅ Spatial query

        String wktPoint = String.format("POINT(%f %f)", lng, lat);
        List<Masjid> nearbyMasjids = masjidRepository.findNearbyUsingSpatial(wktPoint, radiusMeters);

        return nearbyMasjids.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private MasjidDTO mapToDTO(Masjid m) {
        MasjidDTO d = new MasjidDTO();
        d.setId(m.getId());
        d.setPlaceId(m.getPlaceId());
        d.setName(m.getName());
        d.setAddress(m.getAddress());
        d.setLatitude(m.getLatitude());
        d.setLongitude(m.getLongitude());
        d.setHasWomenSection(m.getHasWomenSection());
        d.setFajrAzanTime(m.getFajrAzanTime());
        d.setFajrNamazTime(m.getFajrNamazTime());
        d.setDhuhrAzanTime(m.getDhuhrAzanTime());
        d.setDhuhrNamazTime(m.getDhuhrNamazTime());
        d.setAsrAzanTime(m.getAsrAzanTime());
        d.setAsrNamazTime(m.getAsrNamazTime());
        d.setMaghribAzanTime(m.getMaghribAzanTime());
        d.setMaghribNamazTime(m.getMaghribNamazTime());
        d.setIshaAzanTime(m.getIshaAzanTime());
        d.setIshaNamazTime(m.getIshaNamazTime());
        d.setFridayKhutbahTime(m.getFridayKhutbahTime());
        return d;
    }



    private Masjid mapToEntity(MasjidDTO d) {
        return Masjid.builder()
                .placeId(d.getPlaceId())
                .name(d.getName())
                .address(d.getAddress())
                .latitude(d.getLatitude())
                .longitude(d.getLongitude())
                .location(GeoUtils.createPoint(d.getLatitude(), d.getLongitude()))
                .hasWomenSection(Boolean.TRUE.equals(d.getHasWomenSection()))
                .build();
    }

}



