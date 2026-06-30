package dev.m1stwng.polaris.security.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import dev.m1stwng.polaris.security.entity.SecurityUser;
import dev.m1stwng.polaris.security.exception.JwtException;
import dev.m1stwng.polaris.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@RequiredArgsConstructor
@NullMarked
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authenticationHeader = request.getHeader(AUTHORIZATION);

        if (authenticationHeader == null || !authenticationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authenticationHeader.substring(7);

        try {
            final SecurityUser securityUser = jwtService.verify(token);

            final SecurityContext context = SecurityContextHolder.createEmptyContext();

            context.setAuthentication(
                    new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities())
            );

            SecurityContextHolder.setContext(context);

            filterChain.doFilter(request, response);
        } catch (JWTVerificationException ex) {
            SecurityContextHolder.clearContext();

            authenticationEntryPoint.commence(request, response, new JwtException(ex.getMessage()));
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/auth/");
    }
}
