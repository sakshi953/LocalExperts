package com.localexperts.controller;

import com.localexperts.model.Business;
import com.localexperts.repository.BusinessRepository;
import com.localexperts.repository.CityRepository;
import com.localexperts.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

// ── Analytics Controller ──────────────────────────────────────
// GET /api/analytics/dashboard   - overall stats
// GET /api/analytics/city-stats  - per-city breakdown
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CityRepository cityRepository;

    // ── GET /api/analytics/dashboard ─────────────────────────
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @RequestParam(required = false) String city) {

        Map<String, Object> response = new HashMap<>();
        try {
            List<Business> allActive = city != null
                    ? businessRepository.findByCityAndStatus(city.toLowerCase(), "active")
                    : businessRepository.findByStatus("active");

            // Overview stats
            long totalBusinesses = allActive.size();
            long totalUsers = userRepository.count();
            long totalCities = cityRepository.countByIsActiveTrue();
            long verifiedCount = allActive.stream().filter(Business::isVerified).count();
            long openNow = allActive.stream().filter(Business::isOpen).count();
            long totalReviews = allActive.stream().mapToInt(Business::getReviewCount).sum();

            // Category breakdown
            Map<String, Long> categoryCount = allActive.stream()
                    .collect(Collectors.groupingBy(Business::getCategory, Collectors.counting()));

            List<Map<String, Object>> categoryBreakdown = categoryCount.entrySet().stream()
                    .map(e -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("_id", e.getKey());
                        m.put("count", e.getValue());
                        double avg = allActive.stream()
                                .filter(b -> b.getCategory().equals(e.getKey()))
                                .mapToDouble(Business::getAvgRating).average().orElse(0);
                        m.put("avgRating", Math.round(avg * 10.0) / 10.0);
                        return m;
                    })
                    .sorted((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")))
                    .collect(Collectors.toList());

            // Top 5 by rating
            List<Business> topByRating = allActive.stream()
                    .sorted((a, b) -> Double.compare(b.getAvgRating(), a.getAvgRating()))
                    .limit(5)
                    .collect(Collectors.toList());

            // Top 5 most searched
            List<Business> topBySearches = allActive.stream()
                    .sorted((a, b) -> b.getSearchCount() - a.getSearchCount())
                    .limit(5)
                    .collect(Collectors.toList());

            // Recently added
            List<Business> recentlyAdded = allActive.stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .limit(5)
                    .collect(Collectors.toList());

            Map<String, Object> overview = new HashMap<>();
            overview.put("totalBusinesses", totalBusinesses);
            overview.put("totalUsers", totalUsers);
            overview.put("totalCities", totalCities);
            overview.put("verifiedCount", verifiedCount);
            overview.put("openNow", openNow);
            overview.put("totalReviews", totalReviews);

            Map<String, Object> data = new HashMap<>();
            data.put("overview", overview);
            data.put("categoryBreakdown", categoryBreakdown);
            data.put("topByRating", topByRating);
            data.put("topBySearches", topBySearches);
            data.put("recentlyAdded", recentlyAdded);

            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ── GET /api/analytics/city-stats ────────────────────────
    @GetMapping("/city-stats")
    public ResponseEntity<Map<String, Object>> getCityStats() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Business> allActive = businessRepository.findByStatus("active");

            Map<String, List<Business>> byCity = allActive.stream()
                    .collect(Collectors.groupingBy(Business::getCity));

            List<Map<String, Object>> cityStats = byCity.entrySet().stream()
                    .map(e -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("_id", e.getKey());
                        m.put("count", e.getValue().size());
                        double avg = e.getValue().stream()
                                .mapToDouble(Business::getAvgRating).average().orElse(0);
                        m.put("avgRating", Math.round(avg * 10.0) / 10.0);
                        long verified = e.getValue().stream().filter(Business::isVerified).count();
                        m.put("verified", verified);
                        return m;
                    })
                    .sorted((a, b) -> Integer.compare((int) b.get("count"), (int) a.get("count")))
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("data", cityStats);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
