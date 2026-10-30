package com.localexperts.repository;

import com.localexperts.model.Business;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

// ── Business Repository ───────────────────────────────────────
public interface BusinessRepository extends MongoRepository<Business, String> {

    List<Business> findByStatus(String status);

    List<Business> findByOwner(String ownerId);

    List<Business> findByCityAndStatus(String city, String status);

    List<Business> findByCategoryAndStatus(String category, String status);

    List<Business> findByCityAndCategoryAndStatus(String city, String category, String status);

    @Query("{ 'status': 'active', $or: [ { 'name': { $regex: ?0, $options: 'i' } }, { 'description': { $regex: ?0, $options: 'i' } }, { 'category': { $regex: ?0, $options: 'i' } }, { 'products': { $regex: ?0, $options: 'i' } } ] }")
    List<Business> searchByKeyword(String keyword);

    @Query("{ 'city': ?0, 'status': 'active', $or: [ { 'name': { $regex: ?1, $options: 'i' } }, { 'description': { $regex: ?1, $options: 'i' } }, { 'category': { $regex: ?1, $options: 'i' } }, { 'products': { $regex: ?1, $options: 'i' } } ] }")
    List<Business> searchByKeywordAndCity(String city, String keyword);

    List<Business> findByStatusAndAvgRatingGreaterThanEqualOrderByAvgRatingDesc(String status, double minRating);

    List<Business> findByCityAndStatusAndAvgRatingGreaterThanEqualOrderByAvgRatingDesc(String city, String status, double minRating);

    long countByStatus(String status);
    long countByStatusAndIsVerifiedTrue(String status);
    long countByStatusAndIsOpenTrue(String status);
}
