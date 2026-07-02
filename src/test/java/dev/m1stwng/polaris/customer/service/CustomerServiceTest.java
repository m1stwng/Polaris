package dev.m1stwng.polaris.customer.service;

import dev.m1stwng.polaris.annotation.UnitTest;
import dev.m1stwng.polaris.customer.dto.request.CreateCustomerRequest;
import dev.m1stwng.polaris.customer.dto.request.UpdateCustomerRequest;
import dev.m1stwng.polaris.customer.dto.response.CustomerResponse;
import dev.m1stwng.polaris.customer.entity.Customer;
import dev.m1stwng.polaris.customer.exception.CustomerAlreadyExistsException;
import dev.m1stwng.polaris.customer.exception.CustomerNotFoundException;
import dev.m1stwng.polaris.customer.mapper.CustomerMapper;
import dev.m1stwng.polaris.customer.mapper.CustomerMapperImpl;
import dev.m1stwng.polaris.customer.repository.CustomerRepository;
import dev.m1stwng.polaris.fixture.CustomerFixture;
import dev.m1stwng.polaris.fixture.UserFixture;
import dev.m1stwng.polaris.identity.user.entity.User;
import dev.m1stwng.polaris.identity.user.exception.UserNotFoundException;
import dev.m1stwng.polaris.identity.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.Optional;

