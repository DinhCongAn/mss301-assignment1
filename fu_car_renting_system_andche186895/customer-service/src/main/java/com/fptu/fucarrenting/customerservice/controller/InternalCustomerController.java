package com.fptu.fucarrenting.customerservice.controller;

import com.fptu.fucarrenting.customerservice.dto.CustomerEligibilityResponse;
import com.fptu.fucarrenting.customerservice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/customers")
@RequiredArgsConstructor
public class InternalCustomerController {

    private final CustomerService customerService;

    @GetMapping("/{customerId}/eligibility")
    public CustomerEligibilityResponse checkEligibility(
            @PathVariable Long customerId
    ) {
        return customerService.checkEligibility(customerId);
    }
}