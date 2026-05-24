package com.jarapplication.kiranastore.feature_users.repository;

import com.jarapplication.kiranastore.feature_users.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * USER REPOSITORY: Spring Data MongoDB Interface for User Documents
 *
 * WHAT IT DOES:
 * ├─ Provides CRUD + custom query operations for User documents in MongoDB
 * ├─ Spring Data auto-generates implementation at runtime
 * └─ Contains a derived query for username lookup
 *
 * WHY IT'S NEEDED:
 * ├─ Zero boilerplate: Extend MongoRepository → get all CRUD methods free
 * ├─ Derived query: findByUsername() → auto-generated MongoDB query
 * └─ Used by: UserDAO (data access), CustomUserDetailsService (Spring Security)
 *
 * QUERY METHOD:
 * ├─ findByUsername(String username):
 * │   ├─ Derived query: Spring Data parses method name
 * │   ├─ MongoDB: db.users.findOne({ username: username })
 * │   ├─ Returns: User entity if found, null if not found
 * │   └─ Used for: Login validation, duplicate checking, role/ID lookup
 * │
 * └─ Why null return (not Optional)?
 *    ├─ Spring Data returns null for single-entity queries if not found
 *    ├─ Caller wraps in Optional.ofNullable() where needed
 *    └─ Consider: Optional<User> findByUsername() for null safety
 *
 * @Repository: Marks as data access bean (also enables exception translation)
 */
@Repository // ← Spring bean + MongoDB exception translation
public interface UserRepository extends MongoRepository<User, String> {
    // MongoRepository<User, String>:
    //   ├─ User: The document class
    //   └─ String: Type of @Id field

    // Derived query: Find user by username
    // MongoDB: db.users.findOne({ username: ? })
    // Returns null if not found (caller handles null check)
    User findByUsername(String username);
}
