package com.fptu.fucarrenting.customerservice.dto;

import com.fptu.fucarrenting.customerservice.entity.CustomerStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateCustomerStatusRequest {

    @NotNull(message = "Customer status is required")
    private CustomerStatus customerStatus;
}