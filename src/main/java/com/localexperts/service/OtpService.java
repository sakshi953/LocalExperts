package com.localexperts.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

// ── OTP Service ───────────────────────────────────────────────
// Generates 6-digit OTP, stores in memory, sends via Gmail
@Service
public class OtpService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${otp.expiry.minutes:30}")
    private int otpExpiryMinutes;

    // In-memory OTP store: email -> {otp, expiry, verified}
    private final Map<String, OtpData> otpStore = new HashMap<>();

    // ── Generate and Send OTP ─────────────────────────────────
    public boolean sendOtp(String email) {
        try {
            String otp = generateOtp();
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(otpExpiryMinutes);
            otpStore.put(email.toLowerCase(), new OtpData(otp, expiry));

            // Send email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("sarvesh2032004@gmail.com");
            message.setTo(email);
            message.setSubject("LocalExperts+ — Email Verification OTP");
            message.setText(
                "Dear User,\n\n" +
                "Your OTP for LocalExperts+ registration is:\n\n" +
                "🔐 " + otp + "\n\n" +
                "This OTP is valid for " + otpExpiryMinutes + " minutes.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "— LocalExperts+ Team"
            );
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Email send failed: " + e.getMessage());
            return false;
        }
    }

    // ── Verify OTP ────────────────────────────────────────────
    public boolean verifyOtp(String email, String otp) {
        OtpData data = otpStore.get(email.toLowerCase());
        if (data == null) return false;
        if (LocalDateTime.now().isAfter(data.expiry)) {
            otpStore.remove(email.toLowerCase());
            return false;
        }
        // Already verified earlier — allow re-verification within expiry window
        if (data.verified) return true;
        if (data.otp.equals(otp)) {
            data.verified = true; // Mark verified but keep in store until expiry
            return true;
        }
        return false;
    }

    // ── Generate 6-digit OTP ──────────────────────────────────
    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    // ── Inner class ───────────────────────────────────────────
    private static class OtpData {
        String otp;
        LocalDateTime expiry;
        boolean verified = false;
        OtpData(String otp, LocalDateTime expiry) {
            this.otp = otp;
            this.expiry = expiry;
        }
    }
}
