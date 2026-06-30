package dev.m1stwng.polaris.identity.user.service;

import dev.m1stwng.polaris.annotation.UnitTest;
import dev.m1stwng.polaris.fixture.SecurityUserFixture;
import dev.m1stwng.polaris.fixture.UserFixture;
import dev.m1stwng.polaris.identity.role.entity.Role;
import dev.m1stwng.polaris.identity.user.dto.response.UserResponse;
import dev.m1stwng.polaris.identity.user.entity.User;
import dev.m1stwng.polaris.identity.user.exception.UserNotFoundException;
import dev.m1stwng.polaris.identity.user.mapper.UserMapper;
import dev.m1stwng.polaris.identity.user.mapper.UserMapperImpl;
import dev.m1stwng.polaris.identity.user.repository.UserRepository;
import dev.m1stwng.polaris.security.entity.SecurityUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Optional;

import static dev.m1stwng.polaris.fixture.UserFixture.EMAIL;
import static dev.m1stwng.polaris.fixture.UserFixture.USER_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@UnitTest
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        final UserMapper userMapper = new UserMapperImpl();

        userService = new UserService(userMapper, userRepository);
    }

    @Nested
    class GetAuthenticatedUser {

        @Test
        @DisplayName("Should return a user")
        void shouldReturnUser() {
            final User user = UserFixture.customer();
            user.setId(USER_ID);

            final SecurityUser securityUser = SecurityUserFixture.customer();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            final UserResponse result = userService.getAuthenticatedUser(securityUser);

            verify(userRepository).findById(USER_ID);

            assertAll(
                    () -> assertEquals(USER_ID, result.id()),
                    () -> assertEquals(EMAIL, result.email()),
                    () -> assertEquals(Role.ROLE_CUSTOMER, result.role())
            );

            verifyNoMoreInteractions(userRepository);
        }

        @Test
        void shouldThrowWhenUserNotFound() {
            final SecurityUser securityUser = SecurityUserFixture.customer();

            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            final UserNotFoundException ex = assertThrows(
                    UserNotFoundException.class,
                    () -> userService.getAuthenticatedUser(securityUser)
            );

            verify(userRepository).findById(USER_ID);
            verifyNoMoreInteractions(userRepository);

            assertEquals("User with id %s was not found".formatted(USER_ID), ex.getMessage());
        }
    }
}
