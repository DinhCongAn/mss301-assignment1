package com.fptu.fucarrenting.rentingservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateRentingRequest {

    @NotEmpty(message = "At least one car is required")
    @Size(
            max = 10,
            message = "A renting transaction must not contain more than 10 cars"
    )
    private List<@Valid RentingItemRequest> details;
}