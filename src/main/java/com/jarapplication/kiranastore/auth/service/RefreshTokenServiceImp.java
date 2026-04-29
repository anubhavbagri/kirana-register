package com.jarapplication.kiranastore.auth.service;

import static com.jarapplication.kiranastore.auth.constants.LogConstants.AUTHENTICATION_FAILED;
import static com.jarapplication.kiranastore.auth.constants.LogConstants.INVALID_ACCESS_TOKEN;
import static com.jarapplication.kiranastore.constants.SecurityConstants.REFRESH_TOKEN_EXPIRATION_TIME;
import static com.jarapplication.kiranastore.constants.SecurityConstants.TOKEN_PREFIX;

import com.jarapplication.kiranastore.auth.dao.RefreshTokenDAO;
import com.jarapplication.kiranastore.auth.entity.RefreshToken;
import com.jarapplication.kiranastore.auth.models.RefreshTokenModel;
import com.jarapplication.kiranastore.feature_users.models.AuthResponse;
import com.jarapplication.kiranastore.utils.JwtUtil;
import java.util.*;
import javax.naming.AuthenticationException;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenServiceImp implements RefreshTokenService {
    private final RefreshTokenDAO refreshTokenDao;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    public RefreshTokenServiceImp(RefreshTokenDAO refreshTokenDao, JwtUtil jwtUtil) {
        this.refreshTokenDao = refreshTokenDao;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Implementation of saving refersh token
     *
     * @param userId
     * @return
     */
    @Override
    public RefreshTokenModel saveRefreshToken(String userId) {
        String token = UUID.randomUUID().toString();
        String tokenHash = encoder.encode(token);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(tokenHash);
        refreshToken.setCreatedAt(new Date());
        refreshToken.setTimeout(
                new DateTime(new Date()).plus(REFRESH_TOKEN_EXPIRATION_TIME).toDate());
        refreshToken = refreshTokenDao.save(refreshToken);
        return new RefreshTokenModel(refreshToken.getToken(), refreshToken.getId());
    }

    /**
     * Implementation to generate new Access token
     *
     * @param refreshToken
     * @param accessToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    public AuthResponse generateAccessToken(String refreshToken, String accessToken)
            throws AuthenticationException {
        accessToken = accessToken.replace(TOKEN_PREFIX, "");
        if (!jwtUtil.isValidateToken(accessToken)) {
            throw new AuthenticationException(INVALID_ACCESS_TOKEN);
        }
        String sessionId = jwtUtil.extractSessionId(accessToken);
        Optional<RefreshToken> refreshTokenEntity = refreshTokenDao.findById(sessionId);
        String tokenHash = encoder.encode(refreshToken);
        if (refreshTokenEntity.isEmpty()
                || !refreshTokenEntity.get().getToken().equals(tokenHash)
                || !refreshTokenEntity.get().getTimeout().after(new Date())) {
            throw new AuthenticationException(AUTHENTICATION_FAILED);
        }
        String username = jwtUtil.extractUsername(accessToken);
        List<String> roles = jwtUtil.extractRoles(accessToken);
        String userId = jwtUtil.extractUserId(accessToken);
        String newAccessToken = jwtUtil.generateToken(username, roles, userId, sessionId);
        return new AuthResponse(userId, newAccessToken, refreshToken);
    }
}
