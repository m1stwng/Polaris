package dev.m1stwng.polaris.fixture;

import dev.m1stwng.polaris.identity.role.entity.Role;
import dev.m1stwng.polaris.security.entity.SecurityUser;

import static dev.m1stwng.polaris.fixture.UserFixture.*;

public class SecurityUserFixture {

    private SecurityUserFixture() {}

    public static SecurityUser customer() {
        return new SecurityUser(USER_ID, EMAIL, ENCODED_PASSWORD, Role.ROLE_CUSTOMER);
    }
}
