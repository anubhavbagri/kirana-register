package com.jarapplication.kiranastore.feature_users.service;

import com.jarapplication.kiranastore.feature_users.models.UserRequest;
import java.util.List;

/**
 * USER SERVICE INTERFACE: Contract for User Management Operations
 *
 * WHAT IT DOES:
 * ├─ Defines the contract for user CRUD and lookup operations
 * ├─ Implemented by UserServiceImp
 * └─ Used by: UserController (registration), AuthServiceImp (role/ID lookups)
 *
 * WHY AN INTERFACE?
 * ├─ Dependency Inversion: AuthServiceImp depends on UserService (not UserServiceImp)
 * ├─ Testability: Mock UserService in tests without MongoDB
 * └─ Extensibility: Could have AdminUserService, CachedUserService, etc.
 */
public interface UserService {
    /** Get user roles by username (for JWT claims) */
    List<String> getUserRolesByUsername(String username);

    /** Get MongoDB user ID by username (for JWT claims) */
    String getUserIdByUsername(String username);

    /** Register a new user (validate → hash password → save) */
    UserRequest save(UserRequest userRequest);
}
