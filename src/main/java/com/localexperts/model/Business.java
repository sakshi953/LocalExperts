package com.localexperts.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// ── Business Model ────────────────────────────────────────────
@Data
@NoArgsConstructor
@Document(collection = "businesses")
public class Business {

    @Id
    private String id;

    private String name;

    private String category; // Hotels, Food, Healthcare, etc.

    private String icon = "🏪";

    private String description;

    private Address address = new Address();

    private String city;

    private Contact contact = new Contact();

    private String hours = "Check with business";

    private boolean isOpen = true;

    private boolean isVerified = false;

    private String badge;

    private List<String> products = new ArrayList<>();

    private List<String> highlights = new ArrayList<>();

    private double mapX = 300;
    private double mapY = 250;

    private String gradient = "linear-gradient(135deg,#6c63ff22,#a29bfe22)";

    private List<Review> reviews = new ArrayList<>();

    private double avgRating = 0;
    private int reviewCount = 0;
    private int searchCount = 0;
    private int viewCount = 0;

    private String owner;

    private String gstin; // GST Identification Number (business owners)

    private String status = "pending"; // pending, active, rejected

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ── Recalculate avg rating ────────────────────────────────
    public void updateRating() {
        if (reviews.isEmpty()) {
            this.avgRating = 0;
            this.reviewCount = 0;
            return;
        }
        double sum = reviews.stream().mapToInt(Review::getRating).sum();
        this.avgRating = Math.round((sum / reviews.size()) * 10.0) / 10.0;
        this.reviewCount = reviews.size();
    }

    // ── Recommendation Score ──────────────────────────────────
    // Formula: (Rating × 0.6) + (Popularity × 0.4)
    public double getRecommendScore() {
        double popularity = Math.min(reviewCount / 100.0, 1.0);
        return Math.round((avgRating * 0.6 + popularity * 0.4) * 100.0) / 100.0;
    }

    // ── Inner classes ─────────────────────────────────────────
    @Data
    @NoArgsConstructor
    public static class Address {
        private String street;
        private String city;
        private String state = "Maharashtra";
        private String pincode;
        private String full;
    }

    @Data
    @NoArgsConstructor
    public static class Contact {
        private String phone;
        private String whatsapp;
        private String email;
        private String website;
    }

    @Data
    @NoArgsConstructor
    public static class Review {
        private String user;
        private String name;
        private int rating;
        private String comment;
        private LocalDateTime date = LocalDateTime.now();
    }
}
