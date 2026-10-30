package com.localexperts.repository;

import com.localexperts.model.City;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

// ── City Repository ───────────────────────────────────────────
public interface CityRepository extends MongoRepository<City, String> {
    List<City> findByIsActiveTrueOrderByNameAsc();
    long countByIsActiveTrue();
}
