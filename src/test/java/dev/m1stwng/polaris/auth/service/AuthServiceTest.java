package dev.m1stwng.polaris.auth.service;

import dev.m1stwng.polaris.annotation.UnitTest;
import dev.m1stwng.polaris.auth.dto.request.RegisterRequest;
import dev.m1stwng.polaris.auth.dto.response.Tokenization;
import dev.m1stwng.polaris.auth.exception.DuplicatedEmailException;
import dev.m1stwng.polaris.fixture.UserFixture;
import dev.m1stwng.polaris.identity.role.entity.Role;
import dev.m1stwng.polaris.identity.user.entity.User;
import dev.m1stwng.polaris.identity.user.mapper.UserMapper;
import dev.m1stwng.polaris.identity.user.mapper.UserMapperImpl;
import dev.m1stwng.polaris.identity.user.repository.UserRepository;
import dev.m1stwng.polaris.security.entity.SecurityUser;
import dev.m1stwng.polaris.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import static dev.m1stwng.polaris.fixture.UserFixture.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@UnitTest
public class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    private AuthService authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void setUp() {
        final UserMapper userMapper = new UserMapperImpl();

        authService = new AuthService(jwtService, passwordEncoder, userMapper, userRepository);
    }

    @Nested
    class Register {

        @Test
        @DisplayName("Should register a user")
        void shouldRegisterUser() {
            final RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD);

            final User createdUser = UserFixture.customer();

            createdUser.setId(USER_ID);

            when(userRepository.existsByEmail(NORMALIZED_EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(request.password())).thenReturn(createdUser.getPassword());
            when(userRepository.save(any(User.class))).thenReturn(createdUser);
            when(jwtService.generate(any(SecurityUser.class))).thenReturn(ACCESS_TOKEN);

            final Tokenization result = authService.register(request);

            verify(userRepository).existsByEmail(NORMALIZED_EMAIL);
            verify(passwordEncoder).encode(request.password());
            verify(userRepository).save(userCaptor.capture());
            verify(jwtService).generate(any(SecurityUser.class));

            final User userBeforeSaving = userCaptor.getValue();

            assertAll(
                    () -> assertEquals(ACCESS_TOKEN, result.accessToken()),
                    () -> assertEquals(NORMALIZED_EMAIL, userBeforeSaving.getEmail()),
                    () -> assertEquals(request.password(), userBeforeSaving.getPassword()),
                    () -> assertEquals(Role.ROLE_CUSTOMER, userBeforeSaving.getRole())
            );

            verifyNoMoreInteractions(jwtService, passwordEncoder, userRepository);
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
            verifyNoInteractions(passwordEncoder, jwtService);
        }
    }
}
