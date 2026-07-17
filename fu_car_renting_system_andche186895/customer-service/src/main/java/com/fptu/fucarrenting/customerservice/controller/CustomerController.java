package com.fptu.fucarrenting.customerservice.controller;

import com.fptu.fucarrenting.customerservice.dto.CustomerResponse;
import com.fptu.fucarrenting.customerservice.dto.UpdateCustomerStatusRequest;
import com.fptu.fucarrenting.customerservice.dto.UpdateProfileRequest;
import com.fptu.fucarrenting.customerservice.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/me")
    public CustomerResponse getMyProfile(
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long customerId = jwt.getClaim("customerId");

        return customerService.getMyProfile(customerId);
    }

    @PutMapping("/me")
    public CustomerResponse updateMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        Long customerId = jwt.getClaim("customerId");

        return customerService.updateMyProfile(
                customerId,
                request
        );
    }

    @GetMapping
    public List<CustomerResponse> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{customerId}")
    public CustomerResponse getCustomerById(
            @PathVariable Long customerId
    ) {
        return customerService.getCustomerById(customerId);
    }

    @PatchMapping("/{customerId}/status")
    public CustomerResponse updateCustomerStatus(
            @PathVariable Long customerId,
            @Valid @RequestBody UpdateCustomerStatusRequest request
    ) {
        return customerService.updateCustomerStatus(
                customerId,
                request.getCustomerStatus()
        );
    }
}