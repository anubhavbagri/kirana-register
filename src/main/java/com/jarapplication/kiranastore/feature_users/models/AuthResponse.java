package com.jarapplication.kiranastore.feature_users.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data // ← Lombok annotation eliminates boilerplate(getter/setter/equals/hashCode/toString methods auto-generated at compile Time)
@AllArgsConstructor // Lombok annotation that Auto-generates constructor that accepts all fields as parameters.
public class AuthResponse {
    private String userId;
    private String AccessToken;
    private String RefreshToken;
}

/*
// Without @AllArgsConstructor, you'd write:
public AuthResponse(String userId, String AccessToken, String RefreshToken) {
    this.userId = userId;
    this.AccessToken = AccessToken;
    this.RefreshToken = RefreshToken;
}

// With @AllArgsConstructor:
@AllArgsConstructor
public class AuthResponse {
    private String userId;
    private String AccessToken;
    private String RefreshToken;
    // Constructor auto-generated
}

// Usage:
AuthResponse response = new AuthResponse(
    "user-123",
    "eyJhbGc...",
    "uuid-456"
);

Why it’s needed:
- Eliminates boilerplate for immutable DTOs (Data Transfer Objects)
- Makes construction concise
- Common pattern for request/response objects

Related Lombok annotations:
- @NoArgsConstructor      → No-argument constructor
- @RequiredArgsConstructor → Constructor for final fields only
- @AllArgsConstructor     → Constructor for all fields


*/