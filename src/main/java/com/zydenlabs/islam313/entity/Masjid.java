package com.zydenlabs.islam313.entity;


import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.time.Instant;
import java.time.LocalTime;

import static com.zydenlabs.islam313.utils.GeoUtils.createPoint;

@Entity
@Table(
        name = "masjid",
        uniqueConstraints = @UniqueConstraint(columnNames = "place_id"),
        indexes = {
                @Index(name = "idx_masjid_location", columnList = "location", unique = false)
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Masjid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_id", nullable = false, unique = true)
    private String placeId;

    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private Boolean hasWomenSection = false;


    @Column(columnDefinition = "POINT SRID 4326", nullable = false)
    private Point location;

    // Prayer times
    private LocalTime fajrAzanTime;
    private LocalTime fajrNamazTime;
    private LocalTime dhuhrAzanTime;
    private LocalTime dhuhrNamazTime;
    private LocalTime asrAzanTime;
    private LocalTime asrNamazTime;
    private LocalTime maghribAzanTime;
    private LocalTime maghribNamazTime;
    private LocalTime ishaAzanTime;
    private LocalTime ishaNamazTime;
    private LocalTime fridayKhutbahTime;

    // Use Instant (UTC) for timestamps
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();  // Always UTC
        updatedAt = Instant.now();  // Always UTC
        if (latitude != null && longitude != null) {
            this.location = createPoint(latitude, longitude);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();  // Always UTC
        if (latitude != null && longitude != null) {
            this.location = createPoint(latitude, longitude);
        }
    }





}

