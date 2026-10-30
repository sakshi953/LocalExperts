package com.localexperts;

import com.localexperts.model.Business;
import com.localexperts.model.User;
import com.localexperts.repository.BusinessRepository;
import com.localexperts.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Arrays;

// ══════════════════════════════════════════════════════════════
//  DataSeeder — Runs on every startup
//  Creates default admin user if not already in MongoDB
// ══════════════════════════════════════════════════════════════

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // IDs of old hardcoded/demo businesses to remove on startup
    private static final List<String> DEMO_IDS = Arrays.asList(
        "b1","b2","b3","b4","b5","b6",
        "p1","p2","p3"
    );

    private static final String ADMIN_EMAIL    = "admin@localexperts.com";
    private static final String ADMIN_PASSWORD = "Admin@1234";
    private static final String ADMIN_NAME     = "Admin";

    @Override
    public void run(String... args) {
        // ── Create admin if not exists ────────────────────────
        if (!userRepository.existsByEmail(ADMIN_EMAIL)) {
            User admin = new User();
            admin.setName(ADMIN_NAME);
            admin.setEmail(ADMIN_EMAIL);
            admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
            admin.setRole("admin");
            admin.setCity("vita");
            admin.setVerified(true);
            admin.setEmailVerified(true);
            userRepository.save(admin);

            System.out.println("✅ Admin user created:");
            System.out.println("   Email    : " + ADMIN_EMAIL);
            System.out.println("   Password : " + ADMIN_PASSWORD);
        } else {
            System.out.println("ℹ️  Admin user already exists — skipping seed.");
        }

        // ── Delete old hardcoded demo businesses ─────────────
        int deleted = 0;
        for (String demoId : DEMO_IDS) {
            if (businessRepository.existsById(demoId)) {
                businessRepository.deleteById(demoId);
                deleted++;
            }
        }
        if (deleted > 0) {
            System.out.println("🧹 Removed " + deleted + " old demo business(es) from MongoDB.");
        }

        System.out.println("🌱 DataSeeder complete.");
    }
}
