package com.fptu.fucarrenting.customerservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Telephone is required")
    private String telephone;

    @Past(message = "Customer birthday must be in the past")
    private LocalDate customerBirthday;
}