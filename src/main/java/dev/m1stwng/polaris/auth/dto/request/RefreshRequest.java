package dev.m1stwng.polaris.auth.dto.request;

import java.util.UUID;

public record RefreshRequest(UUID refreshToken) {
}
