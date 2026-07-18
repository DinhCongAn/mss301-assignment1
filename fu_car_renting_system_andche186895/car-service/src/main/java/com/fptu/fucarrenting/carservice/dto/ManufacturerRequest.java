package com.fptu.fucarrenting.carservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ManufacturerRequest {

    @NotBlank(message = "Manufacturer name is required")
    @Size(
            max = 100,
            message = "Manufacturer name must not exceed 100 characters"
    )
    private String manufacturerName;

    @Size(
            max = 1000,
            message = "Description must not exceed 1000 characters"
    )
    private String description;

    @Size(
            max = 100,
            message = "Manufacturer country must not exceed 100 characters"
    )
    private String manufacturerCountry;
}