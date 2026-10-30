package com.localexperts.controller;

import com.localexperts.model.User;
import com.localexperts.repository.UserRepository;
import com.localexperts.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// ── Auth Controller ───────────────────────────────────────────
// POST /api/auth/signup
// POST /api/auth/login
// GET  /api/auth/me
// PUT  /api/auth/update-profile
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ── POST /api/auth/signup ─────────────────────────────────
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            String name = body.get("name");
            String email = body.get("email");
            String password = body.get("password");
            String city = body.getOrDefault("city", "vita");

            if (name == null || email == null || password == null) {
                response.put("success", false);
                response.put("message", "Name, email and password are required");
                return ResponseEntity.badRequest().body(response);
            }

            if (userRepository.existsByEmail(email)) {
                // Email exists — try auto-login with provided password
                Optional<User> existingUser = userRepository.findByEmail(email.toLowerCase());
                if (existingUser.isPresent() && passwordEncoder.matches(password, existingUser.get().getPassword())) {
                    // Password matches — auto login
                    User user = existingUser.get();
                    String token = jwtUtil.generateToken(user.getId());
                    response.put("success", true);
                    response.put("message", "Welcome back! You have been logged in.");
                    response.put("token", token);
                    response.put("user", buildUserMap(user));
                    response.put("autoLogin", true);
                    return ResponseEntity.ok(response);
                } else {
                    // Email exists but wrong password
                    response.put("success", false);
                    response.put("message", "This email is already registered. Please sign in with your existing password.");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            User user = new User();
            user.setName(name);
            user.setEmail(email.toLowerCase());
            user.setPassword(passwordEncoder.encode(password));
            user.setCity(city);
            userRepository.save(user);

            String token = jwtUtil.generateToken(user.getId());

            response.put("success", true);
            response.put("message", "Account created successfully!");
            response.put("token", token);
            response.put("user", buildUserMap(user));
            return ResponseEntity.status(201).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ── POST /api/auth/login ──────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = body.get("email");
            String password = body.get("password");

            if (email == null || password == null) {
                response.put("success", false);
                response.put("message", "Email and password are required");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<User> userOpt = userRepository.findByEmail(email.toLowerCase());
            if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
                response.put("success", false);
                response.put("message", "Invalid email or password");
                return ResponseEntity.status(401).body(response);
            }

            User user = userOpt.get();
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            String token = jwtUtil.generateToken(user.getId());

            response.put("success", true);
            response.put("message", "Welcome back, " + user.getName() + "!");
            response.put("token", token);
            response.put("user", buildUserMap(user));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ── GET /api/auth/me ──────────────────────────────────────
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMe(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) request.getAttribute("currentUser");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Not authenticated");
            return ResponseEntity.status(401).body(response);
        }

        response.put("success", true);
        response.put("user", buildUserMap(currentUser));
        return ResponseEntity.ok(response);
    }

    // ── PUT /api/auth/update-profile ──────────────────────────
    @PutMapping("/update-profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, String> body,
                                                              HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) request.getAttribute("currentUser");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Not authenticated");
            return ResponseEntity.status(401).body(response);
        }

        try {
            if (body.containsKey("name")) currentUser.setName(body.get("name"));
            if (body.containsKey("city")) currentUser.setCity(body.get("city"));
            currentUser.setUpdatedAt(LocalDateTime.now());
            userRepository.save(currentUser);

            response.put("success", true);
            response.put("message", "Profile updated");
            response.put("user", buildUserMap(currentUser));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ── Helper: build user response map ──────────────────────
    private Map<String, Object> buildUserMap(User user) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("name", user.getName());
        userMap.put("email", user.getEmail());
        userMap.put("role", user.getRole());
        userMap.put("city", user.getCity());
        return userMap;
    }
}
