package dev.m1stwng.polaris.fixture;

import dev.m1stwng.polaris.identity.role.entity.Role;
import dev.m1stwng.polaris.identity.user.entity.User;

import java.util.UUID;

public class UserFixture {

    private UserFixture() {
    }

    public static final String ACCESS_TOKEN = "access-token";
    public static final String EMAIL = "customer@example.com";
    public static final String NORMALIZED_EMAIL = "customer@example.com";
    public static final String PASSWORD = "password123";
    public static final UUID USER_ID = UUID.fromString("1d59ed60-36b1-401c-ae7c-d7018e87a82c");

    public static User customer() {
        return User.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .role(Role.ROLE_CUSTOMER)
                .build();
    }
}
