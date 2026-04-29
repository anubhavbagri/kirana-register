package com.jarapplication.kiranastore.auth.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RefreshTokenModel {
    private String refreshToken;
    private String sessionId;
}
