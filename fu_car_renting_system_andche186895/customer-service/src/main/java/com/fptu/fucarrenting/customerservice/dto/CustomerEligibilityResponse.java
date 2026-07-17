package com.fptu.fucarrenting.customerservice.dto;

import com.fptu.fucarrenting.customerservice.entity.CustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomerEligibilityResponse {

    private Long customerId;

    private boolean eligible;

    private CustomerStatus customerStatus;

    private String message;
}