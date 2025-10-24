package com.zydenlabs.islam313.controller;



import com.zydenlabs.islam313.dto.ApiResponse;
import com.zydenlabs.islam313.dto.MasjidDTO;
import com.zydenlabs.islam313.service.MasjidService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/masjids")
public class MasjidController {

    private final MasjidService masjidService;

    public MasjidController(MasjidService masjidService) {
        this.masjidService = masjidService;
    }

    // Calls Google + saves new masjids, then returns combined list
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<MasjidDTO>>> nearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "1000") int radiusMeters) {


        List<MasjidDTO> list = masjidService.fetchNearb yAndSave(lat, lng, radiusMeters);
        return ResponseEntity.ok(new ApiResponse<>("Success", "Masjids fetched", list));
    }

    // Search only inside our DB (no external calls)
    @GetMapping("/nearby-db")
    public ResponseEntity<ApiResponse<List<MasjidDTO>>> nearbyDb(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "1000") double radiusMeters) {

        List<MasjidDTO> list = masjidService.findNearbyFromDb(lat, lng, radiusMeters);
        return ResponseEntity.ok(new ApiResponse<>("Success", "Masjids from DB", list));
    }
}

