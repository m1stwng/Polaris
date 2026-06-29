package dev.m1stwng.polaris.auth.controller;

import dev.m1stwng.polaris.AbstractIntegrationTest;
import dev.m1stwng.polaris.annotation.IntegrationTest;
import dev.m1stwng.polaris.auth.dto.request.RegisterRequest;
import dev.m1stwng.polaris.fixture.UserFixture;
import dev.m1stwng.polaris.identity.role.entity.Role;
import dev.m1stwng.polaris.identity.user.entity.User;
import dev.m1stwng.polaris.identity.user.repository.UserRepository;
import dev.m1stwng.polaris.token.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static dev.m1stwng.polaris.fixture.UserFixture.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
public class AuthControllerIT extends AbstractIntegrationTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void beforeEach() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    class RegisterEndpoint {

        @Test
        @DisplayName("Should register a user")
        void shouldRegisterUser() throws Exception {
            final RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD);

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.accessToken").isString())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").exists())
                    .andExpect(jsonPath("$.refreshToken").isString())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());

            final User user = userRepository.findByEmail(NORMALIZED_EMAIL).orElseThrow();

            assertAll(
                    () -> assertEquals(NORMALIZED_EMAIL, user.getEmail()),
                    () -> assertTrue(passwordEncoder.matches(request.password(), user.getPassword())),
                    () -> assertEquals(Role.ROLE_CUSTOMER, user.getRole()),
                    () -> assertEquals(1, userRepository.count()),
                    () -> assertEquals(1, refreshTokenRepository.count())
            );
        }

        @Test
        @DisplayName("Should return CONFLICT when email is already registered")
        void shouldReturn409WhenEmailAlreadyExists() throws Exception {
            final RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD);

            final User user = UserFixture.customer();

            userRepository.save(user);

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.instance").value("/api/v1/auth/register"))
                    .andExpect(jsonPath("$.title").value("Duplicated email"))
                    .andExpect(jsonPath("$.detail").value("This email is already registered"))
                    .andExpect(jsonPath("$.status").value("409"));

            assertAll(
                    () -> assertEquals(1, userRepository.count()),
                    () -> assertEquals(0, refreshTokenRepository.count())
            );
        }
    }
}
