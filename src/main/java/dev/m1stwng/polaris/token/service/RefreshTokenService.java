package dev.m1stwng.polaris.token.service;

import dev.m1stwng.polaris.token.entity.RefreshToken;
import dev.m1stwng.polaris.token.exception.InvalidRefreshTokenException;
import dev.m1stwng.polaris.token.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${refresh-token.expiration-days}")
    private Long REFRESH_TOKEN_EXPIRATION_DAYS;

    public RefreshToken generate(UUID userId) {
        final RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID())
                .userId(userId)
                .expiresAt(Instant.now().plus(REFRESH_TOKEN_EXPIRATION_DAYS, ChronoUnit.DAYS))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public UUID validateAndRevoke(UUID token) {
        final RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidRefreshTokenException(
                        "Refresh refreshToken with refreshToken %s was not found".formatted(token))
                );

        if (refreshToken.getRevokedAt() != null) {
            throw new InvalidRefreshTokenException(
                    "Refresh refreshToken with refreshToken %s was already revoked".formatted(token)
            );
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException(
                    "Refresh refreshToken with refreshToken %s has already expired".formatted(token)
            );
        }

        refreshToken.setRevokedAt(Instant.now());

        refreshTokenRepository.save(refreshToken);

        return refreshToken.getUserId();
    }

    public void revokeIfPresent(UUID token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshToken -> {
                    if (refreshToken.getRevokedAt() == null) {
                        refreshToken.setRevokedAt(Instant.now());
                        refreshTokenRepository.save(refreshToken);
                    }
                });
    }
}
