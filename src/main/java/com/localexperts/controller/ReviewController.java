package com.localexperts.controller;

import com.localexperts.model.Business;
import com.localexperts.repository.BusinessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// ── Reviews Controller ────────────────────────────────────────
// GET /api/reviews/recent — latest reviews across all businesses
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private BusinessRepository businessRepository;

    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentReviews(
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "10") int limit) {

        Map<String, Object> response = new HashMap<>();
        try {
            List<Business> businesses = city != null
                    ? businessRepository.findByCityAndStatus(city.toLowerCase(), "active")
                    : businessRepository.findByStatus("active");

            List<Map<String, Object>> allReviews = new ArrayList<>();
            for (Business b : businesses) {
                for (Business.Review r : b.getReviews()) {
                    Map<String, Object> reviewMap = new HashMap<>();
                    reviewMap.put("user", r.getUser());
                    reviewMap.put("name", r.getName());
                    reviewMap.put("rating", r.getRating());
                    reviewMap.put("comment", r.getComment());
                    reviewMap.put("date", r.getDate());
                    reviewMap.put("businessName", b.getName());
                    reviewMap.put("businessCity", b.getCity());
                    reviewMap.put("businessCategory", b.getCategory());
                    allReviews.add(reviewMap);
                }
            }

            allReviews.sort((a, b) -> {
                LocalDateTime da = (LocalDateTime) a.get("date");
                LocalDateTime db = (LocalDateTime) b.get("date");
                return db.compareTo(da);
            });

            List<Map<String, Object>> limited = allReviews.stream().limit(limit).collect(Collectors.toList());

            response.put("success", true);
            response.put("data", limited);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
