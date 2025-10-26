package com.zydenlabs.islam313.repository;


import com.zydenlabs.islam313.entity.Masjid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MasjidRepository extends JpaRepository<Masjid, Long> {

    List<Masjid> findByPlaceIdIn(List<String> placeIds);

    // Projection interface to get distance from MySQL
    interface MasjidDistanceProjection {
        Long getId();
        String getPlaceId();
        String getName();
        String getAddress();
        Double getLatitude();
        Double getLongitude();
        Boolean getHasWomenSection();
        Double getDistance();
    }

    // Haversine fallback
    @Query(value = "SELECT * FROM masjid " +
            "WHERE (6371000 * acos( cos(radians(:lat)) * cos(radians(latitude)) * " +
            "cos(radians(longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(latitude)) )) <= :radiusMeters",
            nativeQuery = true)
    List<Masjid> findNearby(@Param("lat") double lat,
                            @Param("lng") double lng,
                            @Param("radiusMeters") double radiusMeters);

    // Spatial query using POINT
    @Query(value = "SELECT *, ST_Distance_Sphere(location, ST_GeomFromText(:point, 4326)) as distance " +
            "FROM masjid " +
            "WHERE ST_Distance_Sphere(location, ST_GeomFromText(:point, 4326)) <= :radiusMeters " +
            "ORDER BY distance ASC",
            nativeQuery = true)
    List<Masjid> findNearbyUsingSpatial(@Param("point") String wktPoint,
                                        @Param("radiusMeters") double radiusMeters);

    @Query(value = "SELECT m.id, m.place_id, m.name, m.address, m.latitude, m.longitude, m.has_women_section, " +
            "ST_Distance_Sphere(m.location, ST_GeomFromText(:point, 4326)) AS distance_meters " +
            "FROM masjid m " +
            "WHERE ST_Distance_Sphere(m.location, ST_GeomFromText(:point, 4326)) <= :radiusMeters " +
            "ORDER BY distance_meters ASC",
            nativeQuery = true)
    List<Object[]> findNearbyWithDistance(
            @Param("point") String wktPoint,
            @Param("radiusMeters") double radiusMeters
    );
}


