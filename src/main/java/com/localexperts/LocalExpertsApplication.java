package com.localexperts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ══════════════════════════════════════════════════════════════
//  LocalExperts+ — Main Spring Boot Application
//  Tech: Java 17 + Spring Boot + MongoDB
//  Author: CSE Final Year Project
// ══════════════════════════════════════════════════════════════

@SpringBootApplication
public class LocalExpertsApplication {
    public static void main(String[] args) {
        SpringApplication.run(LocalExpertsApplication.class, args);
        System.out.println("🚀 LocalExperts+ server running on http://localhost:5000");
        System.out.println("📡 API Base: http://localhost:5000/api");
    }
}
