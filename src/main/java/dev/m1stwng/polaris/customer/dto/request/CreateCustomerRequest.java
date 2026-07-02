package dev.m1stwng.polaris.customer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateCustomerRequest(

        @NotBlank
        @Size(min = 3, max = 50)
        String name,

        @NotBlank
        @Size(min = 3, max = 150)
        String surname,

        @Size(max = 30)
        String phoneNumber,

        @NotNull
        LocalDate dateOfBirth
) {
}
