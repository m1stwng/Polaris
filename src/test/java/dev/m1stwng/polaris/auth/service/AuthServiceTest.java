package dev.m1stwng.polaris.auth.service;

import dev.m1stwng.polaris.annotation.UnitTest;
import dev.m1stwng.polaris.auth.dto.request.LoginRequest;
import dev.m1stwng.polaris.auth.dto.request.LogoutRequest;
import dev.m1stwng.polaris.auth.dto.request.RefreshRequest;
import dev.m1stwng.polaris.auth.dto.request.RegisterRequest;
import dev.m1stwng.polaris.auth.dto.response.Tokenization;
import dev.m1stwng.polaris.auth.exception.DuplicatedEmailException;
import dev.m1stwng.polaris.common.normalization.EmailNormalizer;
import dev.m1stwng.polaris.fixture.UserFixture;
import dev.m1stwng.polaris.identity.role.entity.Role;
import dev.m1stwng.polaris.identity.user.entity.User;
import dev.m1stwng.polaris.identity.user.exception.UserNotFoundException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static dev.m1stwng.polaris.fixture.RefreshTokenFixture.REFRESH_TOKEN_ID;
import static dev.m1stwng.polaris.fixture.RefreshTokenFixture.TOKEN;
import static dev.m1stwng.polaris.fixture.UserFixture.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@UnitTest
public class AuthServiceTest {

    private static final String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.signature";
    private static final String UNNORMALIZED_EMAIL = "     JoHn@eXaMpLe.COM      ";

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

    @Captor
    private ArgumentCaptor<SecurityUser> securityUserCaptor;

    @Captor
    private ArgumentCaptor<UsernamePasswordAuthenticationToken> credentialsCaptor;

