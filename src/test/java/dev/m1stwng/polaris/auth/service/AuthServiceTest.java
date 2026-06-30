package dev.m1stwng.polaris.auth.service;

import dev.m1stwng.polaris.annotation.UnitTest;
import dev.m1stwng.polaris.auth.dto.request.LoginRequest;
import dev.m1stwng.polaris.auth.dto.request.LogoutRequest;
import dev.m1stwng.polaris.auth.dto.request.RegisterRequest;
import dev.m1stwng.polaris.auth.dto.response.Tokenization;
import dev.m1stwng.polaris.auth.exception.DuplicatedEmailException;
import dev.m1stwng.polaris.fixture.RefreshTokenFixture;
import dev.m1stwng.polaris.fixture.SecurityUserFixture;
import dev.m1stwng.polaris.fixture.UserFixture;
import dev.m1stwng.polaris.identity.role.entity.Role;
import dev.m1stwng.polaris.identity.user.entity.User;
import dev.m1stwng.polaris.identity.user.mapper.UserMapper;
import dev.m1stwng.polaris.identity.user.mapper.UserMapperImpl;
import dev.m1stwng.polaris.identity.user.repository.UserRepository;
import dev.m1stwng.polaris.security.entity.SecurityUser;
import dev.m1stwng.polaris.security.service.JwtService;
import dev.m1stwng.polaris.token.entity.RefreshToken;
import dev.m1stwng.polaris.token.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static dev.m1stwng.polaris.fixture.RefreshTokenFixture.REFRESH_TOKEN_ID;
import static dev.m1stwng.polaris.fixture.RefreshTokenFixture.TOKEN;
import static dev.m1stwng.polaris.fixture.UserFixture.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@UnitTest
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserRepository userRepository;

    private AuthService authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setUp() {
        final UserMapper userMapper = new UserMapperImpl();

        authService = new AuthService(
                authenticationManager,
                jwtService,
                passwordEncoder,
                refreshTokenService,
                userMapper,
                userRepository
        );
    }

    @Nested
    class Login {

        @Test
        @DisplayName("Should login a user")
        void shouldLoginUser() {
            final LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

            final Authentication auth = mock(Authentication.class);
            final SecurityUser securityUser = SecurityUserFixture.customer();

            final RefreshToken refreshToken = RefreshTokenFixture.refreshToken();

            refreshToken.setId(REFRESH_TOKEN_ID);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
            when(auth.getPrincipal()).thenReturn(securityUser);
            when(jwtService.generate(securityUser)).thenReturn(ACCESS_TOKEN);
            when(refreshTokenService.generate(securityUser)).thenReturn(refreshToken);

            final Tokenization result = authService.login(request);

            final ArgumentCaptor<UsernamePasswordAuthenticationToken> captor = ArgumentCaptor.forClass(
                    UsernamePasswordAuthenticationToken.class
            );

            verify(authenticationManager).authenticate(captor.capture());
            verify(jwtService).generate(securityUser);
            verify(refreshTokenService).generate(securityUser);

            final UsernamePasswordAuthenticationToken credentials = captor.getValue();

            assertAll(
                    () -> assertEquals(ACCESS_TOKEN, result.accessToken()),
                    () -> assertEquals(TOKEN, result.refreshToken()),
                    () -> assertEquals(NORMALIZED_EMAIL, credentials.getPrincipal()),
                    () -> assertEquals(PASSWORD, credentials.getCredentials())
            );

            verifyNoMoreInteractions(authenticationManager, jwtService, refreshTokenService);
        }

        @Test
        @DisplayName("Should throw when credentials are invalid")
        void shouldThrowWhenCredentialsAreInvalid() {
            final LoginRequest request = new LoginRequest(EMAIL, PASSWORD);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            assertThrows(BadCredentialsException.class, () -> authService.login(request));

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verifyNoMoreInteractions(authenticationManager);

            verifyNoInteractions(jwtService, refreshTokenService);
        }
    }

    @Nested
    class Register {

        @Test
        @DisplayName("Should register a user")
        void shouldRegisterUser() {
            final RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD);

            final User createdUser = UserFixture.customer();

            createdUser.setId(USER_ID);

            final RefreshToken refreshToken = RefreshTokenFixture.refreshToken();

            refreshToken.setId(REFRESH_TOKEN_ID);

            when(userRepository.existsByEmail(NORMALIZED_EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(request.password())).thenReturn(createdUser.getPassword());
            when(userRepository.save(any(User.class))).thenReturn(createdUser);
            when(jwtService.generate(any(SecurityUser.class))).thenReturn(ACCESS_TOKEN);
            when(refreshTokenService.generate(any(SecurityUser.class))).thenReturn(refreshToken);

            final Tokenization result = authService.register(request);

            verify(userRepository).existsByEmail(NORMALIZED_EMAIL);
            verify(passwordEncoder).encode(request.password());
            verify(userRepository).save(userCaptor.capture());
            verify(jwtService).generate(any(SecurityUser.class));
            verify(refreshTokenService).generate(any(SecurityUser.class));

            final User userBeforeSaving = userCaptor.getValue();

            assertAll(
                    () -> assertEquals(ACCESS_TOKEN, result.accessToken()),
                    () -> assertEquals(TOKEN, result.refreshToken()),
                    () -> assertEquals(NORMALIZED_EMAIL, userBeforeSaving.getEmail()),
                    () -> assertEquals(request.password(), userBeforeSaving.getPassword()),
                    () -> assertEquals(Role.ROLE_CUSTOMER, userBeforeSaving.getRole())
            );

            verifyNoMoreInteractions(jwtService, passwordEncoder, refreshTokenService, userRepository);
        }

        @Test
        @DisplayName("Should throw when email is already registered")
        void shouldThrowWhenEmailAlreadyExists() {
            final RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD);

            when(userRepository.existsByEmail(NORMALIZED_EMAIL)).thenReturn(true);

            final DuplicatedEmailException ex = assertThrows(
                    DuplicatedEmailException.class,
                    () -> authService.register(request)
            );

            assertEquals("Email %s is already registered".formatted(request.email()), ex.getMessage());

            verify(userRepository).existsByEmail(NORMALIZED_EMAIL);
            verify(userRepository, never()).save(any(User.class));

            verifyNoMoreInteractions(userRepository);
            verifyNoInteractions(passwordEncoder, jwtService, refreshTokenService);
        }
    }

    @Nested
    class Logout {

        @Test
        @DisplayName("Should logout a user")
        void shouldLogoutUser() {
            final LogoutRequest request = new LogoutRequest(TOKEN);

            authService.logout(request);

            verify(refreshTokenService).revoke(TOKEN);
        }
    }
}
