package com.jarapplication.kiranastore.auth.controller;

import static com.jarapplication.kiranastore.auth.constants.HeaderConstants.AUTHORIZATION;
import static com.jarapplication.kiranastore.auth.constants.HeaderConstants.REFRESH_TOKEN;

import com.jarapplication.kiranastore.auth.service.RefreshTokenService;
import com.jarapplication.kiranastore.feature_users.models.AuthResponse;
import javax.naming.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // Marks class as a REST API controller that handles HTTP requests and returns JSON/XML
@RequestMapping // Base URL; Empty = "/"
public class RefreshController {
    private final RefreshTokenService refreshTokenService;

    @Autowired // Explicitly tells Spring: “Use this constructor for dependency injection”
    public RefreshController(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * to refresh Access token if refresh token is valid
     *
     * @param accessToken
     * @param refreshToken
     * @return
     * @throws AuthenticationException
     * @GetMapping("/generate-token") maps HTTP GET requests to `/generate-token` endpoint to this method
     * @RequestHeader Extracts HTTP headers and binds to method parameters.
     *
     */
    @GetMapping("/generate-token") // Retrieves Data <BASE_URL>/generate-token returns: AuthResponse JSON
    public AuthResponse refreshAccessToken(
            @RequestHeader(AUTHORIZATION) String accessToken,
            @RequestHeader(REFRESH_TOKEN) String refreshToken)
            throws AuthenticationException {
        return refreshTokenService.generateAccessToken(refreshToken, accessToken);
    }
}