    @BeforeEach
    void setUp() {
        final EmailNormalizer emailNormalizer = new EmailNormalizer();
        final UserMapper userMapper = new UserMapperImpl();

        authService = new AuthService(
                authenticationManager,
                emailNormalizer,
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

            final UsernamePasswordAuthenticationToken auth = mock(UsernamePasswordAuthenticationToken.class);

            final SecurityUser securityUser = new SecurityUser(
                    USER_ID,
                    EMAIL,
                    null,
                    Role.ROLE_CUSTOMER
            );

            final RefreshToken refreshToken = RefreshToken.builder()
                    .token(TOKEN)
                    .userId(USER_ID)
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .build();

            refreshToken.setId(REFRESH_TOKEN_ID);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(auth);
            when(auth.getPrincipal()).thenReturn(securityUser);
            when(jwtService.generate(securityUser)).thenReturn(ACCESS_TOKEN);
            when(refreshTokenService.generate(USER_ID)).thenReturn(refreshToken);

            final Tokenization result = authService.login(request);

            verify(authenticationManager).authenticate(credentialsCaptor.capture());
            verify(auth).getPrincipal();
            verify(jwtService).generate(securityUser);
            verify(refreshTokenService).generate(securityUser.id());

            final UsernamePasswordAuthenticationToken credentials = credentialsCaptor.getValue();

            assertAll(
                    () -> assertEquals(ACCESS_TOKEN, result.accessToken()),
                    () -> assertEquals(TOKEN, result.refreshToken()),
                    () -> assertEquals(EMAIL, credentials.getPrincipal()),
                    () -> assertEquals(PASSWORD, credentials.getCredentials())
            );

            verifyNoMoreInteractions(authenticationManager, jwtService, refreshTokenService);
        }

        @Test
        @DisplayName("Should normalize the email when login a user")
        void shouldNormalizeEmailWhenLoginUser() {
            final LoginRequest request = new LoginRequest(UNNORMALIZED_EMAIL, PASSWORD);

            final UsernamePasswordAuthenticationToken auth = mock(UsernamePasswordAuthenticationToken.class);

            final SecurityUser securityUser = new SecurityUser(
                    USER_ID,
                    EMAIL,
                    null,
                    Role.ROLE_CUSTOMER
            );

            final RefreshToken refreshToken = RefreshToken.builder()
                    .token(TOKEN)
                    .userId(USER_ID)
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .build();

            refreshToken.setId(REFRESH_TOKEN_ID);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(auth);
            when(auth.getPrincipal()).thenReturn(securityUser);
            when(jwtService.generate(securityUser)).thenReturn(ACCESS_TOKEN);
            when(refreshTokenService.generate(USER_ID)).thenReturn(refreshToken);

            final Tokenization result = authService.login(request);

            verify(authenticationManager).authenticate(credentialsCaptor.capture());
            verify(auth).getPrincipal();
            verify(jwtService).generate(securityUser);
            verify(refreshTokenService).generate(securityUser.id());

            final UsernamePasswordAuthenticationToken credentials = credentialsCaptor.getValue();

            assertAll(
                    () -> assertEquals(ACCESS_TOKEN, result.accessToken()),
                    () -> assertEquals(TOKEN, result.refreshToken()),
                    () -> assertEquals(EMAIL, credentials.getPrincipal()),
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

            verify(authenticationManager).authenticate(credentialsCaptor.capture());

            final UsernamePasswordAuthenticationToken credentials = credentialsCaptor.getValue();

            assertAll(
                    () -> assertEquals(EMAIL, credentials.getPrincipal()),
                    () -> assertEquals(PASSWORD, credentials.getCredentials())
            );

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

            final RefreshToken refreshToken = RefreshToken.builder()
                    .token(TOKEN)
                    .userId(USER_ID)
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .build();

            refreshToken.setId(REFRESH_TOKEN_ID);

            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(any(User.class))).thenReturn(createdUser);
            when(jwtService.generate(any(SecurityUser.class))).thenReturn(ACCESS_TOKEN);
            when(refreshTokenService.generate(USER_ID)).thenReturn(refreshToken);

            final Tokenization result = authService.register(request);

            verify(userRepository).existsByEmail(EMAIL);
            verify(passwordEncoder).encode(PASSWORD);
            verify(userRepository).save(userCaptor.capture());
            verify(jwtService).generate(securityUserCaptor.capture());
            verify(refreshTokenService).generate(USER_ID);

            final User userBeforeSaving = userCaptor.getValue();
            final SecurityUser securityUser = securityUserCaptor.getValue();

            assertAll(
                    () -> assertEquals(ACCESS_TOKEN, result.accessToken()),
                    () -> assertEquals(TOKEN, result.refreshToken()),
                    () -> assertEquals(EMAIL, userBeforeSaving.getEmail()),
                    () -> assertEquals(ENCODED_PASSWORD, userBeforeSaving.getPassword()),
                    () -> assertEquals(Role.ROLE_CUSTOMER, userBeforeSaving.getRole()),
                    () -> assertEquals(USER_ID, securityUser.id()),
                    () -> assertEquals(EMAIL, securityUser.email()),
                    () -> assertEquals(ENCODED_PASSWORD, securityUser.password()),
                    () -> assertEquals(Role.ROLE_CUSTOMER, securityUser.role())
            );

            verifyNoMoreInteractions(
                    authenticationManager,
                    jwtService,
                    passwordEncoder,
                    refreshTokenService,
                    userRepository
            );
        }

        @Test
        @DisplayName("Should normalize the email when registering a user")
        void shouldNormalizeEmailWhenRegisteringUser() {
            final RegisterRequest request = new RegisterRequest(UNNORMALIZED_EMAIL, PASSWORD);

            final User createdUser = UserFixture.customer();
            createdUser.setId(USER_ID);

            final RefreshToken refreshToken = RefreshToken.builder()
                    .token(TOKEN)
                    .userId(USER_ID)
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .build();

            refreshToken.setId(REFRESH_TOKEN_ID);

            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(userRepository.save(any(User.class))).thenReturn(createdUser);
            when(jwtService.generate(any(SecurityUser.class))).thenReturn(ACCESS_TOKEN);
            when(refreshTokenService.generate(USER_ID)).thenReturn(refreshToken);

            final Tokenization result = authService.register(request);

            verify(userRepository).existsByEmail(EMAIL);
            verify(passwordEncoder).encode(PASSWORD);
            verify(userRepository).save(userCaptor.capture());
            verify(jwtService).generate(securityUserCaptor.capture());
            verify(refreshTokenService).generate(USER_ID);

            final User userBeforeSaving = userCaptor.getValue();
            final SecurityUser securityUser = securityUserCaptor.getValue();

            assertAll(
                    () -> assertEquals(ACCESS_TOKEN, result.accessToken()),
                    () -> assertEquals(TOKEN, result.refreshToken()),
                    () -> assertEquals(EMAIL, userBeforeSaving.getEmail()),
                    () -> assertEquals(ENCODED_PASSWORD, userBeforeSaving.getPassword()),
                    () -> assertEquals(Role.ROLE_CUSTOMER, userBeforeSaving.getRole()),
                    () -> assertEquals(USER_ID, securityUser.id()),
                    () -> assertEquals(EMAIL, securityUser.email()),
                    () -> assertEquals(ENCODED_PASSWORD, securityUser.password()),
                    () -> assertEquals(Role.ROLE_CUSTOMER, securityUser.role())
            );

            verifyNoMoreInteractions(
                    authenticationManager,
                    jwtService,
                    passwordEncoder,
                    refreshTokenService,
                    userRepository
            );
        }

        @Test
        @DisplayName("Should throw when email is already registered")
        void shouldThrowWhenEmailAlreadyExists() {
            final RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD);

            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

            final DuplicatedEmailException ex = assertThrows(
                    DuplicatedEmailException.class,
                    () -> authService.register(request)
            );

            assertEquals("Email %s is already registered".formatted(request.email()), ex.getMessage());

            verify(userRepository).existsByEmail(EMAIL);
            verify(userRepository, never()).save(any(User.class));

            verifyNoMoreInteractions(userRepository);
            verifyNoInteractions(passwordEncoder, jwtService, refreshTokenService);
        }
    }

    @Nested
    class Refresh {

        @Test
        @DisplayName("Should refresh a token")
        void shouldRefreshToken() {
            final RefreshRequest request = new RefreshRequest(TOKEN);

            final User user = UserFixture.customer();
            user.setId(USER_ID);

            final UUID newToken = UUID.fromString("7d8c067a-aefd-40c2-9a44-d9620bf365ba");

            final RefreshToken refreshToken = RefreshToken.builder()
                    .token(newToken)
                    .userId(USER_ID)
                    .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                    .build();

            when(refreshTokenService.validateAndRevoke(TOKEN)).thenReturn(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(jwtService.generate(any(SecurityUser.class))).thenReturn(ACCESS_TOKEN);
            when(refreshTokenService.generate(USER_ID)).thenReturn(refreshToken);

            final Tokenization result = authService.refresh(request);

            verify(refreshTokenService).validateAndRevoke(TOKEN);
            verify(userRepository).findById(USER_ID);
            verify(jwtService).generate(securityUserCaptor.capture());
            verify(refreshTokenService).generate(USER_ID);

            final SecurityUser securityUser = securityUserCaptor.getValue();

            assertAll(
                    () -> assertEquals(ACCESS_TOKEN, result.accessToken()),
                    () -> assertEquals(newToken, result.refreshToken()),
                    () -> assertNotEquals(TOKEN, newToken),
                    () -> assertEquals(USER_ID, securityUser.id()),
                    () -> assertEquals(EMAIL, securityUser.email()),
                    () -> assertEquals(ENCODED_PASSWORD, securityUser.password()),
                    () -> assertEquals(Role.ROLE_CUSTOMER, securityUser.role())
            );

            verifyNoMoreInteractions(refreshTokenService, userRepository, jwtService);
        }

        @Test
        @DisplayName("Should throw when user does not exist")
        void shouldThrowWhenUserDoesNotExist() {
            final RefreshRequest request = new RefreshRequest(TOKEN);

            when(refreshTokenService.validateAndRevoke(TOKEN)).thenReturn(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            final UserNotFoundException ex = assertThrows(
                    UserNotFoundException.class,
                    () -> authService.refresh(request)
            );

            verify(refreshTokenService).validateAndRevoke(TOKEN);
            verify(userRepository).findById(USER_ID);

            assertEquals("User with id %s was not found".formatted(USER_ID), ex.getMessage());

            verifyNoMoreInteractions(refreshTokenService, userRepository);
            verifyNoInteractions(jwtService);
        }
    }

    @Nested
    class Logout {

        @Test
        void shouldLogoutUser() {
            final LogoutRequest request = new LogoutRequest(TOKEN);

            authService.logout(request);

            verify(refreshTokenService).revokeIfPresent(TOKEN);

            verifyNoMoreInteractions(refreshTokenService);
        }
    }
}
