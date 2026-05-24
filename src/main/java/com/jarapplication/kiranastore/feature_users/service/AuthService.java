package com.jarapplication.kiranastore.feature_users.service;

import com.jarapplication.kiranastore.feature_users.models.AuthResponse;

/**
 * AUTH SERVICE INTERFACE: Contract for Authentication Operations
 *
 * WHAT IT DOES:
 * ├─ Defines the contract for user authentication (login)
 * ├─ Implemented by AuthServiceImp
 * └─ Used by UserController.login()
 *
 * WHY AN INTERFACE?
 * ├─ Dependency Inversion: UserController depends on AuthService (abstraction)
 * ├─ Testability: Mock authentication in controller unit tests
 * └─ Extensibility: Could implement OAuth2AuthService, LDAPAuthService, etc.
 */
public interface AuthService {
    /**
     * Authenticates user credentials and returns tokens.
     *
     * @param username ← User's username
     * @param password ← User's plaintext password (compared via BCrypt)
     * @return AuthResponse with userId, JWT access token, and refresh token
     */
    AuthResponse authenticate(String username, String password);
}
