package dev.m1stwng.polaris.auth.service;

import dev.m1stwng.polaris.auth.dto.request.LoginRequest;
import dev.m1stwng.polaris.auth.dto.request.RegisterRequest;
import dev.m1stwng.polaris.auth.dto.response.Tokenization;
import dev.m1stwng.polaris.auth.exception.DuplicatedEmailException;
import dev.m1stwng.polaris.identity.role.entity.Role;
import dev.m1stwng.polaris.identity.user.entity.User;
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

import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public Tokenization login(LoginRequest request) {
        final String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);

        final Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
        );

        final SecurityUser securityUser = Objects.requireNonNull((SecurityUser) auth.getPrincipal());

        final String accessToken = jwtService.generate(securityUser);
        final RefreshToken refreshToken = refreshTokenService.generate(securityUser);

        return new Tokenization(accessToken, refreshToken.getToken());
    }

    public Tokenization register(RegisterRequest request) {
        final String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);

        final boolean exists = userRepository.existsByEmail(normalizedEmail);

        if (exists) {
            throw new DuplicatedEmailException("Email %s is already registered".formatted(normalizedEmail));
        }

        final User user = User.builder()
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_CUSTOMER)
                .build();

        final User createdUser = userRepository.save(user);
        final SecurityUser securityUser = userMapper.userToSecurityUser(createdUser);

        final String accessToken = jwtService.generate(securityUser);
        final RefreshToken refreshToken = refreshTokenService.generate(securityUser);

        return new Tokenization(accessToken, refreshToken.getToken());
    }
}
