package com.localexperts.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

// ── Order Model ───────────────────────────────────────────────
// Supports 6 business types: Hotels, Food, Healthcare,
// Services, Grocery, Education
// Each type has its own extra fields stored in `extraFields`
@Data
@NoArgsConstructor
@Document(collection = "orders")
public class Order {

    @Id
    private String id;

    // ── Core fields (all order types) ────────────────────────
    private String businessId;
    private String businessName;
    private String businessCategory;  // Hotels, Food, Healthcare, Services, Grocery, Education
    private String ownerEmail;        // Business owner's email (for notification)

    private String customerName;
    private String customerEmail;
    private String customerPhone;

    private String status = "pending"; // pending, confirmed, cancelled, completed, delivered

    private String notes;             // Customer's special instructions

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ── Dynamic extra fields per business type ────────────────
    // Hotels:     checkIn, checkOut, roomType, guests, rooms
    // Food:       items (JSON list), tableNo, deliveryAddress, orderType (dine-in/takeaway/delivery)
    // Healthcare: appointmentDate, appointmentTime, doctorName, patientAge, reason
    // Services:   serviceType, scheduledDate, scheduledTime, location
    // Grocery:    items (JSON list), deliveryAddress, deliverySlot
    // Education:  courseName, preferredBatch, studentAge, parentName
    private Map<String, String> extraFields = new HashMap<>();

    // ── Total amount (optional) ───────────────────────────────
    private Double totalAmount;
    private String currency = "INR";

    // ── Helper: get extra field ───────────────────────────────
    public String getExtra(String key) {
        return extraFields != null ? extraFields.getOrDefault(key, "") : "";
    }

    public void setExtra(String key, String value) {
        if (extraFields == null) extraFields = new HashMap<>();
        extraFields.put(key, value);
    }
}
