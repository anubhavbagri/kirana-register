package com.jarapplication.kiranastore.feature_users.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String userId;
    private String AccessToken;
    private String RefreshToken;
}
