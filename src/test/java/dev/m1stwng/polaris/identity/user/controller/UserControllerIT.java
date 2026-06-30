package dev.m1stwng.polaris.identity.user.controller;

import dev.m1stwng.polaris.AbstractIntegrationTest;
import dev.m1stwng.polaris.annotation.IntegrationTest;
import dev.m1stwng.polaris.fixture.SecurityUserFixture;
import dev.m1stwng.polaris.fixture.UserFixture;
import dev.m1stwng.polaris.identity.role.entity.Role;
import dev.m1stwng.polaris.identity.user.entity.User;
import dev.m1stwng.polaris.identity.user.repository.UserRepository;
import dev.m1stwng.polaris.security.entity.SecurityUser;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static dev.m1stwng.polaris.fixture.UserFixture.EMAIL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
public class UserControllerIT extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void beforeEach() {
        userRepository.deleteAll();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class Me {

        @Test
        @DisplayName("Should return the authenticated user")
        void shouldReturnAuthenticatedUser() throws Exception {
            User user = UserFixture.customer();

            user = userRepository.save(user);

            final SecurityUser securityUser = new SecurityUser(
                    user.getId(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getRole()
            );

            final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    securityUser, null, securityUser.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            mockMvc.perform(get("/api/v1/users/me"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.id").isString())
                    .andExpect(jsonPath("$.email").value(EMAIL))
                    .andExpect(jsonPath("$.role").value(Role.ROLE_CUSTOMER.name()));
        }

        @Test
        @DisplayName("Should return NOT FOUND when user does not exist")
        void shouldReturn404WhenUserDoesNotExist() throws Exception {
            final SecurityUser securityUser = SecurityUserFixture.customer();

            final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    securityUser, null, securityUser.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            mockMvc.perform(get("/api/v1/users/me"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.instance").value("/api/v1/users/me"))
                    .andExpect(jsonPath("$.title").value("User not found"))
                    .andExpect(jsonPath("$.status").value("404"));
        }
    }
}
