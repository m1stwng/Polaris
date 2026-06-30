package dev.m1stwng.polaris.auth.controller;

import dev.m1stwng.polaris.AbstractIntegrationTest;
import dev.m1stwng.polaris.annotation.IntegrationTest;
import dev.m1stwng.polaris.auth.dto.request.LoginRequest;
import dev.m1stwng.polaris.auth.dto.request.LogoutRequest;
import dev.m1stwng.polaris.auth.dto.request.RefreshRequest;
import dev.m1stwng.polaris.auth.dto.request.RegisterRequest;
import dev.m1stwng.polaris.identity.role.entity.Role;
import dev.m1stwng.polaris.identity.user.entity.User;
import dev.m1stwng.polaris.identity.user.repository.UserRepository;
import dev.m1stwng.polaris.token.entity.RefreshToken;
import dev.m1stwng.polaris.token.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static dev.m1stwng.polaris.fixture.RefreshTokenFixture.TOKEN;
import static dev.m1stwng.polaris.fixture.UserFixture.EMAIL;
import static dev.m1stwng.polaris.fixture.UserFixture.PASSWORD;
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
    class LoginEndpoint {

        @Test
        @DisplayName("Should login a user")
        void shouldLoginUser() throws Exception {
            final User user = User.builder()
                    .email(EMAIL)
                    .password(passwordEncoder.encode(PASSWORD))
                    .role(Role.ROLE_CUSTOMER)
                    .build();

            userRepository.save(user);

            final LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.accessToken").isString())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").exists())
                    .andExpect(jsonPath("$.refreshToken").isString())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());

            assertEquals(1, refreshTokenRepository.count());
        }

        @Test
        @DisplayName("Should return UNAUTHORIZED when credentials are invalid")
        void shouldReturn401WhenCredentialsAreInvalid() throws Exception {
            final User user = User.builder()
                    .email(EMAIL)
                    .password(passwordEncoder.encode(PASSWORD))
                    .role(Role.ROLE_CUSTOMER)
                    .build();

            userRepository.save(user);

            final LoginRequest request = new LoginRequest("wrong@email.com", PASSWORD);

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.instance").value("/api/v1/auth/login"))
                    .andExpect(jsonPath("$.title").value("Bad credentials"))
                    .andExpect(jsonPath("$.detail").value("Email or password is invalid"))
                    .andExpect(jsonPath("$.status").value("401"));

            assertEquals(0, refreshTokenRepository.count());
        }
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

            final User user = userRepository.findByEmail(EMAIL).orElseThrow();

            assertAll(
                    () -> assertEquals(EMAIL, user.getEmail()),
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

            final User user = User.builder()
                    .email(EMAIL)
                    .password(passwordEncoder.encode(PASSWORD))
                    .role(Role.ROLE_CUSTOMER)
                    .build();

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

    @Nested
    class RefreshEndpoint {

        @Test
        @DisplayName("Should refresh a token")
        void shouldRefreshToken() throws Exception {
            final RefreshRequest request = new RefreshRequest(TOKEN);

            final User user = userRepository.save(User.builder()
                    .email(EMAIL)
                    .password(passwordEncoder.encode(PASSWORD))
                    .role(Role.ROLE_CUSTOMER)
                    .build()
            );

            final RefreshToken refreshToken = refreshTokenRepository.save(RefreshToken.builder()
                    .token(TOKEN)
                    .userId(user.getId())
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .build()
            );

            userRepository.save(user);
            refreshTokenRepository.save(refreshToken);

            mockMvc.perform(post("/api/v1/auth/refresh")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.accessToken").isString())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.refreshToken").exists())
                    .andExpect(jsonPath("$.refreshToken").isString())
                    .andExpect(jsonPath("$.refreshToken").isNotEmpty());

            assertEquals(2, refreshTokenRepository.count());
        }

        @Test
        @DisplayName("Should return UNAUTHORIZED when token does not exist")
        void shouldReturn401WhenTokenDoesNotExist() throws Exception {
            final RefreshRequest request = new RefreshRequest(TOKEN);

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.instance").value("/api/v1/auth/refresh"))
                    .andExpect(jsonPath("$.title").value("Invalid refresh token"))
                    .andExpect(jsonPath("$.detail").value("Refresh token is invalid, expired or not found"))
                    .andExpect(jsonPath("$.status").value("401"));
        }
    }

    @Nested
    class LogoutEndpoint {

        @Test
        @DisplayName("Should logout a user")
        void shouldLogoutUser() throws Exception {
            final LogoutRequest request = new LogoutRequest(TOKEN);

            final User user = userRepository.save(User.builder()
                    .email(EMAIL)
                    .password(passwordEncoder.encode(PASSWORD))
                    .role(Role.ROLE_CUSTOMER)
                    .build()
            );

            refreshTokenRepository.save(RefreshToken.builder()
                    .token(TOKEN)
                    .userId(user.getId())
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .build()
            );

            mockMvc.perform(post("/api/v1/auth/logout")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());

            final RefreshToken refreshToken = refreshTokenRepository.findByToken(TOKEN).orElseThrow();

            assertNotNull(refreshToken.getRevokedAt());
        }
    }
}
