package dev.m1stwng.polaris.auth.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RefreshRequest(@NotNull UUID refreshToken) {
}
