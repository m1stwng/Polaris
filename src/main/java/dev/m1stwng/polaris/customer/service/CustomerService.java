package dev.m1stwng.polaris.customer.service;

import dev.m1stwng.polaris.customer.dto.request.CreateCustomerRequest;
import dev.m1stwng.polaris.customer.dto.request.UpdateCustomerRequest;
import dev.m1stwng.polaris.customer.dto.response.CustomerResponse;
import dev.m1stwng.polaris.customer.entity.Customer;
import dev.m1stwng.polaris.customer.exception.CustomerAlreadyExistsException;
import dev.m1stwng.polaris.customer.exception.CustomerNotFoundException;
import dev.m1stwng.polaris.customer.mapper.CustomerMapper;
import dev.m1stwng.polaris.customer.repository.CustomerRepository;
import dev.m1stwng.polaris.identity.user.entity.User;
import dev.m1stwng.polaris.identity.user.exception.UserNotFoundException;
import dev.m1stwng.polaris.identity.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerMapper customerMapper;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public CustomerResponse findByUserId(UUID userId) {
        return customerRepository.findByUserId(userId)
                .map(customerMapper::customerToCustomerResponse)
                .orElseThrow(() -> new CustomerNotFoundException("Customer with user id %s was not found".formatted(userId)));
    }

    public CustomerResponse create(UUID userId, CreateCustomerRequest request) {
        final boolean exists = customerRepository.existsByUserId(userId);

        if (exists) {
            throw new CustomerAlreadyExistsException("Customer with user id %s already exists".formatted(userId));
        }

        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id %s was not found".formatted(userId)));

        final Customer customer = Customer.builder()
                .name(request.name())
                .surname(request.surname())
                .phoneNumber(request.phoneNumber())
                .dateOfBirth(request.dateOfBirth())
                .user(user)
                .build();

        final Customer createdCustomer = customerRepository.save(customer);

        return customerMapper.customerToCustomerResponse(createdCustomer);
    }

    public CustomerResponse update(UUID userId, UpdateCustomerRequest request) {
        final Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer with user id %s was not found".formatted(userId)));

        customer.setName(request.name());
        customer.setSurname(request.surname());
        customer.setPhoneNumber(request.phoneNumber());
        customer.setDateOfBirth(request.dateOfBirth());

        final Customer updatedCustomer = customerRepository.save(customer);

        return customerMapper.customerToCustomerResponse(updatedCustomer);
    }
}
