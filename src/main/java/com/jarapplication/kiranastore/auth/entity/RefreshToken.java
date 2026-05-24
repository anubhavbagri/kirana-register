package com.jarapplication.kiranastore.auth.entity;

import jakarta.persistence.Id;
import java.util.Date;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * REFRESH TOKEN ENTITY: MongoDB Document for Refresh Token Storage
 *
 * WHAT IT DOES:
 * ├─ Maps to a MongoDB document in the "refreshToken" collection (default name)
 * ├─ Stores BCrypt-hashed refresh tokens with expiration
 * └─ Links tokens to users via userId field
 *
 * WHY MONGODB (not Redis)?
 * ├─ Persistence: Refresh tokens survive server restarts
 * ├─ Consistency: Same DB as users and bills
 * ├─ Indexed: Fast lookups by token and userId
 * └─ Alternative: Redis for faster lookups (TTL auto-expiry built-in)
 *
 * FIELDS:
 * ├─ id: MongoDB document ID → used as "sessionId" in JWT claims
 * │   └─ This links JWT access token to its refresh token
 * │   └─ On refresh: JWT's sessionId → MongoDB lookup → find this document
 * │
 * ├─ token: BCrypt hash of the actual refresh token
 * │   ├─ @Indexed(unique=true): Unique index → no duplicate tokens
 * │   ├─ Client holds the unhashed token → server compares hash
 * │   └─ If DB leaked: Attacker can't use hashed tokens
 * │
 * ├─ userId: Links to User document in "users" collection
 * │   ├─ @Indexed: Fast lookups by userId (find user's refresh tokens)
 * │   └─ Not unique: A user can have multiple active refresh tokens (from different devices)
 * │
 * ├─ timeout: Expiration date for this refresh token
 * │   └─ Set to: createdAt + REFRESH_TOKEN_EXPIRATION_TIME (15 min)
 * │   └─ Checked during refresh: if (timeout.before(now)) → expired
 * │
 * └─ createdAt: Timestamp when the token was created (audit trail)
 *
 * NOTE: Uses jakarta.persistence.Id (JPA) instead of org.springframework.data.annotation.Id
 *       Both work with Spring Data MongoDB, but the Spring annotation is more conventional.
 *
 * @Data (Lombok): Generates getters/setters/equals/hashCode/toString
 * @Document: MongoDB collection (default name = "refreshToken")
 */
@Data     // ← Lombok: getter/setter/equals/hashCode/toString
@Document // ← MongoDB: Uses class name "refreshToken" as collection name
public class RefreshToken {

    @Id // ← MongoDB document primary key (auto-generated ObjectId)
    // Used as "sessionId" in JWT → links access token to this refresh token
    String id;

    @Indexed(unique = true) // ← Unique index on token hash → prevents duplicates
    // BCrypt hash of the actual refresh token (never store plaintext)
    private String token;

    @Indexed // ← Non-unique index on userId → fast user lookups
    // Links to User document in "users" collection
    private String userId;

    // Expiration date: createdAt + 15 minutes
    // Checked during refresh: must be in the future
    private Date timeout;

    // Creation timestamp (audit trail)
    private Date createdAt;
}