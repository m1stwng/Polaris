package dev.m1stwng.polaris.identity.user.dto.response;

import dev.m1stwng.polaris.identity.role.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record UserResponse(

        UUID id,

        @Schema(example = "user@example.com")
        String email,
        Role role
) {
}
