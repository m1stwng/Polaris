package dev.m1stwng.polaris.customer.controller;

import dev.m1stwng.polaris.config.openapi.BadRequestApiResponse;
import dev.m1stwng.polaris.config.openapi.ConflictApiResponse;
import dev.m1stwng.polaris.config.openapi.NotFoundApiResponse;
import dev.m1stwng.polaris.config.openapi.UnauthorizedApiResponse;
import dev.m1stwng.polaris.customer.dto.request.CreateCustomerRequest;
import dev.m1stwng.polaris.customer.dto.request.UpdateCustomerRequest;
import dev.m1stwng.polaris.customer.dto.response.CustomerResponse;
import dev.m1stwng.polaris.customer.service.CustomerService;
import dev.m1stwng.polaris.security.entity.SecurityUser;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @ApiResponse(responseCode = "200")
    @UnauthorizedApiResponse
    @NotFoundApiResponse
    public ResponseEntity<CustomerResponse> findByUserId(@AuthenticationPrincipal SecurityUser securityUser) {
        final CustomerResponse response = customerService.findByUserId(securityUser.id());

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    @BadRequestApiResponse
    @UnauthorizedApiResponse
    @NotFoundApiResponse
    @ConflictApiResponse
    public ResponseEntity<CustomerResponse> create(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestBody @Valid CreateCustomerRequest request
    ) {
        final CustomerResponse response = customerService.create(securityUser.id(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    @ApiResponse(responseCode = "200")
    @BadRequestApiResponse
    @UnauthorizedApiResponse
    @NotFoundApiResponse
    public ResponseEntity<CustomerResponse> update(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestBody @Valid UpdateCustomerRequest request
    ) {
        final CustomerResponse response = customerService.update(securityUser.id(), request);

        return ResponseEntity.ok(response);
    }
}
