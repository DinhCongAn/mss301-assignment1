package com.fptu.fucarrenting.rentingservice.service.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CalculatedRentingItem(
        Long carId,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal price
) {
}