package com.localexperts.controller;

import com.localexperts.model.User;
import com.localexperts.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // ── GET /api/users/:id ─────────────────────────────────────
    // Get user info by ID (admin use — for business detail modal)
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(
            @PathVariable String id,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) request.getAttribute("currentUser");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        // Only admin can fetch any user; owner can fetch self
        if (!"admin".equals(currentUser.getRole()) && !currentUser.getId().equals(id)) {
            response.put("success", false);
            response.put("message", "Access denied");
            return ResponseEntity.status(403).body(response);
        }

        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.status(404).body(response);
        }

        User u = opt.get();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", u.getId());
        userMap.put("name", u.getName());
        userMap.put("email", u.getEmail());
        userMap.put("phone", u.getPhone());
        userMap.put("city", u.getCity());
        userMap.put("role", u.getRole());
        userMap.put("gstin", u.getGstin());
        userMap.put("aadhaar", u.getAadhaarNumber() != null ? "****" + u.getAadhaarNumber().replaceAll("\\s","").substring(Math.max(0, u.getAadhaarNumber().replaceAll("\\s","").length()-4)) : null);
        userMap.put("savedBusinesses", u.getSavedBusinesses());

        response.put("success", true);
        response.put("data", userMap);
        return ResponseEntity.ok(response);
    }

    // ── POST /api/users/wishlist/toggle ───────────────────────
    // Toggle a business in/out of the logged-in user's wishlist
    @PostMapping("/wishlist/toggle")
    public ResponseEntity<Map<String, Object>> toggleWishlist(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) request.getAttribute("currentUser");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Please login to save businesses");
            return ResponseEntity.status(401).body(response);
        }

        String bizId = (String) body.get("businessId");
        if (bizId == null || bizId.isBlank()) {
            response.put("success", false);
            response.put("message", "businessId is required");
            return ResponseEntity.badRequest().body(response);
        }

        List<String> saved = currentUser.getSavedBusinesses();
        if (saved == null) saved = new ArrayList<>();

        boolean added;
        if (saved.contains(bizId)) {
            saved.remove(bizId);
            added = false;
        } else {
            saved.add(bizId);
            added = true;
        }

        currentUser.setSavedBusinesses(saved);
        userRepository.save(currentUser);

        response.put("success", true);
        response.put("added", added);
        response.put("savedBusinesses", saved);
        response.put("message", added ? "Business saved to wishlist!" : "Removed from wishlist");
        return ResponseEntity.ok(response);
    }

    // ── GET /api/users/wishlist ────────────────────────────────
    // Get all saved business IDs for logged-in user
    @GetMapping("/wishlist")
    public ResponseEntity<Map<String, Object>> getWishlist(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) request.getAttribute("currentUser");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        List<String> saved = currentUser.getSavedBusinesses();
        response.put("success", true);
        response.put("data", saved != null ? saved : new ArrayList<>());
        return ResponseEntity.ok(response);
    }
}
