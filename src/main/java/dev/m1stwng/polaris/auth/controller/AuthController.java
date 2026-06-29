package dev.m1stwng.polaris.auth.controller;

import dev.m1stwng.polaris.auth.dto.request.RegisterRequest;
import dev.m1stwng.polaris.auth.dto.response.Tokenization;
import dev.m1stwng.polaris.auth.service.AuthService;
import dev.m1stwng.polaris.config.openapi.BadRequestApiResponse;
import dev.m1stwng.polaris.config.openapi.ConflictApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
@SecurityRequirements
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ApiResponse(responseCode = "201")
    @BadRequestApiResponse
    @ConflictApiResponse
    public ResponseEntity<Tokenization> register(@RequestBody @Valid RegisterRequest request) {
        final Tokenization tokenization = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(tokenization);
    }
}
