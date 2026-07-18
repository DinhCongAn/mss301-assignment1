package com.fptu.fucarrenting.carservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SupplierRequest {

    @NotBlank(message = "Supplier name is required")
    @Size(
            max = 150,
            message = "Supplier name must not exceed 150 characters"
    )
    private String supplierName;

    @Size(
            max = 1000,
            message = "Supplier description must not exceed 1000 characters"
    )
    private String supplierDescription;

    @Size(
            max = 255,
            message = "Supplier address must not exceed 255 characters"
    )
    private String supplierAddress;
}