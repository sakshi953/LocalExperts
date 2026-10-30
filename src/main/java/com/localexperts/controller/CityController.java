package com.localexperts.controller;

import com.localexperts.model.City;
import com.localexperts.model.User;
import com.localexperts.repository.CityRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ── City Controller ───────────────────────────────────────────
// GET  /api/cities       - get all active cities
// POST /api/cities       - add city (admin only)
@RestController
@RequestMapping("/api/cities")
public class CityController {

    @Autowired
    private CityRepository cityRepository;

    // ── GET /api/cities ───────────────────────────────────────
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCities() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<City> cities = cityRepository.findByIsActiveTrueOrderByNameAsc();
            response.put("success", true);
            response.put("data", cities);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ── POST /api/cities (Admin only) ─────────────────────────
    @PostMapping
    public ResponseEntity<Map<String, Object>> addCity(
            @RequestBody City city,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) request.getAttribute("currentUser");

        if (currentUser == null || !currentUser.getRole().equals("admin")) {
            response.put("success", false);
            response.put("message", "Admin access required");
            return ResponseEntity.status(403).body(response);
        }

        try {
            City saved = cityRepository.save(city);
            response.put("success", true);
            response.put("data", saved);
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
