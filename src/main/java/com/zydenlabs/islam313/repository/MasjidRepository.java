package com.zydenlabs.islam313.repository;


import com.zydenlabs.islam313.entity.Masjid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MasjidRepository extends JpaRepository<Masjid, Long> {

    List<Masjid> findByPlaceIdIn(List<String> placeIds);

    // Haversine native query to find nearby masjids by radius in meters
    @Query(value = "SELECT * FROM masjid " +
            "WHERE (6371000 * acos( cos(radians(:lat)) * cos(radians(latitude)) * " +
            "cos(radians(longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(latitude)) )) <= :radiusMeters",
            nativeQuery = true)
    List<Masjid> findNearby(@Param("lat") double lat,
                            @Param("lng") double lng,
                            @Param("radiusMeters") double radiusMeters);
}

