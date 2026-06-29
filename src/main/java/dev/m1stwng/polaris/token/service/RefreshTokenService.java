package dev.m1stwng.polaris.token.service;

import dev.m1stwng.polaris.security.entity.SecurityUser;
import dev.m1stwng.polaris.token.entity.RefreshToken;
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

    public RefreshToken generate(SecurityUser securityUser) {
        final RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID())
                .userId(securityUser.id())
                .expiresAt(Instant.now().plus(REFRESH_TOKEN_EXPIRATION_DAYS, ChronoUnit.DAYS))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }
}
