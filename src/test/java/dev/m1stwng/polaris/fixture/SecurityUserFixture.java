package dev.m1stwng.polaris.fixture;

import dev.m1stwng.polaris.identity.role.entity.Role;
import dev.m1stwng.polaris.security.entity.SecurityUser;

import static dev.m1stwng.polaris.fixture.UserFixture.EMAIL;
import static dev.m1stwng.polaris.fixture.UserFixture.USER_ID;

public class SecurityUserFixture {

    private SecurityUserFixture() {}

    private static final String ENCODED_PASSWORD = "encoded-password";

    public static SecurityUser customer() {
        return new SecurityUser(USER_ID, EMAIL, ENCODED_PASSWORD, Role.ROLE_CUSTOMER);
    }
}
