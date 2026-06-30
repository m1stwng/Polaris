package dev.m1stwng.polaris.identity.user.service;

import dev.m1stwng.polaris.identity.user.dto.response.UserResponse;
import dev.m1stwng.polaris.identity.user.exception.UserNotFoundException;
import dev.m1stwng.polaris.identity.user.mapper.UserMapper;
import dev.m1stwng.polaris.identity.user.repository.UserRepository;
import dev.m1stwng.polaris.security.entity.SecurityUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public UserResponse getAuthenticatedUser(SecurityUser securityUser) {
        return userRepository.findById(securityUser.id())
                .map(userMapper::userToUserResponse)
                .orElseThrow(() -> new UserNotFoundException("User with id %s was not found".formatted(securityUser.id())));
    }
}
