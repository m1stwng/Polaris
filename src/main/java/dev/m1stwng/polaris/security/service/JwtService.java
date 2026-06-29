package dev.m1stwng.polaris.security.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import dev.m1stwng.polaris.security.entity.SecurityUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {
    private final Algorithm algorithm;

    @Value("${jwt.access-token-expiration-seconds}")
    private Long ACCESS_TOKEN_EXPIRATION_SECONDS;

    public JwtService(@Value("${jwt.secret}") String secret) {
        algorithm = Algorithm.HMAC256(secret);
    }

    public String generate(SecurityUser securityUser) {
        final Instant now = Instant.now();

        return JWT.create()
                .withIssuedAt(now)
                .withExpiresAt(now.plusSeconds(ACCESS_TOKEN_EXPIRATION_SECONDS))
                .withSubject(securityUser.email())
                .withClaim("id", String.valueOf(securityUser.id()))
                .withClaim("email", securityUser.email())
                .withClaim("role", securityUser.role().name())
                .sign(algorithm);
    }
}
