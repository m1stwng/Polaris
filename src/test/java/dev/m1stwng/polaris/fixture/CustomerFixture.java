package dev.m1stwng.polaris.fixture;

import dev.m1stwng.polaris.customer.entity.Customer;

import java.time.LocalDate;
import java.util.UUID;

public class CustomerFixture {

    private CustomerFixture() {}

    public static final UUID CUSTOMER_ID = UUID.fromString("741d21d8-edff-4da7-81a5-e04a5a08dffc");
    public static final String CUSTOMER_NAME = "Theresita";
    public static final String CUSTOMER_SURNAME = "McRannell";
    public static final String CUSTOMER_PHONE_NUMBER = "(813) 7529971";
    public static final LocalDate CUSTOMER_DATE_OF_BIRTH = LocalDate.of(1976, 5, 6);

    public static Customer customer()
    {
        return Customer.builder()
                .name(CUSTOMER_NAME)
                .surname(CUSTOMER_SURNAME)
                .phoneNumber(CUSTOMER_PHONE_NUMBER)
                .dateOfBirth(CUSTOMER_DATE_OF_BIRTH)
                .build();
    }
}
