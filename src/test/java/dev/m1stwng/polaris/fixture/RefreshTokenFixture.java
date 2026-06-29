package dev.m1stwng.polaris.fixture;

import dev.m1stwng.polaris.identity.user.entity.User;
import dev.m1stwng.polaris.token.entity.RefreshToken;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static dev.m1stwng.polaris.fixture.UserFixture.USER_ID;

public class RefreshTokenFixture {

    private RefreshTokenFixture() {}

    public static final UUID REFRESH_TOKEN_ID = UUID.fromString("aca0a01b-f529-426c-bd40-5ce4db6c1048");
    public static final UUID TOKEN = UUID.fromString("735e8a38-9c9f-4eb2-8afa-6c62ef92c36a");


    public static RefreshToken refreshToken() {
        final User user = UserFixture.customer();

        user.setId(USER_ID);

        return RefreshToken.builder()
                .token(TOKEN)
                .userId(USER_ID)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();
    }
}
