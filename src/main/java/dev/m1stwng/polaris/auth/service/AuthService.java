package dev.m1stwng.polaris.auth.service;

import dev.m1stwng.polaris.auth.dto.request.LoginRequest;
import dev.m1stwng.polaris.auth.dto.request.LogoutRequest;
import dev.m1stwng.polaris.auth.dto.request.RefreshRequest;
import dev.m1stwng.polaris.auth.dto.request.RegisterRequest;
import dev.m1stwng.polaris.auth.dto.response.Tokenization;
import dev.m1stwng.polaris.auth.exception.DuplicatedEmailException;
import dev.m1stwng.polaris.common.normalization.EmailNormalizer;
import dev.m1stwng.polaris.identity.role.entity.Role;
import dev.m1stwng.polaris.identity.user.entity.User;
import dev.m1stwng.polaris.identity.user.exception.UserNotFoundException;
import dev.m1stwng.polaris.identity.user.mapper.UserMapper;
import dev.m1stwng.polaris.identity.user.repository.UserRepository;
import dev.m1stwng.polaris.security.entity.SecurityUser;
import dev.m1stwng.polaris.security.service.JwtService;
import dev.m1stwng.polaris.token.entity.RefreshToken;
import dev.m1stwng.polaris.token.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final EmailNormalizer emailNormalizer;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public Tokenization login(LoginRequest request) {
        final Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        emailNormalizer.normalize(request.email()),
                        request.password()
                )
        );

        final SecurityUser securityUser = Objects.requireNonNull((SecurityUser) auth.getPrincipal());

        final String accessToken = jwtService.generate(securityUser);
        final RefreshToken refreshToken = refreshTokenService.generate(securityUser.id());

        return new Tokenization(accessToken, refreshToken.getToken());
    }

    public Tokenization register(RegisterRequest request) {
        final String email = emailNormalizer.normalize(request.email());

        final boolean exists = userRepository.existsByEmail(email);

        if (exists) {
            throw new DuplicatedEmailException("Email %s is already registered".formatted(email));
        }

        final User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_CUSTOMER)
                .build();

        final User createdUser = userRepository.save(user);
        final SecurityUser securityUser = userMapper.userToSecurityUser(createdUser);

        final String accessToken = jwtService.generate(securityUser);
        final RefreshToken refreshToken = refreshTokenService.generate(securityUser.id());

        return new Tokenization(accessToken, refreshToken.getToken());
    }

    public Tokenization refresh(RefreshRequest request) {
        final UUID userId = refreshTokenService.validateAndRevoke(request.refreshToken());
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id %s was not found".formatted(userId)));
        final SecurityUser securityUser = userMapper.userToSecurityUser(user);

        final String accessToken = jwtService.generate(securityUser);
        final RefreshToken refreshToken = refreshTokenService.generate(securityUser.id());

        return new Tokenization(accessToken, refreshToken.getToken());
    }

    public void logout(LogoutRequest request) {
        refreshTokenService.revokeIfPresent(request.refreshToken());
    }
}
