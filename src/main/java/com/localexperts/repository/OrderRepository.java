package com.localexperts.repository;

import com.localexperts.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

// ── Order Repository ──────────────────────────────────────────
public interface OrderRepository extends MongoRepository<Order, String> {

    // Find all orders for a business
    List<Order> findByBusinessId(String businessId);

    // Find orders by customer email
    List<Order> findByCustomerEmail(String customerEmail);

    // Find by status
    List<Order> findByStatus(String status);

    // Find by business + status
    List<Order> findByBusinessIdAndStatus(String businessId, String status);

    // Find by business category
    List<Order> findByBusinessCategory(String businessCategory);
}
