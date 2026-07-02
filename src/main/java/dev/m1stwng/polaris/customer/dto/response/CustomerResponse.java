package dev.m1stwng.polaris.customer.dto.response;

import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String name,
        String surname,
        String phoneNumber,
        String dateOfBirth
) {
}
