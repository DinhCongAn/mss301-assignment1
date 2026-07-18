package com.fptu.fucarrenting.rentingservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class RentingItemRequest {

    /*
     * ID của xe thuộc Car Service.
     *
     * Renting Service chỉ lưu carId,
     * không tạo quan hệ JPA với CarInformation.
     */
    @NotNull(message = "Car ID is required")
    @Positive(message = "Car ID must be greater than 0")
    private Long carId;

    /*
     * Ngày bắt đầu thuê.
     */
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    /*
     * Ngày kết thúc thuê.
     */
    @NotNull(message = "End date is required")
    private LocalDate endDate;
}