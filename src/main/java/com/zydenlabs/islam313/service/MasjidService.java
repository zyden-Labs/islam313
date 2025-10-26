package com.zydenlabs.islam313.service;

import com.zydenlabs.islam313.client.GooglePlacesClient;
import com.zydenlabs.islam313.dto.MasjidDTO;
import com.zydenlabs.islam313.entity.Masjid;
import com.zydenlabs.islam313.repository.MasjidRepository;
import com.zydenlabs.islam313.utils.GeoUtils;
import lombok.Data;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
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
        // Fetch from Google
        List<MasjidDTO> google = googlePlacesClient.fetchNearbyMasjids(lat, lng, radiusMeters);
        if (google.isEmpty()) return Collections.emptyList();

        // Check existing
        List<String> placeIds = google.stream()
                .map(MasjidDTO::getPlaceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Masjid> existing = masjidRepository.findByPlaceIdIn(placeIds);
        Map<String, Masjid> existingMap = existing.stream()
                .collect(Collectors.toMap(Masjid::getPlaceId, Function.identity()));

        List<MasjidDTO> result = existing.stream().map(this::mapToDTO).collect(Collectors.toList());

        // Insert new Masjids
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
                // Reload in case of race condition
                List<String> newPlaceIds = toInsert.stream()
                        .map(Masjid::getPlaceId)
                        .collect(Collectors.toList());
                List<Masjid> reloaded = masjidRepository.findByPlaceIdIn(newPlaceIds);
                reloaded.forEach(m -> result.add(mapToDTO(m)));
            }
        }

        // Use spatial query to get distances and sort
        return findNearbyFromDb(lat, lng, radiusMeters);
    }

    public List<MasjidDTO> findNearbyFromDb(double lat, double lng, double radiusMeters) {
        String wktPoint = String.format("POINT(%f %f)", lng, lat);
        List<Object[]> results = masjidRepository.findNearbyWithDistance(wktPoint, radiusMeters);

        List<MasjidDTO> dtos = new ArrayList<>();
        for (Object[] row : results) {
            MasjidDTO dto = new MasjidDTO();
            dto.setId(((Number) row[0]).longValue());
            dto.setPlaceId((String) row[1]);
            dto.setName((String) row[2]);
            dto.setAddress((String) row[3]);
            dto.setLatitude(((Number) row[4]).doubleValue());
            dto.setLongitude(((Number) row[5]).doubleValue());
            dto.setHasWomenSection((Boolean) row[6]);
            dto.setDistanceMeters(((Number) row[7]).doubleValue());
            dtos.add(dto);
        }

        return dtos;
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
                .location(GeoUtils.createPoint(d.getLongitude(), d.getLatitude())) // x=lng, y=lat
                .hasWomenSection(Boolean.TRUE.equals(d.getHasWomenSection()))
                .build();
    }
}



