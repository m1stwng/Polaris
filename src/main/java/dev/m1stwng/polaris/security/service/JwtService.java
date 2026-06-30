package dev.m1stwng.polaris.security.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.JWTVerifier;
import dev.m1stwng.polaris.identity.role.entity.Role;
import dev.m1stwng.polaris.security.entity.SecurityUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    @Value("${jwt.access-token-expiration-seconds}")
    private Long ACCESS_TOKEN_EXPIRATION_SECONDS;

    public JwtService(@Value("${jwt.secret}") String secret) {
        algorithm = Algorithm.HMAC256(secret);
        verifier = JWT.require(algorithm).build();
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

    public SecurityUser verify(String token) {
        final Map<String, Claim> claims = verifier.verify(token).getClaims();

        return new SecurityUser(
                UUID.fromString(claims.get("id").asString()),
                claims.get("email").asString(),
                null,
                Role.valueOf(claims.get("role").asString())
        );
    }
}
