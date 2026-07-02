package dev.m1stwng.polaris.customer.mapper;

import dev.m1stwng.polaris.customer.dto.response.CustomerResponse;
import dev.m1stwng.polaris.customer.entity.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerResponse customerToCustomerResponse(Customer customer);
}
