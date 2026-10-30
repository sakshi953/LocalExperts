package com.localexperts.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// ── User Model ────────────────────────────────────────────────
@Data
@NoArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String email;

    private String password;

    private String role = "user"; // user, admin, business_owner

    private String city = "vita";

    private String avatar = null;

    private boolean isVerified = false;

    // ── Verification Fields ───────────────────────────────────
    private boolean isEmailVerified = false;

    private String aadhaarNumber; // stored encrypted

    private String shopNumber;

    private String phone; // user phone number

    private String gstin; // GST Number for business owners

    private boolean hasBusinessAccount = false;

    private List<SearchHistory> searchHistory = new ArrayList<>();

    private List<String> savedBusinesses = new ArrayList<>();

    private LocalDateTime lastLogin;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    // ── Inner class for search history ───────────────────────
    @Data
    @NoArgsConstructor
    public static class SearchHistory {
        private String query;
        private String city;
        private LocalDateTime searchedAt = LocalDateTime.now();
    }
}
