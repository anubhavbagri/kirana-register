package com.jarapplication.kiranastore.auth.dao;

import com.jarapplication.kiranastore.auth.entity.RefreshToken;
import com.jarapplication.kiranastore.auth.repository.RefreshTokenRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * REFRESH TOKEN DAO: Data Access Object for Refresh Token Operations (MongoDB)
 *
 * WHAT IT DOES:
 * ├─ Wraps RefreshTokenRepository for save + lookup operations
 * ├─ Provides clean data access abstraction for RefreshTokenServiceImp
 * └─ Same DAO pattern as UserDAO, ProductDao, TransactionDao
 *
 * WHY IT'S NEEDED:
 * ├─ Abstraction: Service layer doesn't depend directly on Spring Data repository
 * ├─ Extensibility: Could add caching, logging, or audit logic here
 * └─ Consistency: Same DAO pattern across all modules
 *
 * OPERATIONS:
 * ├─ save(): Persists new refresh token to MongoDB "refreshToken" collection
 * └─ findById(): Looks up refresh token by sessionId (document _id)
 *    └─ Used during token refresh: JWT sessionId → MongoDB lookup → validate
 *
 * @Component: Spring bean (data access layer)
 */
@Component
public class RefreshTokenDAO {
    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public RefreshTokenDAO(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Saves a new refresh token document to MongoDB.
     *
     * @param refreshToken ← RefreshToken entity with hashed token, userId, timeout
     * @return Saved RefreshToken entity with auto-generated MongoDB _id (sessionId)
     */
    public RefreshToken save(RefreshToken refreshToken) {
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Finds a refresh token document by sessionId (MongoDB document _id).
     *
     * USED DURING TOKEN REFRESH:
     * ├─ JWT access token contains sessionId in claims
     * ├─ On refresh: extract sessionId → findById → get stored hash + timeout
     * └─ Then compare client's refresh token against stored hash
     *
     * @param sessionId ← MongoDB document ID (from JWT's sessionId claim)
     * @return Optional<RefreshToken> (empty if session not found)
     */
    public Optional<RefreshToken> findById(String sessionId) {
        return refreshTokenRepository.findById(sessionId);
    }
}
