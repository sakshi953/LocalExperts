package com.localexperts.repository;

import com.localexperts.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

// ── User Repository ───────────────────────────────────────────
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
