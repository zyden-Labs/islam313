package com.zydenlabs.islam313.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "masjid", uniqueConstraints = @UniqueConstraint(columnNames = "place_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Masjid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_id", nullable = false, unique = true)
    private String placeId;        // Google place_id - our de-dup key

    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private Boolean hasWomenSection = false;

    // prayer times as LocalTime (no timezone for MVP)
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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

