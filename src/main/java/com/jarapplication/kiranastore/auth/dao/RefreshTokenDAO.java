package com.jarapplication.kiranastore.auth.dao;

import com.jarapplication.kiranastore.auth.entity.RefreshToken;
import com.jarapplication.kiranastore.auth.repository.RefreshTokenRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenDAO {
    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public RefreshTokenDAO(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * save new refresh token data
     *
     * @param refreshToken
     * @return
     */
    public RefreshToken save(RefreshToken refreshToken) {
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Retrieves Refresh token data from sessionID (id of the entity)
     *
     * @param sessionId
     * @return
     */
    public Optional<RefreshToken> findById(String sessionId) {
        return refreshTokenRepository.findById(sessionId);
    }
}
