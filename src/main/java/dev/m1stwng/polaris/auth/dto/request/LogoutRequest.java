package dev.m1stwng.polaris.auth.dto.request;

import java.util.UUID;

public record LogoutRequest(UUID refreshToken) {
}
