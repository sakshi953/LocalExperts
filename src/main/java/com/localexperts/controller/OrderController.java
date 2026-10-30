package com.localexperts.controller;

import com.localexperts.model.Order;
import com.localexperts.model.Business;
import com.localexperts.model.User;
import com.localexperts.repository.OrderRepository;
import com.localexperts.repository.BusinessRepository;
import com.localexperts.service.OrderEmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

// ── Order Controller ──────────────────────────────────────────
// POST   /api/orders              → Place new order (auth required)
// GET    /api/orders/mine         → My orders as customer
// GET    /api/orders/business/:id → Orders for my business (owner)
// PUT    /api/orders/:id/status   → Update order status (owner/admin)
// GET    /api/orders/:id          → Get single order
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired private OrderRepository orderRepository;
    @Autowired private BusinessRepository businessRepository;
    @Autowired private OrderEmailService emailService;

    // ── POST /api/orders ──────────────────────────────────────
    // Place a new order for any business type.
    // Body fields depend on business category — see frontend modal.
    @PostMapping
    public ResponseEntity<Map<String, Object>> placeOrder(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) request.getAttribute("currentUser");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            String businessId = (String) body.get("businessId");
            if (businessId == null) {
                response.put("success", false);
                response.put("message", "businessId is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Load business
            Optional<Business> opt = businessRepository.findById(businessId);
            if (opt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Business not found");
                return ResponseEntity.status(404).body(response);
            }
            Business business = opt.get();

            // Build order
            Order order = new Order();
            order.setBusinessId(businessId);
            order.setBusinessName(business.getName());
            order.setBusinessCategory(business.getCategory());
            // Owner email — from business contact (set when business was added)
            String ownerEmail = (business.getContact() != null && business.getContact().getEmail() != null
                    && !business.getContact().getEmail().isBlank())
                    ? business.getContact().getEmail()
                    : "sarvesh2032004@gmail.com"; // fallback to admin if owner email missing
            order.setOwnerEmail(ownerEmail);

            order.setCustomerName(currentUser.getName());
            order.setCustomerEmail(currentUser.getEmail());
            order.setCustomerPhone(body.getOrDefault("customerPhone", "").toString());

            order.setNotes(body.getOrDefault("notes", "").toString());

            // ── Dynamic extra fields per category ─────────────
            Map<String, String> extra = new HashMap<>();
            String category = business.getCategory().toLowerCase();

            switch (category) {
                case "hotels" -> {
                    putIfPresent(extra, body, "checkIn");
                    putIfPresent(extra, body, "checkOut");
                    putIfPresent(extra, body, "roomType");
                    putIfPresent(extra, body, "guests");
                    putIfPresent(extra, body, "rooms");
                }
                case "food" -> {
                    putIfPresent(extra, body, "items");         // comma-separated
                    putIfPresent(extra, body, "orderType");     // dine-in / takeaway / delivery
                    putIfPresent(extra, body, "tableNo");
                    putIfPresent(extra, body, "deliveryAddress");
                    putIfPresent(extra, body, "preferredTime");
                }
                case "healthcare" -> {
                    putIfPresent(extra, body, "appointmentDate");
                    putIfPresent(extra, body, "appointmentTime");
                    putIfPresent(extra, body, "doctorName");
                    putIfPresent(extra, body, "patientAge");
                    putIfPresent(extra, body, "reason");
                }
                case "services" -> {
                    putIfPresent(extra, body, "serviceType");
                    putIfPresent(extra, body, "scheduledDate");
                    putIfPresent(extra, body, "scheduledTime");
                    putIfPresent(extra, body, "serviceLocation");
                }
                case "grocery" -> {
                    putIfPresent(extra, body, "items");
                    putIfPresent(extra, body, "deliveryAddress");
                    putIfPresent(extra, body, "deliverySlot");
                }
                case "education" -> {
                    putIfPresent(extra, body, "courseName");
                    putIfPresent(extra, body, "preferredBatch");
                    putIfPresent(extra, body, "studentAge");
                    putIfPresent(extra, body, "parentName");
                }
                default -> {
                    // Accept any extra fields sent
                    body.forEach((k, v) -> {
                        if (!List.of("businessId", "notes", "customerPhone").contains(k)) {
                            extra.put(k, v.toString());
                        }
                    });
                }
            }

            order.setExtraFields(extra);
            order.setStatus("pending");

            // Save
            Order saved = orderRepository.save(order);

            // ── Send emails ───────────────────────────────────
            emailService.sendOrderEmails(saved);

            response.put("success", true);
            response.put("message", "Order placed successfully! Confirmation emails have been sent.");
            response.put("data", saved);
            return ResponseEntity.status(201).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ── GET /api/orders/mine ──────────────────────────────────
    // Customer sees their own orders
    @GetMapping("/mine")
    public ResponseEntity<Map<String, Object>> getMyOrders(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) request.getAttribute("currentUser");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            List<Order> orders = orderRepository.findByCustomerEmail(currentUser.getEmail());
            orders.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
            response.put("success", true);
            response.put("total", orders.size());
            response.put("data", orders);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ── GET /api/orders/business/:businessId ──────────────────
    // Business owner sees all orders for their business
    @GetMapping("/business/{businessId}")
    public ResponseEntity<Map<String, Object>> getBusinessOrders(
            @PathVariable String businessId,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) request.getAttribute("currentUser");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            // Verify ownership or admin
            Optional<Business> opt = businessRepository.findById(businessId);
            if (opt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Business not found");
                return ResponseEntity.status(404).body(response);
            }

            Business business = opt.get();
            boolean isOwner = business.getOwner().equals(currentUser.getId());
            boolean isAdmin  = "admin".equals(currentUser.getRole());

            if (!isOwner && !isAdmin) {
                response.put("success", false);
                response.put("message", "Not authorized");
                return ResponseEntity.status(403).body(response);
            }

            List<Order> orders = orderRepository.findByBusinessId(businessId);
            orders.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

            response.put("success", true);
            response.put("total", orders.size());
            response.put("data", orders);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ── GET /api/orders/:id ───────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrder(
            @PathVariable String id,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) request.getAttribute("currentUser");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Optional<Order> opt = orderRepository.findById(id);
            if (opt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Order not found");
                return ResponseEntity.status(404).body(response);
            }

            Order order = opt.get();

            // Only customer or owner or admin can view
            boolean isCustomer = order.getCustomerEmail().equals(currentUser.getEmail());
            boolean isAdmin    = "admin".equals(currentUser.getRole());

            if (!isCustomer && !isAdmin) {
                // Check if owner of that business
                Optional<Business> biz = businessRepository.findById(order.getBusinessId());
                boolean isOwner = biz.isPresent() && biz.get().getOwner().equals(currentUser.getId());
                if (!isOwner) {
                    response.put("success", false);
                    response.put("message", "Not authorized");
                    return ResponseEntity.status(403).body(response);
                }
            }

            response.put("success", true);
            response.put("data", order);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ── PUT /api/orders/:id/status ────────────────────────────
    // Owner/Admin can update: pending → confirmed / cancelled / completed
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) request.getAttribute("currentUser");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            String newStatus = (String) body.get("status");
            List<String> allowed = List.of("pending", "confirmed", "cancelled", "sent", "delivered");
            if (newStatus == null || !allowed.contains(newStatus)) {
                response.put("success", false);
                response.put("message", "Status must be one of: " + allowed);
                return ResponseEntity.badRequest().body(response);
            }

            Optional<Order> opt = orderRepository.findById(id);
            if (opt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Order not found");
                return ResponseEntity.status(404).body(response);
            }

            Order order = opt.get();

            // Verify ownership
            Optional<Business> biz = businessRepository.findById(order.getBusinessId());
            boolean isOwner = biz.isPresent() && biz.get().getOwner().equals(currentUser.getId());
            boolean isAdmin  = "admin".equals(currentUser.getRole());

            if (!isOwner && !isAdmin) {
                response.put("success", false);
                response.put("message", "Only business owner or admin can update order status");
                return ResponseEntity.status(403).body(response);
            }

            order.setStatus(newStatus);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            response.put("success", true);
            response.put("message", "Order status updated to: " + newStatus);
            response.put("data", order);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ── PUT /api/orders/:id/customer-received ─────────────────
    // Customer confirms they received/got the order → sets status to "delivered"
    // Only the customer who placed the order can call this
    @PutMapping("/{id}/customer-received")
    public ResponseEntity<Map<String, Object>> markAsReceived(
            @PathVariable String id,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();
        User currentUser = (User) request.getAttribute("currentUser");

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Authentication required");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Optional<Order> opt = orderRepository.findById(id);
            if (opt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Order not found");
                return ResponseEntity.status(404).body(response);
            }

            Order order = opt.get();

            // Only the customer who placed the order can mark it received
            if (!order.getCustomerEmail().equals(currentUser.getEmail())) {
                response.put("success", false);
                response.put("message", "Only the customer who placed this order can mark it as received");
                return ResponseEntity.status(403).body(response);
            }

            // Can only mark as received if owner has sent the order
            if (!"sent".equals(order.getStatus())) {
                response.put("success", false);
                response.put("message", "Order can only be marked as received after the business has sent it");
                return ResponseEntity.badRequest().body(response);
            }

            order.setStatus("delivered");
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            response.put("success", true);
            response.put("message", "Order marked as received! Thank you.");
            response.put("data", order);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ── Helper ────────────────────────────────────────────────
    private void putIfPresent(Map<String, String> extra,
                               Map<String, Object> body, String key) {
        if (body.containsKey(key) && body.get(key) != null) {
            String val = body.get(key).toString().trim();
            if (!val.isEmpty()) extra.put(key, val);
        }
    }
}
