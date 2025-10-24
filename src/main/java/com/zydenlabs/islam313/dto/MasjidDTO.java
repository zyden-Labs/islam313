package com.zydenlabs.islam313.dto;

import lombok.Data;
import java.time.LocalTime;

@Data
public class MasjidDTO {
    private Long id;
    private String placeId;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private Boolean hasWomenSection;

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
}

