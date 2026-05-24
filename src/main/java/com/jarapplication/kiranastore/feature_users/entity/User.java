package com.jarapplication.kiranastore.feature_users.entity;

import com.jarapplication.kiranastore.AOP.annotation.Capitalize;
import java.util.List;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * USER ENTITY: MongoDB Document for User Account Data
 *
 * WHAT IT DOES:
 * ├─ Maps to a MongoDB document in the "users" collection
 * ├─ Stores user credentials (username + BCrypt hashed password)
 * ├─ Stores user roles for authorization (e.g., ["ADMIN", "USER"])
 * └─ Referenced throughout the auth/user features
 *
 * WHY MONGODB (not PostgreSQL)?
 * ├─ User data has flexible schema (roles is a list → natural document structure)
 * ├─ Read-heavy: Login checks happen frequently → MongoDB's fast reads
 * ├─ No complex relationships: Users are independent documents
 * └─ Consistent with other document entities (BillEntity, ProductEntity)
 *
 * ANNOTATIONS:
 * ├─ @Data (Lombok): Auto-generates getters, setters, equals, hashCode, toString
 * ├─ @Document(collection = "users"): Maps to MongoDB "users" collection
 * ├─ @Id: MongoDB document primary key (auto-generated ObjectId)
 * └─ @Capitalize: AOP field marker → CapitalizeAspect uppercases this field
 *    └─ username stored in UPPERCASE → case-insensitive lookups
 *
 * EXAMPLE MONGODB DOCUMENT:
 * {
 *   "_id": "6657a1b2c3d4e5f6",
 *   "username": "JOHN",                  ← @Capitalize → stored uppercase
 *   "password": "$2a$10$dXJ3SW6G...",     ← BCrypt hash (never plaintext!)
 *   "roles": ["ADMIN", "USER"]            ← Authorization roles
 * }
 *
 * SECURITY NOTE:
 * ├─ Password is ALWAYS BCrypt hashed before saving (UserServiceImp.save())
 * ├─ Never stored in plaintext
 * └─ BCrypt comparison during login (AuthenticationManager)
 */
@Data // ← Lombok: getter/setter/equals/hashCode/toString
@Document(collection = "users") // ← MongoDB: Maps to "users" collection
public class User {

    @Id // ← MongoDB document primary key (auto-generated ObjectId string)
    private String id;

    @Capitalize // ← AOP: CapitalizeAspect uppercases this field when @CapitalizeMethod is on the calling method
    // Stored as UPPERCASE in MongoDB (e.g., "JOHN")
    // Ensures case-insensitive username matching
    private String username;

    // BCrypt hashed password (e.g., "$2a$10$dXJ3SW6G7P50lGmMQgel...")
    // NEVER stored as plaintext
    private String password;

    // User authorization roles (e.g., ["ADMIN", "USER"])
    // Embedded in JWT token claims during login
    // Used by @PreAuthorize("hasRole('ADMIN')") for method-level security
    private List<String> roles;
}