import static dev.m1stwng.polaris.fixture.CustomerFixture.*;
import static dev.m1stwng.polaris.fixture.UserFixture.USER_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@UnitTest
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    private CustomerService customerService;

    @Captor
    private ArgumentCaptor<Customer> customerCaptor;

    @BeforeEach
    void beforeEach() {
        final CustomerMapper customerMapper = new CustomerMapperImpl();

        customerService = new CustomerService(customerMapper, customerRepository, userRepository);
    }

    @Nested
    class FindByUserId {

        @Test
        @DisplayName("Should find a customer by a user id")
        void shouldFindCustomerByUserId() {
            final User user = UserFixture.customer();
            user.setId(USER_ID);

            final Customer customer = CustomerFixture.customer();
            customer.setId(CUSTOMER_ID);
            customer.setUser(user);

            when(customerRepository.findByUserId(USER_ID)).thenReturn(Optional.of(customer));

            final CustomerResponse result = customerService.findByUserId(USER_ID);

            verify(customerRepository).findByUserId(USER_ID);

            assertAll(
                    () -> assertEquals(CUSTOMER_ID, result.id()),
                    () -> assertEquals(CUSTOMER_NAME, result.name()),
                    () -> assertEquals(CUSTOMER_SURNAME, result.surname()),
                    () -> assertEquals(CUSTOMER_PHONE_NUMBER, result.phoneNumber()),
                    () -> assertEquals(CUSTOMER_DATE_OF_BIRTH.toString(), result.dateOfBirth())
            );

            verifyNoMoreInteractions(customerRepository);
        }

        @Test
        @DisplayName("Should throw when customer not found by user id")
        void shouldThrowWhenCustomerNotFoundByUserId() {
            when(customerRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            final CustomerNotFoundException ex = assertThrows(
                    CustomerNotFoundException.class,
                    () -> customerService.findByUserId(USER_ID)
            );

            verify(customerRepository).findByUserId(USER_ID);

            assertEquals("Customer with user id %s was not found".formatted(USER_ID), ex.getMessage());

            verifyNoMoreInteractions(customerRepository);
        }
    }

    @Nested
    class Create {

        @Test
        @DisplayName("Should create a customer")
        void shouldCreateCustomer() {
            final CreateCustomerRequest request = new CreateCustomerRequest(
                    CUSTOMER_NAME,
                    CUSTOMER_SURNAME,
                    CUSTOMER_PHONE_NUMBER,
                    CUSTOMER_DATE_OF_BIRTH
            );

            final User user = UserFixture.customer();
            user.setId(USER_ID);

            final Customer customer = CustomerFixture.customer();
            customer.setId(CUSTOMER_ID);
            customer.setUser(user);

            when(customerRepository.existsByUserId(USER_ID)).thenReturn(false);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(customerRepository.save(any(Customer.class))).thenReturn(customer);

            final CustomerResponse result = customerService.create(USER_ID, request);

            verify(customerRepository).existsByUserId(USER_ID);
            verify(userRepository).findById(USER_ID);
            verify(customerRepository).save(customerCaptor.capture());

            final Customer customerBeforeSaving = customerCaptor.getValue();

            assertAll(
                    () -> assertEquals(CUSTOMER_ID, result.id()),
                    () -> assertEquals(CUSTOMER_NAME, result.name()),
                    () -> assertEquals(CUSTOMER_SURNAME, result.surname()),
                    () -> assertEquals(CUSTOMER_PHONE_NUMBER, result.phoneNumber()),
                    () -> assertEquals(CUSTOMER_DATE_OF_BIRTH.toString(), result.dateOfBirth())
            );

            assertAll(
                    () -> assertNull(customerBeforeSaving.getId()),
                    () -> assertEquals(CUSTOMER_NAME, customerBeforeSaving.getName()),
                    () -> assertEquals(CUSTOMER_SURNAME, customerBeforeSaving.getSurname()),
                    () -> assertEquals(CUSTOMER_PHONE_NUMBER, customerBeforeSaving.getPhoneNumber()),
                    () -> assertEquals(CUSTOMER_DATE_OF_BIRTH, customerBeforeSaving.getDateOfBirth()),
                    () -> assertEquals(user, customerBeforeSaving.getUser())
            );

            verifyNoMoreInteractions(customerRepository, userRepository);
        }

        @Test
        @DisplayName("Should throw when customer already exists")
        void shouldThrowWhenCustomerAlreadyExists() {
            final CreateCustomerRequest request = new CreateCustomerRequest(
                    CUSTOMER_NAME,
                    CUSTOMER_SURNAME,
                    CUSTOMER_PHONE_NUMBER,
                    CUSTOMER_DATE_OF_BIRTH
            );

            when(customerRepository.existsByUserId(USER_ID)).thenReturn(true);

            final CustomerAlreadyExistsException ex = assertThrows(
                    CustomerAlreadyExistsException.class,
                    () -> customerService.create(USER_ID, request)
            );

            verify(customerRepository).existsByUserId(USER_ID);

            assertEquals("Customer with user id %s already exists".formatted(USER_ID), ex.getMessage());

            verifyNoInteractions(userRepository);
            verifyNoMoreInteractions(customerRepository);
        }

        @Test
        @DisplayName("Should throw when user was not found")
        void shouldThrowWhenUserNotFound() {
            final CreateCustomerRequest request = new CreateCustomerRequest(
                    CUSTOMER_NAME,
                    CUSTOMER_SURNAME,
                    CUSTOMER_PHONE_NUMBER,
                    CUSTOMER_DATE_OF_BIRTH
            );

            when(customerRepository.existsByUserId(USER_ID)).thenReturn(false);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            final UserNotFoundException ex = assertThrows(
                    UserNotFoundException.class,
                    () -> customerService.create(USER_ID, request)
            );

            verify(customerRepository).existsByUserId(USER_ID);
            verify(userRepository).findById(USER_ID);

            assertEquals("User with id %s was not found".formatted(USER_ID), ex.getMessage());

            verifyNoMoreInteractions(customerRepository, userRepository);
        }
    }

    @Nested
    class Update {

        @Test
        @DisplayName("Should update a customer")
        void shouldUpdateCustomer() {
            final LocalDate dateOfBirth = LocalDate.of(2000, 12, 5);

            final UpdateCustomerRequest request = new UpdateCustomerRequest(
                    "new-name",
                    "new-surname",
                    null,
                    dateOfBirth
            );

            final User user = UserFixture.customer();
            user.setId(USER_ID);

            final Customer customer = CustomerFixture.customer();
            customer.setId(CUSTOMER_ID);
            customer.setUser(user);

            final Customer updatedCustomer = Customer.builder()
                    .name("new-name")
                    .surname("new-surname")
                    .phoneNumber(null)
                    .dateOfBirth(dateOfBirth)
                    .user(user)
                    .build();

            updatedCustomer.setId(CUSTOMER_ID);

            when(customerRepository.findByUserId(USER_ID)).thenReturn(Optional.of(customer));
            when(customerRepository.save(any(Customer.class))).thenReturn(customer);

            final CustomerResponse result = customerService.update(USER_ID, request);

            verify(customerRepository).findByUserId(USER_ID);
            verify(customerRepository).save(customerCaptor.capture());

            final Customer customerBeforeSaving = customerCaptor.getValue();

            assertAll(
                    () -> assertEquals(CUSTOMER_ID, result.id()),
                    () -> assertEquals("new-name", result.name()),
                    () -> assertEquals("new-surname", result.surname()),
                    () -> assertNull(result.phoneNumber()),
                    () -> assertEquals(dateOfBirth.toString(), result.dateOfBirth())
            );

            assertAll(
                    () -> assertEquals(CUSTOMER_ID, customerBeforeSaving.getId()),
                    () -> assertEquals("new-name", customerBeforeSaving.getName()),
                    () -> assertEquals("new-surname", customerBeforeSaving.getSurname()),
                    () -> assertNull(customerBeforeSaving.getPhoneNumber()),
                    () -> assertEquals(dateOfBirth, customerBeforeSaving.getDateOfBirth()),
                    () -> assertEquals(user, customerBeforeSaving.getUser())
            );

            verifyNoMoreInteractions(customerRepository);
        }

        @Test
        @DisplayName("Should throw when customer was not found")
        void shouldThrowWhenCustomerNotFound() {
            final LocalDate dateOfBirth = LocalDate.of(2000, 12, 5);

            final UpdateCustomerRequest request = new UpdateCustomerRequest(
                    "new-name",
                    "new-surname",
                    null,
                    dateOfBirth
            );

            when(customerRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            final CustomerNotFoundException ex = assertThrows(
                    CustomerNotFoundException.class,
                    () -> customerService.update(USER_ID, request)
            );

            verify(customerRepository).findByUserId(USER_ID);

            assertEquals("Customer with user id %s was not found".formatted(USER_ID), ex.getMessage());

            verifyNoMoreInteractions(customerRepository);
        }
    }
}
