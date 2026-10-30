package com.localexperts.controller;

import com.localexperts.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

// ── OTP Controller ────────────────────────────────────────────
// POST /api/otp/send   — send OTP to email
// POST /api/otp/verify — verify OTP
@RestController
@RequestMapping("/api/otp")
public class OtpController {

    @Autowired
    private OtpService otpService;

    // ── POST /api/otp/send ────────────────────────────────────
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendOtp(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = body.get("email");
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }

            boolean sent = otpService.sendOtp(email.trim());
            if (sent) {
                response.put("success", true);
                response.put("message", "OTP sent to " + email + ". Please check your inbox.");
            } else {
                response.put("success", false);
                response.put("message", "Failed to send OTP. Please try again.");
            }
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ── POST /api/otp/verify ──────────────────────────────────
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = body.get("email");
            String otp = body.get("otp");

            if (email == null || otp == null) {
                response.put("success", false);
                response.put("message", "Email and OTP are required");
                return ResponseEntity.badRequest().body(response);
            }

            boolean valid = otpService.verifyOtp(email.trim(), otp.trim());
            if (valid) {
                response.put("success", true);
                response.put("message", "Email verified successfully!");
            } else {
                response.put("success", false);
                response.put("message", "Invalid or expired OTP. Please try again.");
            }
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
