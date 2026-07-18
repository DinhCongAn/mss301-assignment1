package com.fptu.fucarrenting.rentingservice.client;

import com.fptu.fucarrenting.rentingservice.client.dto.CustomerEligibilityResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service")
public interface CustomerClient {

    /*
     * Kiểm tra Customer có được phép thuê xe không.
     */
    @GetMapping("/internal/customers/{customerId}/eligibility")
    CustomerEligibilityResponse checkEligibility(
            @PathVariable("customerId") Long customerId
    );
}