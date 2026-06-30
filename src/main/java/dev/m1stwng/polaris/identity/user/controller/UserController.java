package dev.m1stwng.polaris.identity.user.controller;

import dev.m1stwng.polaris.config.openapi.NotFoundApiResponse;
import dev.m1stwng.polaris.config.openapi.UnauthorizedApiResponse;
import dev.m1stwng.polaris.identity.user.dto.response.UserResponse;
import dev.m1stwng.polaris.identity.user.service.UserService;
import dev.m1stwng.polaris.security.entity.SecurityUser;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @ApiResponse(responseCode = "200")
    @UnauthorizedApiResponse
    @NotFoundApiResponse
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal SecurityUser securityUser) {
        final UserResponse response = userService.getAuthenticatedUser(securityUser);

        return ResponseEntity.ok(response);
    }
}
