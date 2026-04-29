package com.jarapplication.kiranastore.feature_users.service;

import com.jarapplication.kiranastore.feature_users.models.AuthResponse;

public interface AuthService {
    AuthResponse authenticate(String username, String password);
}
