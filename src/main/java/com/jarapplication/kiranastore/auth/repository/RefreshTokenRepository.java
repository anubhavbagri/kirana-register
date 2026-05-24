package com.jarapplication.kiranastore.auth.repository;

import com.jarapplication.kiranastore.auth.entity.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * REFRESH TOKEN REPOSITORY: Spring Data MongoDB Interface for Refresh Token Documents
 *
 * WHAT IT DOES:
 * ├─ Provides CRUD operations for RefreshToken documents in MongoDB
 * ├─ No custom query methods → uses inherited save(), findById(), delete()
 * └─ Spring Data auto-generates implementation at runtime
 *
 * WHY EMPTY INTERFACE?
 * ├─ MongoRepository provides all needed methods:
 * │   ├─ save(RefreshToken) → Insert or update document
 * │   ├─ findById(String sessionId) → Lookup by document ID
 * │   └─ delete(RefreshToken) → Remove document (for token revocation)
 * └─ No custom queries needed (lookup by ID is sufficient)
 *
 * @Repository: Spring bean + exception translation (MongoDB exceptions → Spring exceptions)
 */
@Repository // ← Spring bean + MongoDB exception translation
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    // MongoRepository<RefreshToken, String>:
    //   ├─ RefreshToken: The document class
    //   └─ String: Type of @Id field (used as sessionId)
    //
    // Inherited methods used:
    //   ├─ save(RefreshToken) → MongoDB insert/upsert
    //   └─ findById(String) → MongoDB findOne by _id
}
